package com.ballog.mobile.ui.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ballog.mobile.ui.theme.BallogTheme
import kotlinx.coroutines.launch
import com.ballog.mobile.util.FileUtils
import com.ballog.mobile.util.VideoUtils
import com.ballog.mobile.viewmodel.VideoViewModel
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.ballog.mobile.data.dto.HighlightAddRequest
import com.ballog.mobile.data.dto.HighlightUpdateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchVideoTab(matchId: Int) {
    Log.d("MatchVideoTab", "🟦 $matchId 번 매치의 영상 탭 접속")

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    
    // ViewModel과 상태 초기화
    val videoViewModel: VideoViewModel = viewModel()
    val videoUiState by videoViewModel.videoUiState.collectAsState()

    var selectedQuarter by remember { mutableStateOf("1 쿼터") }
    var expanded by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteVideoDialog by remember { mutableStateOf(false) }
    var editingHighlight by remember { mutableStateOf(HighlightUiState()) }
    var deleteVideoId by remember { mutableStateOf(-1) }
    
    // 쿼터 옵션 계산
    val quarterOptions = remember(videoUiState.totalQuarters) {
        (1..videoUiState.totalQuarters).map { "$it 쿼터" }
    }

    // QuarterData 컬렉션 초기화
    val quarterData = remember(quarterOptions) {
        mutableStateMapOf<String, QuarterVideoData>().apply {
            quarterOptions.forEach { this[it] = QuarterVideoData() }
        }
    }

    // 현재 선택된 쿼터 데이터 획득 함수
    fun currentData(): QuarterVideoData = quarterData[selectedQuarter] ?: QuarterVideoData()

    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        Log.d("MatchVideoTab", "🔄 초기 데이터 로드 시작")
        videoViewModel.getMatchVideos(matchId)
    }
    
    // API 응답으로 쿼터 데이터 업데이트 및 썸네일 로드 트리거를 하나로 통합
    LaunchedEffect(videoUiState.quarterList) {
        if (videoUiState.quarterList.isNotEmpty()) {
            Log.d("MatchVideoTab", "🧩 API 응답 기반으로 quarterData 초기화")
            
            // 현재 선택된 쿼터 저장
            val currentQuarter = selectedQuarter
            var isFirstLoad = true
            var firstQuarterVideo: String? = null
            
            // 기존 quarterData 초기화
            quarterData.clear()
            quarterOptions.forEach { quarter ->
                quarterData[quarter] = QuarterVideoData(
                    quarterNumber = quarter.filter { it.isDigit() }.toIntOrNull() ?: 1
                )
            }
            
            // API 응답의 quarterList로 업데이트
            videoUiState.quarterList.forEach { video ->
                val quarter = "${video.quarterNumber ?: 1} 쿼터"
                
                // 비디오 URL이 있는 경우만 추가
                if (video.videoUrl?.isNotBlank() == true) {
                    // 첫 번째 쿼터인지 확인하고 저장 
                    if (quarter == "1 쿼터") {
                        firstQuarterVideo = quarter
                    }
                    // 처음 발견된 비디오가 있는 쿼터 저장 (1쿼터 비디오가 없을 경우 대비)
                    else if (firstQuarterVideo == null) {
                        firstQuarterVideo = quarter
                    }
                    
                    quarterData[quarter] = QuarterVideoData(
                        videoId = video.videoId ?: -1,
                        quarterNumber = video.quarterNumber ?: 1,
                        videoUrl = video.videoUrl ?: "",
                        highlights = video.highlights,
                        // 첫 진입 시 showPlayer 상태 설정
                        showPlayer = false
                    )
                    
                    Log.d("MatchVideoTab", "🧩 $quarter → videoUrl=${video.videoUrl}, highlight=${video.highlights.size}개")
                } else {
                    Log.d("MatchVideoTab", "⚠️ $quarter → 비디오 URL 없음")
                }
            }
            
            // 쿼터 선택 처리
            if (quarterOptions.isNotEmpty()) {
                // 초기 실행시에만 1쿼터로 시작하고, 영상 삭제 후에는 현재 쿼터 유지
                if (selectedQuarter.isEmpty() || selectedQuarter !in quarterOptions) {
                    val targetQuarter = if ("1 쿼터" in quarterOptions) "1 쿼터" else quarterOptions.first()
                    selectedQuarter = targetQuarter
                    Log.d("MatchVideoTab", "🔄 초기 진입 시 1쿼터로 설정: $targetQuarter")
                } else {
                    Log.d("MatchVideoTab", "🔄 기존 쿼터 유지: $selectedQuarter")
                }
                
                // 현재 쿼터 비디오 있으면 썸네일 로드 트리거
                if (quarterData[selectedQuarter]?.videoUrl?.isNotBlank() == true) {
                    // 썸네일 로드 트리거
                    coroutineScope.launch {
                        // 먼저 플레이어 모드로 충분히 표시하여 썸네일 생성 보장
                        kotlinx.coroutines.delay(300)
                        quarterData[selectedQuarter] = quarterData[selectedQuarter]?.copy(showPlayer = true) ?: QuarterVideoData()
                        
                        kotlinx.coroutines.delay(1000)
                        
                        // 그 후 썸네일 모드로 전환
                        Log.d("MatchVideoTab", "🖼️ 첫 진입 시 썸네일 모드로 전환 중...")
                        quarterData[selectedQuarter] = quarterData[selectedQuarter]?.copy(showPlayer = false) ?: QuarterVideoData()
                        
                        // 자동 클릭 시뮬레이션 - 필요한 경우
                        kotlinx.coroutines.delay(200)
                        quarterData[selectedQuarter] = quarterData[selectedQuarter]?.copy(showPlayer = true) ?: QuarterVideoData()
                        
                        // 확실하게 썸네일 노출을 위해 다시 플레이어 모드로 변경
                        kotlinx.coroutines.delay(200)
                        quarterData[selectedQuarter] = quarterData[selectedQuarter]?.copy(showPlayer = false) ?: QuarterVideoData()
                    }
                }
            }
            
            // 선택된 쿼터에 비디오가 없는 경우 처리
            if (quarterData[selectedQuarter]?.videoUrl?.isBlank() == true && firstQuarterVideo != null) {
                Log.d("MatchVideoTab", "⚠️ 선택된 쿼터($selectedQuarter)에 비디오가 없어 로드할 수 없음")
            }
        }
    }

    // 쿼터 옵션 변경 감지 - 이미 다른 로직에서 처리되므로 간소화
    LaunchedEffect(quarterOptions) {
        if (selectedQuarter !in quarterOptions && quarterOptions.isNotEmpty()) {
            selectedQuarter = quarterOptions.first()
        }
    }

    // 비디오 업로드 런처
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("MatchVideoTab", "📁 영상 URI 선택됨: $uri")

            // 모든 쿼터의 showPlayer false로 설정
            quarterData.forEach { (key, value) ->
                quarterData[key] = value.copy(showPlayer = false)
            }

            val currentQuarter = selectedQuarter
            val file = FileUtils.uriToFile(context, it)
            val duration = VideoUtils.getVideoDurationString(context, it)
            val quarterNumber = currentQuarter.filter { it.isDigit() }.toIntOrNull() ?: 1

            // 임시 Uri로 반영
            quarterData[currentQuarter] = QuarterVideoData(
                videoId = -1,
                quarterNumber = quarterNumber,
                videoUrl = it.toString(),
                showPlayer = true,
                highlights = quarterData[currentQuarter]?.highlights ?: emptyList()
            )

            Log.d("MatchVideoTab", "🚀 영상 업로드 시작 → matchId=$matchId, quarter=$quarterNumber")

            videoViewModel.uploadQuarterVideo(
                context = context,
                file = file,
                matchId = matchId,
                quarterNumber = quarterNumber,
                duration = duration
            )
        } ?: Log.w("MatchVideoTab", "⛔ 영상 URI가 null입니다.")
    }

    // UI 렌더링
    Column(modifier = Modifier.fillMaxSize()) {
        val current = currentData()
        
        // 첫 진입 시 showPlayer가 false이면서 videoUrl이 있는 경우를 위한 처리
        LaunchedEffect(current) {
            if (current.videoUrl.isNotBlank() && !current.showPlayer) {
                Log.d("MatchVideoTab", "🔄 첫 렌더링 시 비디오 감지 - 썸네일 표시 준비")
                
                // 썸네일 표시 트리거를 위한 작업
                kotlinx.coroutines.delay(200)
                quarterData[selectedQuarter] = current.copy(showPlayer = true)
                kotlinx.coroutines.delay(500)
                quarterData[selectedQuarter] = current.copy(showPlayer = false)
            }
        }

        HighlightContentSection(
            videoUri = current.videoUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) },
            highlights = current.highlights,
            showPlayer = current.showPlayer,
            selectedQuarter = selectedQuarter,
            quarterOptions = quarterOptions,
            expanded = expanded,
            onTogglePlayer = {
                quarterData[selectedQuarter] = current.copy(showPlayer = !current.showPlayer)
            },
            onQuarterChange = {
                val prevQuarter = selectedQuarter
                selectedQuarter = it

                quarterData[prevQuarter] = quarterData[prevQuarter]?.copy(showPlayer = false) ?: QuarterVideoData()
                quarterData[it] = quarterData[it]?.copy(showPlayer = true) ?: QuarterVideoData()

                // 쿼터 변경 시 editingHighlight 초기화
                editingHighlight = HighlightUiState()

                Log.d("MatchVideoTab", "🔄 쿼터 변경: $prevQuarter → $it")
            },
            onExpandedChange = { expanded = it },
            onAddClick = { 
                // 하이라이트 구간 추가 시 editingHighlight 초기화
                editingHighlight = HighlightUiState()
                showAddSheet = true 
            },
            onEditClick = {
                editingHighlight = it
                showEditSheet = true
            },
            onDeleteVideo = {
                val videoId = current.videoId
                if (videoId > 0) {
                    // 삭제 확인 모달을 표시하기 위한 상태 업데이트
                    deleteVideoId = videoId
                    showDeleteVideoDialog = true
                } else {
                    // 유효한 videoId가 없는 경우 로컬 상태만 초기화하지만 현재 선택된 쿼터는 유지
                    val currentQuarterNumber = selectedQuarter.filter { it.isDigit() }.toIntOrNull() ?: 1
                    quarterData[selectedQuarter] = QuarterVideoData(
                        quarterNumber = currentQuarterNumber
                    )
                    // 선택된 쿼터는 그대로 유지
                    Log.d("MatchVideoTab", "🔄 쿼터 유지: $selectedQuarter")
                }
            },
            onUploadClick = {
                launcher.launch("video/*")
            },
            onHighlightClick = { timestamp ->
                // 비디오가 보이지 않는 경우 보이게 변경
                if (!current.showPlayer) {
                    quarterData[selectedQuarter] = current.copy(showPlayer = true)
                }
                
                // 타임스탬프로 이동
                Log.d("MatchVideoTab", "🔍 하이라이트 클릭: $timestamp 지점으로 이동")
                videoViewModel.seekToTimestamp(timestamp)
            }
        )
    }

    // 하이라이트 추가/수정 동작 처리
    val confirmAction: () -> Unit = {
        val updatedHighlight = editingHighlight.copy(
            startMin = editingHighlight.startMin.padStart(2, '0'),
            startSec = editingHighlight.startSec.padStart(2, '0'),
            endMin = editingHighlight.endMin.padStart(2, '0'),
            endSec = editingHighlight.endSec.padStart(2, '0')
        )
        val current = currentData()

        // UI의 mm:ss 형식을 API 요청용 HH:mm:ss 형식으로 변환
        val startTime = if (updatedHighlight.startMin.contains(":")) {
            "00:${updatedHighlight.startMin}"  // UI에서 mm:ss 형식으로 입력된 경우
        } else {
            "00:${updatedHighlight.startMin}:${updatedHighlight.startSec}"  // 분/초 따로 입력된 경우
        }

        val endTime = if (updatedHighlight.endMin.contains(":")) {
            "00:${updatedHighlight.endMin}"  // UI에서 mm:ss 형식으로 입력된 경우
        } else {
            "00:${updatedHighlight.endMin}:${updatedHighlight.endSec}"  // 분/초 따로 입력된 경우
        }

        coroutineScope.launch {
            if (showAddSheet && current.videoId > 0) {
                Log.d("MatchVideoTab", "🎯 하이라이트 추가 시작")
                Log.d("MatchVideoTab", "📋 현재 쿼터: $selectedQuarter")
                Log.d("MatchVideoTab", "📋 비디오 ID: ${current.videoId}")
                Log.d("MatchVideoTab", "📋 하이라이트 제목: ${updatedHighlight.title}")
                Log.d("MatchVideoTab", "📋 시작 시간: $startTime")
                Log.d("MatchVideoTab", "📋 종료 시간: $endTime")
                
                // API 호출
                val request = HighlightAddRequest(
                    videoId = current.videoId,
                    highlightName = updatedHighlight.title,
                    startTime = startTime,
                    endTime = endTime
                )
                
                try {
                    videoViewModel.addHighlight(request, matchId)
                    // 바텀시트 닫기
                    sheetState.hide()
                    showAddSheet = false
                    Log.d("MatchVideoTab", "✅ 하이라이트 추가 요청 완료")
                    // 토스트 메시지 표시
                    Toast.makeText(context, "하이라이트가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("MatchVideoTab", "❌ 하이라이트 추가 실패", e)
                    Toast.makeText(context, "하이라이트 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else if (showEditSheet && updatedHighlight.id.isNotEmpty()) {
                Log.d("MatchVideoTab", "✏️ 하이라이트 수정 시작")
                Log.d("MatchVideoTab", "📋 하이라이트 ID: ${updatedHighlight.id}")
                Log.d("MatchVideoTab", "📋 수정된 제목: ${updatedHighlight.title}")
                Log.d("MatchVideoTab", "📋 수정된 시작 시간: $startTime")
                Log.d("MatchVideoTab", "📋 수정된 종료 시간: $endTime")
                
                // 수정 API 호출
                val request = HighlightUpdateRequest(
                    highlightId = updatedHighlight.id.toInt(),
                    highlightName = updatedHighlight.title,
                    startTime = startTime,
                    endTime = endTime
                )
                
                try {
                    videoViewModel.updateHighlight(request, matchId)
                    // 바텀시트 닫기
                    sheetState.hide()
                    showEditSheet = false
                    Log.d("MatchVideoTab", "✅ 하이라이트 수정 요청 완료")
                    // 토스트 메시지 표시
                    Toast.makeText(context, "하이라이트가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("MatchVideoTab", "❌ 하이라이트 수정 실패", e)
                    Toast.makeText(context, "하이라이트 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 바텀시트 및 다이얼로그 표시
    if (showAddSheet) {
        HighlightBottomSheet(
            title = "하이라이트 구간 추가",
            sheetState = sheetState,
            highlightState = editingHighlight,
            onStateChange = { editingHighlight = it },
            onDismiss = {
                coroutineScope.launch { sheetState.hide(); showAddSheet = false }
            },
            onConfirm = confirmAction,
            videoUri = currentData().videoUrl.let(Uri::parse)
        )
    }

    if (showEditSheet) {
        HighlightBottomSheet(
            title = "하이라이트 구간 수정",
            sheetState = sheetState,
            highlightState = editingHighlight,
            onStateChange = { editingHighlight = it },
            onDismiss = {
                coroutineScope.launch { sheetState.hide(); showEditSheet = false }
            },
            onConfirm = confirmAction,
            onDelete = {
                Log.d("MatchVideoTab", "🗑️ 하이라이트 삭제 다이얼로그 표시")
                showDeleteDialog = true
            },
            videoUri = currentData().videoUrl.let(Uri::parse),
            confirmButtonText = "저장하기"
        )
    }
    
    // 하이라이트 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("하이라이트 삭제") },
            text = { Text("정말로 삭제하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("MatchVideoTab", "🗑️ 하이라이트 삭제 시작")
                        Log.d("MatchVideoTab", "📋 하이라이트 ID: ${editingHighlight.id}")
                        
                        coroutineScope.launch {
                            videoViewModel.deleteHighlight(editingHighlight.id.toInt(), matchId)
                            sheetState.hide()
                            showEditSheet = false
                            showDeleteDialog = false
                            Toast.makeText(context, "하이라이트가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
    
    // 영상 삭제 확인 다이얼로그
    if (showDeleteVideoDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteVideoDialog = false },
            title = { Text("영상 삭제") },
            text = { Text("${selectedQuarter}의 영상을 정말로 삭제하시겠습니까? 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("MatchVideoTab", "🗑️ 쿼터 영상 삭제 시작")
                        Log.d("MatchVideoTab", "📋 영상 ID: $deleteVideoId")
                        
                        // 실제 삭제 실행
                        videoViewModel.deleteVideo(deleteVideoId, matchId)
                        showDeleteVideoDialog = false
                        Toast.makeText(context, "${selectedQuarter}의 영상이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteVideoDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchVideoTabPreview() {
    BallogTheme {
        MatchVideoTab(matchId = 29)
    }
}
