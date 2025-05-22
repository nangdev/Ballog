package com.ballog.mobile.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ballog.mobile.R
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.*
import com.ballog.mobile.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ballog.mobile.viewmodel.UserViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun ProfileEditScreen(navController: NavController, rootNavController: NavHostController, viewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // ViewModel StateFlow 바인딩
    val userInfo by viewModel.userInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()

    // 입력값 상태
    var nickname by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }

    // 최초 진입 시 유저 정보 불러오기
    LaunchedEffect(Unit) {
        viewModel.getUserInfo()
    }
    // userInfo가 갱신되면 입력값도 갱신
    LaunchedEffect(userInfo) {
        userInfo?.let {
            nickname = it.nickname
            if (it.birthDate.contains("-")) {
                // yyyy-MM-dd 형식
                val parts = it.birthDate.split("-")
                if (parts.size == 3) {
                    birthYear = parts[0]
                    birthMonth = parts[1]
                    birthDay = parts[2]
                }
            } else if (it.birthDate.length == 8) {
                // yyyyMMdd 형식
                birthYear = it.birthDate.substring(0, 4)
                birthMonth = it.birthDate.substring(4, 6)
                birthDay = it.birthDate.substring(6, 8)
            }
        }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastUserInfo by remember { mutableStateOf<com.ballog.mobile.data.model.User?>(null) }
    var isUpdateRequested by remember { mutableStateOf(false) }

    // userInfo가 성공적으로 갱신될 때 AlertDialog 표시
    LaunchedEffect(userInfo, isLoading, error) {
        if (isUpdateRequested && !isLoading && error == null && userInfo != null && userInfo != lastUserInfo) {
            showSuccessDialog = true
            lastUserInfo = userInfo
            isUpdateRequested = false
        }
    }

    Scaffold(
        // snackbarHost = { SnackbarHost(snackbarHostState) }, // 완전히 삭제
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray.Gray100)
                .padding(innerPadding)
        ) {
            TopNavItem(
                title = "정보 수정",
                type = TopNavType.DETAIL_WITH_BACK,
                onBackClick = { navController.popBackStack() },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 프로필 이미지
                Box(
                    modifier = Modifier
                        .size(146.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F5F8))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        profileImageUri != null -> {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "프로필 이미지",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        !userInfo?.profileImageUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = userInfo?.profileImageUrl,
                                contentDescription = "프로필 이미지",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "프로필 선택",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 닉네임 라벨
                Text(
                    text = "닉네임",
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                Input(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "닉네임",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 생년월일 라벨
                Text(
                    text = "생년월일",
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Input(
                        value = birthYear,
                        onValueChange = { birthYear = it },
                        placeholder = "년",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    Input(
                        value = birthMonth,
                        onValueChange = { birthMonth = it },
                        placeholder = "월",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    Input(
                        value = birthDay,
                        onValueChange = { birthDay = it },
                        placeholder = "일",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                BallogButton(
                    onClick = {
                        isUpdateRequested = true
                        val birthDate = "$birthYear-$birthMonth-$birthDay"
                        if (profileImageUri != null) {
                            // S3 업로드 후 성공 시 updateUserInfo 호출
                            viewModel.uploadProfileImage(context, profileImageUri!!) { imageUrl ->
                                viewModel.updateUserInfo(nickname, birthDate, imageUrl)
                            }
                        } else {
                            // 기존 이미지 URL 사용
                            val imageUrl = userInfo?.profileImageUrl ?: ""
                            viewModel.updateUserInfo(nickname, birthDate, imageUrl)
                        }
                    },
                    type = ButtonType.LABEL_ONLY,
                    buttonColor = ButtonColor.BLACK,
                    label = if (isLoading || imageUploadState is com.ballog.mobile.viewmodel.ImageUploadState.Loading) "저장 중..." else "저장",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    enabled = !isLoading && imageUploadState !is com.ballog.mobile.viewmodel.ImageUploadState.Loading
                )

                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (imageUploadState is com.ballog.mobile.viewmodel.ImageUploadState.Error) {
                    Text(
                        text = (imageUploadState as com.ballog.mobile.viewmodel.ImageUploadState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        // 성공 다이얼로그
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                confirmButton = {
                    TextButton(onClick = { showSuccessDialog = false }) {
                        Text("확인")
                    }
                },
                title = { Text("수정 완료") },
                text = { Text("수정이 완료되었습니다") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen(navController = rememberNavController(), rootNavController = rememberNavController())
}
