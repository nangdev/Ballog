package com.ballog.watch.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.ballog.watch.ui.theme.BallogCyan
import com.ballog.watch.ui.theme.BallogBlack

@Composable
fun BallogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(140.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = BallogCyan,  // 시안색
            contentColor = BallogBlack  // 검은색 글씨
        ),
        shape = CircleShape  // 원형 모양 (타원형 효과)
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
} 