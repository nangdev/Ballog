package com.ballog.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballog.mobile.R
import com.ballog.mobile.ui.theme.Gray
import com.ballog.mobile.ui.theme.Primary
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.ballog.mobile.ui.theme.BallogTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.ballog.mobile.ui.theme.pretendard

@Composable
fun DropDown(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var parentWidth by remember { mutableStateOf(0) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .onGloballyPositioned { coordinates ->
                    parentWidth = coordinates.size.width
                }
                .background(Gray.Gray100, RoundedCornerShape(4.dp))
                .border(1.dp, Gray.Gray300, RoundedCornerShape(4.dp))
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedItem,
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = pretendard
            )

            val rotationAngle by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 300, easing = EaseInOut),
                label = "rotateDropdownArrow"
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_navigate_down),
                contentDescription = "Dropdown Arrow",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationAngle)
            )
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .width(with(LocalDensity.current) { parentWidth.toDp() }) // 버튼과 동일한 너비
                .clip(RoundedCornerShape(4.dp))
                .background(Gray.Gray200)
                .border(1.dp, Gray.Gray300, RoundedCornerShape(4.dp))
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // ✅ 내부 여백만 유지
                    text = {
                        Text(
                            text = item,
                            color = if (item == selectedItem) Primary else Gray.Gray800,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = pretendard
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DropDownPreview() {
    var selected by remember { mutableStateOf("1 쿼터") }
    var expanded by remember { mutableStateOf(false) }

    BallogTheme {
        DropDown(
            items = listOf("1 쿼터", "2 쿼터", "3 쿼터", "4 쿼터"),
            selectedItem = selected,
            onItemSelected = { selected = it },
            expanded = expanded,
            onExpandedChange = { expanded = it }
        )
    }
}
