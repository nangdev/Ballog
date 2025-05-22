package com.ballog.mobile.ui.match

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun HeatMapOverlay(
    heatData: List<List<Int>>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF20E9F5)
) {
    val cols = 16
    val rows = 10

    Canvas(modifier = modifier.fillMaxSize()) {
        val spacingRatio = 0.2f  // 셀 대비 spacing 비율 (20%)

        // 가로 기준으로 셀+간격 전체 너비 계산
        val unitWidth = size.width / (cols + spacingRatio * (cols - 1))
        val cellWidth = unitWidth
        val spacingX = unitWidth * spacingRatio

        // 세로 기준 동일 계산
        val unitHeight = size.height / (rows + spacingRatio * (rows - 1))
        val cellHeight = unitHeight
        val spacingY = unitHeight * spacingRatio

        val totalWidth = cellWidth * cols + spacingX * (cols - 1)
        val totalHeight = cellHeight * rows + spacingY * (rows - 1)

        val offsetX = (size.width - totalWidth) / 2
        val offsetY = (size.height - totalHeight) / 2

        for (x in 0 until cols) {
            for (y in 0 until rows) {
                val intensity = heatData.getOrNull(x)?.getOrNull(y) ?: 0
                val alpha = when (intensity) {
                    0 -> 0.1f
                    1, 2 -> 0.3f
                    3, 4 -> 0.5f
                    5, 6 -> 0.6f
                    7, 8 -> 0.7f
                    9, 10 -> 0.9f
                    else -> 0f
                }

                val left = offsetX + x * (cellWidth + spacingX)
                val top = offsetY + y * (cellHeight + spacingY)

                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
    }
}
