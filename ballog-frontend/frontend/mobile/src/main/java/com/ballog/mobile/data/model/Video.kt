package com.ballog.mobile.data.model

import com.ballog.mobile.data.dto.HighlightDto
import com.ballog.mobile.data.dto.VideoResponseDto

// 도메인 모델
data class Video(
    val id: Int?, // videoId
    val url: String?,
    val quarter: Int,
    val uploadSuccess: Boolean,
    val highlights: List<Highlight>
)

data class Highlight(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String
)

// DTO → Model 변환
fun VideoResponseDto.toVideo(): Video {
    return Video(
        id = videoId,
        url = videoUrl,
        quarter = quarterNumber ?: 1,
        uploadSuccess = uploadSuccess,
        highlights = highlightList.map { it.toHighlight() }
    )
}

fun HighlightDto.toHighlight(): Highlight {
    return Highlight(
        id = highlightId,
        title = highlightName,
        startTime = startTime,
        endTime = endTime
    )
}
