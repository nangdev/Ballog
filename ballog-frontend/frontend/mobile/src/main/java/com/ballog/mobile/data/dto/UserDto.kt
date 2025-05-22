package com.ballog.mobile.data.dto

data class UserInfoResponse(
    val email: String,
    val nickname: String,
    val birthDate: String,
    val profileImageUrl: String
)

data class UserUpdateRequest(
    val nickname: String,
    val birthDate: String,
    val profileImageUrl: String
)

fun UserInfoResponse.toUser(): com.ballog.mobile.data.model.User =
    com.ballog.mobile.data.model.User(
        email = this.email,
        nickname = this.nickname,
        birthDate = this.birthDate,
        profileImageUrl = this.profileImageUrl
    )

// 홈화면에 사용자 통계
data class UserStatisticsDto(
    val nickname: String,
    val heatmap: List<List<Int>>,
    val distance: List<Double>,
    val speed: List<Double>,
    val sprint: List<Int>,
    val heartRate: List<Int>
)

// 내 선수카드 보기
data class PlayerCardResponseDto(
    val nickname: String,
    val profileImageUrl: String,
    val cardStats: PlayerCardStatsDto
)

data class PlayerCardStatsDto(
    val speed: Int,
    val stamina: Int,
    val attack: Int,
    val defense: Int,
    val recovery: Int
)

data class RecommendedPlayer(
    val name: String,
    val position: String,
    val style: String,
    val reason: String,
    val train: String,
    val imageUrl: String
)

data class AiRecommendDto(
    val conclusion: String,
    val heatmapAnalysis: String,
    val sprintAnalysis: String,
    val speedAnalysis: String,
    val staminaAnalysis: String,
    val recommendedPlayer: RecommendedPlayer
)
