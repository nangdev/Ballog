package com.ballog.mobile.ui.video

data class VideoUiState(
    val totalQuarters: Int = 1,
    val quarterList: List<QuarterVideoData> = emptyList()
)

data class QuarterVideoData(
    val videoId: Int = -1,
    val quarterNumber: Int = 1,
    val videoUrl: String = "",
    val highlights: List<HighlightUiState> = emptyList(),
    val showPlayer: Boolean = false
)

data class HighlightUiState(
    val id: String = "",
    val title: String = "",
    val startMin: String = "",
    val startSec: String = "",
    val endMin: String = "",
    val endSec: String = ""
)
