package com.ballog.mobile.ui.main

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ballog.mobile.ui.components.NavigationBar
import com.ballog.mobile.ui.home.HomeScreen
import com.ballog.mobile.ui.match.MatchScreen
import com.ballog.mobile.ui.match.MatchDetailScreen
import com.ballog.mobile.ui.team.TeamListScreen
import com.ballog.mobile.ui.team.TeamDetailScreen
import com.ballog.mobile.ui.team.TeamSettingScreen
import com.ballog.mobile.ui.team.TeamDelegateScreen
import com.ballog.mobile.ui.team.TeamKickScreen
import com.ballog.mobile.ui.team.TeamCreateScreen
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import com.ballog.mobile.ui.components.NavigationTab
import com.ballog.mobile.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ballog.mobile.data.api.RetrofitInstance
import com.ballog.mobile.data.service.MatchReportService
import com.ballog.mobile.data.service.MatchReportServiceSingleton
import com.ballog.mobile.data.service.SamsungHealthDataService
import com.ballog.mobile.ui.match.MatchDataScreen
import com.ballog.mobile.ui.match.MatchRegisterMode
import com.ballog.mobile.ui.match.MatchRegisterScreen
import com.ballog.mobile.viewmodel.MatchViewModel
import com.ballog.mobile.viewmodel.TeamViewModel
import com.ballog.mobile.ui.team.TeamUpdateScreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.ballog.mobile.ui.user.LikedVideosScreen
import com.ballog.mobile.ui.user.MyPageScreen
import com.ballog.mobile.ui.user.ProfileEditScreen
import com.ballog.mobile.ui.home.MatchDataReportScreen
import com.ballog.mobile.viewmodel.UserViewModel

private const val TAG = "MainScreen"

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: AuthViewModel,
    initialTeamId: Int? = null
) {
    val context = LocalContext.current
    val matchApi = RetrofitInstance.matchApi
    val samsungHealthDataService = remember { SamsungHealthDataService(context) }

    // 앱 시작 시점에서 싱글턴 초기화
    LaunchedEffect(Unit) {
        MatchReportServiceSingleton.init(
            context = context,
            matchApi = matchApi,
            samsungHealthDataService = samsungHealthDataService
        )
    }

    var selectedTab by remember { mutableStateOf(if (initialTeamId != null) NavigationTab.TEAM else NavigationTab.HOME) }
    val teamNavController = rememberNavController()
    val teamViewModel = remember { TeamViewModel() }
    val matchTabNavController = rememberNavController()
    // 이동 요청을 위한 State
    var pendingMatchDetail by remember { mutableStateOf<Pair<Int, String>?>(null) }
    
    // 초기 팀 ID가 있으면 팀 상세 화면으로 자동 이동
    LaunchedEffect(initialTeamId) {
        if (initialTeamId != null) {
            // 팀 목록 화면의 백스택 항목 생성
            teamNavController.navigate("team_list")

            // 팀 상세 화면으로 이동
            teamNavController.navigate("team_detail/$initialTeamId")

            // 로그 출력
            Log.d(TAG, "팀 상세 화면으로 자동 이동: teamId=$initialTeamId")
        }
    }

    Scaffold(
//        bottomBar = {
//            Text("네비게이션 바") // NavigationBar 임시 제거
//        }
        bottomBar = {
            NavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                },
                onActionClick = {
                    // 중앙 버튼 눌렀을 때 동작
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                NavigationTab.HOME -> HomeTabScreen()
                NavigationTab.MATCH -> MatchTabScreen(navController = matchTabNavController)
                NavigationTab.TEAM -> TeamTabScreen(teamNavController, teamViewModel)
                NavigationTab.MYPAGE -> ProfileTabScreen(
                    navController = rememberNavController(),
                    rootNavController = navController
                )
                NavigationTab.DATA -> MatchDataScreen(
                    navController = navController,
                    matchTabNavController = matchTabNavController,
                    setSelectedTab = { selectedTab = it },
                    matchReportService = MatchReportServiceSingleton.getInstance(),
                    setPendingMatchDetail = { pendingMatchDetail = it }
                )
            }
        }
    }
    // 탭이 MATCH로 바뀌고, 이동 요청이 있으면 navigate
    LaunchedEffect(selectedTab, pendingMatchDetail) {
        if (selectedTab == NavigationTab.MATCH && pendingMatchDetail != null) {
            val (matchId, matchName) = pendingMatchDetail!!
            matchTabNavController.navigate("match/detail/$matchId/$matchName")
            pendingMatchDetail = null
        }
    }
}

