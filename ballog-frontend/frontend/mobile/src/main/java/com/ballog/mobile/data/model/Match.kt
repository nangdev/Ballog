package com.ballog.mobile.data.model

// 매치 도메인 모델 정의 예정


// 매치 리스트 조회 API 도메인 모델 정의
data class Match(
    val id: Int,
    val date: String,       // "2025-04-30"
    val startTime: String,  // "20:00"
    val endTime: String,    // "22:00"
    val matchName: String
)

// UI 상태 표현을 위한 sealed class
sealed class MatchState {
    object Loading : MatchState()
    data class Success(val matches: List<Match>) : MatchState()
    data class Error(val message: String) : MatchState()
}
