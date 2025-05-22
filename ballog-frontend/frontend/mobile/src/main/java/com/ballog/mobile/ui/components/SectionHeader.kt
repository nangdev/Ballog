package com.ballog.mobile.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp + 2.dp) // 아래 경계선 높이 포함
    ) {
        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 90f else 0f,
            animationSpec = tween(durationMillis = 300, easing = EaseInOut),
            label = "rotateSectionIcon"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Gray.Gray100)
                .clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigate_next),
                    contentDescription = "Toggle Section",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }

            Text(
                text = title,
                color = Gray.Gray800,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = pretendard,
                modifier = Modifier.padding(start = 0.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(Gray.Gray300)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SectionHeaderInteractivePreview() {
    BallogTheme {
        var isExpanded by remember { mutableStateOf(false) }

        SectionHeader(
            title = "내 기록",
            isExpanded = isExpanded,
            onToggle = { isExpanded = !isExpanded }
        )
    }
}