@Composable
fun TeamTabScreen(
    teamNavController: NavHostController,
    teamViewModel: TeamViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    
    // 팀 관련 화면에 대한 중첩 네비게이션
    NavHost(
        navController = teamNavController,
        startDestination = "team_list"
    ) {
        composable("team_list") {
            Log.d(TAG, "팀 목록 화면 표시")
            TeamListScreen(navController = teamNavController, viewModel = teamViewModel)
        }

        // 팀 생성 화면 추가
        composable("team/create") {
            Log.d(TAG, "팀 생성 화면 표시")
            TeamCreateScreen(
                teamViewModel = teamViewModel,
                onNavigateBack = {
                    teamNavController.popBackStack()
                },
                onClose = {
                    teamNavController.popBackStack()
                }
            )
        }

        composable(
            route = "team_detail/{teamId}",
            arguments = listOf(
                navArgument("teamId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getInt("teamId") ?: 0
            Log.d(TAG, "팀 상세 화면 표시: teamId=$teamId")
            TeamDetailScreen(
                navController = teamNavController,
                teamId = teamId,
                viewModel = teamViewModel
            )
        }
        // 팀 설정 화면 추가
        composable(
            route = "team/settings/{teamId}",
            arguments = listOf(
                navArgument("teamId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getInt("teamId") ?: 0
            Log.d(TAG, "팀 설정 화면 표시: teamId=$teamId")
            TeamSettingScreen(
                navController = teamNavController,
                teamId = teamId,
                viewModel = teamViewModel,
                onBackClick = {
                    teamNavController.popBackStack()
                },
                onDelegateClick = {
                    teamNavController.navigate("team/delegate/$teamId")
                },
                onKickMemberClick = {
                    teamNavController.navigate("team/kick/$teamId")
                },
                onInviteLinkClick = {
                    // 초대 링크 다이얼로그는 TeamSettingScreen 내부에서 처리됩니다
                    Log.d(TAG, "팀 초대 링크 생성 요청")
                },
                onDeleteTeamClick = {
                    // 팀 삭제 요청
                    Log.d(TAG, "팀 삭제 요청 시작 - teamId: $teamId")
                    coroutineScope.launch {
                        try {
                            val result = teamViewModel.deleteTeam(teamId)
                            Log.d(TAG, "팀 삭제 결과: ${if (result.isSuccess) "성공" else "실패"}")
                            if (result.isSuccess) {
                                Log.d(TAG, "팀 삭제 성공, 이전 화면으로 이동")
                                teamNavController.navigate("team_list")
                            } else {
                                Log.d(TAG, "팀 삭제 실패: ${result.exceptionOrNull()?.message}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "팀 삭제 중 예외 발생", e)
                        }
                    }
                },
                onLeaveTeamClick = {
                    // 팀 탈퇴 요청
                    Log.d(TAG, "팀 탈퇴 요청")
                    coroutineScope.launch {
                        val result = teamViewModel.leaveTeam(teamId)
                        if (result.isSuccess) {
                            teamNavController.navigate("team_list")
                        }
                    }
                },
                onUpdateTeamClick = {
                    // 팀 수정 화면으로 이동
                    Log.d(TAG, "팀 수정 화면으로 이동: teamId=$teamId")
                    teamNavController.navigate("team/update/$teamId")
                }
            )
        }
        // 팀 수정 화면 추가
        composable(
            route = "team/update/{teamId}",
            arguments = listOf(
                navArgument("teamId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getInt("teamId") ?: 0
            Log.d(TAG, "팀 수정 화면 표시: teamId=$teamId")
            TeamUpdateScreen(
                teamId = teamId,
                teamViewModel = teamViewModel,
                onNavigateBack = {
                    teamNavController.popBackStack()
                },
                onClose = {
                    teamNavController.popBackStack()
                }
            )
        }
        // 권한 위임 화면 추가
        composable(
            route = "team/delegate/{teamName}",
            arguments = listOf(
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            Log.d(TAG, "팀 권한 위임 화면 표시: teamName=$teamName")
            TeamDelegateScreen(
                navController = teamNavController,
                teamName = teamName,
                viewModel = teamViewModel,
                onBackClick = {
                    teamNavController.popBackStack()
                },
                onCloseClick = {
                    teamNavController.popBackStack()
                }
            )
        }
        // 멤버 강제 퇴장 화면 추가
        composable(
            route = "team/kick/{teamName}",
            arguments = listOf(
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            Log.d(TAG, "팀 멤버 강제 퇴장 화면 표시: teamName=$teamName")
            TeamKickScreen(
                navController = teamNavController,
                teamName = teamName,
                viewModel = teamViewModel,
                onBackClick = {
                    teamNavController.popBackStack()
                },
                onCloseClick = {
                    teamNavController.popBackStack()
                },
                onSuccess = {
                    teamNavController.navigate("team_list") {
                        popUpTo("team_list") { inclusive = true }
                    }
                }
            )
        }
        // 팀 매치 등록 화면 추가
        composable(
            route = "match/register/{date}?teamId={teamId}",
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("teamId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
            val teamId = backStackEntry.arguments?.getInt("teamId")?.takeIf { it != -1 }
            val matchViewModel: MatchViewModel = viewModel()

            MatchRegisterScreen(
                mode = if (teamId != null) MatchRegisterMode.TEAM else MatchRegisterMode.PERSONAL,
                navController = teamNavController,
                viewModel = matchViewModel,
                selectedDate = selectedDate,
                teamId = teamId
            )
        }

        // 매치 상세
        composable(
            route = "match/detail/{matchId}/{matchName}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.IntType },
                navArgument("matchName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
            val matchName = backStackEntry.arguments?.getString("matchName") ?: "매치 상세"

            MatchDetailScreen(
                navController = teamNavController,
                matchId = matchId,
                initialTitle = matchName
            )
        }

    }
}

@Composable
fun MatchTabScreen(navController: NavHostController) {
    val matchViewModel: MatchViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "match/main"
    ) {
        composable("match/main") {
            MatchScreen(navController = navController, viewModel = matchViewModel)
        }

        composable(
            route = "match/register/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()

            MatchRegisterScreen(
                mode = MatchRegisterMode.PERSONAL,
                navController = navController,
                viewModel = matchViewModel,
                selectedDate = selectedDate
            )
        }

        // 매치 상세
        composable(
            route = "match/detail/{matchId}/{matchName}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.IntType },
                navArgument("matchName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
            val matchName = backStackEntry.arguments?.getString("matchName") ?: "매치 상세"

            MatchDetailScreen(
                navController = navController,
                matchId = matchId,
                initialTitle = matchName
            )
        }
    }
}

@Composable
private fun ProfileTabScreen(navController: NavHostController, rootNavController: NavHostController) {
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = "mypage"
    ) {
        composable("mypage") {
            MyPageScreen(navController, rootNavController)
        }
        composable("profile/edit") {
            ProfileEditScreen(navController, rootNavController)
        }
        composable("mypage/liked-videos") {
            LikedVideosScreen()
        }
        // 필요시 추가 스크린...
    }
}

@Composable
fun HomeTabScreen() {
    val navController = rememberNavController()
    var nickname by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { backStackEntry ->
            val userViewModel: UserViewModel = viewModel(backStackEntry)
            HomeScreen(
                viewModel = userViewModel,
                onNavigateToStatisticsPage = {
                    navController.navigate("statistics/${nickname}")
                },
                onNicknameLoaded = { loadedNickname -> nickname = loadedNickname }
            )
        }
        composable(
            "statistics/{nickname}",
            arguments = listOf(navArgument("nickname") { type = NavType.StringType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val userViewModel: UserViewModel = viewModel(parentEntry)
            val nicknameArg = backStackEntry.arguments?.getString("nickname") ?: ""
            MatchDataReportScreen(
                nickname = nicknameArg,
                onBack = { navController.popBackStack() },
                viewModel = userViewModel
            )
        }
    }
}

