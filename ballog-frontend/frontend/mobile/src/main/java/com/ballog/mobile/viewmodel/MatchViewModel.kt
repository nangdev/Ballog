package com.ballog.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ballog.mobile.BallogApplication
import com.ballog.mobile.data.api.RetrofitInstance
import com.ballog.mobile.data.dto.MatchDetailResponseDto
import com.ballog.mobile.data.dto.MatchItemDto
import com.ballog.mobile.data.dto.MatchRegisterRequest
import com.ballog.mobile.data.dto.TeamMatchRegisterRequest
import com.ballog.mobile.data.dto.TeamMember
import com.ballog.mobile.data.model.FieldCorners
import com.ballog.mobile.data.model.Match
import com.ballog.mobile.data.model.MatchState
import com.ballog.mobile.ui.components.DateMarkerState
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.ballog.mobile.data.model.MatchDataCardInfo

sealed class MatchUiState {
    object WaitingForStadiumData : MatchUiState() // 경기장 데이터 대기
    object Loading : MatchUiState()
    object NoData : MatchUiState()
    data class Success(val data: List<MatchDataCardInfo>) : MatchUiState()
    data class StadiumDataSuccess(val fieldCorners: FieldCorners) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = com.ballog.mobile.data.repository.MatchRepository(application.applicationContext)
    private val tokenManager = BallogApplication.getInstance().tokenManager
    private val matchApi = RetrofitInstance.matchApi

    // 매치 상태 (로딩 / 성공 / 에러)
    private val _matchState = MutableStateFlow<MatchState>(MatchState.Loading)
    val matchState: StateFlow<MatchState> = _matchState

    private val _uiState = MutableStateFlow<MatchUiState>(
        MatchUiState.WaitingForStadiumData
    )
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var lastTimestamp: Long = 0L

    private suspend fun getConnectedNodesSuspend(nodeClient: NodeClient): List<Node> =
        suspendCancellableCoroutine { cont ->
            nodeClient.connectedNodes
                .addOnSuccessListener { nodes -> cont.resume(nodes) }
                .addOnFailureListener { cont.resume(emptyList()) }
        }

