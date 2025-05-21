package com.ballog.mobile.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ballog.mobile.R
import com.ballog.mobile.data.model.Match
import com.ballog.mobile.data.model.MatchState
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.*
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.MatchViewModel
import com.ballog.mobile.viewmodel.buildCalendar
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MatchScreen(navController: NavController, viewModel: MatchViewModel = viewModel()) {
    val today = remember { LocalDate.now() }
    var currentMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }
    var selectedDate by remember { mutableStateOf(today) }
    val matchState by viewModel.matchState.collectAsState()
    val formattedMonth = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월"))
    val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    var showLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentMonth) {
        val startTime = System.currentTimeMillis()
        viewModel.fetchMyMatches(currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
        val duration = System.currentTimeMillis() - startTime
        val minLoadingTime = 500L // 최소 0.5초 유지

        if (duration < minLoadingTime) {
            delay(minLoadingTime - duration)
        }

        showLoading = false
    }


    Column {
        TopNavItem(title = "매치", type = TopNavType.MAIN_BASIC)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .background(Gray.Gray100),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when {
                showLoading -> {
                    MatchSkeletonCard()
                }
                matchState is MatchState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "에러: ${(matchState as MatchState.Error).message}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = pretendard,
                            color = Gray.Gray500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                matchState is MatchState.Success -> {
                    val matches = (matchState as MatchState.Success).matches
                    val calendarData = buildCalendar(currentMonth, matches).map { week ->
                        week.map { marker ->
                            marker.copy(selected = marker.date == selectedDate.dayOfMonth.toString() && marker.thisMonth)
                        }
                    }

                    MatchCalendar(
                        month = formattedMonth,
                        dates = calendarData,
                        onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
                        onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                        onDateClick = { day -> selectedDate = day }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val filteredMatches = matches.filter { it.date == selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (filteredMatches.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "매치 일정이 없습니다",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = pretendard,
                                    color = Gray.Gray500, // 색상 옅게
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            filteredMatches.forEach { match ->
                                MatchCard(
                                    timeLabel = "매치 시간",
                                    startTime = match.startTime,
                                    endTime = match.endTime,
                                    matchName = match.matchName,
                                    onClick = {
                                        navController.navigate("match/detail/${match.id}/${match.matchName}")
                                    }
                                )
                            }
                        }
                        BallogButton(
                            onClick = {
                                navController.navigate("match/register/${selectedDateStr}?mode=PERSONAL")
                            },
                            type = ButtonType.BOTH,
                            buttonColor = ButtonColor.GRAY,
                            icon = painterResource(id = R.drawable.ic_add),
                            label = "매치 등록"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchScreenPreview() {
    BallogTheme {
        val today = LocalDate.of(2025, 4, 22)
        var selectedDate = today
        val matches = listOf(
            Match(1, "2025-04-22", "20:00", "22:00", "강동 송파 풋살장"),
            Match(2, "2025-04-30", "18:00", "20:00", "잠실 풋살장")
        )
        val calendarData = buildCalendar(today.withDayOfMonth(1), matches).map { week ->
            week.map { marker ->
                marker.copy(selected = marker.date == selectedDate.dayOfMonth.toString() && marker.thisMonth)
            }
        }
        val filteredMatches = matches.filter { it.date == selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray.Gray100),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopNavItem(title = "팀", type = TopNavType.MAIN_BASIC)
            Spacer(modifier = Modifier.height(24.dp))

            MatchCalendar(
                month = "2025년 4월",
                dates = calendarData,
                onPrevMonth = {},
                onNextMonth = {},
                onDateClick = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (filteredMatches.isEmpty()) {
                    Text("매치 일정이 없습니다", modifier = Modifier.padding(bottom = 8.dp))
                } else {
                    filteredMatches.forEach { match ->
                        MatchCard(
                            timeLabel = "매치 시간",
                            startTime = match.startTime,
                            endTime = match.endTime,
                            matchName = match.matchName
                        )
                    }
                }
                BallogButton(
                    onClick = { /* TODO: navigate to match creation */ },
                    type = ButtonType.BOTH,
                    buttonColor = ButtonColor.GRAY,
                    icon = painterResource(id = R.drawable.ic_add),
                    label = "매치 등록"
                )
            }
        }
    }
}
