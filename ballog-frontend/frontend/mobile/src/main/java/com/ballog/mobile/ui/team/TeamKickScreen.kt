package com.ballog.mobile.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ballog.mobile.R
import com.ballog.mobile.data.model.TeamMemberModel
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.viewmodel.TeamViewModel
import kotlinx.coroutines.launch

@Composable
fun TeamKickScreen(
    navController: NavController,
    teamName: String,
    viewModel: TeamViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    var selectedMember by remember { mutableStateOf<Int?>(null) }
    val teamMembers by viewModel.teamMembers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Get team ID from teamName
    val teamId = teamName.toIntOrNull() ?: 0

    LaunchedEffect(teamId) {
        viewModel.getTeamMemberList(teamId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.Gray100)
    ) {
        TopNavItem(
            title = "멤버 강제 퇴장",
            type = TopNavType.DETAIL_WITH_BACK,
            onBackClick = onBackClick,
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "알 수 없는 오류가 발생했습니다",
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Gray.Gray500,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                    items(teamMembers) { member ->
                        TeamMemberItem(
                            member = member,
                            isSelected = member.id == selectedMember,
                            onSelect = { selectedMember = member.id }
                        )
                        Divider(color = Gray.Gray200)
                    }
                }
            }
        }

        // 저장 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            BallogButton(
                onClick = {
                    val memberId = selectedMember
                    if (memberId == null) {
                        viewModel.setError("퇴장시킬 멤버를 선택해주세요")
                        return@BallogButton
                    }
                    coroutineScope.launch {
                        val result = viewModel.deleteTeamMember(teamId, memberId)
                        if (result.isSuccess) {
                            onSuccess()
                        }
                    }
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = "저장하기",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TeamMemberItem(
    member: TeamMemberModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Gray.Gray200 else Gray.Gray100
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = member.nickname,
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Primary else Gray.Gray800
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamKickScreenPreview() {
    TeamKickScreen(
        navController = rememberNavController(),
        teamName = "1"
    )
}
