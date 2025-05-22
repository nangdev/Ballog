package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.BallogTheme
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.System
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    hasError: Boolean = false,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Gray.Gray200)
                .then(
                    if (hasError) {
                        Modifier.border(
                            width = 1.dp,
                            color = Gray.Gray200,
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            textStyle = TextStyle(
                fontFamily = pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 16.70703125.sp,
                color = Gray.Gray700
            ),
            singleLine = true,
            cursorBrush = SolidColor(Color(0xFF1B1B1D)),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            visualTransformation = when {
                isPassword && !isPasswordVisible -> PasswordVisualTransformation()
                else -> VisualTransformation.None
            },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 16.70703125.sp,
                                    color = Gray.Gray500
                                )
                            )
                        }
                        innerTextField()
                    }
                    if (value.isNotEmpty()) {
                        if (isPassword && onPasswordVisibilityChange != null) {
                            IconButton(
                                onClick = { onPasswordVisibilityChange(!isPasswordVisible) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isPasswordVisible) {
                                            R.drawable.ic_eye
                                        } else {
                                            R.drawable.ic_eye
                                        }
                                    ),
                                    contentDescription = if (isPasswordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    },
                                    tint = Gray.Gray400
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onValueChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Clear text",
                                    tint = Gray.Gray400
                                )
                            }
                        }
                    }
                }
            }
        )

        if (hasError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 14.3203125.sp,
                    color = System.Red
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputPreview() {
    BallogTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                // Default state preview
                Input(
                    value = "",
                    onValueChange = {},
                    placeholder = "이메일",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Filled state preview
                Input(
                    value = "kimssafy@ballog.com",
                    onValueChange = {},
                    placeholder = "이메일",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Password field preview
                Input(
                    value = "password123",
                    onValueChange = {},
                    placeholder = "비밀번호",
                    isPassword = true,
                    isPasswordVisible = false,
                    onPasswordVisibilityChange = {},
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Error state preview
                Input(
                    value = "kimssafy@ballog",
                    onValueChange = {},
                    placeholder = "이메일",
                    hasError = true,
                    errorMessage = "올바른 이메일 형식이 아닙니다."
                )
            }
        }
    }
}
