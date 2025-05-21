package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun MatchCard(
    modifier: Modifier = Modifier,
    timeLabel: String,
    startTime: String,
    endTime: String,
    matchName: String,
    onClick: () -> Unit = {}
) {
    fun formatTime(time: String): String {
        return time.split(":").let { parts ->
            if (parts.size >= 2) "${parts[0]}:${parts[1]}" else time
        }
    }
    val timeRange = "${formatTime(startTime)} - ${formatTime(endTime)}"
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Gray.Gray200, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeLabel,
                color = Gray.Gray500,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = pretendard
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeRange,
                color = Gray.Gray800,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = pretendard
            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = matchName,
            color = Gray.Gray800,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard,
            maxLines = 1
        )
    }
}

@Preview(showBackground = false)
@Composable
fun MatchCardPreview() {
    BallogTheme {
        MatchCard(
            timeLabel = "매치 시작시간",
            startTime = "15:00:00",
            endTime = "16:30:00",
            matchName = "잠실 올림픽 공식 풋살 경기장"
        )
    }
}
