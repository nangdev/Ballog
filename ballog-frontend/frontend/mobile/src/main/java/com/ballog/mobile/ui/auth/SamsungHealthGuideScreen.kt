package com.ballog.mobile.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.ui.platform.LocalContext
import com.ballog.mobile.data.util.OnboardingPrefs
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SamsungHealthGuideScreen(
    onConnectClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(150.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = if (page == 0) R.drawable.guide1 else R.drawable.guide2),
                        contentDescription = "Guide ${page + 1}",
                        modifier = Modifier
                            .height(if (page == 0) 120.dp else 200.dp)
                            .width(if (page == 0) 220.dp else 300.dp)
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "Samsung Health 연결",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = pretendard,
                    color = Primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (page == 0) "더보기에서 새운동을 만들어주세요" else "운동 이름, 기록할 데이터를 위 이미지와 같이 설정해주세요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = pretendard,
                    color = Gray.Gray700,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                // 인디케이터(점)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(2) { idx ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == idx) Primary else Gray.Gray300)
                        )
                        if (idx == 0) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                if (page == 0) {
                    BallogButton(
                        onClick = {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.sec.android.app.shealth")
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("market://details?id=com.sec.android.app.shealth")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(marketIntent)
                                Toast.makeText(context, "삼성헬스가 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        type = ButtonType.LABEL_ONLY,
                        buttonColor = ButtonColor.BLACK,
                        label = "설정하러 가기",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    BallogButton(
                        onClick = {
                            OnboardingPrefs.setGuideCompleted(context, true)
                            onConnectClick()
                        },
                        type = ButtonType.LABEL_ONLY,
                        buttonColor = ButtonColor.BLACK,
                        label = "다음",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSamsungHealthGuideScreen() {
    SamsungHealthGuideScreen()
}
