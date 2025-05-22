package com.ballog.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.components.Input
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Surface
import com.ballog.mobile.ui.theme.System
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.ballog.mobile.viewmodel.AuthViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.ballog.mobile.data.model.SignUpProgress
import kotlinx.coroutines.launch

@Composable
fun SignupBirthScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val signUpProgress by viewModel.signUpProgress.collectAsState()
    println("BirthScreen - Current progress: $signUpProgress")

    // 현재 진행 상태 확인
    LaunchedEffect(signUpProgress) {
        println("BirthScreen - Progress changed to: $signUpProgress")
        if (signUpProgress != SignUpProgress.BIRTH_DATE && signUpProgress != SignUpProgress.PROFILE) {
            println("BirthScreen - Invalid progress state, popping back")
            navController.popBackStack()
        }
    }

    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val yearFocusRequester = remember { FocusRequester() }
    val monthFocusRequester = remember { FocusRequester() }
    val dayFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Request focus and show keyboard when the screen mounts
    LaunchedEffect(Unit) {
        println("BirthScreen - Requesting focus and showing keyboard")
        yearFocusRequester.requestFocus()
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
                text = "3/4",
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
                text = "생년월일을 알려주세요.",
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
                text = "선수카드 제작을 위해 필요해요",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 19.09.sp,
                    color = Gray.Gray700
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Input(
                    value = year,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            year = it
                            hasError = false
                            errorMessage = null
                            if (it.length == 4) {
                                monthFocusRequester.requestFocus()
                            }
                        }
                    },
                    placeholder = "YYYY",
                    hasError = hasError,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(yearFocusRequester)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Input(
                    value = month,
                    onValueChange = {
                        if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                            val monthInt = it.toIntOrNull() ?: 0
                            if (monthInt <= 12) {
                                month = it
                                hasError = false
                                errorMessage = null
                                if (it.length == 2) {
                                    dayFocusRequester.requestFocus()
                                }
                            }
                        }
                    },
                    placeholder = "MM",
                    hasError = hasError,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(monthFocusRequester)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Input(
                    value = day,
                    onValueChange = {
                        if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                            val dayInt = it.toIntOrNull() ?: 0
                            if (dayInt <= 31) {
                                day = it
                                hasError = false
                                errorMessage = null
                            }
                        }
                    },
                    placeholder = "DD",
                    hasError = hasError,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(dayFocusRequester)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = TextStyle(
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 14.32.sp,
                        color = System.Red
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            BallogButton(
                onClick = {
                    println("BirthScreen - Next button clicked")
                    when {
                        year.length != 4 || month.isEmpty() || day.isEmpty() -> {
                            println("BirthScreen - Validation failed: Empty fields")
                            hasError = true
                            errorMessage = "생년월일을 모두 입력해주세요."
                        }
                        !isValidDate(year.toInt(), month.toInt(), day.toInt()) -> {
                            println("BirthScreen - Validation failed: Invalid date")
                            hasError = true
                            errorMessage = "올바른 생년월일을 입력해주세요."
                        }
                        else -> {
                            println("BirthScreen - Starting navigation process")
                            coroutineScope.launch {
                                isLoading = true
                                println("BirthScreen - Setting birth date: $year-$month-$day")
                                viewModel.setSignUpBirthDate("$year-$month-$day")
                                isLoading = false
                                println("BirthScreen - Navigating to profile image screen")
                                navController.navigate(Routes.SIGNUP_PROFILE_IMAGE) {
                                    popUpTo(Routes.SIGNUP_BIRTHDAY) {
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

private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
    return try {
        when {
            year < 1900 || year > 2024 -> false
            month < 1 || month > 12 -> false
            day < 1 || day > 31 -> false
            month in listOf(4, 6, 9, 11) && day > 30 -> false
            month == 2 -> {
                val isLeapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
                if (isLeapYear) day <= 29 else day <= 28
            }
            else -> true
        }
    } catch (e: Exception) {
        false
    }
}
