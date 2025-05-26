package com.ballog.mobile.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.ballog.mobile.R
import com.ballog.mobile.ui.match.HeatMap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun MatchDataReportScreen(
    nickname: String,
    onBack: () -> Unit = {},
    viewModel: UserViewModel = viewModel()
) {
    val aiRecommend = viewModel.aiRecommend.collectAsState().value
    val statistics by viewModel.userStatistics.collectAsState()
    // --- 추가: 그래프 상세 모달 상태 ---
    var selectedGraph by remember { mutableStateOf<Int?>(null) }

    // --- 임시(더미) 그래프 데이터 ---
    // --- 임시 오각형 카드용 데이터 ---
    val playerStats = PlayerStats(attack = 80, defence = 60, speed = 70, recovery = 90, stamina = 50)
    val averageStats = PlayerStats(attack = 65, defence = 55, speed = 60, recovery = 80, stamina = 60)

    LaunchedEffect(Unit) {
        viewModel.fetchAiRecommend()
    }

    if (aiRecommend == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val conclusion = aiRecommend.conclusion
    val heatmapAnalysis = aiRecommend.heatmapAnalysis
    val sprintAnalysis = aiRecommend.sprintAnalysis
    val speedAnalysis = aiRecommend.speedAnalysis
    val staminaAnalysis = aiRecommend.staminaAnalysis
    val similarPlayerName = aiRecommend.recommendedPlayer?.name
    val similarPlayerPosition = aiRecommend.recommendedPlayer?.position
    val style = aiRecommend.recommendedPlayer?.style
    val reason = aiRecommend.recommendedPlayer?.reason
    val train = aiRecommend.recommendedPlayer?.train
    val imageUrl = aiRecommend.recommendedPlayer?.imageUrl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopNavItem(
            title = "플레이스타일 분석 레포트",
            type = TopNavType.DETAIL_WITH_BACK,
            onBackClick = onBack,
        )
        // 스크롤 가능한 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // 매치 데이터 분석 요약
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 결론 부분
                Text(
                    text = "${conclusion} ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    fontFamily = pretendard
                )
                // nickname 부분
                Text(
                    text = nickname,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = pretendard
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .padding(start = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "경기 데이터 보고서",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray.Gray700,
                    fontFamily = pretendard
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            // --- 추가: 2x2 그래프 그리드 ---
            MatchDataReportGraphs(
                onGraphClick = { selectedGraph = it },
                playerStats = playerStats,
                averageStats = averageStats,
                heatmapData = statistics!!.heatmap,
                speedData = statistics!!.speed,
                sprintData = statistics!!.sprint,
                staminaData = statistics!!.heartRate
            )
            if (selectedGraph != null) {
                GraphDetailDialog(
                    graphType = selectedGraph!!,
                    playerStats = playerStats,
                    averageStats = averageStats,
                    heatmapData = statistics!!.heatmap,
                    heatMapAnalysis = heatmapAnalysis,
                    speedData = statistics!!.speed,
                    sprintData = statistics!!.sprint,
                    speedAnalysis = speedAnalysis,
                    sprintAnalysis = sprintAnalysis,
                    staminaData = statistics!!.heartRate,
                    staminaAnalysis = staminaAnalysis,
                    onDismiss = { selectedGraph = null }
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .height(1.dp)
                    .background(Gray.Gray300)
                    .padding(start = 24.dp, bottom = 8.dp)
            )

            // 비슷한 선수 카드
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "유사한 스타일의 선수",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray.Gray700,
                fontFamily = pretendard,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFF1b1b1d), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = similarPlayerName.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary,
                        fontFamily = pretendard
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // 선수 이미지
                    Box(
//                        modifier = Modifier
//                            .size(118.dp)
//                            .background(Color.Gray, RoundedCornerShape(59.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "선수 이미지",
                                modifier = Modifier.size(120.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = "선수 이미지",
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = similarPlayerPosition.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        fontFamily = pretendard
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 157.dp)
                    .background(Color(0xFFF2F5F8), RoundedCornerShape(8.dp)),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "플레이 스타일",
                        fontSize = 14.sp,
                        color = Gray.Gray500,
                        fontFamily = pretendard
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = style.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray.Gray700,
                        fontFamily = pretendard,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "훈련 방향",
                        fontSize = 14.sp,
                        color = Gray.Gray500,
                        fontFamily = pretendard
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = train.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray.Gray700,
                        fontFamily = pretendard
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }
}

// --- 추가: 2x2 그래프 그리드 컴포저블 ---
@Composable
fun MatchDataReportGraphs(
    onGraphClick: (Int) -> Unit,
    playerStats: PlayerStats,
    averageStats: PlayerStats,
    heatmapData: List<List<Int>>,
    speedData: List<Double>,
    sprintData: List<Int>,
    staminaData: List<Int>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 오각형 비교 카드 (PlayerStatCard 한 개)
            GraphCard(
                title = "선수 스탯",
                onClick = { onGraphClick(1) },
                content = {
                    PlayerStatCard(
                        playerStats = playerStats,
                        averageStats = averageStats,
                        modifier = Modifier.size(110.dp),
                        showLabels = false
                    )
                },
                modifier = Modifier.weight(1f)
            )
            GraphCard(
                title = "히트맵",
                onClick = { onGraphClick(2) },
                content = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeatMap(
                            heatData = heatmapData,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(312f / 200f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GraphCard(
                title = "스프린트/스피드",
                onClick = { onGraphClick(3) },
                content = {
                    if (speedData != null && sprintData != null) {
                        SimpleLineChart(
                            speed = speedData,
                            sprint = sprintData,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(110.dp)
                        )
                    } else {
                        Text("데이터 없음")
                    }
                },
                modifier = Modifier.weight(1f)
            )
            GraphCard(
                title = "체력",
                onClick = { onGraphClick(4) },
                content = {
                    StaminaBarChartGrid(
                        barHeight = 18.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(110.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GraphCard(
    title: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFFF2F5F8), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Gray.Gray700,
                    fontFamily = pretendard,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "상세 보기",
                    tint = Gray.Gray700,
                    modifier = Modifier.size(18.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                content()
            }
        }
    }
}

@Composable
fun GraphDetailDialog(
    graphType: Int,
    playerStats: PlayerStats,
    averageStats: PlayerStats,
    heatmapData: List<List<Int>>,
    heatMapAnalysis: String,
    speedData: List<Double>,
    sprintData: List<Int>,
    speedAnalysis: String,
    sprintAnalysis: String,
    staminaData: List<Int>,
    staminaAnalysis: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when(graphType){
                        1 -> "선수 스탯"
                        2 -> "히트맵"
                        3 -> "운동 능력"
                        4 -> "체력"
                        else -> ""
                    },
                    fontSize = 20.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Bold,
                    color = Gray.Gray700,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "닫기",
                        tint = Gray.Gray700
                    )
                }
            }
        },
        text = {
            when (graphType) {
                1 -> {
                    val totalFeedback =
                        "전체적으로 공격과 수비 모두에서 뛰어난 밸런스를 보여줍니다. 스피드와 체력이 좋아 경기 내내 활발한 움직임을 유지할 수 있습니다. 회복력 또한 우수해 짧은 시간 내에 컨디션을 회복할 수 있습니다. 팀의 핵심 역할을 수행할 수 있는 전천후 플레이어입니다."

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PlayerStatCard(
                            playerStats = playerStats,
                            averageStats = averageStats,
                            modifier = Modifier.fillMaxWidth(0.98f),
                            showLabels = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "스탯 분석 결과",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Gray.Gray700,
                            fontFamily = pretendard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Start),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = totalFeedback,
                                color = Gray.Gray500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = pretendard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Start),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
                2 -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    HeatMap(
                        heatData = heatmapData,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(312f / 200f)
                            .clip(RoundedCornerShape(20.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "히트맵 분석 결과",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Gray.Gray700,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = heatMapAnalysis,
                        fontSize = 13.sp,
                        color = Gray.Gray500,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                }
                3 -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SimpleLineChart(
                        speed = speedData,
                        sprint = sprintData,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(220.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "스피드/스프린트 분석 결과",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Gray.Gray700,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = speedAnalysis,
                        fontSize = 13.sp,
                        color = Gray.Gray500,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = sprintAnalysis,
                        fontSize = 13.sp,
                        color = Gray.Gray500,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                }
                4 -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StaminaBarChartModal(
                        myValue = staminaData.average().toFloat(),
                        avgValue = 132.4f,
                        barMaxWidthDp = 220.dp,
                        barHeight = 18.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(110.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "체력 분석 결과",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Gray.Gray700,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = staminaAnalysis,
                        fontSize = 13.sp,
                        color = Gray.Gray500,
                        fontFamily = pretendard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        textAlign = TextAlign.Start
                    )
                }
                else -> Text("")
            }
        },
        confirmButton = {}
    )
}

@Composable
fun SimpleLineChart(
    speed: List<Double>,
    sprint: List<Int>,
    modifier: Modifier = Modifier
) {
    val pointCount = minOf(speed.size, sprint.size)
    val chartPadding = 32.dp
    Box(
        modifier = modifier
            .background(ComposeColor.Black, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val width = size.width
            val height = size.height
            val xStep = width / (pointCount - 1).coerceAtLeast(1)
            val allValues = speed.map { it.toFloat() } + sprint.map { it.toFloat() }
            val minY = allValues.minOrNull() ?: 0f
            val maxY = allValues.maxOrNull() ?: 1f
            fun valueToY(v: Float): Float = height - ((v - minY) / (maxY - minY).coerceAtLeast(0.01f) * height)

            // Y축
            drawLine(
                color = Gray.Gray100,
                start = Offset(0f, 0f),
                end = Offset(0f, height),
                strokeWidth = 2f
            )

            // X축
            drawLine(
                color = Gray.Gray100,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 2f
            )
            // 스피드(파란색)
            speed.map { it.toFloat() }.zipWithNext().forEachIndexed { i, (v1, v2) ->
                drawLine(
                    color = Primary,
                    start = Offset(xStep * i, valueToY(v1)),
                    end = Offset(xStep * (i + 1), valueToY(v2)),
                    strokeWidth = 4f
                )
            }
            // 스프린트(연한 Primary)
            sprint.map { it.toFloat() }.zipWithNext().forEachIndexed { i, (v1, v2) ->
                drawLine(
                    color = Primary.copy(alpha = 0.5f),
                    start = Offset(xStep * i, valueToY(v1)),
                    end = Offset(xStep * (i + 1), valueToY(v2)),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun StaminaBarChartModal(
    myValue: Float,
    avgValue: Float,
    barMaxWidthDp: Dp,
    barHeight: Dp,
    modifier: Modifier = Modifier
) {
    val total = (myValue + avgValue).coerceAtLeast(1f)
    val myRatio = myValue / total
    val avgRatio = avgValue / total
    Column(
        modifier = modifier
            .background(ComposeColor.Black, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(barHeight)
                    .width((barMaxWidthDp * myRatio).coerceAtLeast(12.dp))
                    .background(Primary, RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text("나", color = Primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(barHeight)
                    .width((barMaxWidthDp * avgRatio).coerceAtLeast(12.dp))
                    .background(Gray.Gray400, RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text("평균", color = Gray.Gray400, fontWeight = FontWeight.Bold)
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun StaminaBarChartGrid(
    barHeight: Dp,
    modifier: Modifier = Modifier
) {
    val barRatio = 0.85f
    BoxWithConstraints(
        modifier = modifier
            .background(ComposeColor.Black, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .fillMaxSize()
    ) {
        val maxBarWidth = maxWidth
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .width(maxBarWidth)
                        .background(Primary, RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(8.dp))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .width(maxBarWidth * barRatio)
                        .background(Gray.Gray400, RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

fun Float.format(digits: Int) = "% .${digits}f".format(this)

