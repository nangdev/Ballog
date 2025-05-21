package com.ballog.mobile.data.service

import android.content.Context
import android.util.Log
import com.ballog.mobile.data.model.Exercise
import com.ballog.mobile.data.model.GpsPoint
import com.ballog.mobile.data.model.LiveDataSegment
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import java.util.zip.Inflater


/**
 * 삼성 헬스 API를 이용해 운동 데이터를 가져오는 서비스
 *
 * 이 서비스는 custom_title이 "Ballog"인 운동 데이터만 필터링해서 가져오며,
 * 모든 운동 이름은 "Ballog"로 설정됩니다.
 * 또한 속력이 일정 이상일 때 스프린트 횟수를 계산합니다.
 */
class SamsungHealthDataService(private val context: Context) {
    /** 로그 태그 */
    private val TAG = "SamsungHealth"

    /** 삼성 헬스 데이터 저장소 */
    private val healthDataStore: HealthDataStore = HealthDataService.getStore(context)
    private val metricsCalculator = ExerciseMetricsCalculator(context)

    companion object {
        /** 시간 형식 (시:분) */
        private const val TIME_FORMAT = "HH:mm"

        /** 날짜 형식 (년월일) */
        private const val DATE_FORMAT = "yyyy년 MM월 dd일"

        /** 삼성 헬스 API에서 난독화된 데이터 맵 필드명 */
        private const val FIELD_DATA_MAP = "a"

        /** 앱 식별용 이름 (Ballog 앱 데이터만 가져오기 위함) */
        private const val APP_NAME = "Ballog"

        /** 스프린트로 간주할 최소 속력 (m/s) */
        private const val SPRINT_THRESHOLD_SPEED = 2.8f // 약 18 km/h

        /** 스프린트 상태가 유지되어야 하는 최소 시간 (밀리초) */
        private const val SPRINT_MIN_DURATION = 500 // 0.2초
    }

    /**
     * 삼성 헬스에서 Ballog 앱의 운동 데이터를 가져옵니다.
     *
     * @return Ballog 앱의 운동 데이터 목록, 권한이 없거나 오류 시 빈 목록 반환
     */
    suspend fun getExercise(): List<Exercise> = withContext(Dispatchers.IO) {
        try {
            // 권한 확인
            if (!checkPermissions()) {
                Log.e(TAG, "필요한 권한이 없습니다")
                return@withContext emptyList()
            }

            // 운동 데이터 읽기
            val exerciseList = readExercise()
            if(exerciseList.isEmpty()){
                return@withContext emptyList()
            }
            Log.d(TAG, "운동 데이터 ${exerciseList.size}개 불러옴")
            return@withContext exerciseList
        } catch (e: Exception) {
            Log.e(TAG, "운동 데이터 조회 실패: ${e.message}")
            emptyList()
        }
    }

