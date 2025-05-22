package com.ballog.mobile.data.model

data class Exercise(
    val id: String,
    val exerciseType: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val distance: Float, // meters
    val avgSpeed: Float, // km/h
    val maxSpeed: Float, // km/h
    val calories: Float, // kcal
    val avgHeartRate: Int, // bpm
    val maxHeartRate: Int, // bpm
    val gpsPoints: List<GpsPoint>,
    val liveDataSegments: List<LiveDataSegment> = emptyList(),
    val timestamp: Long = 0L,
    val sprintCount: Int = 0, // 스프린트 횟수 추가 (기본값 0)
    val heatmapData: List<List<Int>> = List(10) { List(16) { 0 } }  // 기본값으로 빈 히트맵 초기화
)

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class LiveDataSegment(
    val startTime: Long,
    val heartRate: Float,
    val cadence: Float,
    val count: Int,
    val power: Float,
    val speed: Float,
    val distance: Float
)
