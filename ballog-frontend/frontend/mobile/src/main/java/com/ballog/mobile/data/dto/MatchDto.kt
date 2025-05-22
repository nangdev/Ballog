package com.ballog.mobile.data.dto

// 매치 관련 요청/응답 DTO 정의 예정

// 개인 매치 리스트 조회 / 팀 매치 리스트 조회
data class MatchListResponse(
    val matchList: List<MatchItemDto>
)

data class MatchItemDto(
    val matchId: Int,
    val matchName: String,
    val matchDate: String,   // e.g., "2025-04-22"
    val startTime: String,   // e.g., "20:00"
    val endTime: String      // e.g., "22:00"
)

// 개인 매치 등록 요청
data class MatchRegisterRequest(
    val matchDate: String,   // yyyy-MM-dd
    val startTime: String,   // HH:mm
    val endTime: String,     // HH:mm
    val matchName: String
)

// 팀 매치 등록 요청
data class TeamMatchRegisterRequest(
    val teamId: Int,
    val matchDate: String,
    val startTime: String,
    val endTime: String,
    val matchName: String,
    val participantList: List<Int>
)

data class MatchDetailResponseDto(
    val participantList: List<ParticipantDto>,
    val quarterList: List<QuarterDto>
)

data class ParticipantDto(
    val nickName: String,
    val role: String,
    val profileImageUrl: String
)

data class QuarterDto(
    val quarterNumber: Int,
    val reportData: ReportDataDto,
    val matchName: String,
    val matchSide: String
)

data class ReportDataDto(
    val startTime: String,
    val endTime: String,
    val distance: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val calories: Int,
    val sprint: Int,
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val heatmap: List<List<Int>>
)

data class MatchReportResponse(
    val matchId: Int,
    val matchName: String
)

data class DayMatchesRequest(
    val dates: List<String>
)

data class DayMatchesResponse(
    val matchList: List<MatchItemDto>
)

data class HeatMapDto(
    val heatMap: List<List<Int>>,
    val gameSide: String
)
