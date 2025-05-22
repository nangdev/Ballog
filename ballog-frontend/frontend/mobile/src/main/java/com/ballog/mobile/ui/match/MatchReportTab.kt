package com.ballog.mobile.ui.match

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import com.ballog.mobile.ui.components.DropDown
import com.ballog.mobile.ui.components.SectionHeader
import com.ballog.mobile.ui.components.TeamPlayerCard
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ballog.mobile.data.dto.MatchDetailResponseDto
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard

private const val TAG = "MatchReportTab"

@Composable
fun MatchReportTab(matchDetail: MatchDetailResponseDto) {
    val quarters = matchDetail.quarterList
    val participants = matchDetail.participantList

    // DropDown 상태
    var selectedQuarterIndex by remember { mutableIntStateOf(0) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    // SectionHeader 상태
    var myRecordExpanded by remember { mutableStateOf(true) }
    var playerListExpanded by remember { mutableStateOf(true) }

    val quarterOptions = quarters.map { "${it.quarterNumber} 쿼터" }
    val selectedQuarter = quarters.getOrNull(selectedQuarterIndex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // DropDown
        if (quarterOptions.isNotEmpty()) {
            DropDown(
                items = quarterOptions,
                selectedItem = quarterOptions.getOrNull(selectedQuarterIndex) ?: "",
                onItemSelected = { selectedLabel ->
                    selectedQuarterIndex = quarterOptions.indexOf(selectedLabel)
                },
                expanded = dropDownExpanded,
                onExpandedChange = { dropDownExpanded = it },
                modifier = Modifier
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HeatMap
            if (selectedQuarter != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    // elevation = 4.dp // 필요시 추가
                ) {
                    Row(
                        modifier = Modifier
                            .background(Gray.Gray100) // 회색 배경
                            .height(40.dp) // 카드 높이
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 왼쪽 진영
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedQuarter.matchSide == "left") "Home" else "Away",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = pretendard,
                                color = Gray.Gray500
                            )
                        }
                        // 가운데 세로선
                        Divider(
                            color = Gray.Gray100,
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                        )
                        // 오른쪽 진영
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedQuarter.matchSide == "left") "Away" else "Home",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = pretendard,
                                color = Gray.Gray500
                            )
                        }
                    }
                }
            if (selectedQuarter?.reportData != null) {
                HeatMap(heatData = selectedQuarter.reportData.heatmap, modifier = Modifier)
            } else {
                // 빈 heatmap 보여주기 (예: 전부 0)
                val emptyHeat = List(10) { List(16) { 0 } }
                HeatMap(heatData = emptyHeat, modifier = Modifier)
            }
            }
        }
        // 내 기록 Section
        SectionHeader(
            title = "내 기록",
            isExpanded = myRecordExpanded,
            onToggle = { myRecordExpanded = !myRecordExpanded },
            modifier = Modifier
        )

        AnimatedVisibility(
            visible = myRecordExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (selectedQuarter?.reportData != null) {
                val report = selectedQuarter.reportData
                val reportCards = listOf(
                    Triple("총 시간", calculateDuration(report.startTime, report.endTime), "분"),
                    Triple("이동 거리", "%.1f".format(report.distance.toDouble()), "km"),
                    Triple("최고 속도", "%.1f".format(report.maxSpeed.toDouble()), "km/h"),
                    Triple("평균 속도", "%.1f".format(report.avgSpeed.toDouble()), "km/h"),
                    Triple("스프린트", report.sprint.toString(), "회"),
                    Triple("소모 칼로리", "%.1f".format(report.calories.toDouble()), "kcal"),
                    Triple("평균 심박수", "%.1f".format(report.avgHeartRate.toDouble()), "bpm"),
                    Triple("최대 심박수", "%.1f".format(report.maxHeartRate.toDouble()), "bpm")
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reportCards.forEach { (label, value, unit) ->
                        MatchReportCard(label = label, value = value, unit = unit)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 매치 기록이 등록되지 않았어요",
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Normal,
                        color = Gray.Gray400
                    )
                }
            }
        }

        // 참여 인원 Section (개인 경기에서는 표시 안 함)
        if (participants.isNotEmpty()) {
            SectionHeader(
                title = "참여 인원",
                isExpanded = playerListExpanded,
                onToggle = { playerListExpanded = !playerListExpanded },
                modifier = Modifier
            )

            AnimatedVisibility(
                visible = playerListExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    participants.forEach { player ->
                        TeamPlayerCard(
                            name = player.nickName,
                            isManager = player.role.uppercase() == "MANAGER"
                        )
                    }
                }
            }
        }
    }

    Log.d(TAG, "선택된 쿼터: ${selectedQuarter?.quarterNumber}")
}


fun calculateDuration(start: String, end: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        val startTime = java.time.LocalTime.parse(start, formatter)
        val endTime = java.time.LocalTime.parse(end, formatter)
        val duration = java.time.Duration.between(startTime, endTime).toMinutes()
        duration.toString()
    } catch (e: Exception) {
        "-"
    }
}

