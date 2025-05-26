package com.ballog.mobile.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 플레이어/평균 비교용 스탯 데이터
// (TeamInfoCard의 TeamStats와 동일 구조)
data class PlayerStats(
    val attack: Int,    // 0 to 100
    val defence: Int,   // 0 to 100
    val speed: Int,     // 0 to 100
    val recovery: Int,  // 0 to 100
    val stamina: Int    // 0 to 100
) {
    private fun toRatio(value: Int): Float = value.coerceIn(0, 100) / 100f
    val attackRatio: Float get() = toRatio(attack)
    val defenceRatio: Float get() = toRatio(defence)
    val speedRatio: Float get() = toRatio(speed)
    val recoveryRatio: Float get() = toRatio(recovery)
    val staminaRatio: Float get() = toRatio(stamina)
}

@Composable
fun PlayerStatCard(
    playerStats: PlayerStats,
    averageStats: PlayerStats,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Gray.Gray700, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(312.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1B1B1D)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(214.dp).align(Alignment.Center)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.width / 2

                    // Draw filled background circles (TeamInfoCard 스타일)
                    val circles = listOf(
                        Color(0xFF2B2B2B),
                        Color(0xFF3A3C3E),
                        Color(0xFF505458),
                        Color(0xFF575B5F),
                        Color(0xFF676A6E)
                    )
                    circles.forEachIndexed { index, color ->
                        val radius = maxRadius * (1 - index * 0.2f)
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = center
                        )
                    }

                    // 각 꼭짓점의 비율과 각도
                    fun statPoints(stats: PlayerStats) = listOf(
                        Triple(stats.attackRatio, -PI / 2, "ATTACK"),       // Top
                        Triple(stats.defenceRatio, 0.0, "DEFENCE"),         // Right
                        Triple(stats.recoveryRatio, PI / 3, "RECOVERY"),    // Bottom Right
                        Triple(stats.staminaRatio, 2 * PI / 3, "STAMINA"),  // Bottom Left
                        Triple(stats.speedRatio, PI, "SPEED")               // Left
                    )

                    // 평균 오각형 (회색)
                    val avgPath = Path().apply {
                        statPoints(averageStats).forEachIndexed { i, (value, angle, _) ->
                            val r = maxRadius * value
                            val x = center.x + r * cos(angle).toFloat()
                            val y = center.y + r * sin(angle).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    // Glow 효과 (회색)
                    for (i in 5 downTo 1) {
                        drawPath(
                            path = avgPath,
                            color = Color(0xFFB0B0B0).copy(alpha = 0.10f / i),
                            style = Stroke(
                                width = (i * 8).toFloat(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.cornerPathEffect(8f)
                            )
                        )
                    }
                    drawPath(
                        path = avgPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFB0B0B0).copy(alpha = 0.7f),
                                Color(0xFFB0B0B0).copy(alpha = 0.8f)
                            ),
                            center = center,
                            radius = size.width / 2f * 0.8f
                        )
                    )
                    drawPath(
                        path = avgPath,
                        color = Color(0xFFB0B0B0).copy(alpha = 0.7f),
                        style = Stroke(
                            width = 2.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 나 오각형 (민트)
                    val myPath = Path().apply {
                        statPoints(playerStats).forEachIndexed { i, (value, angle, _) ->
                            val r = maxRadius * value
                            val x = center.x + r * cos(angle).toFloat()
                            val y = center.y + r * sin(angle).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    // Glow 효과 (민트)
                    val mintColor = Primary
                    val pastelMint = Color(
                        red = mintColor.red * 0.95f + 0.05f,
                        green = mintColor.green * 0.95f + 0.05f,
                        blue = mintColor.blue * 0.95f + 0.05f,
                        alpha = 1f
                    )
                    for (i in 5 downTo 1) {
                        drawPath(
                            path = myPath,
                            color = pastelMint.copy(alpha = 0.15f / i),
                            style = Stroke(
                                width = (i * 8).toFloat(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.cornerPathEffect(8f)
                            )
                        )
                    }
                    drawPath(
                        path = myPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                pastelMint.copy(alpha = 0.7f),
                                pastelMint.copy(alpha = 0.8f)
                            ),
                            center = center,
                            radius = size.width / 2f * 0.8f
                        )
                    )
                    drawPath(
                        path = myPath,
                        color = pastelMint.copy(alpha = 0.7f),
                        style = Stroke(
                            width = 2.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 점선(외곽~꼭짓점)
                    statPoints(averageStats).forEach { (value, angle, _) ->
                        val startRadius = maxRadius
                        val endRadius = maxRadius * value
                        val startX = center.x + startRadius * cos(angle).toFloat()
                        val startY = center.y + startRadius * sin(angle).toFloat()
                        val endX = center.x + endRadius * cos(angle).toFloat()
                        val endY = center.y + endRadius * sin(angle).toFloat()
                        val path = Path()
                        path.moveTo(startX, startY)
                        path.lineTo(endX, endY)
                        drawPath(
                            path = path,
                            color = Gray.Gray500,
                            style = Stroke(
                                width = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                            )
                        )
                    }

                    // 꼭짓점 점 (민트)
                    statPoints(playerStats).forEach { (value, angle, _) ->
                        val radius = maxRadius * value
                        val x = center.x + radius * cos(angle).toFloat()
                        val y = center.y + radius * sin(angle).toFloat()
                        drawCircle(
                            color = pastelMint,
                            radius = 3f,
                            center = Offset(x, y)
                        )
                    }
                    // 꼭짓점 점 (회색)
                    statPoints(averageStats).forEach { (value, angle, _) ->
                        val radius = maxRadius * value
                        val x = center.x + radius * cos(angle).toFloat()
                        val y = center.y + radius * sin(angle).toFloat()
                        drawCircle(
                            color = Color(0xFFB0B0B0),
                            radius = 3f,
                            center = Offset(x, y)
                        )
                    }
                }
                // 꼭짓점 라벨
                if (showLabels) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "ATTACK",
                            color = Gray.Gray300,
                            fontSize = 8.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 28.dp)
                        )
                        Text(
                            text = "DEFENCE",
                            color = Gray.Gray300,
                            fontSize = 8.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp)
                        )
                        Text(
                            text = "SPEED",
                            color = Gray.Gray300,
                            fontSize = 8.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 22.dp)
                        )
                        Text(
                            text = "RECOVERY",
                            color = Gray.Gray300,
                            fontSize = 8.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 53.dp, bottom = 45.dp)
                        )
                        Text(
                            text = "STAMINA",
                            color = Gray.Gray300,
                            fontSize = 8.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 49.dp, bottom = 45.dp)
                        )
                    }
                }
            }
        }
    }
}
