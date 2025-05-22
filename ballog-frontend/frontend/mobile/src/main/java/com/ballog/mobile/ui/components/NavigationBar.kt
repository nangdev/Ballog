package com.ballog.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.pretendard

enum class NavigationTab { HOME, MATCH, TEAM, MYPAGE, DATA }

@Composable
fun NavigationBar(
    selectedTab: NavigationTab = NavigationTab.HOME,
    onTabSelected: (NavigationTab) -> Unit = {},
    onActionClick: () -> Unit = {}
) {
    val activeColor = Gray.Gray800
    val inactiveColor = Gray.Gray400
    val backgroundColor = Gray.Gray100
    val borderColor = Gray.Gray300

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // 바 배경
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .border(BorderStroke(1.dp, borderColor))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavigationBarTab(
                    iconRes = R.drawable.ic_home,
                    label = "홈",
                    selected = selectedTab == NavigationTab.HOME,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(NavigationTab.HOME) },
                    modifier = Modifier.weight(1f)
                )
                NavigationBarTab(
                    iconRes = R.drawable.ic_calendar,
                    label = "매치",
                    selected = selectedTab == NavigationTab.MATCH,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(NavigationTab.MATCH) },
                    modifier = Modifier.weight(1f)
                )
                NavigationBarTab(
                    iconRes = R.drawable.ic_watch, // 아무거나
                    label = "",
                    selected = false,
                    activeColor = Color.Transparent,
                    inactiveColor = Color.Transparent,
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    enabled = false // 안보이게
                )
                NavigationBarTab(
                    iconRes = R.drawable.ic_team,
                    label = "팀",
                    selected = selectedTab == NavigationTab.TEAM,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(NavigationTab.TEAM) },
                    modifier = Modifier.weight(1f)
                )
                NavigationBarTab(
                    iconRes = R.drawable.ic_profile,
                    label = "내 정보",
                    selected = selectedTab == NavigationTab.MYPAGE,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(NavigationTab.MYPAGE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 워치 아이콘만 위로 튀어나오게 배치
        Box(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .absoluteOffset(y = (-20).dp) // 위로 튀어나오게
                .clip(CircleShape)
                .background(activeColor)
                .clickable { onTabSelected(NavigationTab.DATA) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_watch),
                contentDescription = "워치",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NavigationBarTab(
    iconRes: Int,
    label: String,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = if (selected) activeColor else inactiveColor
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (enabled) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = pretendard,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNavigationBar() {
    NavigationBar(
        selectedTab = NavigationTab.HOME,
        onTabSelected = {},
        onActionClick = {}
    )
}
