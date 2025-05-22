package com.ballog.mobile.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ballog.mobile.ui.components.PlayerCard
import com.ballog.mobile.ui.components.CardFace

@Composable
fun PlayerCardDialog(
    name: String,
    imageUrl: String?,
    stats: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PlayerCardDialogContent(
            name = name,
            imageUrl = imageUrl,
            stats = stats,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun PlayerCardDialogContent(
    name: String,
    imageUrl: String?,
    stats: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    var flipped by remember { mutableStateOf(false) }
    val rotationY = remember { Animatable(0f) }
    val density = LocalDensity.current.density

    // 애니메이션 시작
    LaunchedEffect(Unit) {
        flipped = true
        rotationY.animateTo(
            targetValue = 720f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = { 1 - (1 - it) * (1 - it) * (1 - it) } // 감속 곡선
            )
        )
    }

    // 전체 반투명 배경 + 바깥 클릭 dismiss
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss)
    ) {
        // 중앙 회전 카드
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    this.rotationY = rotationY.value % 360f
                    cameraDistance = 12 * density
                }
                .pointerInput(Unit) {} // 클릭 이벤트 전파 차단
        ) {
            val normalizedRotation = rotationY.value % 360f

            if (normalizedRotation <= 90f || normalizedRotation >= 270f) {
                PlayerCard(name = name, imageUrl = imageUrl, stats = stats, face = CardFace.FRONT)
            } else {
                Box(modifier = Modifier.graphicsLayer(rotationY = 180f)) {
                    PlayerCard(name = name, imageUrl = imageUrl, stats = stats, face = CardFace.BACK)
                }
            }
        }
    }
}
