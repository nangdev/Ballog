package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.System
import com.ballog.mobile.ui.theme.pretendard

enum class ButtonType {
    ICON_ONLY,
    LABEL_ONLY,
    BOTH
}

enum class ButtonColor {
    ALERT,
    GRAY,
    BLACK,
    PRIMARY
}

@Composable
fun BallogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.LABEL_ONLY,
    buttonColor: ButtonColor = ButtonColor.PRIMARY,
    icon: Painter? = null,
    label: String? = null,
    enabled: Boolean = true
) {
    val backgroundColor = when (buttonColor) {
        ButtonColor.ALERT -> Gray.Gray300
        ButtonColor.GRAY -> Gray.Gray300
        ButtonColor.BLACK -> Gray.Gray700
        ButtonColor.PRIMARY -> Primary
    }

    val contentColor = when (buttonColor) {
        ButtonColor.ALERT -> System.Red
        ButtonColor.GRAY -> Gray.Gray500
        ButtonColor.BLACK -> Primary
        ButtonColor.PRIMARY -> Gray.Gray700
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        when (type) {
            ButtonType.ICON_ONLY -> {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon?.let {
                        Icon(
                            painter = it,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            ButtonType.LABEL_ONLY -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label ?: "",
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = if (buttonColor == ButtonColor.PRIMARY || buttonColor == ButtonColor.BLACK)
                            FontWeight.W600 else FontWeight.W500,
                        fontFamily = pretendard
                    )
                }
            }
            ButtonType.BOTH -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    icon?.let {
                        Icon(
                            painter = it,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Text(
                        text = label ?: "",
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = pretendard
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BallogButtonPreview() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Icon Only Button
        BallogButton(
            onClick = { },
            type = ButtonType.ICON_ONLY,
            buttonColor = ButtonColor.ALERT,
            icon = painterResource(id = R.drawable.ic_trash),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Label Only Buttons
        ButtonColor.values().forEach { color ->
            BallogButton(
                onClick = { },
                type = ButtonType.LABEL_ONLY,
                buttonColor = color,
                label = "버튼 텍스트",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Both Icon and Label Button
        BallogButton(
            onClick = { },
            type = ButtonType.BOTH,
            buttonColor = ButtonColor.ALERT,
            icon = painterResource(id = R.drawable.ic_trash),
            label = "영상 삭제",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Disabled Button
        BallogButton(
            onClick = { },
            type = ButtonType.LABEL_ONLY,
            buttonColor = ButtonColor.GRAY,
            label = "비활성화 버튼",
            enabled = false,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