    /**
     * 삼성 헬스에서 운동 데이터를 읽어옵니다.
     * custom_title이 "Ballog"인 데이터만 필터링합니다.
     *
     * @return Ballog 앱의 운동 데이터 목록
     */
    private suspend fun readExercise(): List<Exercise> {
        // 최근 7일 데이터 조회를 위한 시간 설정
        val endTime = LocalDateTime.now()
        val startTime = endTime.minusDays(3)

        try {
            // 데이터 요청 객체 생성
            val readRequest = DataTypes.EXERCISE.readDataRequestBuilder
                .setLocalTimeFilter(LocalTimeFilter.of(startTime, endTime))
                .build()

            // 데이터 조회
            val result = healthDataStore.readData(readRequest)
            val dataList = result.dataList

            Log.d(TAG, "운동 데이터 ${dataList.size}개 발견")

            // Ballog 앱 데이터 개수 확인 (로그용)
            val exerciseList = mutableListOf<Exercise>()
            val ballogDataCount = dataList.count { dataPoint ->
                try {
                    val dataMap = extractDataMap(dataPoint)
                    val customTitle = dataMap["custom_title"] as? String
                    customTitle == APP_NAME
                } catch (e: Exception) {
                    false
                }
            }
            Log.d(TAG, "Ballog 앱 데이터 ${ballogDataCount}개 발견")

            // 각 데이터 포인트 처리
            dataList.forEach { dataPoint ->
                try {
                    val dataMap = extractDataMap(dataPoint)

                    // custom_title이 Ballog인지 확인
                    val customTitle = dataMap["custom_title"] as? String
                    if (customTitle != APP_NAME) {
                        // Ballog가 아닌 데이터는 건너뜀
                        return@forEach
                    }

                    // 디버깅을 위해 모든 키-값 쌍 로깅
                    Log.d(TAG, "=== Ballog 운동 데이터 내용 ===")
                    dataMap.entries.forEach { entry ->
                        Log.d(TAG, "  ${entry.key}: ${entry.value}")
                    }

                    // 이름은 항상 "Ballog"로 설정
                    val exerciseName = APP_NAME

                    // 세션 정보 추출
                    val sessions = dataMap["sessions"] as? List<*>

                    sessions?.firstOrNull()?.let { session ->
                        // 운동 데이터 객체 생성
                        val exercise = createExercise(session = session, uid = dataPoint.uid, customName = exerciseName)
                        exerciseList.add(exercise)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "DataPoint 처리 실패: ${e.message}")
                }
            }

            Log.d(TAG, "총 ${exerciseList.size}개의 Ballog 운동 데이터 변환 완료")
            return exerciseList.sortedBy { it.timestamp }

        } catch (e: Exception) {
            Log.e(TAG, "운동 데이터 읽기 실패: ${e.message}")
            return emptyList()
        }
    }

    /**
     * 데이터 포인트에서 데이터 맵을 추출합니다.
     * 삼성 헬스 API는 난독화되어 있어 리플렉션을 사용해야 합니다.
     *
     * @param dataPoint 삼성 헬스 데이터 포인트
     * @return 추출된 데이터 맵
     */
    private fun extractDataMap(dataPoint: Any): Map<*, *> {
        // 첫 번째 난독화된 필드(a) 접근
        val fieldDataMapField = dataPoint::class.java.getDeclaredField(FIELD_DATA_MAP)
        fieldDataMapField.isAccessible = true
        val fieldDataMap = fieldDataMapField.get(dataPoint)

        // 두 번째 난독화된 필드(a) 접근
        val dataMapField = fieldDataMap::class.java.getDeclaredField(FIELD_DATA_MAP)
        dataMapField.isAccessible = true
        return dataMapField.get(fieldDataMap) as Map<*, *>
    }

    /**
     * 세션과 사용자 정의 이름으로 운동 데이터 객체를 생성합니다.
     *
     * @param session 운동 세션 객체
     * @param uid 고유 ID
     * @param customName 사용자 정의 이름 (항상 "Ballog"로 설정)
     * @return 생성된 운동 데이터 객체
     */
    private fun createExercise(session: Any, uid: String, customName: String?): Exercise {
        try {
            Log.e("DEBUG", "createExercise() 진입 - ID: $uid")
            // 세션 헬퍼 생성
            val sessionHelper = SessionHelper(session)
            val timeInfo = TimeInfo(sessionHelper.getStartTime(), sessionHelper.getEndTime())

            // 실시간 데이터 추출
            val liveDataSegments = extractLiveData(session)

            // 스프린트 횟수 계산
            val sprintCount = calculateSprintCount(liveDataSegments)
            Log.d(TAG, "스프린트 횟수: $sprintCount")

            // GPS 포인트 추출
            val gpsPoints = extractGpsPoints(session)
            Log.d(TAG, "GPS 포인트 추출 완료 - ID: $uid, 포인트 수: ${gpsPoints.size}")

            // 운동 데이터 객체 생성 및 반환
            return Exercise(
                id = uid,
                exerciseType = customName ?: APP_NAME,
                date = timeInfo.dateString,
                startTime = timeInfo.startTimeString,
                endTime = timeInfo.endTimeString,
                duration = (sessionHelper.getDuration()?.toMinutes() ?: 0L).toString(),
                distance = sessionHelper.getDistance(),
                avgSpeed = sessionHelper.getAvgSpeed(),
                maxSpeed = sessionHelper.getMaxSpeed(),
                calories = sessionHelper.getCalories(),
                avgHeartRate = sessionHelper.getAvgHeartRate().toInt(),
                maxHeartRate = sessionHelper.getMaxHeartRate().toInt(),
                gpsPoints = gpsPoints,
                liveDataSegments = liveDataSegments,
                timestamp = sessionHelper.getStartTime()?.toEpochMilli() ?: 0L,
                sprintCount = sprintCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "운동 데이터 변환 실패 - ID: $uid", e)
            throw e
        }
    }

