package com.ballog.mobile.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.theme.BallogTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun LikedVideosScreen(onBack: (() -> Unit)? = null, onClose: (() -> Unit)? = null) {
    val dummyList = listOf(
        LikedVideoItem("1", "2025.05.08 15:00", "강동 송파 풋살장 어쩌구저쩌구쏼라쏼라", "하이라이트 1"),
        LikedVideoItem("2", "2025.05.08 15:00", "강동 송파 풋살장", "하이라이트 2"),
        LikedVideoItem("3", "2025.05.08 15:00", "강동 송파 풋살장", "하이라이트 3")
    )

    Column {
        TopNavItem(
            title = "좋아요한 영상",
            type = TopNavType.DETAIL_WITH_BACK,
            onBackClick = { onBack?.invoke() },
            onActionClick = { onClose?.invoke() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyList) { video ->
                LikedHighlightCardFigma(video = video)
            }
        }
    }
}

@Composable
fun LikedHighlightCardFigma(video: LikedVideoItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF2F5F8), shape = RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFECEEF0),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "일시",
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9BA0A5)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = video.dateTime,
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "위치",
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9BA0A5)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = video.location,
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = video.title,
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LikedVideosScreenPreview() {
    BallogTheme {
        LikedVideosScreen()
    }
}
