package com.ballog.mobile.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard

data class LikedVideoItem(
    val id: String,
    val dateTime: String,
    val location: String,
    val title: String
)

@Composable
fun LikedHighlightCard(video: LikedVideoItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray.Gray200, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "일시",
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Gray.Gray500
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = video.dateTime,
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Gray.Gray800
            )

            Spacer(modifier = Modifier.width(48.dp))

            Text(
                text = "위치",
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Gray.Gray500
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = video.location,
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Gray.Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f) // ✅ 줄바꿈 없이 한 줄로 말줄임 처리
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = video.title,
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight.Medium,
            color = Gray.Gray800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LikedHighlightCardPreview() {
    BallogTheme {
        LikedHighlightCard(
            video = LikedVideoItem(
                id = "1",
                dateTime = "2025.05.08 15:00",
                location = "강동송파풋살장",
                title = "하이라이트 1"
            )
        )
    }
}