    /**
     * 실시간 데이터 세그먼트를 분석하여 스프린트 횟수를 계산합니다.
     * 속력이 SPRINT_THRESHOLD_SPEED 이상이고, 그 상태가 SPRINT_MIN_DURATION 이상 유지되면
     * 하나의 스프린트로 간주합니다.
     *
     * @param segments 실시간 데이터 세그먼트 목록
     * @return 스프린트 횟수
     */
    private fun calculateSprintCount(segments: List<LiveDataSegment>): Int {
        if (segments.isEmpty()) return 0

        var sprintCount = 0
        var isInSprint = false
        var sprintStartTime = 0L

        // 시간 순으로 정렬
        val sortedSegments = segments.sortedBy { it.startTime }

        for (segment in sortedSegments) {
            // 속력이 임계값 이상인 경우
            if (segment.speed >= SPRINT_THRESHOLD_SPEED) {
                // 현재 스프린트 중이 아니면 새 스프린트 시작
                if (!isInSprint) {
                    isInSprint = true
                    sprintStartTime = segment.startTime
                }
                // 이미 스프린트 중이면 계속 진행
            } else {
                // 스프린트가 끝난 경우
                if (isInSprint) {
                    // 스프린트 지속 시간 계산
                    val sprintDuration = segment.startTime - sprintStartTime

                    // 최소 지속 시간을 초과했으면 스프린트 카운트 증가
                    if (sprintDuration >= SPRINT_MIN_DURATION) {
                        sprintCount++
                        Log.d(TAG, "스프린트 감지: 지속시간 ${sprintDuration}ms, 시작 시간: $sprintStartTime")
                    }

                    isInSprint = false
                }
            }
        }

        // 마지막 세그먼트가 스프린트 중이었는지 확인
        if (isInSprint) {
            val lastSegment = sortedSegments.last()
            val sprintDuration = lastSegment.startTime - sprintStartTime

            if (sprintDuration >= SPRINT_MIN_DURATION) {
                sprintCount++
                Log.d(TAG, "마지막 스프린트 감지: 지속시간 ${sprintDuration}ms, 시작 시간: $sprintStartTime")
            }
        }

        return sprintCount
    }

    /**
     * 세션에서 실시간 데이터를 추출합니다.
     *
     * @param session 운동 세션 객체
     * @return 실시간 데이터 목록
     */
    private fun extractLiveData(session: Any): List<LiveDataSegment> {
        // 필드 E의 압축된 데이터 추출
        return extractCompressedLiveData(session)
    }

