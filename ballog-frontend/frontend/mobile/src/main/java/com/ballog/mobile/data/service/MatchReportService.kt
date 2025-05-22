package com.ballog.mobile.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ballog.mobile.BallogApplication
import com.ballog.mobile.data.api.MatchApi
import com.ballog.mobile.data.dto.DayMatchesRequest
import com.ballog.mobile.data.dto.GpsLocation
import com.ballog.mobile.data.dto.MatchReportResponse
import com.ballog.mobile.data.model.Exercise
import com.ballog.mobile.data.model.GpsPoint
import com.ballog.mobile.data.dto.GameReportData
import com.ballog.mobile.data.dto.MatchItemDto
import com.ballog.mobile.data.dto.MatchReportData
import com.ballog.mobile.data.dto.MatchReportRequest
import com.ballog.mobile.data.dto.QuarterReport
import com.ballog.mobile.data.repository.MatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 매치 리포트 데이터를 생성하고 서버에 전송하는 서비스
 */
class MatchReportService(
    private val context: Context,
    private val matchApi: MatchApi,
    private val samsungHealthDataService: SamsungHealthDataService
){
    private val TAG = "MatchReport"
    private val matchRepository = MatchRepository(context)
    private val metricsCalculator = ExerciseMetricsCalculator(context)
    private val tokenManager = BallogApplication.getInstance().tokenManager

    // 카드 리스트 상태 관리
    private val _quarterReportList = MutableStateFlow<List<QuarterReport>>(emptyList())
    val quarterReportList: StateFlow<List<QuarterReport>> = _quarterReportList.asStateFlow()

    private val _selectedQuarterList = MutableStateFlow<List<String>>(emptyList())
    val selectedQuarterList: StateFlow<List<String>> = _selectedQuarterList.asStateFlow()

    private val _selectedMatchId = MutableStateFlow<Int>(0)
    val selectedMatchId: StateFlow<Int> = _selectedMatchId.asStateFlow()

    private val _dayMatchesList = MutableStateFlow<List<MatchItemDto>>(emptyList())
    val dayMatchesList: StateFlow<List<MatchItemDto>> = _dayMatchesList.asStateFlow()

    private val _fieldCorners = MutableStateFlow<List<GpsLocation>>(emptyList())
    val fieldCorners: StateFlow<List<GpsLocation>> = _fieldCorners.asStateFlow()

    val prefs = context.getSharedPreferences("field_corners", Context.MODE_PRIVATE)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "lat1" || key == "lon1" ||
            key == "lat2" || key == "lon2" ||
            key == "lat3" || key == "lon3" ||
            key == "lat4" || key == "lon4" ||
            key == "timestamp"
        ) {
            // 값이 바뀔 때마다 StateFlow 갱신
            CoroutineScope(Dispatchers.IO).launch {
                val fieldCorners = matchRepository.getFieldCorners()
                _fieldCorners.value = fieldCorners ?: emptyList()
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }


    fun addSelectedQuarter(id: String) {
        _selectedQuarterList.value = _selectedQuarterList.value + id
    }
    fun removeSelectedQuarter(id: String) {
        _selectedQuarterList.value = _selectedQuarterList.value - id
    }

    // 전체 체크
    fun checkAllQuarters(ids: List<String>) {
        _selectedQuarterList.value = ids
    }
    // 전체 해제
    fun uncheckAllQuarters() {
        _selectedQuarterList.value = emptyList()
    }

    fun setSelectedMatchId(id: Int) {
        _selectedMatchId.value = id
    }

    fun deleteReport(id: String){
        _quarterReportList.value = _quarterReportList.value.filter { it.id != id }
    }

    fun clearQuarterReportList(){
        _quarterReportList.value = emptyList()
    }

    suspend fun fetchDayMatches(days: List<String>) {
        val result = getDayMatches(days)
        _dayMatchesList.value = result
    }

    fun clearCornerData(){
        matchRepository.clearFieldCorners()
    }
    /**
     * 삼성 헬스 데이터를 가져와 매치 리포트를 생성하고 서버에 전송합니다.
     */
    suspend fun createMatchReport(usedId: List<String>) {
        Log.d(TAG, "[createMatchReport] 호출됨")
        // 1. 삼성 헬스 데이터 가져오기
        val healthDataList = samsungHealthDataService.getExercise()

        val exerciseList = healthDataList.filter { exercise ->
            exercise.id !in usedId
        }
        Log.d(TAG, "[createMatchReport] exerciseList.size = ${exerciseList.size}")
        Log.d(TAG, "[createMatchReport] exerciseList = $exerciseList")
        if (exerciseList.isEmpty()) {
            Log.e(TAG, "[createMatchReport] 삼성 헬스 데이터가 없습니다")
            return
        }

        // 2. 경기장 모서리 좌표 가져오기
        val fieldCorners = matchRepository.getFieldCorners()
        if (fieldCorners == null) {
            Log.e(TAG, "[createMatchReport] 경기장 좌표 데이터가 없습니다")
            return
        }
        _fieldCorners.value = fieldCorners
        Log.d(TAG, "[createMatchReport] fieldCorners.size = ${fieldCorners.size}, fieldCorners = $fieldCorners")

        // 3. 쿼터별 리포트 데이터 생성
        _quarterReportList.value = createQuarterReports(exerciseList, fieldCorners)
        Log.d(TAG, "[createMatchReport] _quarterReportList.size = ${_quarterReportList.value.size}")
        Log.d(TAG, "[createMatchReport] _quarterReportList = ${_quarterReportList.value}")
        if (_quarterReportList.value.isEmpty()) {
            Log.e(TAG, "[createMatchReport] 생성된 리포트 데이터가 없습니다")
        } else {
            Log.d(TAG, "[createMatchReport] 리포트 데이터: ${_quarterReportList.value}")
        }
        Log.d(TAG, "[createMatchReport] 끝")
    }

    suspend fun sendMatchReport() : MatchReportResponse? {
        try {
            val token = tokenManager.getAccessToken()
            Log.d(TAG, "[sendMatchReport] token: $token")
            val reportDataList: List<MatchReportData> = _quarterReportList.value.map { quarter ->
                MatchReportData(
                    quarterNumber = quarter.quarterNumber,
                    gameSide = quarter.gameSide,
                    gameReportData = quarter.gameReportData
                )
            }
            Log.d(TAG, "[sendMatchReport] reportDataList: $reportDataList")
            // 4. 서버에 전송
            val request = MatchReportRequest(
                matchId = _selectedMatchId.value,
                reportDataList = reportDataList
            )
            Log.d(TAG, "[sendMatchReport] 서버 전송 시작: $request")

            val response = matchApi.sendMatchReport(
                token = "Bearer $token",
                request = request
            )
            Log.d(TAG, "[sendMatchReport] 서버 응답: isSuccessful=${response.isSuccessful}, code=${response.code()}, body=${response.body()}, errorBody=${response.errorBody()?.string()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "[sendMatchReport] 서버 응답 실패: code=${response.code()}, body=$errorBody")
            }

            val success = response.body()?.isSuccess == true
            Log.d(TAG, "[sendMatchReport] 서버 전송 결과: $success")
            matchRepository.clearFieldCorners()
            return response.body()!!.result

        } catch (e: Exception) {
            Log.e(TAG, "[sendMatchReport] 리포트 생성 및 전송 실패: ${e.message}", e)
            return null
        }
    }

    fun updateNumberAndSide(
        id: String,
        quarterNumber: Int,
        side: String
    ) {
        Log.d(TAG, "[updateNumberAndSide] 호출됨: id=${'$'}id, quarterNumber=${'$'}quarterNumber, side=${'$'}side")
        _quarterReportList.value = _quarterReportList.value.map { report ->
            if (report.id == id) {
                Log.d(TAG, "[updateNumberAndSide] 업데이트 대상 찾음: report=$report")
                report.copy(
                    quarterNumber = quarterNumber,
                    gameSide = side
                )
            } else {
                report
            }
        }
        Log.d(TAG, "[updateNumberAndSide] 업데이트 후 _quarterReportList: ${'$'}{_quarterReportList.value}")
    }

    fun getHeatMapData(
        id: String
    ): List<List<Int>> {
        Log.d(TAG, "[getHeatMapData] 호출됨: id=${'$'}id")
        _quarterReportList.value.forEach { report ->
            if (report.id == id) {
                Log.d(TAG, "[getHeatMapData] heatmap 반환: report=$report")
                return report.gameReportData.heatmap
            }
        }
        Log.d(TAG, "[getHeatMapData] 해당 id 없음, 0으로 초기화된 heatmap 반환")
        return List(10) { List(16) { 0 } }
    }

    /**
     * 쿼터별 리포트 데이터를 생성합니다.
     */
    private fun createQuarterReports(
        exerciseList: List<Exercise>,
        fieldCorners: List<GpsLocation>
    ): List<QuarterReport> {
        Log.d(TAG, "[createQuarterReports] 호출됨: exerciseList.size=${exerciseList.size}, fieldCorners.size=${fieldCorners.size}")
        Log.d(TAG, "[createQuarterReports] exerciseList = $exerciseList")
        Log.d(TAG, "[createQuarterReports] fieldCorners = $fieldCorners")
        return exerciseList.mapNotNull { quarter ->
            try {
                Log.d(TAG, "[createQuarterReports] quarter: $quarter")

                // 2. 경기장 내 GPS 포인트만 필터링
                Log.d(TAG, "[createQuarterReports] quarter.gpsPoints.size = ${quarter.gpsPoints.size}, quarter.gpsPoints = ${quarter.gpsPoints}")
                val filteredGpsPoints = filterGpsPointsInsideField(quarter.gpsPoints, fieldCorners)
                Log.d(TAG, "[createQuarterReports] filteredGpsPoints.size=${filteredGpsPoints.size}, filteredGpsPoints=${filteredGpsPoints}")
                if (filteredGpsPoints.isEmpty()) {
                    Log.e(TAG, "[createQuarterReports] 쿼터 ${quarter.id}의 필터링된 GPS 포인트가 없습니다. quarter.gpsPoints = ${quarter.gpsPoints}, fieldCorners = $fieldCorners")
                }

                // 3. GPS 포인트를 그리드로 변환
                val heatmapGrid = metricsCalculator.calculateHeatmap(quarter.gpsPoints)
                Log.d(TAG, "[createQuarterReports] heatmapGrid 생성 완료: heatmapGrid=${heatmapGrid.heatMap.map { it.toList() }}")

                // 4. 쿼터 리포트 생성
                val report = QuarterReport(
                    id = quarter.id,
                    date = quarter.date,
                    quarterNumber = null,
                    gameSide = heatmapGrid.gameSide,
                    gameReportData = GameReportData(
                        startTime = quarter.startTime,
                        endTime = quarter.endTime,
                        distance = quarter.distance.toDouble() * 0.001,
                        avgSpeed = quarter.avgSpeed.toDouble() * 3.6,
                        maxSpeed = quarter.maxSpeed.toDouble() * 3.6,
                        calories = quarter.calories.toInt(),
                        sprint = quarter.sprintCount,
                        avgHeartRate = quarter.avgHeartRate,
                        maxHeartRate = quarter.maxHeartRate,
                        heatmap = heatmapGrid.heatMap.map { it.toList() }
                    )
                )
                Log.d(TAG, "[createQuarterReports] QuarterReport 생성: $report")
                report
            } catch (e: Exception) {
                Log.e(TAG, "[createQuarterReports] 쿼터 리포트 생성 실패", e)
                null
            }
        }
    }

    /**
     * 시간 범위에 맞는 운동 데이터를 찾습니다.
     */
    private fun findExerciseForTimeRange(
        exerciseList: List<Exercise>,
        startTime: String,
        endTime: String
    ): Exercise? {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeMillis = timeFormat.parse(startTime)?.time ?: return null
        val endTimeMillis = timeFormat.parse(endTime)?.time ?: return null

        return exerciseList.firstOrNull()
    }

    /**
     * 경기장 내부에 있는 GPS 포인트만 필터링합니다.
     */
    private fun filterGpsPointsInsideField(
        gpsPoints: List<GpsPoint>,
        fieldCorners: List<GpsLocation>
    ): List<GpsPoint> {
        return gpsPoints.filter { point ->
            val location = GpsLocation(point.latitude, point.longitude)
            isPointInsideField(location, fieldCorners)
        }
    }

    /**
     * 점이 경기장 내부에 있는지 확인합니다.
     */
    private fun isPointInsideField(
        point: GpsLocation,
        fieldCorners: List<GpsLocation>
    ): Boolean {
        var inside = false
        var j = fieldCorners.size - 1

        for (i in fieldCorners.indices) {
            val xi = fieldCorners[i].longitude
            val yi = fieldCorners[i].latitude
            val xj = fieldCorners[j].longitude
            val yj = fieldCorners[j].latitude

            val intersect = ((yi > point.latitude) != (yj > point.latitude)) &&
                (point.longitude < (xj - xi) * (point.latitude - yi) / (yj - yi) + xi)

            if (intersect) inside = !inside
            j = i
        }

        return inside
    }

    /**
     * GPS 포인트를 16x10 그리드로 변환합니다.
     */
    private fun createHeatmapGrid(
        gpsPoints: List<GpsPoint>,
        fieldCorners: List<GpsLocation>
    ): Array<IntArray> {
        val grid = Array(10) { IntArray(16) }

        for (point in gpsPoints) {
            val location = GpsLocation(point.latitude, point.longitude)
            val gridPosition = convertToGridPosition(location, fieldCorners)

            if (gridPosition != null) {
                val (row, col) = gridPosition
                if (row in 0..9 && col in 0..15) {
                    grid[row][col]++
                }
            }
        }

        return normalizeGrid(grid)
    }

    /**
     * 좌표를 그리드 위치로 변환합니다.
     */
    private fun convertToGridPosition(
        location: GpsLocation,
        fieldCorners: List<GpsLocation>
    ): Pair<Int, Int>? {
        if (!isPointInsideField(location, fieldCorners)) {
            return null
        }

        val minLat = fieldCorners.minOf { it.latitude }
        val maxLat = fieldCorners.maxOf { it.latitude }
        val minLng = fieldCorners.minOf { it.longitude }
        val maxLng = fieldCorners.maxOf { it.longitude }

        val normalizedX = (location.longitude - minLng) / (maxLng - minLng)
        val normalizedY = (location.latitude - minLat) / (maxLat - minLat)

        val gridCol = (normalizedX * 16).toInt().coerceIn(0, 15)
        val gridRow = (normalizedY * 10).toInt().coerceIn(0, 9)

        return Pair(gridRow, gridCol)
    }

    /**
     * 그리드 값을 1-10 범위로 정규화합니다.
     */
    private fun normalizeGrid(grid: Array<IntArray>): Array<IntArray> {
        val result = Array(10) { IntArray(16) }
        val maxValue = grid.maxOf { row -> row.maxOf { it } }

        if (maxValue == 0) return result

        for (row in 0..9) {
            for (col in 0..15) {
                result[row][col] = if (grid[row][col] == 0) {
                    0
                } else {
                    1 + ((grid[row][col] * 9) / maxValue)
                }
            }
        }

        return result
    }

    suspend fun getDayMatches(days: List<String>): List<MatchItemDto> {
        Log.d(TAG, "[getDayMatches] 입력 days: $days")
        val convertDays = days.map { convertDateFormat(it) }
        Log.d(TAG, "[getDayMatches] 변환된 convertDays: $convertDays")

        return try {
            val token = tokenManager.getAccessToken()
            val response = matchApi.getDayMatches(
                token = "Bearer $token",
                request = DayMatchesRequest(convertDays)
            )
            Log.d(TAG, "[getDayMatches] 서버 응답: isSuccessful=${response.isSuccessful}, code=${response.code()}, body=${response.body()}, errorBody=${response.errorBody()?.string()}")
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Log.d(TAG, "[getDayMatches] 성공: ${response.body()!!.result!!.matchList}")
                response.body()!!.result!!.matchList
            } else {
                Log.e(TAG, "[getDayMatches] 실패: code=${response.code()}, message=${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "[getDayMatches] 예외 발생: ${e.message}", e)
            emptyList()
        }
    }

    private fun convertDateFormat(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(input, inputFormatter)
        return date.format(outputFormatter)
    }



}

object MatchReportServiceSingleton {
    private var instance: MatchReportService? = null

    fun init(context: Context, matchApi: MatchApi, samsungHealthDataService: SamsungHealthDataService) {
        if (instance == null) {
            instance = MatchReportService(context, matchApi, samsungHealthDataService)
        }
    }

    fun getInstance(): MatchReportService {
        return instance ?: throw IllegalStateException("MatchReportServiceSingleton is not initialized. Call init() first.")
    }
}
