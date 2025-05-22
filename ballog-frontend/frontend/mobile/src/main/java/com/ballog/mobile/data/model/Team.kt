package com.ballog.mobile.data.model

import com.ballog.mobile.data.dto.PlayerCard
import com.ballog.mobile.data.dto.CardStats
import com.ballog.mobile.data.dto.TeamMember

data class Team(
    val teamId: Int,
    val name: String? = null,
    val logoImageUrl: String? = null,
    val foundationDate: String? = null
)

data class TeamDetail(
    val teamId: Int = 0,
    val name: String = "",
    val logoImageUrl: String = "",
    val foundationDate: String = "",
    val stats: TeamStats = TeamStats(0, 0, 0, 0, 0),
    val players: List<Player> = emptyList()
)

data class TeamStats(
    val speed: Int = 0,
    val stamina: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val recovery: Int = 0
)

data class Player(
    val nickname: String = "",
    val playStyle: String = "",
    val rank: String = "",
    val cardImageUrl: String = "",
    val stats: PlayerStats = PlayerStats(0, 0, 0, 0, 0),
    val role: String = ""
)

data class PlayerStats(
    val speed: Int = 0,
    val stamina: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val recovery: Int = 0
)

data class TeamMemberModel(
    val id: Int,
    val nickname: String
)

// Extension functions to convert DTOs to domain models
fun com.ballog.mobile.data.dto.TeamInfo.toTeam() = Team(
    teamId = teamId,
    name = teamName,
    logoImageUrl = logoImageUrl,
    foundationDate = foundationDate
)

fun com.ballog.mobile.data.dto.TeamDetailResponse.toTeamDetail() = TeamDetail(
    teamId = teamId,
    name = teamName ?: "",
    logoImageUrl = logoImageUrl ?: "",
    foundationDate = foundationDate ?: "",
    stats = TeamStats(
        speed = averageCardStats?.avgSpeed ?: 0,
        stamina = averageCardStats?.avgStamina ?: 0,
        attack = averageCardStats?.avgAttack ?: 0,
        defense = averageCardStats?.avgDefense ?: 0,
        recovery = averageCardStats?.avgRecovery ?: 0
    ),
    players = playerCards?.map { it.toPlayer() } ?: emptyList()
)

fun PlayerCard.toPlayer() = Player(
    nickname = nickName ?: "",
    playStyle = playStyle ?: "",
    rank = rank ?: "",
    cardImageUrl = cardImageUrl ?: "",
    stats = cardStats?.toPlayerStats() ?: PlayerStats(0, 0, 0, 0, 0),
    role = role ?: ""
)

fun CardStats.toPlayerStats() = PlayerStats(
    speed = speed,
    stamina = stamina,
    attack = attack,
    defense = defense,
    recovery = recovery
)

fun TeamMember.toTeamMemberModel() = TeamMemberModel(
    id = teamMemberId,
    nickname = nickname
)