    /**
     * 세션의 E 필드에서 압축된 실시간 데이터를 추출합니다.
     *
     * @param session 운동 세션 객체
     * @return 추출된 실시간 데이터 목록, 실패 시 빈 목록 반환
     */
    private fun extractCompressedLiveData(session: Any): List<LiveDataSegment> {
        try {
            // E 필드 접근
            val fieldE = session::class.java.getDeclaredField("E")
            fieldE.isAccessible = true
            val byteArrayList = fieldE.get(session) as? List<*>

            if (byteArrayList != null && byteArrayList.isNotEmpty()) {
                for (item in byteArrayList) {
                    if (item is ByteArray) {
                        // 압축 해제
                        val decompressed = decompress(item)
                        if (decompressed.isNotEmpty()) {
                            // JSON 파싱
                            val parsedData = parseLiveData(decompressed)
                            if (parsedData.isNotEmpty()) {
                                return parsedData
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "필드 E 접근 실패: ${e.message}")
        }
        return emptyList()
    }

    /**
     * 압축된 바이너리 데이터를 해제합니다.
     * GZIP과 Deflate(Inflater) 두 가지 압축 방식을 모두 시도합니다.
     *
     * @param compressed 압축된 바이너리 데이터
     * @return 압축 해제된 문자열 (보통 JSON 형식), 실패 시 빈 문자열 반환
     */
    private fun decompress(compressed: ByteArray): String {
        return try {
            // GZIP 압축 해제 시도
            val gzipInputStream = GZIPInputStream(compressed.inputStream())
            gzipInputStream.readBytes().toString(Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                // Inflater(Deflate) 압축 해제 시도
                val inflater = Inflater()
                inflater.setInput(compressed)

                val outputStream = ByteArrayOutputStream(compressed.size)
                val buffer = ByteArray(1024)

                while (!inflater.finished()) {
                    val count = inflater.inflate(buffer)
                    outputStream.write(buffer, 0, count)
                }

                outputStream.close()
                outputStream.toString("UTF-8")
            } catch (e2: Exception) {
                // 두 방식 모두 실패한 경우
                ""
            }
        }
    }

    /**
     * 압축 해제된 JSON 문자열을 파싱하여 라이브 데이터 목록으로 변환합니다.
     *
     * @param jsonString JSON 형식의 문자열
     * @return 파싱된 라이브 데이터 세그먼트 목록
     */
    private fun parseLiveData(jsonString: String): List<LiveDataSegment> {
        val segments = mutableListOf<LiveDataSegment>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val segment = jsonArray.getJSONObject(i)
                segments.add(
                    LiveDataSegment(
                        startTime = segment.optLong("start_time", 0),
                        heartRate = segment.optDouble("heart_rate", 0.0).toFloat(),
                        cadence = segment.optDouble("cadence", 0.0).toFloat(),
                        count = segment.optInt("count", 0),
                        power = segment.optDouble("power", 0.0).toFloat(),
                        speed = segment.optDouble("speed", 0.0).toFloat(),
                        distance = segment.optDouble("distance", 0.0).toFloat()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON 파싱 실패: ${e.message}")
        }

        return segments
    }

    /**
     * 세션에서 GPS 위치 데이터를 추출합니다.
     *
     * @param session 운동 세션 객체
     * @return GPS 위치 데이터 목록
     */
    private fun extractGpsPoints(session: Any): List<GpsPoint> {
        Log.e("DEBUG", "extractGpsPoints() 진입")
        val gpsPoints = mutableListOf<GpsPoint>()
        try {

            val getRouteMethod = session::class.java.methods.find { it.name == "getRoute" }
            if (getRouteMethod != null) {
                val route = getRouteMethod.invoke(session) as? List<*>
                route?.forEach { location ->
                    if (location != null) {
                        try {
                            val locationHelper = LocationHelper(location)
                            val gpsPoint = locationHelper.toGpsPoint()
                            if (gpsPoint != null) {
                                gpsPoints.add(gpsPoint)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "GPS 포인트 추출 실패: ${e.message}")
                        }
                    }
                }
            } else {
                Log.e(TAG, "getRoute 메서드가 session 객체에 없습니다.")
            }
        } catch (e: Exception) {
            Log.e("DEBUG", "extractGpsPoints 오류: ${e.message}")
        }
        Log.e("DEBUG", "최종 추출된 gpsPoints 개수: ${gpsPoints.size}")
        return gpsPoints
    }

    /**
     * 세션 객체에서 필요한 정보를 추출하는 헬퍼 클래스
     * 리플렉션을 사용하여 세션 객체의 메서드에 접근합니다.
     */
    private inner class SessionHelper(private val session: Any) {
        /** 운동 지속 시간을 가져옵니다. */
        fun getDuration(): Duration? = invokeMethod("getDuration") as? Duration

        /** 운동 시작 시간을 가져옵니다. */
        fun getStartTime(): java.time.Instant? = invokeMethod("getStartTime") as? java.time.Instant

        /** 운동 종료 시간을 가져옵니다. */
        fun getEndTime(): java.time.Instant? = invokeMethod("getEndTime") as? java.time.Instant

        /** 운동 거리를 가져옵니다. */
        fun getDistance(): Float = invokeMethod("getDistance") as? Float ?: 0f

        /** 평균 속도를 가져옵니다. */
        fun getAvgSpeed(): Float = invokeMethod("getMeanSpeed") as? Float ?: 0f

        /** 최대 속도를 가져옵니다. */
        fun getMaxSpeed(): Float = invokeMethod("getMaxSpeed") as? Float ?: 0f

        /** 소모 칼로리를 가져옵니다. */
        fun getCalories(): Float = invokeMethod("getCalories") as? Float ?: 0f

        /** 평균 심박수를 가져옵니다. */
        fun getAvgHeartRate(): Float = invokeMethod("getMeanHeartRate") as? Float ?: 0f

        /** 최대 심박수를 가져옵니다. */
        fun getMaxHeartRate(): Float = invokeMethod("getMaxHeartRate") as? Float ?: 0f

        /**
         * 리플렉션을 사용하여 세션 객체의 메서드를 호출합니다.
         *
         * @param methodName 호출할 메서드 이름
         * @return 메서드 호출 결과, 실패 시 null
         */
        private fun invokeMethod(methodName: String): Any? {
            return try {
                session::class.java.getMethod(methodName).invoke(session)
            } catch (e: Exception) { null }
        }
    }

    /**
     * 위치 객체에서 GPS 포인트를 추출하는 헬퍼 클래스
     * 리플렉션을 사용하여 위치 객체의 메서드에 접근합니다.
     */
    private inner class LocationHelper(private val location: Any) {
        /**
         * 위치 객체에서 위도, 경도, 시간 정보를 추출하여 GpsPoint 객체로 변환합니다.
         *
         * @return 생성된 GpsPoint 객체, 실패 시 null
         */
        fun toGpsPoint(): GpsPoint? {
            val latitude = location::class.java.getMethod("getLatitude").invoke(location) as? Float
            val longitude = location::class.java.getMethod("getLongitude").invoke(location) as? Float
            val timestamp = location::class.java.getMethod("getTimestamp").invoke(location) as? java.time.Instant

            return if (latitude != null && longitude != null) {
                GpsPoint(
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble(),
                    timestamp = timestamp?.toEpochMilli() ?: 0L
                )
            } else null
        }
    }

    /**
     * 시작 시간과 종료 시간을 포맷팅하는 헬퍼 클래스
     */
    private inner class TimeInfo(startTime: java.time.Instant?, endTime: java.time.Instant?) {
        /** 시작 시간 문자열 (HH:mm 형식) */
        val startTimeString: String

        /** 종료 시간 문자열 (HH:mm 형식) */
        val endTimeString: String

        /** 날짜 문자열 (yyyy년 MM월 dd일 형식) */
        val dateString: String

        init {
            val timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT)
            val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
            val zoneId = java.time.ZoneId.systemDefault()

            val startDateTime = startTime?.atZone(zoneId)?.toLocalDateTime()
            val endDateTime = endTime?.atZone(zoneId)?.toLocalDateTime()

            startTimeString = startDateTime?.format(timeFormatter) ?: "00:00"
            endTimeString = endDateTime?.format(timeFormatter) ?: "00:00"
            dateString = startDateTime?.format(dateFormatter) ?: "날짜 없음"
        }
    }

    /**
     * 필요한 권한이 있는지 확인합니다.
     *
     * @return 모든 필요한 권한이 있으면 true, 아니면 false
     */
    private suspend fun checkPermissions(): Boolean {
        // 필요한 권한 목록
        val permissions = setOf(
            Permission.of(DataTypes.EXERCISE, AccessType.READ),
            Permission.of(DataTypes.HEART_RATE, AccessType.READ),
            Permission.of(DataTypes.EXERCISE_LOCATION, AccessType.READ)
        )

        // 부여된 권한 확인
        val grantedPermissions = healthDataStore.getGrantedPermissions(permissions)
        return grantedPermissions.containsAll(permissions)
    }
}
