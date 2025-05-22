package com.ballog.mobile.ui.match

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.TabMenu
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ballog.mobile.viewmodel.MatchViewModel
import com.ballog.mobile.ui.video.MatchVideoTab

private const val TAG = "MatchDetailScreen"

@Composable
fun MatchDetailScreen(
    navController: NavController,
    matchId: Int,
    initialTitle: String = "매치 상세",
    viewModel: MatchViewModel = viewModel()
) {
    Log.d(TAG, "MatchDetailScreen 시작: matchId=$matchId")

    var selectedTab by remember { mutableIntStateOf(0) }
    val matchDetail by viewModel.matchDetail.collectAsState()

    val isLoading = matchDetail == null
    val error: String? = null // 필요 시 matchDetailState 도입 가능

    LaunchedEffect(matchId) {
        viewModel.fetchMatchDetail(matchId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = initialTitle,
            type = TopNavType.DETAIL_WITH_BACK,
            onBackClick = { navController.popBackStack() },
        )

        TabMenu(
            leftTabText = "레포트",
            rightTabText = "영상",
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Gray.Gray500,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                val quarterList = matchDetail!!.quarterList
                val totalQuarters = quarterList.size

                when (selectedTab) {
                    0 -> MatchReportTab(matchDetail = matchDetail!!)
                    1 -> MatchVideoTab(
                        matchId = matchId
                    )
                }
            }
        }
    }
}


