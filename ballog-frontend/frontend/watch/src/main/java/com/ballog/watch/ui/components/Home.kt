package com.ballog.watch.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ballog.watch.R

@Composable
fun HomeScreen(onMeasureClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 1) 화면 상단 절반: 로고
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth(),
        ) {
            // 상단 1/2 (빈 영역)
            Box(modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
            )
            // 하단 1/2: 축구공 이미지
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.soccer_ball),
                    contentDescription = "축구공",
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // 2) 화면 하단 절반: 버튼
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BallogButton(
                text = "경기장 측정하기",
                onClick = onMeasureClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)  // 너비 80%
                    .height(40.dp)       // 높이 고정
            )
        }
    }
}
