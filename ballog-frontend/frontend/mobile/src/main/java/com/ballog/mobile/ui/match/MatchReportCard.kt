package com.ballog.mobile.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MatchReportCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Gray.Gray200,
                shape = RoundedCornerShape(8.dp)
            )
            .height(51.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Gray.Gray500,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = pretendard
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = Gray.Gray700,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = pretendard
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                color = Gray.Gray500,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = pretendard
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchReportCardPreview() {
    BallogTheme {
        MatchReportCard(
            label = "총 득점",
            value = "3",
            unit = "골"
        )
    }
}
