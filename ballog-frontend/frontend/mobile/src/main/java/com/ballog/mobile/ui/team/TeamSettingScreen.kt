package com.ballog.mobile.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.ballog.mobile.R
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.System
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.TeamViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun TeamSettingScreen(
    navController: NavController,
    teamId: Int,
    viewModel: TeamViewModel,
    onBackClick: () -> Unit = {},
    onDelegateClick: () -> Unit = {},
    onKickMemberClick: () -> Unit = {},
    onInviteLinkClick: () -> Unit = {},
    onDeleteTeamClick: () -> Unit = {},
    onLeaveTeamClick: () -> Unit = {},
    onUpdateTeamClick: () -> Unit = {}
) {
    var showInviteModal by remember { mutableStateOf(false) }
    val inviteLink by viewModel.inviteLink.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val teamDetail by viewModel.teamDetail.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var showCopyMessage by remember { mutableStateOf(false) }
    
    println("TeamSettingScreen: 현재 팀 ID - $teamId")
    println("TeamSettingScreen: teamDetail - $teamDetail")

    // 팀 상세 정보 로드
    LaunchedEffect(teamId) {
        if (teamId > 0) {
            println("TeamSettingScreen: getTeamDetail 호출 - teamId: $teamId")
            viewModel.getTeamDetail(teamId)
        } else {
            println("TeamSettingScreen: 유효하지 않은 teamId - $teamId")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = "팀 설정",
            type = TopNavType.DETAIL_WITH_BACK,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            SettingMenuItem(
                text = "팀 수정",
                onClick = onUpdateTeamClick
            )

            SettingMenuItem(
                text = "권한 위임",
                onClick = onDelegateClick
            )
            
            SettingMenuItem(
                text = "멤버 강제 퇴장",
                onClick = onKickMemberClick
            )
            
            // Invite Link Button
            SettingMenuItem(
                text = "멤버 초대 링크",
                onClick = {
                    println("TeamSettingScreen: 초대 링크 버튼 클릭됨, 팀 ID - $teamId")
                    if (teamId <= 0) {
                        viewModel.setError("유효하지 않은 팀 ID입니다")
                        return@SettingMenuItem
                    }
                    
                    showInviteModal = true
                    viewModel.setError(null) // 이전 에러 초기화
                    viewModel.setLoading(true) // 로딩 상태 시작
                    
                    coroutineScope.launch {
                        println("TeamSettingScreen: 초대 링크 생성 시작")
                        try {
                            val result = viewModel.generateInviteLink(teamId)
                            println("TeamSettingScreen: 초대 링크 생성 결과 - $result")
                        } catch (e: Exception) {
                            println("TeamSettingScreen: 초대 링크 생성 중 예외 발생 - ${e.message}")
                            viewModel.setError("초대 링크 생성 중 오류가 발생했습니다: ${e.message}")
                        } finally {
                            viewModel.setLoading(false) // 로딩 상태 종료
                        }
                    }
                    onInviteLinkClick()
                }
            )
            
            SettingMenuItem(
                text = "팀 삭제",
                onClick = onDeleteTeamClick,
                isWarning = true
            )
            
            SettingMenuItem(
                text = "팀 탈퇴",
                onClick = onLeaveTeamClick,
                isWarning = true
            )
        }
    }

    // Invite Link Modal
    if (showInviteModal) {
        Dialog(onDismissRequest = { showInviteModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = Gray.Gray100
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = "멤버 초대 링크",
                            fontSize = 20.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Bold,
                            color = Gray.Gray800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "팀에 초대하고 싶은 분에게 링크를 전달해주세요!",
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            color = Gray.Gray500
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Gray.Gray500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 텍스트 필드와 복사 버튼
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .background(Gray.Gray200, RoundedCornerShape(8.dp))
                                    ) {
                                        TextField(
                                            value = inviteLink ?: "",
                                            onValueChange = {},
                                            readOnly = true,
                                            modifier = Modifier.fillMaxSize(),
                                            colors = TextFieldDefaults.colors(
                                                unfocusedContainerColor = Gray.Gray200,
                                                focusedContainerColor = Gray.Gray200,
                                                unfocusedIndicatorColor = Gray.Gray200,
                                                focusedIndicatorColor = Gray.Gray200
                                            ),
                                            textStyle = LocalTextStyle.current.copy(
                                                fontSize = 14.sp,
                                                fontFamily = pretendard,
                                                textAlign = TextAlign.Start
                                            ),
                                            singleLine = true
                                        )
                                    }
                                    Surface(
                                        onClick = { 
                                            inviteLink?.let { 
                                                clipboardManager.setText(AnnotatedString(it))
                                                showCopyMessage = true
                                            } 
                                        },
                                        modifier = Modifier
                                            .size(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Gray.Gray100
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(1.dp, Gray.Gray300, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_copy),
                                                contentDescription = "Copy link",
                                                tint = Gray.Gray600
                                            )
                                        }
                                    }
                                }
                                
                                // 복사 성공 메시지 - Popup 사용
                                if (showCopyMessage) {
                                    Popup(
                                        alignment = Alignment.Center,
                                        onDismissRequest = { showCopyMessage = false }
                                    ) {
                                        Surface(
                                            color = Gray.Gray800,
                                            shape = RoundedCornerShape(8.dp),
                                            shadowElevation = 8.dp,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                        ) {
                                            Text(
                                                text = "링크가 복사되었습니다",
                                                color = Gray.Gray100,
                                                fontSize = 14.sp,
                                                fontFamily = pretendard,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                            )
                                        }
                                    }
                                    
                                    // 3초 후 메시지 숨기기
                                    LaunchedEffect(Unit) {
                                        delay(3000)
                                        showCopyMessage = false
                                    }
                                }
                            }
                        }

                        if (error != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = System.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = error ?: "",
                                    color = System.Red,
                                    fontSize = 14.sp,
                                    fontFamily = pretendard,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        BallogButton(
                            onClick = { showInviteModal = false },
                            type = ButtonType.LABEL_ONLY,
                            buttonColor = ButtonColor.BLACK,
                            label = "닫기",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingMenuItem(
    text: String,
    onClick: () -> Unit,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    color = if (isWarning) System.Red else Gray.Gray800,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            Divider(
                color = Gray.Gray200,
                thickness = 1.dp
            )
        }
    }
}
