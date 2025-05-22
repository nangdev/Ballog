package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import androidx.compose.ui.text.font.FontFamily
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun TabMenu(
    leftTabText: String,
    rightTabText: String,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Gray.Gray100)
    ) {
        // Top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Gray.Gray200)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            // Left Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(0) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = leftTabText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedTab == 0) Primary else Gray.Gray800,
                    textAlign = TextAlign.Center,
                    fontFamily = pretendard
                )
                if (selectedTab == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Primary)
                    )
                }
            }

            // Right Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(1) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rightTabText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedTab == 1) Primary else Gray.Gray800,
                    textAlign = TextAlign.Center,
                    fontFamily = pretendard
                )
                if (selectedTab == 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Primary)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabMenuPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabMenu(
            leftTabText = "팀 정보",
            rightTabText = "매치",
            selectedTab = 0
        )
        
        TabMenu(
            leftTabText = "팀 정보",
            rightTabText = "매치",
            selectedTab = 1
        )
    }
} 
