package com.ballog.mobile.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard

enum class TopNavType {
    MAIN_WITH_CREATE,        // 생성/추가 버튼이 있는 메인
    MAIN_WITH_CLOSE,         // 닫기 버튼이 있는 메인
    MAIN_BASIC,             // 기본 메인 (아이콘 없음)
    DETAIL_WITH_BACK,       // 뒤로가기만 있는 상세
    DETAIL_WITH_BACK_CLOSE, // 뒤로가기와 닫기가 있는 상세
    DETAIL_WITH_BACK_SETTINGS // 뒤로가기와 설정이 있는 상세
}

@Composable
fun TopNavItem(
    title: String,
    type: TopNavType,
    navController: NavController? = null,
    onBackClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Gray.Gray100
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (type) {
                TopNavType.MAIN_WITH_CREATE -> {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = pretendard,
                        color = Gray.Gray700,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)  // Added padding for main title
                    )
                    IconButton(
                        onClick = { onActionClick?.invoke() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "추가",
                            tint = Gray.Gray700
                        )
                    }
                }
                TopNavType.MAIN_WITH_CLOSE -> {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Bold,
                        color = Gray.Gray700,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)  // Added padding for main title
                    )
                    IconButton(
                        onClick = { onActionClick?.invoke() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "닫기",
                            tint = Gray.Gray700
                        )
                    }
                }
                TopNavType.MAIN_BASIC -> {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Bold,
                        color = Gray.Gray700,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)  // Added padding for main title
                    )
                }
                TopNavType.DETAIL_WITH_BACK, TopNavType.DETAIL_WITH_BACK_CLOSE, TopNavType.DETAIL_WITH_BACK_SETTINGS -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                onBackClick?.invoke() ?: navController?.popBackStack()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_navigate_before),
                                contentDescription = "뒤로가기",
                                tint = Gray.Gray700
                            )
                        }
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Bold,
                            color = Gray.Gray700,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        when (type) {
                            TopNavType.DETAIL_WITH_BACK_CLOSE -> {
                                IconButton(
                                    onClick = { onActionClick?.invoke() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "닫기",
                                        tint = Gray.Gray700
                                    )
                                }
                            }
                            TopNavType.DETAIL_WITH_BACK_SETTINGS -> {
                                IconButton(
                                    onClick = { onActionClick?.invoke() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_setting),
                                        contentDescription = "설정",
                                        tint = Gray.Gray700
                                    )
                                }
                            }
                            else -> {
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "메인 - 추가 버튼")
@Composable
fun TopNavItemMainWithAddIconPreview() {
    TopNavItem(
        title = "팀",
        type = TopNavType.MAIN_WITH_CREATE,
        onActionClick = {}
    )
}

@Preview(showBackground = true, name = "메인 - 닫기 버튼")
@Composable
fun TopNavItemMainWithExitIconPreview() {
    TopNavItem(
        title = "팀",
        type = TopNavType.MAIN_WITH_CLOSE,
        onActionClick = {}
    )
}

@Preview(showBackground = true, name = "메인 - 아이콘 없음")
@Composable
fun TopNavItemMainWithoutIconPreview() {
    TopNavItem(
        title = "팀",
        type = TopNavType.MAIN_BASIC,
        onActionClick = {}
    )
}

@Preview(showBackground = true, name = "네비게이션 - 뒤로가기만")
@Composable
fun TopNavItemNavigationLeftPreview() {
    TopNavItem(
        title = "팀 상세",
        type = TopNavType.DETAIL_WITH_BACK,
        onBackClick = {}
    )
}

@Preview(showBackground = true, name = "네비게이션 - 뒤로가기 + 닫기")
@Composable
fun TopNavItemNavigationRightPreview() {
    TopNavItem(
        title = "팀 상세",
        type = TopNavType.DETAIL_WITH_BACK_CLOSE,
        onBackClick = {},
        onActionClick = {}
    )
}

@Preview(showBackground = true, name = "네비게이션 - 뒤로가기 + 설정")
@Composable
fun TopNavItemNavigationSettingsPreview() {
    TopNavItem(
        title = "팀 상세",
        type = TopNavType.DETAIL_WITH_BACK_SETTINGS,
        onBackClick = {},
        onActionClick = {}
    )
}

