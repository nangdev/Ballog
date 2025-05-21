package com.ballog.mobile.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ballog.mobile.R
import com.ballog.mobile.data.model.MatchState
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.components.MatchCalendar
import com.ballog.mobile.ui.components.MatchCard
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.MatchViewModel
import com.ballog.mobile.viewmodel.buildCalendar
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TeamMatchTab(
    navController: NavController,
    teamId: Int,
    viewModel: MatchViewModel = viewModel()
)
 {
    val today = remember { LocalDate.now() }
    var currentMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }
    var selectedDate by remember { mutableStateOf(today) }
    val matchState by viewModel.matchState.collectAsState()
    val formattedMonth = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월"))
    val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

     var showLoading by remember { mutableStateOf(true) }

     LaunchedEffect(currentMonth) {
         val formattedMonth = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
         android.util.Log.d("TeamMatchTab", "📡 fetchTeamMatches 요청: teamId=$teamId, month=$formattedMonth")

         val startTime = System.currentTimeMillis()
         viewModel.fetchTeamMatches(teamId, formattedMonth)
         val duration = System.currentTimeMillis() - startTime
         val minLoadingTime = 500L
         if (duration < minLoadingTime) {
             delay(minLoadingTime - duration)
         }
         showLoading = false
     }



     Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .background(Gray.Gray100),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         when {
             showLoading -> {
                 MatchSkeletonCard()
             }
             matchState is MatchState.Error -> {
                 Text(
                     text = "에러: ${(matchState as MatchState.Error).message}",
                     fontSize = 16.sp,
                     fontWeight = FontWeight.Medium,
                     fontFamily = pretendard,
                     modifier = Modifier.padding(16.dp)
                 )
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

                val filteredMatches = matches.filter { it.date == selectedDate.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

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
                            navController.navigate("match/register/$selectedDateStr?teamId=$teamId")
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
