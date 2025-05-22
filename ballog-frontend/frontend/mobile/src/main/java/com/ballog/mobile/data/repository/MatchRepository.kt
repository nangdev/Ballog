package com.ballog.mobile.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataMap
import androidx.core.content.edit
import com.ballog.mobile.data.dto.GpsLocation

/**
 * 경기장 및 위치 데이터를 관리하는 Repository
 */
class MatchRepository(private val context: Context) {
    private val TAG = "MatchRepository"
    private val PREF_NAME = "field_corners"

    /**
     * 16x10 그리드에 히트맵 데이터를 저장하는 클래스
     */
    data class HeatMapGrid(
        val grid: Array<IntArray> = Array(10) { IntArray(16) }
    ) {
        fun addValue(row: Int, col: Int, value: Int = 1) {
            if (row in 0..9 && col in 0..15) {
                grid[row][col] += value
            }
        }

        fun getValue(row: Int, col: Int): Int {
            return if (row in 0..9 && col in 0..15) grid[row][col] else 0
        }

        fun clear() {
            for (i in 0..9) {
                for (j in 0..15) {
                    grid[i][j] = 0
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HeatMapGrid

            if (!grid.contentDeepEquals(other.grid)) return false

            return true
        }

        override fun hashCode(): Int {
            return grid.contentDeepHashCode()
        }
    }

    // 히트맵 그리드 데이터
    private val heatMapGrid = HeatMapGrid()

    /**
     * 히트맵 그리드 데이터를 1~10 사이의 값으로 정규화하여 반환
     */
    fun getNormalizedHeatMapData(): Array<IntArray> {
        val grid = heatMapGrid.grid
        val result = Array(10) { IntArray(16) }

        // 데이터의 최대값 찾기
        var maxValue = 0
        for (row in 0..9) {
            for (col in 0..15) {
                maxValue = maxOf(maxValue, grid[row][col])
            }
        }

        // 값이 없으면 빈 배열 반환
        if (maxValue == 0) {
            return result
        }

        // 값을 1~10 범위로 정규화
        for (row in 0..9) {
            for (col in 0..15) {
                val normalizedValue = if (grid[row][col] == 0) {
                    0  // 0은 그대로 0 (데이터 없음)
                } else {
                    // 1~10 범위로 정규화 (1부터 시작하기 위해 1 더함)
                    1 + ((grid[row][col] * 9) / maxValue)
                }
                result[row][col] = normalizedValue
            }
        }

        return result
    }

    /**
     * 서버에 전송할 히트맵 데이터 배열 가져오기
     * @return 10x16 크기의 2차원 배열 (행 10개, 열 16개)
     */
    fun getHeatMapDataForServer(): Array<IntArray> {
        return getNormalizedHeatMapData()
    }


    /**
     * 워치에서 전송된 경기장 모서리 좌표 데이터 저장
     */
    fun saveFieldDataFromWatch(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        lat3: Double, lon3: Double,
        lat4: Double, lon4: Double,
        timestamp: Long
    ) {
        Log.d(TAG, "경기장 모서리 데이터 수신 시작")
        Log.d(TAG, "수신된 데이터: lat1=$lat1, lon1=$lon1, lat2=$lat2, lon2=$lon2, lat3=$lat3, lon3=$lon3, lat4=$lat4, lon4=$lon4, timestamp=$timestamp")

        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putFloat("lat1", lat1.toFloat())
            putFloat("lon1", lon1.toFloat())
            putFloat("lat2", lat2.toFloat())
            putFloat("lon2", lon2.toFloat())
            putFloat("lat3", lat3.toFloat())
            putFloat("lon3", lon3.toFloat())
            putFloat("lat4", lat4.toFloat())
            putFloat("lon4", lon4.toFloat())
            putLong("timestamp", timestamp)
            apply()
        }

        Log.d(TAG, "경기장 모서리 데이터 저장 완료")
    }

    /**
     * 저장된 경기장 모서리 좌표 불러오기
     */
    fun getFieldCorners(): List<GpsLocation>? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        if (!sharedPrefs.contains("lat1")) {
            return null
        }

        val corners = mutableListOf<GpsLocation>()

        corners.add(GpsLocation(
            sharedPrefs.getFloat("lat1", 0f).toDouble(),
            sharedPrefs.getFloat("lon1", 0f).toDouble()
        ))
        corners.add(GpsLocation(
            sharedPrefs.getFloat("lat2", 0f).toDouble(),
            sharedPrefs.getFloat("lon2", 0f).toDouble()
        ))
        corners.add(GpsLocation(
            sharedPrefs.getFloat("lat3", 0f).toDouble(),
            sharedPrefs.getFloat("lon3", 0f).toDouble()
        ))
        corners.add(GpsLocation(
            sharedPrefs.getFloat("lat4", 0f).toDouble(),
            sharedPrefs.getFloat("lon4", 0f).toDouble()
        ))

        return corners
    }

    /**
     * 경기장 모서리 데이터 명시적 삭제
     */
    fun clearFieldCorners() {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit() { clear() }
        Log.d(TAG, "경기장 모서리 데이터 삭제 완료")
    }

