package com.ballog.mobile.ui.match

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary

@Composable
fun HeatMap(
    heatData: List<List<Int>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(312f / 200f)
                .clip(RoundedCornerShape(20.dp))
                .background(Gray.Gray700) // 이미지 안 보일 때 대비
        ) {
            Image(
                painter = painterResource(id = R.drawable.futsal),
                contentDescription = "HeatMap Background",
                modifier = Modifier.fillMaxSize()
            )

            HeatMapOverlay(
                heatData = heatData,
                color = Primary,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HeatMapPreview() {
    val sampleData = List(15) { List(10) { (0..5).random() } }

    com.ballog.mobile.ui.theme.BallogTheme {
        HeatMap(heatData = sampleData)
    }
}
