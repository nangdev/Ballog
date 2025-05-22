package com.ballog.mobile.ui.match

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.*
import com.ballog.mobile.ui.theme.*
import com.ballog.mobile.viewmodel.MatchViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

enum class MatchRegisterMode { PERSONAL, TEAM }

@Composable
fun MatchRegisterScreen(
    mode: MatchRegisterMode,
    navController: NavController,
    selectedDate: String,
    teamId: Int? = null, // teamId는 TEAM 모드에서만 사용
    viewModel: MatchViewModel = viewModel()
) {
    Log.d(
        "MatchRegisterScreen",
        "🟢 매치 등록화면 진입\nmode=$mode, selectedDate=$selectedDate, teamId=$teamId"
    )

    val isTeam = mode == MatchRegisterMode.TEAM

    // 팀일 경우 맴버 불러오기
    val players by viewModel.teamPlayers.collectAsState()
    var selectedPlayerIds by remember { mutableStateOf(setOf<Int>()) }


    LaunchedEffect(teamId) {
        if (isTeam && teamId != null) {
            viewModel.fetchTeamPlayers(teamId)
        }
    }

    var matchName by remember { mutableStateOf("") }
    val hourFocus = remember { FocusRequester() }
    val minuteFocus = remember { FocusRequester() }
    val endHourFocus = remember { FocusRequester() }
    val endMinuteFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
    var endMin by remember { mutableStateOf("") }
    var endMinute by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        TopNavItem(
            type = TopNavType.DETAIL_WITH_BACK,
            title = "매치 등록",
            onBackClick = { navController.popBackStack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "매치 이름을 정해주세요 !",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Input(
            value = matchName,
            onValueChange = { matchName = it },
            placeholder = "매치 이름",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
        )


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "매치 시작 시간을 알려주세요!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Input(
                value = hour,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        hour = it
                        if (it.length == 2) minuteFocus.requestFocus()
                    }
                },
                placeholder = "00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f).focusRequester(hourFocus)
            )
            Text(
                text = ":",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = pretendard,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Input(
                value = minute,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) minute = it
                },
                placeholder = "00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f).focusRequester(minuteFocus)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "매치 종료 시간을 알려주세요!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = pretendard,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Input(
                value = endMin,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        endMin = it
                        if (it.length == 2) endMinuteFocus.requestFocus()
                    }
                },
                placeholder = "00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f).focusRequester(endHourFocus)
            )
            Text(
                text = ":",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = pretendard,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Input(
                value = endMinute,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) endMinute = it
                },
                placeholder = "00",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f).focusRequester(endMinuteFocus)
            )
        }

        if (isTeam) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "같이 뛸 사람들을 골라주세요!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = pretendard,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .background(Gray.Gray200, RoundedCornerShape(8.dp))
            ) {
                Column {
                    players.forEachIndexed { index, player ->
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedPlayerIds.contains(player.teamMemberId),
                                    onCheckedChange = {
                                        selectedPlayerIds = if (it) {
                                            selectedPlayerIds + player.teamMemberId
                                        } else {
                                            selectedPlayerIds - player.teamMemberId
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Gray.Gray800,
                                        checkmarkColor = Primary,
                                        uncheckedColor = Gray.Gray400
                                    )
                                )
                                Text(
                                    text = player.nickname,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = pretendard
                                )
                            }

                            if (index < players.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Gray.Gray300)
                                        .padding(start = 48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BallogButton(
            onClick = {
                if (isTeam) {
                    // TODO: 팀 매치 등록 로직
                    viewModel.registerTeamMatch(
                        teamId = teamId!!,
                        date = selectedDate,
                        startTime = "${hour.padStart(2, '0')}:${minute.padStart(2, '0')}",
                        endTime = "${endMin.padStart(2, '0')}:${endMinute.padStart(2, '0')}",
                        matchName = matchName,
                        participantIds = selectedPlayerIds.toList(),
                        onSuccess = {
                            navController.popBackStack()
                        },
                        onError = { error ->
                            println("❌ 등록 실패: $error")
                        }
                    )
                } else {
                    if (
                        matchName.isBlank() ||
                        hour.isBlank() || minute.isBlank() ||
                        endMin.isBlank() || endMinute.isBlank()
                    ) {
                        println("⚠️ 입력값이 부족합니다.")
                        return@BallogButton
                    }

                    val date = selectedDate
                    val startTime = "${hour.padStart(2, '0')}:${minute.padStart(2, '0')}"
                    val endTime = "${endMin.padStart(2, '0')}:${endMinute.padStart(2, '0')}"

                    viewModel.registerMyMatch(
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        matchName = matchName,
                        onSuccess = {
                            println("✅ 매치 등록 성공")
                            navController.popBackStack()
                        },
                        onError = { error ->
                            println("❌ 매치 등록 실패: $error")
                        }
                    )
                }
            },
            type = ButtonType.LABEL_ONLY,
            buttonColor = ButtonColor.BLACK,
            label = "저장하기",
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
