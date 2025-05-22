package com.ballog.mobile.ui.team

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.TeamCard
import com.ballog.mobile.ui.components.TeamInfo
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.TeamViewModel

private const val TAG = "TeamListScreen"

@Composable
fun TeamListScreen(
    navController: NavController,
    viewModel: TeamViewModel = viewModel()
) {
    Log.d(TAG, "TeamListScreen 실행")
    
    val teams by viewModel.teamList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        Log.d(TAG, "팀 목록 요청")
        viewModel.getUserTeamList()
    }
    
    Log.d(TAG, "상태 - isLoading: $isLoading, error: $error, teams: ${teams.size}개")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = "팀",
            type = TopNavType.MAIN_WITH_CREATE,
            navController = navController,
            onActionClick = {
                try {
                    // 중첩 NavHost에 맞는 경로 사용
                    val createRoute = "team/create"
                    Log.d(TAG, "팀 생성 화면으로 이동: $createRoute")
                    navController.navigate(createRoute)
                } catch (e: Exception) {
                    Log.e(TAG, "팀 생성 화면 이동 오류: ${e.message}", e)
                }
            }
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "알 수 없는 오류가 발생했습니다",
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Gray.Gray500,
                        textAlign = TextAlign.Center
                    )
                }
            }
            teams.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "소속된 팀이 없습니다",
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Medium,
                            color = Gray.Gray500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(teams) { team ->
                        val safeLogoUrl = team.logoImageUrl?.takeIf {
                            !it.isNullOrBlank() &&
                            it != "null" &&
                            it != "about:blank" &&
                            it != "http://" &&
                            it.trim().isNotEmpty()
                        }
                        TeamCard(
                            team = TeamInfo(
                                name = team.name.orEmpty(),
                                foundingDate = team.foundationDate.orEmpty(),
                                logoImageUrl = safeLogoUrl
                            ),
                            onClick = {
                                Log.d(TAG, "팀 카드 클릭: teamId=${team.teamId}, 이름=${team.name}")
                                if (team.teamId > 0) {
                                    try {
                                        // 중첩 네비게이션에서 사용하는 경로 형식으로 변경
                                        val detailRoute = "team_detail/${team.teamId}"
                                        Log.d(TAG, "팀 상세 화면으로 이동(중첩 라우트): $detailRoute")
                                        navController.navigate(detailRoute)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "네비게이션 오류: ${e.message}", e)
                                    }
                                } else {
                                    Log.e(TAG, "유효하지 않은 teamId: ${team.teamId}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamListScreenPreview() {
    TeamListScreen(rememberNavController())
}

@Preview(
    name = "팀 목록 화면 - 빈 상태",
    showBackground = true,
    backgroundColor = 0xFFF8F9FB // Gray.Gray100 색상
)
@Composable
fun TeamListScreenEmptyPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = "팀",
            type = TopNavType.MAIN_WITH_CREATE,
            navController = rememberNavController(),
            onActionClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "소속된 팀이 없습니다",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Gray.Gray500,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(
    name = "팀 목록 화면 - 로딩 상태",
    showBackground = true,
    backgroundColor = 0xFFF8F9FB
)
@Composable
fun TeamListScreenLoadingPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = "팀",
            type = TopNavType.MAIN_WITH_CREATE,
            navController = rememberNavController(),
            onActionClick = {}
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview(
    name = "팀 목록 화면 - 에러 상태",
    showBackground = true,
    backgroundColor = 0xFFF8F9FB
)
@Composable
fun TeamListScreenErrorPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = "팀",
            type = TopNavType.MAIN_WITH_CREATE,
            navController = rememberNavController(),
            onActionClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "팀 목록을 불러오는데 실패했습니다",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = Gray.Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}
