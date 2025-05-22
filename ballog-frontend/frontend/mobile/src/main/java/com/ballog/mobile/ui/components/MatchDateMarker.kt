package com.ballog.mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.*

@Composable
fun MatchDateMarker(
    date: String,
    marked: Boolean,
    selected: Boolean,
    thisMonth: Boolean,
    modifier: Modifier = Modifier
) {
    val dateColor = when {
        !thisMonth -> Gray.Gray500
        selected -> Primary
        else -> Gray.Gray100
    }
    val markVisible = marked

    Box(
        modifier = modifier
            .size(width = 40.dp, height = 48.dp)
            .padding(top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date,
                color = dateColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = pretendard,
            )
            if (markVisible) {
                Image(
                    painter = painterResource(id = R.drawable.mark),
                    contentDescription = "Marker",
                    modifier = Modifier.size(10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp)) // 없는 경우에도 height 유지
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun PreviewMatchDateMarker() {
    BallogTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MatchDateMarker(date = "8", marked = true, selected = false, thisMonth = true)
            MatchDateMarker(date = "9", marked = false, selected = true, thisMonth = true)
            MatchDateMarker(date = "10", marked = false, selected = false, thisMonth = false)
        }
    }
}
