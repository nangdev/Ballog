package com.ballog.mobile.ui.video

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.components.*
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightBottomSheet(
    title: String,
    sheetState: SheetState,
    highlightState: HighlightUiState,
    onStateChange: (HighlightUiState) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: (() -> Unit)? = null,
    videoUri: Uri?,
    confirmButtonText: String = "저장하기"
) {
    val context = LocalContext.current

    BallogBottomSheet(
        title = title,
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // 시스템 UI를 위한 하단 패딩 추가
        ) {
            HighlightForm(
                state = highlightState,
                onStateChange = onStateChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            val confirmAction = {
                // 시간 형식 정규화 및 유효성 검사
                val normalizedHighlight = normalizeTimeFormat(highlightState)
                val (isValid, message) = validateTimeRange(normalizedHighlight)

                if (isValid) {
                    onStateChange(normalizedHighlight)
                    onConfirm()
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }

            if (onDelete == null) {
                // 추가 모드
                BallogButton(
                    onClick = confirmAction,
                    label = confirmButtonText,
                    buttonColor = ButtonColor.BLACK,
                    type = ButtonType.LABEL_ONLY,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // 수정 모드
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BallogButton(
                        onClick = confirmAction,
                        label = confirmButtonText,
                        buttonColor = ButtonColor.BLACK,
                        type = ButtonType.LABEL_ONLY,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BallogButton(
                        onClick = onDelete,
                        icon = painterResource(id = R.drawable.ic_trash),
                        buttonColor = ButtonColor.ALERT,
                        type = ButtonType.ICON_ONLY,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

// 타임스탬프 형식 정규화 함수
private fun normalizeTimeFormat(state: HighlightUiState): HighlightUiState {
    // 시작 시간 정규화
    val (normalizedStartMin, normalizedStartSec) = normalizeTime(
        state.startMin, 
        state.startSec
    )
    
    // 종료 시간 정규화
    val (normalizedEndMin, normalizedEndSec) = normalizeTime(
        state.endMin, 
        state.endSec
    )
    
    return state.copy(
        startMin = normalizedStartMin,
        startSec = normalizedStartSec,
        endMin = normalizedEndMin,
        endSec = normalizedEndSec
    )
}

// MM:SS 또는 개별 분/초 입력을 정규화
private fun normalizeTime(minutes: String, seconds: String): Pair<String, String> {
    if (minutes.contains(":")) {
        // MM:SS 형식으로 저장된 경우
        val parts = minutes.split(":")
        val min = parts.getOrNull(0)?.padStart(2, '0') ?: "00"
        val sec = parts.getOrNull(1)?.padStart(2, '0') ?: "00"
        return min to sec
    }
    
    // 개별 필드로 저장된 경우
    val min = minutes.ifEmpty { "0" }.padStart(2, '0')
    val sec = seconds.ifEmpty { "0" }.padStart(2, '0')
    return min to sec
}

// 시간 범위 유효성 검사
private fun validateTimeRange(state: HighlightUiState): Pair<Boolean, String> {
    try {
        val startMinutes = state.startMin.toInt()
        val startSeconds = state.startSec.toInt()
        val endMinutes = state.endMin.toInt()
        val endSeconds = state.endSec.toInt()
        
        val startTotalSeconds = startMinutes * 60 + startSeconds
        val endTotalSeconds = endMinutes * 60 + endSeconds
        
        return if (startTotalSeconds < endTotalSeconds) {
            true to ""
        } else {
            false to "시작 시간은 종료 시간보다 작아야 합니다."
        }
    } catch (e: Exception) {
        return false to "시간 형식이 올바르지 않습니다."
    }
}

@Composable
fun HighlightForm(
    state: HighlightUiState,
    onStateChange: (HighlightUiState) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        // 제목 입력
        FormFieldWithLabel("하이라이트 제목") {
            Input(
                value = state.title,
                onValueChange = { onStateChange(state.copy(title = it)) },
                placeholder = "하이라이트 구간의 제목을 입력해주세요."
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 시작 시간 추출
        val (startMinValue, startSecValue) = extractTimeValues(state.startMin, state.startSec)
        
        // 시작 시간 입력
        FormFieldWithLabel("시작 지점") {
            TimeInputFields(
                min = startMinValue,
                onMinChange = { 
                    updateTimeState(state, onStateChange, isStart = true, isMin = true, it)
                },
                sec = startSecValue,
                onSecChange = { 
                    updateTimeState(state, onStateChange, isStart = true, isMin = false, it)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 종료 시간 추출
        val (endMinValue, endSecValue) = extractTimeValues(state.endMin, state.endSec)
        
        // 종료 시간 입력
        FormFieldWithLabel("종료 지점") {
            TimeInputFields(
                min = endMinValue,
                onMinChange = { 
                    updateTimeState(state, onStateChange, isStart = false, isMin = true, it)
                },
                sec = endSecValue,
                onSecChange = { 
                    updateTimeState(state, onStateChange, isStart = false, isMin = false, it)
                }
            )
        }
    }
}

// 시간 값 추출 함수
private fun extractTimeValues(minutes: String, seconds: String): Pair<String, String> {
    return if (minutes.contains(":")) {
        // MM:SS 형식으로 저장된 경우
        val parts = minutes.split(":")
        val min = parts.getOrNull(0) ?: ""
        val sec = parts.getOrNull(1) ?: ""
        min to sec
    } else {
        // 개별 필드로 저장된 경우
        minutes to seconds
    }
}

// 시간 상태 업데이트 함수
private fun updateTimeState(
    state: HighlightUiState,
    onStateChange: (HighlightUiState) -> Unit,
    isStart: Boolean,
    isMin: Boolean,
    value: String
) {
    if (isStart) {
        // 시작 시간 업데이트
        if (state.startMin.contains(":")) {
            // MM:SS 형식일 경우
            val parts = state.startMin.split(":")
            val min = if (isMin) value else parts.getOrNull(0) ?: ""
            val sec = if (!isMin) value else parts.getOrNull(1) ?: ""
            onStateChange(state.copy(startMin = "$min:$sec"))
        } else {
            // 개별 필드일 경우
            if (isMin) {
                onStateChange(state.copy(startMin = value))
            } else {
                onStateChange(state.copy(startSec = value))
            }
        }
    } else {
        // 종료 시간 업데이트
        if (state.endMin.contains(":")) {
            // MM:SS 형식일 경우
            val parts = state.endMin.split(":")
            val min = if (isMin) value else parts.getOrNull(0) ?: ""
            val sec = if (!isMin) value else parts.getOrNull(1) ?: ""
            onStateChange(state.copy(endMin = "$min:$sec"))
        } else {
            // 개별 필드일 경우
            if (isMin) {
                onStateChange(state.copy(endMin = value))
            } else {
                onStateChange(state.copy(endSec = value))
            }
        }
    }
}

@Composable
private fun FormFieldWithLabel(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun TimeInputFields(
    min: String,
    onMinChange: (String) -> Unit,
    sec: String,
    onSecChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 분(minutes) 입력 필드
        Input(
            value = min,
            onValueChange = { newValue -> 
                if (newValue.length <= 2 && newValue.all { c -> c.isDigit() }) {
                    onMinChange(newValue)
                }
            },
            placeholder = "00",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f)
        )
        Text(
            ":",
            fontFamily = pretendard,
            color = Gray.Gray500,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
        // 초(seconds) 입력 필드
        Input(
            value = sec,
            onValueChange = { newValue -> 
                if (newValue.length <= 2 && newValue.all { c -> c.isDigit() }) {
                    onSecChange(newValue)
                }
            },
            placeholder = "00",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f)
        )
    }
}
