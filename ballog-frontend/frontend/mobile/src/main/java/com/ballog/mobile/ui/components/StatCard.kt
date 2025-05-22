package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard

/**
 * bars: 최근 매치 데이터. 값이 true/false면 최근 경기만 색상, 나머지는 회색. 값이 Float이면 0~1로 길이 조절.
 * bars.size < 5면 나머지는 빈 바(짧은 회색)로 채움.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    bars: List<Float>,         // 0~1 사이 실수 값
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val totalBars = 5
    val barWidth = 12.dp
    val barMaxHeight = 48.dp
    val barMinHeight = 8.dp
    val barSpacing = 6.dp

    // bars 개수가 부족하면 앞에 0f로 채워서 5개 맞춤
    val filledBars = List(totalBars - bars.size) { 0f } + bars

    Column(
        modifier = modifier
            .background(Gray.Gray200, shape = RoundedCornerShape(20.dp))
            .width(150.dp)
            .height(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Gray.Gray500,
            fontFamily = pretendard,
            modifier = Modifier.padding(top = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Gray.Gray700,
            fontFamily = pretendard
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .height(barMaxHeight)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            filledBars.forEachIndexed { idx, ratio ->
                val isLast = idx == filledBars.lastIndex
                val isEmpty = ratio <= 0f
                val height = if (isEmpty) barMinHeight else barMaxHeight * ratio
                val color = when {
                    isLast && !isEmpty -> barColor       // 마지막 bar만 강조 색
                    else -> Gray.Gray400
                }

                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(height.coerceAtLeast(barMinHeight))
                        .background(color, shape = RoundedCornerShape(4.dp))
                )

                if (idx != filledBars.lastIndex) {
                    Spacer(modifier = Modifier.width(barSpacing))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "StatCard Preview - 기본")
@Composable
fun StatCardPreview() {
    StatCard(
        title = "이동거리",
        value = "5.2km",
        bars = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f),
        barColor = Color(0xFF7EE4EA) // Primary 색 직접 지정
    )
}

@Preview(showBackground = true, name = "StatCard Preview - 3개만")
@Composable
fun StatCardPreviewShort() {
    StatCard(
        title = "스프린트",
        value = "12회",
        bars = listOf(0.4f, 0.7f, 1.0f), // 앞쪽 2개는 자동으로 짧은 bar
        barColor = Color(0xFF7EE4EA)
    )
}

@Preview(showBackground = true, name = "StatCard Preview - 비어있는 경우")
@Composable
fun StatCardPreviewEmpty() {
    StatCard(
        title = "심박수",
        value = "-",
        bars = emptyList(), // 모두 짧은 회색 bar
        barColor = Color(0xFF7EE4EA)
    )
}


