package com.ballog.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ballog.mobile.data.model.AuthResult
import com.ballog.mobile.data.repository.MatchRepository
import com.ballog.mobile.navigation.AppNavHost
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.viewmodel.AuthViewModel
import com.ballog.mobile.viewmodel.TeamViewModel
import com.google.android.gms.wearable.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    private val TAG = "MainActivity"
    // 딥 링크 데이터 저장
    private var pendingDeepLinkData: DeepLinkData? = null

    // 앱이 이미 실행중일 때 딥 링크 처리를 위한 상태 업데이트 트리거
    private val _deepLinkEvent = MutableStateFlow<Boolean>(false)
    val deepLinkEvent: StateFlow<Boolean> = _deepLinkEvent.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) // 시스템 스플래시에서 앱 테마로 전환
        super.onCreate(savedInstanceState)

        // 시스템 바 설정
        WindowCompat.setDecorFitsSystemWindows(window, true)
        Log.d(TAG, "onCreate - addListener 호출")
        // Wearable Data API 리스너 등록
        Wearable.getDataClient(this).addListener(this)

        // Intent에서 딥 링크 데이터 처리
        println("MainActivity - onCreate with intent: ${intent?.action}, data: ${intent?.data}")
        handleIntent(intent)
        println("MainActivity - After handleIntent in onCreate, pendingDeepLinkData: $pendingDeepLinkData")

        setContent {
            val navController = rememberNavController()
            val tokenManager = (application as BallogApplication).tokenManager
            val teamViewModel: TeamViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val authState by authViewModel.authState.collectAsState()
            val coroutineScope = rememberCoroutineScope()
            val deepLinkTrigger by deepLinkEvent.collectAsState()

            BallogTheme {
                AppNavHost(navController)

                LaunchedEffect(Unit) {
                    try {
                        val hasTokens = tokenManager.hasTokens().first()
                        val context = applicationContext
                        val onboardingDone = com.ballog.mobile.data.util.OnboardingPrefs.isOnboardingCompleted(context)
                        val permissionDone = com.ballog.mobile.data.util.OnboardingPrefs.isPermissionCompleted(context)
                        val guideDone = com.ballog.mobile.data.util.OnboardingPrefs.isGuideCompleted(context)

                        if (pendingDeepLinkData != null) {
                            // 딥링크가 있으면 온보딩/권한/가이드 패스, 바로 초대 플로우
                            if (hasTokens) {
                                handleNavigationAfterLogin(navController, teamViewModel, coroutineScope)
                            } else {
                                navController.navigate(com.ballog.mobile.navigation.Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                            }
                        } else {
                            // 딥링크 없으면 온보딩/권한/가이드 순서대로 분기
                            when {
                                !hasTokens -> navController.navigate(com.ballog.mobile.navigation.Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
                                !onboardingDone -> navController.navigate(com.ballog.mobile.navigation.Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
                                !permissionDone -> navController.navigate(com.ballog.mobile.navigation.Routes.PERMISSION_REQUEST) { popUpTo(0) { inclusive = true } }
                                !guideDone -> navController.navigate(com.ballog.mobile.navigation.Routes.SAMSUNG_HEALTH_GUIDE) { popUpTo(0) { inclusive = true } }
                                else -> handleNavigationAfterLogin(navController, teamViewModel, coroutineScope)
                            }
                        }
                    } catch (e: Exception) {
                        navController.navigate(com.ballog.mobile.navigation.Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
                    }
                }

                // 로그인 상태가 변경될 때마다 실행
                LaunchedEffect(authState) {
                    if (authState is AuthResult.Success) {
                        println("MainActivity - Login successful, handling navigation")
                        handleNavigationAfterLogin(navController, teamViewModel, coroutineScope)
                    }
                }

                // 딥 링크 이벤트가 발생했을 때 실행
                LaunchedEffect(deepLinkTrigger) {
                    if (deepLinkTrigger) {
                        println("MainActivity - Deep link event triggered")
                        handleNavigationAfterLogin(navController, teamViewModel, coroutineScope)
                        _deepLinkEvent.value = false // 이벤트 처리 후 초기화
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Wearable Data API 리스너 해제
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "데이터 변경 이벤트 수신")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path?.compareTo("/field_corners") == 0) {
                    Log.d(TAG, "경기장 모서리 데이터 수신")
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val repository = MatchRepository(applicationContext)
                    try {
                        repository.saveFieldDataFromWatch(
                            lat1 = dataMap.getDouble("lat1"),
                            lon1 = dataMap.getDouble("lon1"),
                            lat2 = dataMap.getDouble("lat2"),
                            lon2 = dataMap.getDouble("lon2"),
                            lat3 = dataMap.getDouble("lat3"),
                            lon3 = dataMap.getDouble("lon3"),
                            lat4 = dataMap.getDouble("lat4"),
                            lon4 = dataMap.getDouble("lon4"),
                            timestamp = dataMap.getLong("timestamp")
                        )
                        Log.d(TAG, "경기장 모서리 데이터 저장 완료")
                    } catch (e: Exception) {
                        Log.e(TAG, "경기장 모서리 데이터 저장 실패: ${e.message}")
                    }
                }
            }
        }
    }

    private fun handleNavigationAfterLogin(
        navController: androidx.navigation.NavController,
        teamViewModel: TeamViewModel,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ) {
        println("MainActivity - handleNavigationAfterLogin called, pendingDeepLinkData: $pendingDeepLinkData")

        // 딥 링크가 있으면 팀원 추가 후 TeamDetail 화면으로 이동
        if (pendingDeepLinkData != null && pendingDeepLinkData?.type == DeepLinkType.TEAM_INVITE) {
            val teamId = pendingDeepLinkData?.teamId ?: run {
                println("MainActivity - TeamId is null, navigating to main")
                navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } }
                return
            }

            val inviteCode = pendingDeepLinkData?.inviteCode ?: run {
                println("MainActivity - InviteCode is null, navigating to main")
                navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } }
                return
            }

            println("MainActivity - Processing deep link after login: Team Invite, teamId=$teamId, code=$inviteCode")

            // 로딩 표시 설정
            teamViewModel.setLoading(true)

            // 팀원 추가 처리
            coroutineScope.launch {
                try {
                    println("MainActivity - Starting addTeamMember for teamId: $teamId")
                    val result = teamViewModel.addTeamMember(teamId)

                    if (result.isSuccess) {
                        println("MainActivity - Successfully added to team: $teamId, navigating to team detail")

                        // 팀 탭으로 이동하기 위해 메인 화면으로 먼저 이동
                        // 이후 TeamTab으로 자동 선택되도록 팀 ID를 전달
                        println("MainActivity - Navigating to main screen with teamId: $teamId")

                        try {
                            // 메인 화면으로 이동하며 팀 ID를 함께 전달
                            navController.navigate("${Routes.MAIN}?teamId=$teamId") {
                                popUpTo(0) { inclusive = true }
                            }
                            println("MainActivity - Navigation successful")
                        } catch (e: Exception) {
                            println("MainActivity - Navigation error: ${e.message}")
                            e.printStackTrace()
                            // 내비게이션 실패 시 메인으로 이동
                            navController.navigate(Routes.MAIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                        println("MainActivity - Failed to add to team: $errorMsg")
                        // 실패해도 메인 화면으로는 이동
                        navController.navigate(Routes.MAIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    println("MainActivity - Error adding to team: ${e.message}")
                    e.printStackTrace()
                    // 예외 발생 시 메인 화면으로 이동
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                } finally {
                    teamViewModel.setLoading(false)
                    // 처리 후 딥 링크 데이터 초기화
                    pendingDeepLinkData = null
                }
            }
        } else {
            println("MainActivity - No pending deep link, navigating to main screen")
            // 딥 링크가 없으면 메인 화면으로 이동
            navController.navigate(Routes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 현재 Activity의 Intent를 업데이트

        // 앱이 이미 실행 중일 때 딥 링크를 처리합니다.
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            handleIntent(intent)
            // Compose에서 감지할 수 있는 이벤트 트리거
            _deepLinkEvent.value = true
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            println("MainActivity - handleIntent called with null intent")
            return
        }

        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data

        println("MainActivity - handleIntent called with action: $appLinkAction, data: $appLinkData")

        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            println("MainActivity - Received deep link: $appLinkData")
            println("MainActivity - URI Scheme: ${appLinkData.scheme}, Host: ${appLinkData.host}")
            println("MainActivity - Path: ${appLinkData.path}, Query: ${appLinkData.query}")
            println("MainActivity - Full URI components: scheme=${appLinkData.scheme}, host=${appLinkData.host}, path=${appLinkData.path}, query=${appLinkData.query}")

            // Query 파라미터 모두 출력
            appLinkData.queryParameterNames.forEach { name ->
                val value = appLinkData.getQueryParameter(name)
                println("MainActivity - Query parameter: $name = $value")
            }

            when {
                // 팀 초대 링크 처리 - 커스텀 URL 스킴만 확인
                appLinkData.scheme == "ballog" && appLinkData.host == "team-invite" -> {
                    println("MainActivity - Detected team invite deep link")

                    val teamId = appLinkData.getQueryParameter("teamId")?.toIntOrNull()
                    val inviteCode = appLinkData.getQueryParameter("code")

                    println("MainActivity - Extracted teamId: $teamId, code: $inviteCode")

                    if (teamId != null && !inviteCode.isNullOrEmpty()) {
                        pendingDeepLinkData = DeepLinkData(
                            type = DeepLinkType.TEAM_INVITE,
                            teamId = teamId,
                            inviteCode = inviteCode
                        )
                        println("MainActivity - Parsed team invite deep link: teamId=$teamId, code=$inviteCode")
                        println("MainActivity - pendingDeepLinkData set to: $pendingDeepLinkData")
                    } else {
                        println("MainActivity - Invalid team invite link parameters")
                    }
                }
                else -> {
                    println("MainActivity - Unrecognized deep link scheme/host")
                }
            }
        } else {
            println("MainActivity - Not a deep link intent: action=${intent.action}, data=${intent.data}")
        }
    }
}

// 딥 링크 타입 정의
enum class DeepLinkType {
    TEAM_INVITE
}

// 딥 링크 데이터 클래스
data class DeepLinkData(
    val type: DeepLinkType,
    val teamId: Int? = null,
    val inviteCode: String? = null
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BallogTheme {
        Greeting("Android")
    }
}