    /**
     * 내 매치 리스트 불러오기
     * @param month yyyy-MM 형태의 월 문자열
     */
    fun fetchMyMatches(month: String) {
        viewModelScope.launch {
            _matchState.value = MatchState.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull() ?: return@launch
                val response = matchApi.getMyMatches("Bearer $token", month)
                val body = response.body()

                android.util.Log.d("MatchViewModel", "✅ 응답 결과: ${body}")

                if (response.isSuccessful && body?.isSuccess == true) {
                    // Dto → Domain 변환 후 상태 갱신
                    val matches = body.result?.matchList?.map { it.toDomain() } ?: emptyList()
                    _matchState.value = MatchState.Success(matches)
                } else {
                    _matchState.value = MatchState.Error(body?.message ?: "매치 데이터를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _matchState.value = MatchState.Error("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    /**
     * 팀 매치 리스트 불러오기
     */
    fun fetchTeamMatches(teamId: Int, month: String) {
        viewModelScope.launch {
            _matchState.value = MatchState.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull() ?: return@launch
                val response = matchApi.getTeamMatches("Bearer $token", teamId, month)
                val body = response.body()

                if (response.isSuccessful && body?.isSuccess == true) {
                    val matches = body.result?.matchList?.map { it.toDomain() } ?: emptyList()
                    _matchState.value = MatchState.Success(matches)
                } else {
                    _matchState.value = MatchState.Error(body?.message ?: "팀 매치를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _matchState.value = MatchState.Error("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    /**
     * 팀 맴버 불러오기
     */
    // 상태 선언
    private val _teamPlayers = MutableStateFlow<List<TeamMember>>(emptyList())
    val teamPlayers: StateFlow<List<TeamMember>> = _teamPlayers

    fun fetchTeamPlayers(teamId: Int) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                _teamPlayers.value = emptyList()
                return@launch
            }

            try {
                val response = RetrofitInstance.teamApi.getTeamMemberList("Bearer $token", teamId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _teamPlayers.value = response.body()?.result?.teamMemberList ?: emptyList()

                    val members = response.body()?.result?.teamMemberList ?: emptyList()
                    _teamPlayers.value = members

                    // 로그 출력
                    android.util.Log.d(
                        "MatchViewModel",
                        "✅ 팀 멤버 로딩 성공: 총 ${members.size}명 → ${members.joinToString { it.nickname }}"
                    )
                } else {
                    _teamPlayers.value = emptyList()
                }
            } catch (e: Exception) {
                _teamPlayers.value = emptyList()
            }
        }
    }

    /**
     * 서버에 개인 신규 매치 등록하는 함수
     */
    fun registerMyMatch(
        date: String,
        startTime: String,
        endTime: String,
        matchName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                onError("로그인이 필요합니다")
                return@launch
            }

            val request = MatchRegisterRequest(
                matchDate = date,
                startTime = startTime,
                endTime = endTime,
                matchName = matchName
            )

            val response = matchApi.registerMyMatch("Bearer $token", request)
            android.util.Log.d("MatchViewModel", "📤 요청 내용: $request")


            if (response.isSuccessful && response.body()?.isSuccess == true) {
                onSuccess()
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("MatchViewModel", "❌ 매치 등록 실패: code=${response.code()}, body=$errorBody")
                onError(response.body()?.message ?: "매치 등록 실패 (${response.code()})")
            }
        }
    }

    /**
     * 서버에 팀 신규 매치 등록 함수
     */
    fun registerTeamMatch(
        teamId: Int,
        date: String,
        startTime: String,
        endTime: String,
        matchName: String,
        participantIds: List<Int>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                onError("로그인이 필요합니다")
                return@launch
            }

            val request = TeamMatchRegisterRequest(
                teamId = teamId,
                matchDate = date,
                startTime = startTime,
                endTime = endTime,
                matchName = matchName,
                participantList = participantIds
            )

            try {
                val response = RetrofitInstance.matchApi.registerTeamMatch("Bearer $token", request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "등록 실패")
                }
            } catch (e: Exception) {
                onError("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    /* 매치 상세 조회 */
    private val _matchDetail = MutableStateFlow<MatchDetailResponseDto?>(null)
    val matchDetail: StateFlow<MatchDetailResponseDto?> = _matchDetail

    fun fetchMatchDetail(matchId: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken().firstOrNull() ?: return@launch
                val response = matchApi.getMatchDetail("Bearer $token", matchId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    android.util.Log.d("MatchViewModel", "✅ 매치 상세 조회 성공: $result")
                    _matchDetail.value = result
                } else {
                    android.util.Log.e("MatchViewModel", "❌ 매치 상세 조회 실패: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("MatchViewModel", "❌ 예외 발생: ${e.localizedMessage}")
            }
        }
    }
    
    // 샘플: 상태 전환 메서드 (나중에 실제 연동 로직으로 대체)

    fun setLoading() {
        android.util.Log.d("MatchViewModel", "상태 전환: Loading")
        _uiState.value = MatchUiState.Loading
    }

    fun setNoData() {
        android.util.Log.d("MatchViewModel", "상태 전환: NoData")
        _uiState.value = MatchUiState.NoData
    }
    fun setSuccess(data: List<MatchDataCardInfo>) {
        android.util.Log.d("MatchViewModel", "상태 전환: Success($data)")
        _uiState.value = MatchUiState.Success(data)
    }

    fun setWaitingForStadiumData(){
        android.util.Log.d("MatchViewModel", "상태 전환: Waiting")
        _uiState.value = MatchUiState.WaitingForStadiumData
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    fun checkFieldCorners() {
        viewModelScope.launch {
            val corners = repository.getFieldCorners()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("field_corners", android.content.Context.MODE_PRIVATE)
            val timestamp = sharedPrefs.getLong("timestamp", 0L)
            if (corners != null && corners.size == 4 && timestamp != lastTimestamp) {
                lastTimestamp = timestamp
                _uiState.value = MatchUiState.Loading
            } else if (corners == null || corners.size != 4) {
                _uiState.value = MatchUiState.WaitingForStadiumData
            }
        }
    }

    suspend fun checkStadiumDataState(): Boolean {
        val corners = repository.getFieldCorners()
        val sharedPrefs = getApplication<Application>().getSharedPreferences(
            "field_corners",
            android.content.Context.MODE_PRIVATE
        )
        val timestamp = sharedPrefs.getLong("timestamp", 0L)
        return corners != null && corners.size == 4
    }

    private fun checkSamsungHealthData() {
        println("삼성 헬스 체크 중")
        // TODO: 실제 삼성헬스 데이터 확인 로직으로 대체
        // 임시: 50% 확률로 데이터 있다고 가정
        val hasData = (0..1).random() == 0
        if (hasData) {
            // 임시 Success 데이터
            _uiState.value = MatchUiState.Success(
                listOf(
                )
            )
        } else {
            _uiState.value = MatchUiState.NoData
        }
    }
}

/**
 * MatchItemDto → Domain Model로 변환
 */
fun MatchItemDto.toDomain(): Match {
    return Match(
        id = matchId,
        date = matchDate,
        startTime = startTime,
        endTime = endTime,
        matchName = matchName,
    )
}

/**
 * 달력 데이터를 생성하는 유틸 함수
 * @param currentMonth 현재 기준 월
 * @param matches 이 달에 등록된 매치 리스트
 * @return 7일 단위로 나눠진 달력 상태 리스트
 */
fun buildCalendar(currentMonth: LocalDate, matches: List<Match>): List<List<DateMarkerState>> {
    val yearMonth = YearMonth.from(currentMonth)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek
    val dayOffset = (firstDayOfWeek.value % 7) // Sunday = 0

    val prevMonth = yearMonth.minusMonths(1)
    val prevMonthLength = prevMonth.lengthOfMonth()

    val totalCells = ((dayOffset + daysInMonth + 6) / 7) * 7
    val calendarDates = mutableListOf<DateMarkerState>()

    // ⬅ 앞달 날짜 추가
    for (i in dayOffset downTo 1) {
        val day = prevMonthLength - i + 1
        calendarDates.add(DateMarkerState(day.toString(), marked = false, selected = false, thisMonth = false))
    }

    // 📅 이번달 날짜 추가
    val matchDaySet = matches.mapNotNull { it.date.takeLast(2).toIntOrNull() }.toSet()
    for (day in 1..daysInMonth) {
        val marked = day in matchDaySet
        calendarDates.add(DateMarkerState(day.toString(), marked = marked, selected = false, thisMonth = true))
    }

    // ➡ 다음달 날짜 추가 (달력 빈칸 채우기용)
    val remaining = totalCells - calendarDates.size
    for (day in 1..remaining) {
        calendarDates.add(DateMarkerState(day.toString(), marked = false, selected = false, thisMonth = false))
    }

    return calendarDates.chunked(7) // 7일씩 한 주 단위로 자르기
}