    /**
     * GPS 좌표를 그리드 위치로 변환
     */
    fun convertToGridPosition(location: GpsLocation): Pair<Int, Int>? {
        val corners = getFieldCorners() ?: return null

        // 모서리 좌표 정렬
        val sortedCorners = sortCorners(corners)

        // 경기장 내부에 있는지 확인
        if (!isInsideField(location, sortedCorners)) {
            Log.d(TAG, "위치가 경기장 밖입니다: $location")
            return null
        }

        // 좌표를 정규화된 위치로 변환
        val normalizedPosition = normalizePosition(location, sortedCorners)

        // 정규화된 위치를 그리드 셀로 변환
        val gridCol = (normalizedPosition.first * 16).toInt().coerceIn(0, 15)
        val gridRow = (normalizedPosition.second * 10).toInt().coerceIn(0, 9)

        Log.d(TAG, "그리드 위치 계산: $location -> ($gridRow, $gridCol)")
        return Pair(gridRow, gridCol)
    }

    /**
     * 좌표가 경기장 내부에 있는지 확인
     */
    private fun isInsideField(location: GpsLocation, corners: List<GpsLocation>): Boolean {
        var inside = false
        var j = corners.size - 1

        for (i in corners.indices) {
            val xi = corners[i].longitude
            val yi = corners[i].latitude
            val xj = corners[j].longitude
            val yj = corners[j].latitude

            val intersect = ((yi > location.latitude) != (yj > location.latitude)) &&
                (location.longitude < (xj - xi) * (location.latitude - yi) / (yj - yi) + xi)

            if (intersect) inside = !inside
            j = i
        }

        return inside
    }

    /**
     * 좌표를 경기장 내의 정규화된 위치로 변환
     */
    private fun normalizePosition(location: GpsLocation, corners: List<GpsLocation>): Pair<Double, Double> {
        val minLat = corners.minOf { it.latitude }
        val maxLat = corners.maxOf { it.latitude }
        val minLng = corners.minOf { it.longitude }
        val maxLng = corners.maxOf { it.longitude }

        val normalizedX = (location.longitude - minLng) / (maxLng - minLng)
        val normalizedY = (location.latitude - minLat) / (maxLat - minLat)

        return Pair(normalizedX, normalizedY)
    }

    /**
     * 모서리 좌표를 시계방향으로 정렬
     */
    private fun sortCorners(corners: List<GpsLocation>): List<GpsLocation> {
        val centerLat = corners.sumOf { it.latitude } / corners.size
        val centerLng = corners.sumOf { it.longitude } / corners.size
        val center = GpsLocation(centerLat, centerLng)

        return corners.sortedBy { corner ->
            Math.atan2(corner.latitude - center.latitude, corner.longitude - center.longitude)
        }
    }

    /**
     * 위치를 히트맵 그리드에 추가
     */
    fun addLocationToHeatMap(location: GpsLocation): Boolean {
        val gridPosition = convertToGridPosition(location) ?: run {
            // 로그 추가
            Log.d(TAG, "위치가 경기장 밖이거나 경기장 데이터가 없음: $location")
            return false
        }

        val row = gridPosition.first
        val col = gridPosition.second
        heatMapGrid.addValue(row, col)

        // 로그 추가 (매 10번째 데이터마다 로그 출력)
        if ((heatMapGrid.getValue(row, col) % 10) == 0) {
            Log.d(TAG, "그리드 위치 ($row, $col)에 히트맵 값 추가: ${heatMapGrid.getValue(row, col)}")
        }

        return true
    }

    fun logHeatMapData() {
        val grid = heatMapGrid.grid
        Log.d(TAG, "======= 히트맵 데이터 현황 =======")

        // 데이터 유무 확인
        var hasData = false
        var totalCount = 0

        // 행별로 로그 출력
        for (row in 0..9) {
            val rowData = StringBuilder()
            for (col in 0..15) {
                val value = grid[row][col]
                rowData.append(String.format("%3d", value))
                if (value > 0) {
                    hasData = true
                    totalCount += value
                }
            }
            Log.d(TAG, "행 $row: $rowData")
        }

        // 데이터 요약
        if (hasData) {
            Log.d(TAG, "총 데이터 포인트 수: $totalCount")
        } else {
            Log.d(TAG, "히트맵 데이터가 없습니다.")
        }
        Log.d(TAG, "============================")
    }

    /**
     * 디버깅용: 경기장 모서리 좌표 로깅
     */
    fun logFieldCorners() {
        val corners = getFieldCorners()

        if (corners == null) {
            Log.d(TAG, "저장된 경기장 모서리 데이터가 없습니다.")
            return
        }

        Log.d(TAG, "======= 경기장 모서리 좌표 =======")
        for (i in corners.indices) {
            Log.d(TAG, "모서리 ${i+1}: (${corners[i].latitude}, ${corners[i].longitude})")
        }
        Log.d(TAG, "=============================")
    }


    suspend fun saveFieldCorners(corners: ArrayList<DataMap>) {
        // TODO: 경기장 모서리 데이터를 저장하는 로직 구현
        // 현재는 로그만 출력
        println("경기장 모서리 데이터 저장: $corners")
    }
}
