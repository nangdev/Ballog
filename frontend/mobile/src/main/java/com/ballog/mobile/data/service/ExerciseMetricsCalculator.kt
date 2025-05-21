package com.ballog.mobile.data.service

import android.content.Context
import android.util.Log
import com.ballog.mobile.data.dto.HeatMapDto
import com.ballog.mobile.data.model.GpsPoint
import com.ballog.mobile.data.repository.MatchRepository

enum class GameSide {
    LEFT, RIGHT
}

class ExerciseMetricsCalculator(
    private val context: Context
) {
    // 로그 태그 상수 정의
    companion object {
        private const val TAG = "HeatmapCalc"
    }

    /**
     * 가장 초기의 GPS 포인트를 사용하여 진영을 감지합니다.
     * @param gpsPoints 시간순으로 정렬된 GPS 포인트 리스트
     * @return 감지된 게임 진영 (LEFT 또는 RIGHT)
     */
    fun detectGameSide(gpsPoints: List<GpsPoint>): GameSide {
        // 데이터가 없으면 기본값 반환
        if (gpsPoints.isEmpty()) {
            Log.i(TAG, "GPS 포인트가 없어 기본값(LEFT) 반환")
            return GameSide.LEFT // 기본값으로 왼쪽 설정
        }

        // 전체 경기장 범위 추정
        val minLng = gpsPoints.minOf { it.longitude }
        val maxLng = gpsPoints.maxOf { it.longitude }

        // 경기장 중앙선 추정
        val midLng = (minLng + maxLng) / 2.0

        // 처음 10개 포인트만 사용 (또는 전체가 10개 미만이면 모두 사용)
        val initialPoints = if (gpsPoints.size > 10) gpsPoints.take(10) else gpsPoints

        // 초기 위치의 평균 경도 계산
        val avgLng = initialPoints.map { it.longitude }.average()

        // 중앙선과 비교하여 진영 결정
        val gameSide = if (avgLng < midLng) GameSide.LEFT else GameSide.RIGHT

        Log.i(TAG, "게임 진영 감지: $gameSide (중앙선: $midLng, 초기 평균 경도: $avgLng)")
        return gameSide
    }

    fun calculateHeatmap(gpsPoints: List<GpsPoint>): HeatMapDto {
        val ROWS = 16
        val COLS = 10

        val grid = Array(ROWS) { IntArray(COLS) { 0 } }
        val cellCounts = mutableMapOf<Pair<Int, Int>, Int>()

        if (gpsPoints.isEmpty()) {
            Log.i(TAG, "GPS 포인트가 없습니다. 빈 히트맵 반환")
            return HeatMapDto(grid.map { it.toList() },
                    gameSide = "left"
                )
        }

        // 게임 진영 감지 및 로깅
        val gameSide = if(detectGameSide(gpsPoints).toString() == "LEFT") "left" else "right"
        Log.i(TAG, "히트맵 생성 시작 - 감지된 진영: $gameSide")

        // Min-Max 정규화 사용
        Log.i(TAG, "정규화 방식: Min-Max 정규화 사용")

        // 모든 GPS 포인트를 사용하여 Min-Max 계산
        val minLat = gpsPoints.minOf { it.latitude }
        val maxLat = gpsPoints.maxOf { it.latitude }
        val minLng = gpsPoints.minOf { it.longitude }
        val maxLng = gpsPoints.maxOf { it.longitude }

        Log.d(TAG, "GPS 포인트 범위: 남서(${minLat}, ${minLng}), 북동(${maxLat}, ${maxLng})")

        gpsPoints.forEach { point ->
            val normalizedLat = (point.latitude - minLat) / (maxLat - minLat)
            val normalizedLng = (point.longitude - minLng) / (maxLng - minLng)

            val row = (normalizedLat * (ROWS-1)).toInt().coerceIn(0, ROWS-1)
            val col = (normalizedLng * (COLS-1)).toInt().coerceIn(0, COLS-1)

            val key = Pair(row, col)
            cellCounts[key] = (cellCounts[key] ?: 0) + 1
        }

        // 최대 카운트 찾기
        val maxCount = cellCounts.values.maxOrNull() ?: 1
        Log.d(TAG, "셀별 최대 카운트: $maxCount, 총 채워진 셀 수: ${cellCounts.size}")

        // 백분위수 기반 정규화 - 히트맵에 더 적합한 방식
        // 방문 횟수를 정렬하여 백분위수 계산
        val sortedCounts = cellCounts.values.filter { it > 0 }.sorted()

        if (sortedCounts.isNotEmpty()) {
            // 히트맵 강도에 대한 임계값 계산 (0-100% 범위에서)
            val thresholds = mutableListOf<Int>()

            // 주변에 영향을 주는 효과를 위해 상위 백분위 기준으로 임계값 설정
            // 상위 5%는 10, 상위 15%는 8, 상위 30%는 6, 상위 50%는 4, 나머지는 2
            val percentiles = listOf(0.95, 0.85, 0.7, 0.5)

            for (p in percentiles) {
                val index = (sortedCounts.size * p).toInt().coerceAtMost(sortedCounts.size - 1)
                thresholds.add(sortedCounts[index])
            }

            Log.d(TAG, "히트맵 임계값: $thresholds")

            // 임계값 기반으로 각 셀에 값 할당
            cellCounts.forEach { (pos, count) ->
                if (pos.first in 0 until ROWS && pos.second in 0 until COLS) {
                    val normalizedValue = when {
                        count == 0 -> 0
                        count >= thresholds[0] -> 10  // 상위 5%
                        count >= thresholds[1] -> 8   // 상위 5-15%
                        count >= thresholds[2] -> 6   // 상위 15-30%
                        count >= thresholds[3] -> 4   // 상위 30-50%
                        else -> 2                     // 하위 50%
                    }
                    grid[pos.first][pos.second] = normalizedValue
                }
            }

            // 주변 영향 확산 - 주변 셀들에 영향 주기
            val tempGrid = Array(ROWS) { row -> IntArray(COLS) { col -> grid[row][col] } }

            for (row in 0 until ROWS) {
                for (col in 0 until COLS) {
                    if (grid[row][col] > 0) {
                        // 주변 셀들에 영향 주기 (상하좌우)
                        val neighbors = listOf(
                            Pair(row-1, col), Pair(row+1, col),
                            Pair(row, col-1), Pair(row, col+1)
                        )

                        for ((r, c) in neighbors) {
                            if (r in 0 until ROWS && c in 0 until COLS) {
                                // 주변 셀의 값이 현재 셀의 영향을 받을 수 있는 경우에만 업데이트
                                val spreadValue = grid[row][col] - 2
                                if (spreadValue > tempGrid[r][c]) {
                                    tempGrid[r][c] = spreadValue
                                }
                            }
                        }
                    }
                }
            }

            // 확산된 값 적용
            for (row in 0 until ROWS) {
                for (col in 0 until COLS) {
                    grid[row][col] = tempGrid[row][col]
                }
            }
        } else {
            // 방문한 셀이 없는 경우, 기본 방식 적용
            cellCounts.forEach { (pos, count) ->
                if (pos.first in 0 until ROWS && pos.second in 0 until COLS) {
                    val normalizedValue = if (count == 0) 0 else {
                        val ratio = count.toDouble() / maxCount
                        (1 + (ratio * 9)).toInt().coerceIn(1, 10)
                    }
                    grid[pos.first][pos.second] = normalizedValue
                }
            }
        }

        // 최종 히트맵 정보 로깅
        val filledCells = grid.sumOf { row -> row.count { it > 0 } }
        val totalCells = ROWS * COLS
        val filledPercent = (filledCells * 100 / totalCells)

        Log.i(TAG, "히트맵 생성 완료: ${ROWS}x${COLS} 그리드, 채워진 셀: $filledCells/$totalCells (${filledPercent}%)")

        return HeatMapDto(
                heatMap = grid.map { it.toList() },
                gameSide = gameSide.toString()
            )
    }
}
