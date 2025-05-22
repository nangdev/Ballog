package com.ballog.mobile.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.components.Input
import com.ballog.mobile.ui.theme.pretendard
import androidx.navigation.NavController
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Surface
import com.ballog.mobile.viewmodel.AuthViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.ballog.mobile.data.model.SignUpProgress
import com.ballog.mobile.ui.theme.System

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val signUpProgress by viewModel.signUpProgress.collectAsState()

    // 현재 진행 상태 확인
    if (signUpProgress != SignUpProgress.EMAIL_PASSWORD && signUpProgress != SignUpProgress.EMAIL_VERIFICATION) {
        println("SignupScreen - Invalid progress state (current: $signUpProgress, expected: EMAIL_PASSWORD), popping back")
        LaunchedEffect(Unit) {
            println("SignupScreen - Executing popBackStack")
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

    val signUpData = viewModel.signUpData.collectAsState()
    var email by remember { mutableStateOf(signUpData.value.email) }
    var password by remember { mutableStateOf(signUpData.value.password) }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var hasEmailError by remember { mutableStateOf(false) }
    var hasPasswordError by remember { mutableStateOf(false) }
    var hasConfirmPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordErrorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 이메일 인증 상태 관찰
    val emailVerificationState by viewModel.emailVerificationState.collectAsState()

    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Request focus and show keyboard when the screen mounts
    LaunchedEffect(Unit) {
        confirmPasswordFocusRequester.requestFocus()
        keyboardController?.show()
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
                text = "풋살은 볼로그와 함께",
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
                lineHeight = 28.64.sp,
                color = Gray.Gray700,
                fontFamily = pretendard
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "계정을 만들어 볼로그와 함께하세요!",
                fontSize = 15.sp,
                fontWeight = FontWeight.W200,
                lineHeight = 28.64.sp,
                color = Gray.Gray700,
                fontFamily = pretendard
            )

            Spacer(modifier = Modifier.height(20.dp))

            Input(
                value = email,
                onValueChange = { },
                placeholder = "이메일",
                hasError = hasEmailError,
                errorMessage = if (hasEmailError) {
                    when {
                        email.isEmpty() -> "이메일을 입력해주세요"
                        !isValidEmail(email) -> "올바른 이메일 형식이 아닙니다"
                        else -> ""
                    }
                } else "",
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            Input(
                value = password,
                onValueChange = {
                    password = it
                    hasPasswordError = false
                    if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                        hasConfirmPasswordError = true
                        confirmPasswordErrorMessage = "비밀번호가 일치하지 않습니다."
                    } else {
                        hasConfirmPasswordError = false
                    }
                },
                placeholder = "비밀번호",
                hasError = hasPasswordError,
                errorMessage = if (hasPasswordError) {
                    when {
                        password.isEmpty() -> "비밀번호를 입력해주세요"
                        !isValidPassword(password) -> "비밀번호는 8자 이상이어야 합니다"
                        else -> ""
                    }
                } else "",
                isPassword = true,
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityChange = { isPasswordVisible = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Input(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (it != password) {
                        hasConfirmPasswordError = true
                        confirmPasswordErrorMessage = "비밀번호가 일치하지 않습니다."
                    } else {
                        hasConfirmPasswordError = false
                    }
                },
                placeholder = "비밀번호 확인",
                hasError = hasConfirmPasswordError,
                errorMessage = confirmPasswordErrorMessage,
                isPassword = true,
                isPasswordVisible = isConfirmPasswordVisible,
                onPasswordVisibilityChange = { isConfirmPasswordVisible = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester)
            )

            if (hasError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = TextStyle(
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = System.Red
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            BallogButton(
                onClick = {
                    coroutineScope.launch {
                        println("SignupScreen - Next button clicked")
                        when {
                            email.isEmpty() -> {
                                println("SignupScreen - Validation failed: Empty email")
                                hasEmailError = true
                            }
                            password.isEmpty() -> {
                                println("SignupScreen - Validation failed: Empty password")
                                hasPasswordError = true
                            }
                            !isValidEmail(email) -> {
                                println("SignupScreen - Validation failed: Invalid email")
                                hasEmailError = true
                            }
                            !isValidPassword(password) -> {
                                println("SignupScreen - Validation failed: Invalid password")
                                hasPasswordError = true
                            }
                            password != confirmPassword -> {
                                println("SignupScreen - Validation failed: Passwords do not match")
                                hasConfirmPasswordError = true
                                confirmPasswordErrorMessage = "비밀번호가 일치하지 않습니다."
                            }
                            else -> {
                                println("SignupScreen - Starting navigation process")
                                isLoading = true
                                keyboardController?.hide()
                                
                                // 이메일/비밀번호 설정 (동기적 처리)
                                println("SignupScreen - Setting email and password: $email")
                                viewModel.setSignUpEmailAndPassword(email, password)
                                
                                // 이메일 인증 화면으로 상태 변경
                                println("SignupScreen - Proceeding to email verification")
                                viewModel.proceedToEmailVerification()
                                
                                // 먼저 네비게이션 진행
                                println("SignupScreen - Navigating to email verification screen")
                                navController.navigate(Routes.SIGNUP_EMAIL_VERIFICATION) {
                                    popUpTo(Routes.SIGNUP) { inclusive = true }
                                    launchSingleTop = true
                                }
                                
                                // 이메일 발송은 백그라운드에서 처리
                                coroutineScope.launch {
                                    try {
                                        println("SignupScreen - Sending verification email")
                                        viewModel.sendVerificationEmail(email)
                                    } catch (e: Exception) {
                                        println("SignupScreen - Error sending email: ${e.message}")
                                        e.printStackTrace()
                                    } finally {
                                        isLoading = false
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

// 이메일 유효성 검사 함수
private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return email.matches(emailRegex.toRegex())
}

// 비밀번호 유효성 검사 함수
private fun isValidPassword(password: String): Boolean {
    return password.length >= 8
}
