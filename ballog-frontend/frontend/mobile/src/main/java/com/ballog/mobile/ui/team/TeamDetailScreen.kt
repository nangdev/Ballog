package com.ballog.mobile.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.R
import com.ballog.mobile.data.model.Player
import com.ballog.mobile.data.model.TeamDetail
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.TeamPlayerCard
import com.ballog.mobile.ui.components.TabMenu
import com.ballog.mobile.ui.components.TeamInfoCard
import com.ballog.mobile.ui.components.TeamStats
import com.ballog.mobile.ui.home.PlayerCardDialog
import com.ballog.mobile.ui.match.TeamMatchTab
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.TeamViewModel

@Composable
fun TeamDetailScreen(
    navController: NavController,
    teamId: Int,
    viewModel: TeamViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val teamDetail by viewModel.teamDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    LaunchedEffect(teamId) {
        viewModel.getTeamDetail(teamId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
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

            teamDetail != null -> {
                TopNavItem(
                    title = teamDetail!!.name,
                    type = TopNavType.DETAIL_WITH_BACK_SETTINGS,
                    onBackClick = { navController.popBackStack() },
                    onActionClick = {
                        navController.navigate("team/settings/$teamId")
                    }
                )

                TabMenu(
                    leftTabText = "팀 정보",
                    rightTabText = "매치",
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // 명시적 여백 처리
                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> TeamInfoTab(
                        teamDetail = teamDetail!!,
                        selectedPlayer = selectedPlayer,
                        onDismiss = { selectedPlayer = null },
                        onPlayerSelected = { selectedPlayer = it }
                    )

                    1 -> TeamMatchTab(navController = navController, teamId = teamId)
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "팀 정보를 불러올 수 없습니다",
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

    selectedPlayer?.let { player ->
        PlayerCardDialog(
            name = player.nickname,
            imageUrl = player.cardImageUrl,
            stats = listOf(
                "Speed" to player.stats.speed.toString(),
                "Stamina" to player.stats.stamina.toString(),
                "Attack" to player.stats.attack.toString(),
                "Defense" to player.stats.defense.toString(),
                "Recovery" to player.stats.recovery.toString()
            ),
            onDismiss = { selectedPlayer = null }
        )
    }
}

@Composable
private fun TeamInfoTab(
    teamDetail: TeamDetail,
    selectedPlayer: Player?,
    onDismiss: () -> Unit,
    onPlayerSelected: (Player) -> Unit
) {
    if (teamDetail.name.isEmpty() && teamDetail.players.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "팀 정보를 불러올 수 없습니다",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = Gray.Gray500,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TeamInfoCard(
                stats = TeamStats(
                    attack = teamDetail.stats.attack,
                    defence = teamDetail.stats.defense,
                    speed = teamDetail.stats.speed,
                    recovery = teamDetail.stats.recovery,
                    stamina = teamDetail.stats.stamina
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "멤버 수",
                    tint = Gray.Gray800,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = teamDetail.players.size.toString(),
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Normal,
                    color = Gray.Gray800
                )
            }
        }

        if (teamDetail.players.isNotEmpty()) {
            items(teamDetail.players) { player ->
                TeamPlayerCard(
                    name = player.nickname,
                    isManager = player.role == "MANAGER",
                    modifier = Modifier.fillMaxWidth(),
                    onCardClick = { onPlayerSelected(player) }
                )
            }
        } else {
            item {
                Text(
                    text = "팀원이 없습니다",
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Gray.Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}


