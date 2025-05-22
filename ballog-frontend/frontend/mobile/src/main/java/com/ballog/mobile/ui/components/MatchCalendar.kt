package com.ballog.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.*
import java.time.LocalDate

private val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
fun MatchCalendar(
    month: String,
    dates: List<List<DateMarkerState>>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val year = month.substring(0, 4).toIntOrNull() ?: LocalDate.now().year
    val monthNum = month.substring(5, 7).toIntOrNull() ?: LocalDate.now().monthValue

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        color = Gray.Gray700
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigate_before),
                    contentDescription = "이전 달",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onPrevMonth() },
                    tint = Gray.Gray100
                )

                Text(
                    text = month,
                    color = Gray.Gray100,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = pretendard
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_navigate_next),
                    contentDescription = "다음 달",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNextMonth() },
                    tint = Gray.Gray100
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        color = Gray.Gray400,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = pretendard,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            dates.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { dateMarkerState ->
                        val dayInt = dateMarkerState.date.toIntOrNull()
                        val dayDate = if (dayInt != null && dateMarkerState.thisMonth) {
                            LocalDate.of(year, monthNum, dayInt)
                        } else null

                        MatchDateMarker(
                            date = dateMarkerState.date,
                            marked = dateMarkerState.marked,
                            selected = dateMarkerState.selected,
                            thisMonth = dateMarkerState.thisMonth,
                            modifier = if (dayDate != null) Modifier.clickable { onDateClick(dayDate) } else Modifier
                        )
                    }
                }
            }
        }
    }
}

data class DateMarkerState(
    val date: String,
    val marked: Boolean,
    val selected: Boolean,
    val thisMonth: Boolean
)

@Preview(showBackground = false)
@Composable
fun PreviewMatchCalendar() {
    BallogTheme {
        val sampleDates = listOf(
            listOf(
                DateMarkerState("30", marked = false, selected = false, thisMonth = false),
                DateMarkerState("1", marked = true, selected = false, thisMonth = true),
                DateMarkerState("2", marked = false, selected = false, thisMonth = true),
                DateMarkerState("3", marked = false, selected = true, thisMonth = true),
                DateMarkerState("4", marked = true, selected = false, thisMonth = true),
                DateMarkerState("5", marked = false, selected = false, thisMonth = true),
                DateMarkerState("6", marked = false, selected = false, thisMonth = true),
            ),
            listOf(
                DateMarkerState("7", marked = false, selected = false, thisMonth = true),
                DateMarkerState("8", marked = true, selected = false, thisMonth = true),
                DateMarkerState("9", marked = false, selected = false, thisMonth = true),
                DateMarkerState("10", marked = false, selected = false, thisMonth = true),
                DateMarkerState("11", marked = true, selected = false, thisMonth = true),
                DateMarkerState("12", marked = false, selected = false, thisMonth = true),
                DateMarkerState("13", marked = false, selected = false, thisMonth = true),
            )
        )
        MatchCalendar(
            month = "2025-04",
            dates = sampleDates,
            onPrevMonth = {},
            onNextMonth = {},
            onDateClick = {}
        )
    }
}
