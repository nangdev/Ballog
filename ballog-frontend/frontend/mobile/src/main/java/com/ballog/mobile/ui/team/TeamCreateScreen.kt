package com.ballog.mobile.ui.team

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import com.ballog.mobile.ui.theme.System

@Composable
fun TeamCreateScreen(
    teamViewModel: TeamViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var teamName by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    
    // 로고 이미지 관련 상태
    val logoImageUri by teamViewModel.logoImageUri.collectAsState()
    val logoImageUrl by teamViewModel.logoImageUrl.collectAsState()
    
    val isLoading by teamViewModel.isLoading.collectAsState()
    val error by teamViewModel.error.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // 화면 종료 시 상태 초기화
    LaunchedEffect(Unit) {
        // 화면 진입 시 상태 초기화
        teamViewModel.resetTeamCreationState()
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

    fun handleCreateTeam() {
        if (!validateForm()) return
        
        coroutineScope.launch {
            try {
                // 로딩 상태 설정
                teamViewModel.setLoading(true)
                
                val foundationDate = "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
                
                // 디버깅: 초기 이미지 URL 상태 확인
                println("TeamCreateScreen: 팀 생성 시작 - 초기 이미지 URL: $logoImageUrl")
                
                // 이미지가 있는 경우에만 업로드
                if (logoImageUri != null) {
                    println("TeamCreateScreen: 이미지 선택됨, 업로드 시작")
                    
                    // 이미지 업로드 시도
                    try {
                        val result = teamViewModel.uploadLogoImage(context, logoImageUri)
                        
                        result.fold(
                            onSuccess = { url ->
                                println("TeamCreateScreen: 이미지 업로드 성공, URL: $url")
                                
                                // 팀 생성 API 호출
                                val safeUrl = url.trim()
                                println("TeamCreateScreen: 이미지 업로드 성공, 팀 생성 진행 with URL: $safeUrl")
                                
                                val teamResult = teamViewModel.addTeam(
                                    teamName = teamName,
                                    logoImageUrl = safeUrl,
                                    foundationDate = foundationDate
                                )
                                teamResult.fold(
                                    onSuccess = {
                                        println("TeamCreateScreen: 팀 생성 성공")
                                        onNavigateBack()
                                    },
                                    onFailure = { e ->
                                        println("TeamCreateScreen: 팀 생성 실패: ${e.message}")
                                        teamViewModel.setError("팀 생성에 실패했습니다: ${e.message}")
                                    }
                                )
                            },
                            onFailure = { e ->
                                println("TeamCreateScreen: 이미지 업로드 실패: ${e.message}")
                                teamViewModel.setError("로고 이미지 업로드에 실패했습니다. 다시 시도해주세요. 오류: ${e.message}")
                            }
                        )
                    } catch (e: Exception) {
                        println("TeamCreateScreen: 이미지 업로드 예외: ${e.message}")
                        teamViewModel.setError("로고 이미지 업로드에 실패했습니다. 다시 시도해주세요.")
                    }
                } else {
                    // 이미지가 없는 경우 바로 팀 생성
                    val teamResult = teamViewModel.addTeam(
                        teamName = teamName,
                        logoImageUrl = "",
                        foundationDate = foundationDate
                    )
                    teamResult.fold(
                        onSuccess = {
                            println("TeamCreateScreen: 팀 생성 성공")
                            onNavigateBack()
                        },
                        onFailure = { e ->
                            println("TeamCreateScreen: 팀 생성 실패: ${e.message}")
                            teamViewModel.setError("팀 생성에 실패했습니다: ${e.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                println("TeamCreateScreen: 전체 프로세스 예외: ${e.message}")
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
            title = "팀 생성",
            onBackClick = handleNavigateBack,
            onActionClick = handleClose
        )

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

            Spacer(modifier = Modifier.weight(1f))

            // Error Message
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = System.Red,
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Create Team Button
            BallogButton(
                onClick = { handleCreateTeam() },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = "팀 생성하기",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(
    name = "팀 생성 화면",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun TeamCreateScreenPreview() {
    TeamCreateScreen()
}
