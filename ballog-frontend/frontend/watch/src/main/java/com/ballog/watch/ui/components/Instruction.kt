package notfound.ballog.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.ballog.watch.ui.components.BallogButton
import com.ballog.watch.ui.theme.BallogCyan

@Composable
fun InstructionScreen(onContinueClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 절반: 중앙 텍스트
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = """
                    경기장을 한바퀴 돌면서
                    각 모서리에서 측정 버튼을 눌러주세요
                """.trimIndent(),
                textAlign = TextAlign.Center,
                color = BallogCyan
            )
        }

        // 하단 절반: 중앙 버튼 (HomeScreen 과 동일한 위치)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BallogButton(
                text = "시작하기",
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)  // 너비 80%
                    .height(40.dp)       // 고정 높이
            )
        }
    }
}
