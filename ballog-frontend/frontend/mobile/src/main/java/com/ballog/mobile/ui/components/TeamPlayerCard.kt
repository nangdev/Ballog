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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun TeamPlayerCard(
    name: String,
    isManager: Boolean = false,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null // nullable 처리
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        color = Gray.Gray200
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Gray.Gray800
                )

                if (isManager) {
                    Box(modifier = Modifier.padding(top = 2.dp)) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFF7EE4EA).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "매니저",
                                fontSize = 12.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight.Normal,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 카드 아이콘: 클릭 리스너가 주어졌을 때만 표시
            if (onCardClick != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp) // 터치 영역 확보
                        .clickable(onClick = onCardClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_card),
                        contentDescription = "카드",
                        tint = Gray.Gray800,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
