package com.ballog.mobile.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import com.ballog.mobile.R
import com.ballog.mobile.ui.components.*
import com.ballog.mobile.ui.match.HeatMap
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.UserViewModel
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType

@Composable
fun HomeScreen(
    viewModel: UserViewModel = viewModel(),
    onNavigateToStatisticsPage: () -> Unit = {},
    onNicknameLoaded: (String) -> Unit = {}
) {
    val statistics by viewModel.userStatistics.collectAsState()
    val playerCardInfo by viewModel.playerCardInfo.collectAsState()

    val nickname = statistics?.nickname ?: ""

    // 닉네임이 바뀔 때마다 콜백 호출
    LaunchedEffect(nickname) {
        onNicknameLoaded(nickname)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUserStatistics()
        if (viewModel.shouldForceRefresh()) {
            viewModel.refreshPlayerCardInfo()
        } else {
            viewModel.fetchPlayerCardInfoIfNeeded()
        }
    }

    var showPlayerCard by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val heatmap = statistics?.heatmap ?: emptyList()

    val distanceList = statistics?.distance ?: emptyList()
    val speedList = statistics?.speed ?: emptyList()
    val sprintList = statistics?.sprint?.map { it.toFloat() } ?: emptyList()
    val heartRateList = statistics?.heartRate?.map { it.toFloat() } ?: emptyList()

    val distanceText = distanceList.safeAverage()
    val speedText = speedList.safeAverage()
    val sprintText = sprintList.safeAverage()
    val heartRateText = heartRateList.safeAverage()

    val distanceNorm = normalize(distanceList.map { it.toFloat() })
    val speedNorm = normalize(speedList.map { it.toFloat() })
    val sprintNorm = normalize(sprintList)
    val heartRateNorm = normalize(heartRateList)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray.Gray100)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopNavItem(
                title = if (!nickname.isNullOrBlank()) "${nickname}님, 안녕하세요!" else "-",
                type = TopNavType.MAIN_BASIC
            )

            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                BallogButton(
                    onClick = {
                        viewModel.fetchPlayerCardInfoIfNeeded()
                        showPlayerCard = true
                    },
                    type = ButtonType.BOTH,
                    buttonColor = ButtonColor.BLACK,
                    icon = painterResource(id = R.drawable.ic_card),
                    label = "선수 카드 보기",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                HeatMap(
                    heatData = heatmap,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "통계",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray.Gray700,
                                fontFamily = pretendard
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "최근 5경기의 평균을 나타냅니다",
                                fontSize = 12.sp,
                                color = Gray.Gray500,
                                fontFamily = pretendard
                            )
                        }
                        IconButton(
                            onClick = onNavigateToStatisticsPage,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_report),
                                contentDescription = "통계 상세 이동",
                                tint = Gray.Gray700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "이동거리",
                            value = "${distanceText}km",
                            bars = distanceNorm,
                            barColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "평균속도",
                            value = "${speedText}km/h",
                            bars = speedNorm,
                            barColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "스프린트",
                            value = "${sprintText}회",
                            bars = sprintNorm,
                            barColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "평균심박",
                            value = "${heartRateText}bpm",
                            bars = heartRateNorm,
                            barColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (showPlayerCard && playerCardInfo != null) {
            PlayerCardDialog(
                name = playerCardInfo!!.nickname,
                imageUrl = playerCardInfo!!.profileImageUrl,
                stats = playerCardInfo!!.stats,
                onDismiss = { showPlayerCard = false }
            )
        }
    }
}

fun normalize(list: List<Float>): List<Float> {
    val max = list.maxOrNull()?.takeIf { it > 0f } ?: 1f
    return list.map { it / max }.takeLast(5)
}

fun Double.format1f(): String = String.format("%.1f", this)

fun List<Number>.safeAverage(): String {
    return if (this.isNotEmpty()) {
        (this.map { it.toDouble() }.average()).format1f()
    } else {
        "– "
    }
}
