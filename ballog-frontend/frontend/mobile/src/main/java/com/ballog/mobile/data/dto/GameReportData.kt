package com.ballog.mobile.data.dto

data class GameReportData(
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

data class QuarterReport(
    val id: String,
    val date: String,
    val quarterNumber: Int?,
    val gameSide: String?,
    val gameReportData: GameReportData
)

data class MatchReportData(
    val quarterNumber: Int?,
    val gameSide: String?,
    val gameReportData: GameReportData
)

data class MatchReportRequest(
    val matchId: Int,
    val reportDataList: List<MatchReportData>
)
