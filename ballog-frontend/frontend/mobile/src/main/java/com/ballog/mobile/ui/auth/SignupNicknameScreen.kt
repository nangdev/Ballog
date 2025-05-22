package com.ballog.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.ui.components.Input
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Surface
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.ballog.mobile.data.model.SignUpProgress
import com.ballog.mobile.viewmodel.AuthViewModel
import androidx.activity.compose.BackHandler
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.theme.System
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun SignupNicknameScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    println("SignupNicknameScreen - Composing screen")
    
    // 뒤로가기 처리
    BackHandler {
        println("SignupNicknameScreen - Back button pressed")
        navController.popBackStack()
    }

    val signUpProgress by viewModel.signUpProgress.collectAsState()
    
    // 상태가 변경될 때마다 로그 출력
    LaunchedEffect(signUpProgress) {
        println("SignupNicknameScreen - Progress changed to: $signUpProgress")
    }

    // 현재 진행 상태 확인
    if (signUpProgress != SignUpProgress.NICKNAME && signUpProgress != SignUpProgress.BIRTH_DATE) {
        println("SignupNicknameScreen - Progress mismatch (current: $signUpProgress, expected: NICKNAME or BIRTH_DATE), popping back")
        LaunchedEffect(Unit) {
            println("SignupNicknameScreen - Executing popBackStack")
            navController.popBackStack()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
        ) {
            // 로딩 인디케이터나 빈 화면을 표시
        }
        return
    }

    var nickname by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val signUpData by viewModel.signUpData.collectAsState()

    // ViewModel의 저장된 값으로 초기화
    LaunchedEffect(signUpData) {
        println("SignupNicknameScreen - SignUpData changed: ${signUpData.nickname}")
        if (signUpData.nickname.isNotEmpty()) {
            nickname = signUpData.nickname
        }
        focusRequester.requestFocus()
    }

    // 닉네임 유효성 검사
    fun validateNickname(value: String): String? {
        return when {
            value.isBlank() -> "닉네임을 입력해주세요"
            value.length < 2 -> "닉네임은 2자 이상이어야 합니다"
            value.length > 10 -> "닉네임은 10자 이하여야 합니다"
            !value.matches(Regex("^[가-힣a-zA-Z0-9]+$")) -> "한글, 영문, 숫자만 사용 가능합니다"
            else -> null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(53.dp))

            Text(
                text = "2/4",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 19.09.sp,
                    color = Gray.Gray800
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "닉네임을 정해주세요.",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 23.87.sp,
                    color = Gray.Gray700
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "한글, 영문, 숫자를 사용할 수 있어요 (2-10자)",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Gray.Gray600
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Input(
                value = nickname,
                onValueChange = {
                    nickname = it
                    errorMessage = validateNickname(it)
                    hasError = errorMessage != null
                },
                placeholder = "닉네임",
                hasError = hasError,
                errorMessage = errorMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.weight(1f))

            BallogButton(
                onClick = {
                    when {
                        nickname.isEmpty() -> {
                            hasError = true
                            errorMessage = "닉네임을 입력해주세요."
                        }
                        else -> {
                            coroutineScope.launch {
                                isLoading = true
                                println("SignupNicknameScreen - Completing verification")
                                viewModel.completeVerification()
                                println("SignupNicknameScreen - Setting nickname: $nickname")
                                viewModel.setSignUpNickname(nickname)
                                isLoading = false
                                println("SignupNicknameScreen - Navigating to birth screen")
                                navController.navigate(Routes.SIGNUP_BIRTHDAY) {
                                    popUpTo(Routes.SIGNUP_NICKNAME) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = if (isLoading) "처리 중..." else "다음",
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )
        }
    }
}
