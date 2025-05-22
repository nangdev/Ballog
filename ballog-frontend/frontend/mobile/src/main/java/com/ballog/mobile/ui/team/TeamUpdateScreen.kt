package com.ballog.mobile.ui.team

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.TeamViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Dialog
import java.time.LocalDate

@Composable
fun TeamUpdateScreen(
    teamId: Int,
    teamViewModel: TeamViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var teamName by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var showManagerOnlyModal by remember { mutableStateOf(false) }

    // 로고 이미지 관련 상태
    val logoImageUri by teamViewModel.logoImageUri.collectAsState()
    val logoImageUrl by teamViewModel.logoImageUrl.collectAsState()
    val teamDetail by teamViewModel.teamDetail.collectAsState()

    val isLoading by teamViewModel.isLoading.collectAsState()
    val error by teamViewModel.error.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 팀 상세 정보 로드
    LaunchedEffect(teamId) {
        teamViewModel.getTeamDetail(teamId)
    }

    // 팀 상세 정보가 로드되면 초기값 설정
    LaunchedEffect(teamDetail) {
        teamDetail?.let { detail ->
            teamName = detail.name
            detail.foundationDate?.let { date ->
                val localDate = LocalDate.parse(date)
                year = localDate.year.toString()
                month = localDate.monthValue.toString()
                day = localDate.dayOfMonth.toString()
            }
            detail.logoImageUrl?.let { url ->
                teamViewModel.setLogoImageUrl(url)
            }
        }
    }

    // 화면 나가기/닫기 함수
    val handleNavigateBack = {
        teamViewModel.resetTeamCreationState()
        onNavigateBack()
    }

    val handleClose = {
        teamViewModel.resetTeamCreationState()
        onClose()
    }

    // 갤러리에서 이미지 선택을 위한 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            teamViewModel.setLogoImageUri(it)
        }
    }

    fun validateForm(): Boolean {
        if (teamName.isBlank()) {
            teamViewModel.setError("팀명을 입력해주세요")
            return false
        }
        if (year.isBlank() || month.isBlank() || day.isBlank()) {
            teamViewModel.setError("창단일자를 모두 입력해주세요")
            return false
        }
        return true
    }

    fun handleUpdateTeam() {
        if (!validateForm()) return

        coroutineScope.launch {
            try {
                // 로딩 상태 설정
                teamViewModel.setLoading(true)

                val foundationDate = "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"

                // 이미지가 변경된 경우에만 업로드
                val finalLogoImageUrl = if (logoImageUri != null) {
                    println("TeamUpdateScreen: 이미지 선택됨, 업로드 시작")
                    val uploadResult = teamViewModel.uploadLogoImage(context, logoImageUri)
                    uploadResult.fold(
                        onSuccess = { url ->
                            println("TeamUpdateScreen: 이미지 업로드 성공, URL: $url")
                            url
                        },
                        onFailure = { e ->
                            println("TeamUpdateScreen: 이미지 업로드 실패: ${e.message}")
                            teamViewModel.setError("로고 이미지 업로드에 실패했습니다: ${e.message}")
                            return@launch
                        }
                    )
                } else {
                    logoImageUrl ?: run {
                        teamViewModel.setError("로고 이미지가 필요합니다")
                        return@launch
                    }
                }

                // 팀 정보 업데이트
                println("TeamUpdateScreen: 팀 정보 업데이트 시작 - 팀ID: $teamId, 팀명: $teamName, 로고URL: $finalLogoImageUrl, 창단일: $foundationDate")
                val updateResult = teamViewModel.updateTeamInfo(teamId, teamName, finalLogoImageUrl, foundationDate)
                updateResult.fold(
                    onSuccess = {
                        println("TeamUpdateScreen: 팀 정보 업데이트 성공")
                        onNavigateBack()
                    },
                    onFailure = { e ->
                        println("TeamUpdateScreen: 팀 정보 업데이트 실패: ${e.message}")
                        if (e.message?.contains("500") == true) {
                            showManagerOnlyModal = true
                        } else {
                            teamViewModel.setError("팀 정보 업데이트에 실패했습니다: ${e.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                println("TeamUpdateScreen: 전체 프로세스 예외: ${e.message}")
                teamViewModel.setError("처리 중 오류가 발생했습니다: ${e.message}")
            } finally {
                teamViewModel.setLoading(false)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top Navigation Bar
        TopNavItem(
            type = TopNavType.DETAIL_WITH_BACK,
            title = "팀 정보 수정",
            onBackClick = handleNavigateBack,
            onActionClick = handleClose
        )

        // Error Message
        if (error != null) {
            Text(
                text = error ?: "",
                color = Primary,
                fontSize = 12.sp,
                fontFamily = pretendard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            // Logo Upload Area
            Box(
                modifier = Modifier
                    .size(146.dp)
                    .background(Gray.Gray200, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isLoading) { galleryLauncher.launch("image/*") }
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Gray.Gray500,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (logoImageUri != null) {
                    // 선택된 이미지 표시
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = logoImageUri,
                            contentDescription = "Team Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // 닫기 버튼 (오른쪽 위)
                        IconButton(
                            onClick = { teamViewModel.setLogoImageUri(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Remove Image",
                                tint = Gray.Gray800,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (logoImageUrl != null) {
                    // 기존 이미지 표시
                    AsyncImage(
                        model = logoImageUrl,
                        contentDescription = "Team Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 기본 추가 UI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Add logo",
                            tint = Gray.Gray500
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "로고 이미지",
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray.Gray500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Team Name Input
            Text(
                text = "팀명",
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = Gray.Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Gray.Gray200, RoundedCornerShape(8.dp))
            ) {
                TextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    placeholder = {
                        Text(
                            text = "팀명",
                            fontFamily = pretendard,
                            color = Gray.Gray500,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Gray.Gray200,
                        focusedContainerColor = Gray.Gray200,
                        unfocusedIndicatorColor = Gray.Gray200,
                        focusedIndicatorColor = Gray.Gray200
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Foundation Date Input
            Text(
                text = "창단일자",
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = Gray.Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Year Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .background(Gray.Gray200, RoundedCornerShape(8.dp))
                ) {
                    TextField(
                        value = year,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                if (newValue.length <= 4) {
                                    val yearNum = newValue.toIntOrNull() ?: 0
                                    if (yearNum <= 9999) {
                                        year = newValue
                                    }
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = "년",
                                fontFamily = pretendard,
                                color = Gray.Gray500,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Gray.Gray200,
                            focusedContainerColor = Gray.Gray200,
                            unfocusedIndicatorColor = Gray.Gray200,
                            focusedIndicatorColor = Gray.Gray200
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Month Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .background(Gray.Gray200, RoundedCornerShape(8.dp))
                ) {
                    TextField(
                        value = month,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                if (newValue.length <= 2) {
                                    val monthNum = newValue.toIntOrNull() ?: 0
                                    if (monthNum <= 12) {
                                        month = newValue
                                    }
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = "월",
                                fontFamily = pretendard,
                                color = Gray.Gray500,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Gray.Gray200,
                            focusedContainerColor = Gray.Gray200,
                            unfocusedIndicatorColor = Gray.Gray200,
                            focusedIndicatorColor = Gray.Gray200
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Day Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .background(Gray.Gray200, RoundedCornerShape(8.dp))
                ) {
                    TextField(
                        value = day,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                if (newValue.length <= 2) {
                                    val dayNum = newValue.toIntOrNull() ?: 0
                                    if (dayNum <= 31) {
                                        day = newValue
                                    }
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = "일",
                                fontFamily = pretendard,
                                color = Gray.Gray500,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Gray.Gray200,
                            focusedContainerColor = Gray.Gray200,
                            unfocusedIndicatorColor = Gray.Gray200,
                            focusedIndicatorColor = Gray.Gray200
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        // Save Button - Positioned at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            BallogButton(
                onClick = { handleUpdateTeam() },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.PRIMARY,
                label = "팀 수정하기",
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 매니저 권한 모달
    if (showManagerOnlyModal) {
        Dialog(onDismissRequest = { showManagerOnlyModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = Gray.Gray100
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "권한이 없습니다",
                        fontSize = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Bold,
                        color = Gray.Gray800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "팀 정보 수정은 매니저만 가능합니다",
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        color = Gray.Gray500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    BallogButton(
                        onClick = { showManagerOnlyModal = false },
                        type = ButtonType.LABEL_ONLY,
                        buttonColor = ButtonColor.BLACK,
                        label = "확인",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamUpdateScreenPreview() {
    TeamUpdateScreen(teamId = 1)
}
