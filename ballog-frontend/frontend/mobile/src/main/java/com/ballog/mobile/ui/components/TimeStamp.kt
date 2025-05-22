package com.ballog.mobile.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TimeStamp(
    startTime: String,
    endTime: String
) {
    // 시간 형식 검증 및 패딩 적용
    val formattedStartTime = formatTimeWithPadding(startTime)
    val formattedEndTime = formatTimeWithPadding(endTime)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(Primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = formattedStartTime,
            fontSize = 11.sp,
            color = Primary,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "-",
            fontSize = 11.sp,
            color = Primary,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formattedEndTime,
            fontSize = 11.sp,
            color = Primary,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard
        )
    }
}

/**
 * 시간 문자열을 검증하고 2자리 패딩을 적용합니다.
 * 빈 문자열은 "00:00"으로 설정하고, 각 부분이 2자리가 되도록 합니다.
 */
private fun formatTimeWithPadding(time: String): String {
    if (time.isBlank()) return "00:00"
    
    val parts = time.split(":")
    return when {
        parts.size == 2 -> {
            val min = parts[0].padStart(2, '0')
            val sec = parts[1].padStart(2, '0')
            "$min:$sec"
        }
        parts.size == 1 && time.length <= 2 -> {
            // 숫자만 있는 경우 분으로 처리
            val min = time.padStart(2, '0')
            "$min:00"
        }
        else -> "00:00"  // 잘못된 형식인 경우 기본값 반환
    }
}
