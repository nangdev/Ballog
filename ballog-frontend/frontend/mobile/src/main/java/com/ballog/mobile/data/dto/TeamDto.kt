package com.ballog.mobile.data.dto

data class TeamListResponse(
    val teamList: List<TeamInfo>
)

data class TeamInfo(
    val teamId: Int,
    val teamName: String? = null,
    val logoImageUrl: String? = null,
    val foundationDate: String? = null
)

data class TeamAddRequest(
    val teamName: String,
    val logoImageUrl: String? = null,
    val foundationDate: String
)

data class TeamUpdateRequest(
    val teamId: Int,
    val teamName: String,
    val logoImage: String,
    val foundationDate: String
)

data class TeamMemberAddRequest(
    val teamId: Int,
    val role: String
)

data class TeamDetailResponse(
    val teamId: Int = 0,
    val teamName: String? = null,
    val logoImageUrl: String? = null,
    val foundationDate: String? = null,
    val averageCardStats: AverageCardStats? = null,
    val playerCards: List<PlayerCard>? = null
)

data class AverageCardStats(
    val avgSpeed: Int = 0,
    val avgStamina: Int = 0,
    val avgAttack: Int = 0,
    val avgDefense: Int = 0,
    val avgRecovery: Int = 0
)

data class PlayerCard(
    val nickName: String? = null,
    val playStyle: String? = null,
    val rank: String? = null,
    val role: String? = null,
    val cardImageUrl: String? = null,
    val cardStats: CardStats? = null
)

data class CardStats(
    val speed: Int = 0,
    val stamina: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val recovery: Int = 0
)

data class TeamMemberListResponse(
    val teamMemberList: List<TeamMember>
)

data class TeamMember(
    val teamMemberId: Int,
    val nickname: String
)




