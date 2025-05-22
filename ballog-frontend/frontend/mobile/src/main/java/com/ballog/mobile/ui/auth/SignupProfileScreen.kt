package com.ballog.mobile.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.Surface
import com.ballog.mobile.viewmodel.AuthViewModel
import com.ballog.mobile.data.model.SignUpProgress
import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.ballog.mobile.data.model.AuthResult
import com.ballog.mobile.data.model.ImageUploadState
import com.ballog.mobile.ui.theme.System
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun SignupProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val signUpProgress by viewModel.signUpProgress.collectAsState()
    println("ProfileScreen - Current progress: $signUpProgress")

    // 현재 진행 상태 확인
    LaunchedEffect(signUpProgress) {
        println("ProfileScreen - Progress changed to: $signUpProgress")
        if (signUpProgress != SignUpProgress.PROFILE && signUpProgress != SignUpProgress.COMPLETED) {
            println("ProfileScreen - Invalid progress state, popping back")
            navController.popBackStack()
        }
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val signUpData by viewModel.signUpData.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()

    // 이미지 업로드 상태 관찰
    LaunchedEffect(imageUploadState) {
        when (imageUploadState) {
            is ImageUploadState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is ImageUploadState.Success -> {
                isLoading = false
                errorMessage = null
                // 업로드된 이미지 URL이 ViewModel에 자동으로 설정됨
            }
            is ImageUploadState.Error -> {
                isLoading = false
                errorMessage = (imageUploadState as ImageUploadState.Error).message
            }
            else -> {}
        }
    }

    LaunchedEffect(signUpData) {
        // 프로필 이미지 URL이 이미 있다면 그것을 표시
        if (signUpData.profileImageUrl != null && signUpData.profileImageUrl!!.startsWith("http")) {
            // URL이 이미 있는 경우 (이전에 설정된 프로필 이미지)
            selectedImageUri = null // 로컬 URI는 없음
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // 이미지 선택만 하고 업로드는 하지 않음
            errorMessage = null // 이미지 선택 시 에러 메시지 초기화
        }
    }

    // 회원가입 상태 관찰
    val signUpState by viewModel.signUpState.collectAsState()
    LaunchedEffect(signUpState) {
        when (signUpState) {
            is AuthResult.Success -> {
                println("ProfileScreen - SignUp successful, navigating to login")
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SIGNUP_PROFILE_IMAGE) { inclusive = true }
                }
            }
            is AuthResult.Error -> {
                println("ProfileScreen - SignUp failed: ${(signUpState as AuthResult.Error).message}")
                isLoading = false
                errorMessage = (signUpState as AuthResult.Error).message
            }
            else -> {}
        }
    }

    // 회원가입 진행 함수 수정 - 이미지가 선택된 경우 S3 업로드 후 회원가입 진행
    fun attemptSignUp(isSkip: Boolean = false) {
        // 이미 업로드 중이면 중복 실행 방지
        if (isLoading) return
        
        // 건너뛰기가 아니고 이미지가 없는 경우 에러 표시
        if (!isSkip && selectedImageUri == null && signUpData.profileImageUrl == null) {
            errorMessage = "프로필 이미지를 선택해주세요"
            return
        }
        
        coroutineScope.launch {
            errorMessage = null
            isLoading = true
            
            try {
                // 1. 이미지가 선택되었고, 아직 업로드되지 않았으면 S3에 업로드
                if (selectedImageUri != null && signUpData.profileImageUrl == null) {
                    // 이미지 업로드 시작
                    viewModel.uploadProfileImage(context, selectedImageUri)
                    
                    // 업로드 결과 기다리기
                    var uploadCompleted = false
                    var retryCount = 0
                    
                    while (!uploadCompleted && retryCount < 10) {
                        delay(500) // 0.5초마다 확인
                        
                        when (val state = imageUploadState) {
                            is ImageUploadState.Success -> {
                                uploadCompleted = true
                                // 업로드 성공 시 계속 진행
                            }
                            is ImageUploadState.Error -> {
                                // 업로드 실패 시 에러 표시하고 종료
                                errorMessage = state.message
                                isLoading = false
                                return@launch
                            }
                            else -> {
                                // 아직 진행 중이면 대기
                                retryCount++
                            }
                        }
                    }
                    
                    // 타임아웃 체크
                    if (!uploadCompleted) {
                        errorMessage = "이미지 업로드 시간이 초과되었습니다. 다시 시도해주세요."
                        isLoading = false
                        return@launch
                    }
                }
                
                // 2. 회원가입 API 호출
                println("ProfileScreen - Starting signup process" + if (isSkip) " (skip)" else "")
                viewModel.signUp()
                
            } catch (e: Exception) {
                errorMessage = "오류가 발생했습니다: ${e.message}"
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "4/4",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 19.09.sp,
                    color = Gray.Gray800
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "프로필 이미지를 선택해주세요",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 23.87.sp,
                    color = Gray.Gray700
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "멋진 선수 이미지로 바꿔드릴게요!",
                style = TextStyle(
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 19.09.sp,
                    color = Gray.Gray700
                )
            )
            Spacer(modifier = Modifier.height(64.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(0.7f)
                    .heightIn(max = 160.dp)
                    .shadow(12.dp, RoundedCornerShape(26.3.dp), clip = false)
                    .border(2.dp, Color(0xFF9BA0A5), RoundedCornerShape(26.3.dp))
                    .clip(RoundedCornerShape(26.3.dp))
                    .background(Surface)
            ) {
                // 전체 배경 이미지 제거
                
                Column(modifier = Modifier.fillMaxSize()) {
                    // 상단 이미지 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 26.3.dp, topEnd = 26.3.dp))
                            .background(Gray.Gray600),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 선택한 이미지가 있으면 상단 영역 전체를 채우도록 표시
                        if (selectedImageUri != null || signUpData.profileImageUrl != null) {
                            // 로컬 URI 또는 서버 URL 처리
                            val imageSource = selectedImageUri?.toString() ?: signUpData.profileImageUrl
                            
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(data = imageSource)
                                        .build()
                                ),
                                contentDescription = "프로필 이미지",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // 이미지 삭제 버튼 (오른쪽 상단에 위치)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable {
                                        // 이미지 선택 취소
                                        selectedImageUri = null
                                        viewModel.setSignUpProfileImageUrl(null)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "이미지 삭제",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (selectedImageUri == null && signUpData.profileImageUrl == null) {
                                Image(
                                    painter = painterResource(id = R.drawable.defaultimage),
                                    contentDescription = "프로필 이미지",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                )
                                
                                // 업로드 버튼을 이미지 중앙에 겹치게 (이미지가 없을 때만 표시)
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(36.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B1B1D))
                                        .clickable {
                                            imagePicker.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_camera),
                                            contentDescription = "이미지 업로드",
                                            modifier = Modifier.size(20.dp),
                                            colorFilter = ColorFilter.tint(Primary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "이미지 업로드",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Primary,
                                            fontFamily = pretendard
                                        )
                                    }
                                }
                            } else {
                                // 이미지 있을 때는 사용자가 이미지를 탭하면 재선택할 수 있도록 함
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            imagePicker.launch("image/*")
                                        }
                                )
                            }
                        }
                    }
                    // 이름 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(Gray.Gray500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BALLOG",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            fontFamily = pretendard
                        )
                    }
                    // 하단 능력치
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(bottomStart = 26.3.dp, bottomEnd = 26.3.dp))
                            .background(Gray.Gray600)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatBarCard(label = "Speed", value = 78)
                        StatBarCard(label = "Stamina", value = 80)
                        StatBarCard(label = "Attack", value = 64)
                        StatBarCard(label = "Defense", value = 80)
                        StatBarCard(label = "Recovery", value = 76)
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            // 에러 메시지 표시
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = System.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = pretendard
                    )
                }
            }

            BallogButton(
                onClick = {
                    attemptSignUp(isSkip = true) // 건너뛰기 허용
                },
                type = ButtonType.BOTH,
                buttonColor = ButtonColor.GRAY,
                icon = painterResource(id = R.drawable.ic_trash),
                label = if (isLoading) "처리 중..." else "건너뛰기",
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            BallogButton(
                onClick = {
                    attemptSignUp(isSkip = false) // 이미지 필수
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = if (isLoading) "처리 중..." else "볼로그 시작하기",
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )
        }
    }
}

@Composable
private fun StatBarCard(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Surface,
            fontFamily = pretendard,
            modifier = Modifier.width(70.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Gray.Gray500)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Primary)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.toString(),
            fontSize = 16.8.sp,
            fontWeight = FontWeight.Bold,
            color = Surface,
            fontFamily = pretendard
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignupProfileScreenPreview() {
    SignupProfileScreen(navController = rememberNavController(), viewModel = AuthViewModel())
}
