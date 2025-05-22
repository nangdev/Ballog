package com.ballog.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.pretendard
import androidx.navigation.NavController
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.navigation.Routes
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Surface
import com.ballog.mobile.ui.theme.System
import com.ballog.mobile.viewmodel.AuthViewModel
import com.ballog.mobile.data.model.EmailVerificationResult
import com.ballog.mobile.data.model.SignUpProgress
import androidx.activity.compose.BackHandler

@Composable
fun SignupVerificationScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // 상태 값들을 수집
    val signUpProgress by viewModel.signUpProgress.collectAsState()
    val signUpData by viewModel.signUpData.collectAsState()
    val emailVerificationState by viewModel.emailVerificationState.collectAsState()
    
    // 안전한 코루틴 스코프
    val coroutineScope = rememberCoroutineScope()

    // 뒤로가기 처리
    BackHandler {
        navController.popBackStack()
    }

    // 진행 상태 검사 및 처리
    if (signUpProgress != SignUpProgress.EMAIL_VERIFICATION && signUpProgress != SignUpProgress.NICKNAME) {
        // LaunchedEffect 대신 SideEffect 사용
        SideEffect {
            navController.popBackStack()
        }
        return
    }

    // 로컬 UI 상태
    var verificationCode by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var shouldVerify by remember { mutableStateOf(false) }
    
    // UI 상태 업데이트 - 컴포지션 안에서 직접 처리
    when (emailVerificationState) {
        is EmailVerificationResult.Error -> {
            val error = emailVerificationState as EmailVerificationResult.Error
            // CancellationException 관련 에러 메시지는 무시
            if (!error.message.contains("Cancellation", ignoreCase = true) && 
                !error.message.contains("coroutine scope", ignoreCase = true)) {
                hasError = true
                errorMessage = error.message
            }
            isLoading = false
        }
        is EmailVerificationResult.Loading -> {
            isLoading = true
            hasError = false
        }
        is EmailVerificationResult.Success -> {
            isLoading = false
            hasError = false
            
            // 인증 성공 및 코드 6자리인 경우 네비게이션
            if (verificationCode.length == 6) {
                // 단 한번 실행되도록 flag 사용
                val firstRun = remember { mutableStateOf(true) }
                if (firstRun.value) {
                    firstRun.value = false
                    
                    // SideEffect 내에서 UI 업데이트 (코루틴 없이)
                    SideEffect {
                        viewModel.completeVerification()
                        navController.navigate(Routes.SIGNUP_NICKNAME) {
                            popUpTo(Routes.SIGNUP_EMAIL_VERIFICATION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
    
    // 자동 인증 호출을 위한 SideEffect
    if (shouldVerify && verificationCode.length == 6 && !isLoading) {
        shouldVerify = false
        SideEffect {
            viewModel.verifyEmailNonSuspend(signUpData.email, verificationCode)
        }
    }

    // 포커스 및 커서 관련
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }
    
    // 커서 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                0f at 500
                1f at 1000
            },
            repeatMode = RepeatMode.Restart
        ), label = "cursorAnim"
    )

    // UI 구성
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
                text = "1/4",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray.Gray800,
                fontFamily = pretendard
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${signUpData.email}로 전송된\n인증번호를 입력해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 23.87.sp,
                color = Gray.Gray700,
                fontFamily = pretendard
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } catch (e: Exception) {
                            // 포커스 요청 실패는 무시
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = verificationCode,
                    onValueChange = {
                        // 숫자만 입력 가능하고 6자리로 제한
                        val newValue = it.filter { char -> char.isDigit() }
                        if (newValue.length <= 6) {
                            verificationCode = newValue
                            
                            // 6자리 입력 완료 시 자동 검증 플래그 설정
                            if (newValue.length == 6 && !isLoading) {
                                shouldVerify = true
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    textStyle = TextStyle(
                        color = Color.Transparent,
                        fontSize = 24.sp
                    ),
                    cursorBrush = SolidColor(Color.Transparent),
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused.value = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(280.dp)
                            ) {
                                for (i in 0 until 6) {
                                    val char = verificationCode.getOrNull(i)?.toString() ?: ""
                                    val showCursor = isFocused.value && verificationCode.length == i
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(width = 40.dp, height = 48.dp)
                                            .background(
                                                color = if (hasError) Gray.Gray200 else Gray.Gray300,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (hasError) System.Red else Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    ) {
                                        if (char.isNotEmpty()) {
                                            Text(
                                                text = char,
                                                fontFamily = pretendard,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                lineHeight = 28.64.sp,
                                                color = if (hasError) System.Red else Gray.Gray700,
                                                textAlign = TextAlign.Center
                                            )
                                        } else if (showCursor) {
                                            Box(
                                                Modifier
                                                    .width(2.dp)
                                                    .height(28.dp)
                                                    .background(Gray.Gray500.copy(alpha = cursorAlpha), RoundedCornerShape(1.dp))
                                            )
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .alpha(0f)
                            ) {
                                innerTextField()
                            }
                        }
                    }
                )
            }

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
                    if (verificationCode.isEmpty()) {
                        hasError = true
                        errorMessage = "인증 코드를 입력해주세요."
                    } else {
                        // verifyEmailNonSuspend 직접 호출
                        viewModel.verifyEmailNonSuspend(signUpData.email, verificationCode)
                    }
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = if (isLoading) "처리 중..." else "인증하기",
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )
        }
    }
}
