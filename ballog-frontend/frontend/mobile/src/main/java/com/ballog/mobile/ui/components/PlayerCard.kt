package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage

enum class CardFace {
    FRONT, BACK
}

@Composable
fun PlayerCard(
    name: String,
    imageUrl: String? = null, // ← URL로 변경
    stats: List<Pair<String, String>>,
    face: CardFace = CardFace.FRONT,
    modifier: Modifier = Modifier
) {
    when (face) {
        CardFace.FRONT -> {
            Box(
                modifier = modifier
                    .width(280.dp)
                    .height(450.dp)
                    .shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF20E9F5).copy(alpha = 0.8f),
                        spotColor = Color(0xFF20E9F5).copy(alpha = 1f)
                    )
                    .background(Gray.Gray700, shape = RoundedCornerShape(24.dp))
                    .border(6.dp, Gray.Gray500, shape = RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    ) {
                        val aspectRatio = 3f / 2f // 기본 비율
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxWidth / aspectRatio)
                                .background(Gray.Gray700),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrl ?: R.drawable.ic_profile,
                                contentDescription = "Player Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // 이름
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Gray.Gray500)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            fontFamily = pretendard
                        )
                    }
                    // 이름 아래 공간 전체를 Column으로 확보
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 💡 남은 영역을 다 차지하게 하고
                            .background(Gray.Gray700)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center // 💡 자식들을 수직 중앙 정렬
                    ) {
                        // 스탯 리스트 박스
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            stats.forEach { (statName, statValue) ->
                                val valueInt = statValue.toIntOrNull() ?: 0
                                val ratio = (valueInt.coerceIn(0, 100)) / 100f
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = statName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = pretendard,
                                        modifier = Modifier.width(64.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(ratio)
                                                .background(Primary, RoundedCornerShape(4.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = statValue,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = pretendard,
                                        modifier = Modifier.width(32.dp),
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        CardFace.BACK -> {
            // 뒷면 카드 디자인
            Box(
                modifier = modifier
                    .width(280.dp)
                    .height(450.dp)
                    .shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF20E9F5).copy(alpha = 0.8f),
                        spotColor = Color(0xFF20E9F5).copy(alpha = 1f)
                    )
                    .background(Gray.Gray700, shape = RoundedCornerShape(24.dp))
                    .border(6.dp, Gray.Gray500, shape = RoundedCornerShape(24.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // 중앙 텍스트
                    Text(
                        text = "Ballog",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        fontFamily = pretendard,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPlayerCardFigma() {
    PlayerCard(
        name = "KIM GAHEE",
        stats = listOf(
            "Speed" to "78",
            "Stamina" to "80",
            "Attack" to "64",
            "Defense" to "80",
            "Recovery" to "76"
        ),
        face = CardFace.FRONT
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewPlayerCardFigmaBack() {
    PlayerCard(
        name = "KIM GAHEE",
        stats = listOf(
            "Speed" to "78",
            "Stamina" to "80",
            "Attack" to "64",
            "Defense" to "80",
            "Recovery" to "76"
        ),
        face = CardFace.BACK
    )
}
