package com.ballog.mobile.ui.match

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.ballog.mobile.ui.theme.pretendard
import com.ballog.mobile.navigation.TopNavItem
import com.ballog.mobile.navigation.TopNavType
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import com.ballog.mobile.ui.components.Input
import com.ballog.mobile.ui.components.BallogButton
import com.ballog.mobile.ui.components.ButtonType
import com.ballog.mobile.ui.components.ButtonColor
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import com.ballog.mobile.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ballog.mobile.viewmodel.MatchViewModel
import com.ballog.mobile.viewmodel.MatchUiState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.ballog.mobile.data.model.MatchDataCardInfo
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.ballog.mobile.data.service.MatchReportService
import com.ballog.mobile.ui.theme.System
import kotlinx.coroutines.launch
import com.ballog.mobile.ui.components.NavigationTab
import com.ballog.mobile.data.dto.MatchItemDto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import com.ballog.mobile.data.repository.IdStore
import kotlinx.coroutines.delay

@Composable
fun MatchDataScreen(
    navController: NavHostController,
    matchTabNavController: NavHostController,
    setSelectedTab: (NavigationTab) -> Unit,
    viewModel: MatchViewModel = viewModel(),
    matchReportService: MatchReportService,
    setPendingMatchDetail: (Pair<Int, String>?) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var matchData by remember { mutableStateOf<List<MatchDataCardInfo>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val quarterReportList by matchReportService.quarterReportList.collectAsState()
    val selectedQuarterList by matchReportService.selectedQuarterList.collectAsState()
    val selectedMatchId by matchReportService.selectedMatchId.collectAsState()
    val dayMatchesList by matchReportService.dayMatchesList.collectAsState()
    val fieldCorners by matchReportService.fieldCorners.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val idStore = remember { IdStore(context) }
    var stateFlag = remember { mutableStateOf(false)}

    // 최초 진입 시 SharedPreferences 값만 확인
    LaunchedEffect(Unit) {
        Log.d("MatchDataScreen", "uiState: $uiState")
        viewModel.checkFieldCorners()
        println(quarterReportList)
    }

    LaunchedEffect(fieldCorners) {
        if(fieldCorners.isEmpty())
        else
            viewModel.setLoading()
    }

    // MatchUiState가 Loading일 때만 데이터 로드
    LaunchedEffect(uiState) {
        var handled = false
        if (uiState is MatchUiState.Loading) {
            scope.launch {
                try {
                    matchReportService.createMatchReport(idStore.getAll())
                    delay(1000)
                    if(stateFlag.value){
                        matchReportService.fetchDayMatches(quarterReportList
                            .map { it. date }
                            .toSet()
                            .toList())
                        val mapped = quarterReportList.map { exercise ->
                            MatchDataCardInfo(
                                id = exercise.id,
                                date = exercise.date,
                                startTime = exercise.gameReportData.startTime,
                                endTime = exercise.gameReportData.endTime,
                                buttonText = if(exercise.gameSide == null || exercise.quarterNumber == null) "정보 입력하기" else "정보 수정하기"
                            )
                        }
                        matchData = mapped
                        stateFlag.value = false
                        if(quarterReportList.isEmpty()) {
                            viewModel.setNoData()
                        }
                        else
                            viewModel.setSuccess(matchData)
                    } else {
                        if(quarterReportList.isEmpty()) {
                            viewModel.setNoData()
                        }
                        else
                            viewModel.setSuccess(matchData)
                    }
                } catch (e: Exception) {
                    viewModel.setNoData()
                }
            }
            handled = true
        }
        else if(uiState is MatchUiState.Success) {
            matchReportService.fetchDayMatches(quarterReportList
                .map { it. date }
                .toSet()
                .toList())
            val mapped = quarterReportList.map { exercise ->
                MatchDataCardInfo(
                    id = exercise.id,
                    date = exercise.date,
                    startTime = exercise.gameReportData.startTime,
                    endTime = exercise.gameReportData.endTime,
                    buttonText = if(exercise.gameSide == null || exercise.quarterNumber == null) "정보 입력하기" else "정보 수정하기"
                )
            }
            matchData = mapped

            viewModel.setSuccess(matchData)
            handled = true
        }
    }

    // quarterReportList 값이 변경될 때만 matchData와 viewModel 상태를 갱신
    LaunchedEffect(quarterReportList) {
        if (uiState is MatchUiState.Loading) {
            if (quarterReportList.isEmpty()) {
                viewModel.setNoData()
            } else {
                matchReportService.fetchDayMatches(quarterReportList
                    .map { it. date }
                    .toSet()
                    .toList())
                val mapped = quarterReportList.map { exercise ->
                    MatchDataCardInfo(
                        id = exercise.id,
                        date = exercise.date,
                        startTime = exercise.gameReportData.startTime,
                        endTime = exercise.gameReportData.endTime,
                        buttonText = if(exercise.gameSide == null || exercise.quarterNumber == null) "정보 입력하기" else "정보 수정하기"
                    )
                }
                matchData = mapped
                viewModel.setSuccess(matchData)
            }
        }
        if(uiState is MatchUiState.Success){
            if(quarterReportList.isEmpty())
                viewModel.setNoData()
        }
    }

    when (uiState) {
        is MatchUiState.WaitingForStadiumData -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(10.dp))
                    WatchWithAnimatedCircles()
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "경기장 데이터를 기다리고 있습니다",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = pretendard,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "워치를 연동해주세요",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = pretendard,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
        is MatchUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "데이터 로딩 중입니다",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = pretendard,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        is MatchUiState.NoData -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopNavItem(
                    title = "매치 데이터 연동",
                    type = TopNavType.MAIN_BASIC
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = pretendard,
                        modifier = Modifier.weight(1f).padding(start = 10.dp)
                    )
                    IconButton(onClick = { stateFlag.value = true
                        viewModel.setLoading() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reload),
                            contentDescription = "새로고침",
                            tint = Gray.Gray700
                        )
                    }
                    IconButton(onClick = { matchReportService.clearCornerData()
                        viewModel.setWaitingForStadiumData() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "코너삭제",
                            tint = Gray.Gray700
                        )
                    }
                }
                Spacer(modifier = Modifier.height(180.dp))
                Image(
                    painter = painterResource(id = R.drawable.samsung_health_144x144),
                    contentDescription = "삼성헬스 아이콘",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "삼성 헬스 데이터가 없습니다.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = pretendard,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "삼성 헬스에서 운동 데이터를 연동해 주세요.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = pretendard,
                    color = Color.Gray
                )
            }
        }
        is MatchUiState.Success -> {
            var showModal by remember { mutableStateOf(false) }
            var modalData by remember { mutableStateOf<MatchDataCardInfo?>(null) }
            val cardList = quarterReportList.map { exercise ->
                MatchDataCardInfo(
                    id = exercise.id,
                    date = exercise.date,
                    startTime = exercise.gameReportData.startTime,
                    endTime = exercise.gameReportData.endTime,
                    buttonText = if(exercise.gameSide == null || exercise.quarterNumber == null) "정보 입력하기" else "정보 수정하기"
                )
            }
            var showMatchSelectModal by remember { mutableStateOf(false) }

            // 체크된 쿼터 id 리스트 상태
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                TopNavItem(
                    title = "매치 데이터 연동",
                    type = TopNavType.MAIN_BASIC
                )
                var isStep1Expanded by remember { mutableStateOf(false) }
                var isStep2Expanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isStep1Expanded = !isStep1Expanded }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_navigate_next),
                        contentDescription = "STEP 1",
                        modifier = Modifier.rotate(if (isStep1Expanded) 90f else 0f)
                    )
                    Text(
                        text = "저장할 쿼터를 선택하고 정보를 입력하세요",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        fontFamily = pretendard,
                        modifier = Modifier.weight(1f).padding(start = 10.dp)
                    )
                }

                AnimatedVisibility(visible = isStep1Expanded) {
                    Column {
                        Row( // 쿼터 데이터 리스트 타이틀 + 새로고침/삭제
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "쿼터 데이터 리스트",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = pretendard,
                                modifier = Modifier.weight(1f).padding(start = 10.dp)
                            )
                            IconButton(onClick = {
                                stateFlag.value = true
                                viewModel.setLoading()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_reload),
                                    contentDescription = "새로고침",
                                    tint = Gray.Gray700
                                )
                            }
                            IconButton(onClick = {
                                matchReportService.clearCornerData()
                                viewModel.setWaitingForStadiumData()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "코너삭제",
                                    tint = Gray.Gray700
                                )
                            }
                        }

                        val allChecked = cardList.isNotEmpty() && cardList.all { selectedQuarterList.contains(it.id) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = allChecked,
                                onCheckedChange = {
                                    if (allChecked) {
                                        matchReportService.uncheckAllQuarters()
                                    } else {
                                        matchReportService.checkAllQuarters(cardList.map { it.id })
                                    }
                                },
                                enabled = cardList.isNotEmpty()
                            )
                            Text(
                                text = if (allChecked) "전체 해제" else "전체 선택",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = pretendard,
                                color = Color.Black
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            cardList.forEach { card ->
                                MatchDataCard(
                                    id = card.id,
                                    date = card.date,
                                    startTime = card.startTime,
                                    endTime = card.endTime,
                                    buttonText = card.buttonText,
                                    checked = selectedQuarterList.contains(card.id),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            matchReportService.addSelectedQuarter(card.id)
                                        } else {
                                            matchReportService.removeSelectedQuarter(card.id)
                                        }
                                    },
                                    onButtonClick = {
                                        modalData = card
                                        showModal = true
                                    },
                                    onDeleteClick = {
                                        idStore.add(card.id)
                                        matchReportService.deleteReport(card.id)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isStep2Expanded = !isStep2Expanded }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_navigate_next),
                        contentDescription = "STEP 2",
                        modifier = Modifier.rotate(if (isStep2Expanded) 90f else 0f)
                    )
                    Text(
                        text = "매치를 선택하세요",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        fontFamily = pretendard,
                        modifier = Modifier.weight(1f).padding(start = 10.dp)
                    )
                }

                AnimatedVisibility(visible = isStep2Expanded) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayMatchesList.find { it.matchId == selectedMatchId.toInt() }
                                    ?.let { "${it.matchName}  ${it.matchDate} ${it.startTime.substring(0, 5)}-${it.endTime.substring(0, 5)}" }
                                    ?: "아직 매치를 선택하지 않았습니다.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = pretendard,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    if (selectedQuarterList.isEmpty()) {
                                        errorMessage = "선택된 쿼터가 없습니다"
                                    } else {
                                        showMatchSelectModal = true
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = "매치 선택"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                BallogButton(
                    onClick = {
                        scope.launch {
                            var flag = false;
                            quarterReportList.map { quarter ->
                                if (quarter.quarterNumber == null || quarter.gameSide == null) {
                                    flag = true
                                }
                            }
                            if (flag) {
                                errorMessage = "입력하지 않은 쿼터 정보가 있습니다."
                            } else {
                                val matchReportResponse = matchReportService.sendMatchReport()
                                if (matchReportResponse == null) {
                                    errorMessage = "매치 데이터 전송에 실패하였습니다."
                                } else if (selectedMatchId.toInt() == 0){
                                    errorMessage = "매치를 선택해주세요."
                                } else {
                                    quarterReportList.map { quarter ->
                                        idStore.add(quarter.id)
                                    }
                                    matchReportService.clearQuarterReportList()
                                    setSelectedTab(NavigationTab.MATCH)
                                    setPendingMatchDetail(matchReportResponse.matchId to matchReportResponse.matchName)
                                    viewModel.setWaitingForStadiumData()
                                }
                            }
                        }
                    },
                    type = ButtonType.LABEL_ONLY,
                    buttonColor = ButtonColor.BLACK,
                    label = "선택 쿼터 저장",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(start = 24.dp, end = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (showMatchSelectModal) {
                    MatchSelectModal(
                        matches = dayMatchesList,
                        selectedMatchId = selectedMatchId,
                        onDismiss = { showMatchSelectModal = false },
                        onMatchSelected = { match ->
                            matchReportService.setSelectedMatchId(match.matchId)
                            showMatchSelectModal = false
                        }
                    )
                }
            }
            if (showModal && modalData != null) {
                MatchDataModal(
                    data = modalData!!,
                    matchReportService = matchReportService,
                    onDismiss = { showModal = false }
                )
            }
        }
        is MatchUiState.Error -> {
            val message = (uiState as? MatchUiState.Error)?.message ?: "알 수 없는 에러"
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "에러 발생: $message",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        else -> {
            // StadiumDataSuccess 등 미처리 상태 → 삼성헬스 데이터 없음 UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopNavItem(
                    title = "매치 데이터 연동",
                    type = TopNavType.MAIN_BASIC
                )
                Spacer(modifier = Modifier.height(180.dp))
                Image(
                    painter = painterResource(id = R.drawable.samsung_health_144x144),
                    contentDescription = "삼성헬스 아이콘",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "삼성 헬스 데이터가 없습니다.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = pretendard,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "삼성 헬스에서 운동 데이터를 연동해 주세요.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = pretendard,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MatchDataModal(
    data: MatchDataCardInfo,
    matchReportService: MatchReportService,
    onDismiss: () -> Unit
) {
    val quarterReportList by matchReportService.quarterReportList.collectAsState()
    var quarter by remember { mutableStateOf("") }
    var selectedSide by remember { mutableStateOf<String>("left") } // "LEFT" or "RIGHT"
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        quarterReportList.map { quarterReport ->
            if(data.id == quarterReport.id) {
                if (quarterReport.quarterNumber != null)
                    quarter = quarterReport.quarterNumber.toString()
                if(quarterReport.gameSide != null)
                    selectedSide = quarterReport.gameSide
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(Gray.Gray100)
                .width(310.dp)
                .height(520.dp)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "쿼터 정보를 입력해주세요",
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Gray.Gray800,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                // 쿼터 입력
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "몇번째 쿼터인가요 ?",
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Gray.Gray800,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Input(
                        value = quarter,
                        onValueChange = { quarter = it },
                        placeholder = "쿼터 숫자",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(24.dp))
                // 진영 선택
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "내 진영을 선택해 주세요",
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Gray.Gray800,
                    )
                    Text(
                        text = "진영은 초기 GPS 기반 자동 배정되어 있습니다",
                        fontFamily = pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 8.sp,
                        color = Gray.Gray800,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // 진영 선택 HeatMap + Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    ) {
                        HeatMap(
                            heatData = matchReportService.getHeatMapData(data.id),
                            modifier = Modifier.matchParentSize()
                        )
                        Row(Modifier.matchParentSize()) {
                            // 왼쪽 영역
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedSide = "left" }
                            ) {
                                if (selectedSide == "left") {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Gray.Gray700.copy(alpha = 0.3f))
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_team),
                                        contentDescription = "왼쪽 선택됨",
                                        tint = Gray.Gray100,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            // 오른쪽 영역
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedSide = "right" }
                            ) {
                                if (selectedSide == "right") {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Gray.Gray700.copy(alpha = 0.3f))
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_team),
                                        contentDescription = "오른쪽 선택됨",
                                        tint = Gray.Gray100,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = System.Red,
                            fontSize = 12.sp,
                            fontFamily = pretendard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    BallogButton(
                        onClick = {
                            if(quarter == ""){
                                errorMessage = "쿼터 번호를 입력해주세요"
                            }
                            else {
                                matchReportService.updateNumberAndSide(
                                    id = data.id,
                                    quarterNumber = quarter.toInt(),
                                    side = selectedSide
                                )
                                onDismiss()
                            }},
                        type = ButtonType.LABEL_ONLY,
                        buttonColor = ButtonColor.BLACK,
                        label = "저장",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun WatchWithAnimatedCircles() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer(scaleX = scale3, scaleY = scale3)
                .background(
                    color = Primary.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer(scaleX = scale2, scaleY = scale2)
                .background(
                    color = Primary.copy(alpha = 0.30f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer(scaleX = scale1, scaleY = scale1)
                .background(
                    color = Primary.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )
        Image(
            painter = painterResource(id = R.drawable.ic_watch),
            contentDescription = "워치 아이콘",
            modifier = Modifier.size(110.dp)
        )
    }
}

@Composable
fun MatchSelectModal(
    matches: List<MatchItemDto>,
    selectedMatchId: Int,
    onDismiss: () -> Unit,
    onMatchSelected: (MatchItemDto) -> Unit
) {
    var selectedMatch by remember { mutableStateOf<MatchItemDto?>(matches.find { it.matchId == selectedMatchId && selectedMatchId != 0 }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("매치 선택") },
        text = {
            Column {
                if (matches.isEmpty()) {
                    Text(
                        text = "등록된 매치가 없습니다",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    matches.forEach { match ->
                        val isSelected = selectedMatch?.matchId == match.matchId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMatch = match }
                                .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = match.matchName, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF00796B) else Color.Unspecified)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${match.matchDate} ${match.startTime.substring(0,5)}-${match.endTime.substring(0,5)}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            BallogButton(
                onClick = {
                    selectedMatch?.let { onMatchSelected(it) }
                },
                type = ButtonType.LABEL_ONLY,
                buttonColor = ButtonColor.BLACK,
                label = "저장",
                modifier = Modifier.fillMaxWidth(),
                enabled = matches.isNotEmpty() && selectedMatch != null
            )
        }
    )
}
