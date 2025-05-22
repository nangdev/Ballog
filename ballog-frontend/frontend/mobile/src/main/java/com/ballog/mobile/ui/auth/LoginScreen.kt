package com.ballog.mobile.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.components.Input
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Surface
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ballog.mobile.viewmodel.AuthViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.ballog.mobile.data.model.AuthResult
import com.ballog.mobile.navigation.Routes
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.ballog.mobile.MainActivity

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    return email.matches(emailRegex.toRegex())
}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // 화면이 처음 표시될 때 authState 초기화
    LaunchedEffect(Unit) {
        viewModel.resetAuthState()
    }

    // 뒤로가기 핸들러 추가
    BackHandler {
        navController.navigate(Routes.ONBOARDING) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }
    var context = LocalContext.current

    val lastCredentials by viewModel.lastSignUpCredentials.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var hasEmailError by remember { mutableStateOf(false) }
    var hasPasswordError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }

    // 만약 회원가입 후 이동했다면 이메일과 비밀번호 채우기
    LaunchedEffect(lastCredentials) {
        lastCredentials?.let { (savedEmail, savedPassword) ->
            println("LoginScreen - Filling in credentials from signup: $savedEmail")
            email = savedEmail
            password = savedPassword
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val authState by viewModel.authState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthResult.Success -> {
                // MainActivity를 재실행하여 분기 로직을 항상 타도록
                val context = navController.context
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
            is AuthResult.Error -> {
                when ((authState as AuthResult.Error).message) {
                    "회원가입이 필요합니다" -> {
                        println("LoginScreen - Before setting signup data: email=$email, password=$password")
                        viewModel.resetSignUpProgress()  // 회원가입 진행 상태 초기화
                        viewModel.setSignUpEmailAndPassword(email, password)
                        println("LoginScreen - After setting signup data")
                        navController.navigate(Routes.SIGNUP) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                    "비밀번호가 일치하지 않습니다" -> {
                        hasPasswordError = true
                        passwordErrorMessage = (authState as AuthResult.Error).message
                    }
                    else -> {
                        hasEmailError = true
                        emailErrorMessage = (authState as AuthResult.Error).message
                    }
                }
            }
            is AuthResult.Loading -> {
                // 로딩 상태는 isLoading 변수로 처리
            }
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
                text = "풋살은 볼로그와 함께",
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
                lineHeight = 28.64.sp,
                color = Gray.Gray700,
                fontFamily = pretendard
            )

            Spacer(modifier = Modifier.height(40.dp))

            Input(
                value = email,
                onValueChange = {
                    email = it
                    hasEmailError = false
                },
                placeholder = "이메일",
                hasError = hasEmailError,
                errorMessage = emailErrorMessage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Input(
                value = password,
                onValueChange = {
                    password = it
                    hasPasswordError = false
                },
                placeholder = "비밀번호",
                hasError = hasPasswordError,
                errorMessage = passwordErrorMessage,
                isPassword = true,
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityChange = { isPasswordVisible = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "비밀번호를 잊으셨나요?",
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                color = Gray.Gray800,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        // 비밀번호 찾기 기능 구현 예정
                    },
                fontFamily = pretendard
            )

            BallogButton(
                onClick = {
                    when {
                        email.isEmpty() -> {
                            hasEmailError = true
                            emailErrorMessage = "이메일을 입력해주세요"
                        }
                        !isValidEmail(email) -> {
                            hasEmailError = true
                            emailErrorMessage = "올바른 이메일 형식이 아닙니다"
                        }
                        password.isEmpty() -> {
                            hasPasswordError = true
                            passwordErrorMessage = "비밀번호를 입력해주세요"
                        }
                        else -> {
                            keyboardController?.hide()
                            coroutineScope.launch {
                                isLoading = true
                                viewModel.login(context, email, password)
                                isLoading = false
                            }
                        }
                    }
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = if (isLoading) "로그인 중..." else "회원가입 / 로그인",
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )
        }
    }
}
