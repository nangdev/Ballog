package com.ballog.mobile.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun HighlightCard(
    title: String,
    startTime: String,
    endTime: String,
    onEdit: () -> Unit,
    onLike: () -> Unit,
    onClick: () -> Unit = {}
) {
    var isLiked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray.Gray200, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Gray.Gray800,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = pretendard,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "편집",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEdit() },
                    tint = Gray.Gray700
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeStamp(startTime = startTime, endTime = endTime)

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(id = if (isLiked) R.drawable.ic_heart_fill else R.drawable.ic_heart),
                    contentDescription = if (isLiked) "좋아요 취소" else "좋아요",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { 
                            isLiked = !isLiked
                            onLike() 
                        },
                    tint = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HighlightCardPreview() {
    BallogTheme {
        HighlightCard(
            title = "구간 1 - 제목이 길어질 경우 말줄임 처리됩니다다다다다다다다다다",
            startTime = "4:39",
            endTime = "6:34",
            onEdit = {},
            onLike = {}
        )
    }
}
