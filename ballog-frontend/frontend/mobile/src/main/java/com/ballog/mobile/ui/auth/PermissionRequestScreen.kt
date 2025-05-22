package com.ballog.mobile.ui.auth

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.pretendard
import androidx.compose.ui.platform.LocalContext
import com.ballog.mobile.data.service.SamsungHealthPermissionManager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.ballog.mobile.data.util.OnboardingPrefs
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.ballog.mobile.data.service.SamsungHealthDataService
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.permission.Permission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

private const val TAG = "MatchDetailScreen"

@Composable
fun PermissionRequestScreen(
    onConnectClick: () -> Unit = {},
) {

    val context = LocalContext.current
    var healthDataStore by remember { mutableStateOf<HealthDataStore?>(null) }
    var samsungHealthDataService by remember { mutableStateOf<SamsungHealthDataService?>(null) }
    var isHealthDataLoading by remember { mutableStateOf(false) }
    var healthDataError by remember { mutableStateOf<String?>(null) }
    //var requestPermissionTrigger by remember { mutableStateOf(false) }
    Log.d("PermissionRequestScreen", "Composable context type: ${context::class.java.name}")
    val scope = rememberCoroutineScope()

    // 삼성 헬스 초기화 및 권한 요청/데이터 로드까지 한 번에 처리
    LaunchedEffect(Unit) {
        try {
            val healthDataStore = HealthDataService.getStore(context)
            val samsungHealthDataService = SamsungHealthDataService(context)
            isHealthDataLoading = true
            try {
                val permissions = setOf(
                    Permission.of(DataTypes.EXERCISE, AccessType.READ),
                    Permission.of(DataTypes.HEART_RATE, AccessType.READ),
                    Permission.of(DataTypes.EXERCISE_LOCATION, AccessType.READ)
                )
                val grantedPermissions = healthDataStore.requestPermissions(
                    permissions,
                    context as Activity
                )
                if (grantedPermissions?.containsAll(permissions) == true) {
                    OnboardingPrefs.setPermissionCompleted(context, true)
                    val exerciseData = samsungHealthDataService.getExercise()
                    Log.d(TAG, "운동 데이터 로딩 완료: ${exerciseData?.size ?: 0}개")
                } else {
                    healthDataError = "필요한 권한이 부여되지 않았습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, "데이터 로딩 실패: ${e.message}")
                healthDataError = "데이터 로딩 실패: ${e.message}"
            } finally {
                isHealthDataLoading = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Samsung Health 초기화 실패: ${e.message}")
            healthDataError = "삼성 헬스 초기화 실패: ${e.message}"
        }
    }

//    LaunchedEffect(requestPermissionTrigger) {
//        if (requestPermissionTrigger) {
//            val activity = context as? android.app.Activity
//            if (activity == null) {
//                Log.e("PermissionRequestScreen", "context is not Activity!")
//                android.widget.Toast.makeText(context, "권한 요청 실패: Activity 컨텍스트가 아님", android.widget.Toast.LENGTH_SHORT).show()
//            } else {
//                try {
//                    SamsungHealthPermissionManager(context).initialize()
//                        .onSuccess { healthDataStore ->
//                            Log.d("PermissionRequestScreen", "healthDataStore initialized: $healthDataStore")
//                            SamsungHealthPermissionManager(context).requestPermissions()
//                                .onSuccess { grantedPermissions ->
//                                    Log.d("PermissionRequestScreen", "grantedPermissions: $grantedPermissions")
//                                }
//                                .onFailure { e ->
//                                    Log.e("PermissionRequestScreen", "권한 요청 실패: ${e.message}")
//                                }
//                        }
//                        .onFailure { e ->
//                            Log.e("PermissionRequestScreen", "Samsung Health 초기화 실패: ${e.message}")
//                        }
//                } catch (e: Exception) {
//                    Log.e("PermissionRequestScreen", "권한 요청 예외: ${e.message}")
//                }
//            }
//            requestPermissionTrigger = false
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        // 중앙 컨텐츠: 아이콘, 이미지, 텍스트를 위아래로 배치
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 이미지는 한 줄(Row)로 배치
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ballog_logo_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_link),
                    contentDescription = "ic_link",
                    modifier = Modifier.size(30.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        Gray.Gray700.copy(alpha = 0.4f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.samsung_health_72x72),
                    contentDescription = "Samsung Health",
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(140.dp))
            // 텍스트는 세로(Column)로 배치
            Text(
                text = "Samsung Health 연결",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = pretendard,
                color = Primary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "볼로그는 삼성헬스와 함께합니다",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = pretendard,
                color = Gray.Gray700,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
        // 하단 컨텐츠: 텍스트, 점, 버튼
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BallogButton(
                onClick = {
                    OnboardingPrefs.setPermissionCompleted(context, true)
                    onConnectClick()
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = "다음",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionRequestScreenPreview() {
    PermissionRequestScreen()
}
