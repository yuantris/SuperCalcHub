package com.core.app.supercalchub.features.mathboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextEntryPanel(vm: BoardViewModel) {
    val textColors = listOf(
        Color.Black, Color(0xFFE53935), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF2196F3), Color(0xFF9C27B0)
    )
    AnimatedVisibility(
        visible = vm.showTextPanel,
        enter = slideInVertically { it }, exit = slideOutVertically { it }
    ) {
        Column(Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = vm.textInput, onValueChange = { vm.textInput = it },
                    modifier = Modifier.weight(1f), placeholder = { Text("请输入文字") },
                    singleLine = true, textStyle = TextStyle(fontSize = 16.sp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vm.selectTool(DrawingTool.Freehand) }) {
                    Icon(Icons.Rounded.Check, "确认", tint = Color(0xFF2196F3))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("字色", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                textColors.forEach { c ->
                    Box(Modifier.size(24.dp).clip(CircleShape).background(c)
                        .border(if (vm.textColor == c) 2.dp else 0.dp, Color.White, CircleShape)
                        .clickable { vm.textColor = c })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("字号", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                listOf(16f, 24f, 32f, 48f, 64f).forEach { s ->
                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (vm.textFontSize == s) Color(0xFF2196F3) else Color(0xFFF5F5F5))
                        .clickable { vm.textFontSize = s }, Alignment.Center) {
                        Text("${s.toInt()}", color = if (vm.textFontSize == s) Color.White else Color.DarkGray,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
