package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard

data class TeamInfo(
    val name: String? = null,
    val foundingDate: String? = null,
    val logoImageUrl: String? = null
)

@Composable
fun TeamCard(
    team: TeamInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray.Gray700
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (team.logoImageUrl.isNullOrBlank() || team.logoImageUrl == "null") {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray.Gray600),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_team),
                        contentDescription = "팀 이미지",
                        tint = Gray.Gray400,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                SubcomposeAsyncImage(
                    model = team.logoImageUrl,
                    contentDescription = "팀 이미지",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            color = Gray.Gray400
                        )
                    },
                    error = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_team),
                            contentDescription = "팀 이미지",
                            tint = Gray.Gray400
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = team.name?.takeIf { it.isNotBlank() } ?: "이름 없음",
                    color = Gray.Gray100,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = pretendard
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "창단일자",
                        color = Gray.Gray400,
                        fontSize = 12.sp,
                        fontFamily = pretendard
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = team.foundingDate?.takeIf { it.isNotBlank() } ?: "정보 없음",
                        color = Gray.Gray100,
                        fontSize = 12.sp,
                        fontFamily = pretendard
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamCardPreview() {
    TeamCard(
        team = TeamInfo(
            name = "FS 핑크팬서",
            foundingDate = "2023.08.14",
            logoImageUrl = "https://picsum.photos/200"  // 실제 URL 사용
        ),
        onClick = {}
    )
}
