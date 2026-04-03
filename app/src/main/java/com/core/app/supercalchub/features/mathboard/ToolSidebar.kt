package com.core.app.supercalchub.features.mathboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ToolSidebar(vm: BoardViewModel, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color.Black, Color(0xFFE53935), Color(0xFFFF9800), Color(0xFFFFEB3B),
        Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF2196F3),
        Color(0xFF9C27B0), Color(0xFFE91E63)
    )

    Column(
        modifier
            .width(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E).copy(alpha = 0.92f))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Btn(Icons.Rounded.Edit, "画笔", vm.currentTool == DrawingTool.Freehand) { vm.selectTool(DrawingTool.Freehand) }
        Btn(Icons.Default.ArrowForward, "直线", vm.currentTool == DrawingTool.Line) { vm.selectTool(DrawingTool.Line) }
        Btn(Icons.Rounded.ArrowForward, "矩形", vm.currentTool == DrawingTool.Rectangle) { vm.selectTool(DrawingTool.Rectangle) }
        Btn(Icons.Rounded.ArrowForward, "圆形", vm.currentTool == DrawingTool.Circle) { vm.selectTool(DrawingTool.Circle) }
        Btn(Icons.Rounded.ArrowForward, "梯形", vm.currentTool == DrawingTool.Trapezoid) { vm.selectTool(DrawingTool.Trapezoid) }
        Btn(Icons.Rounded.ArrowForward, "多边形", vm.currentTool == DrawingTool.Polygon) { vm.selectTool(DrawingTool.Polygon) }
        Btn(Icons.Rounded.ArrowForward, "文字", vm.currentTool == DrawingTool.Text) { vm.selectTool(DrawingTool.Text) }
        Divider(Modifier.padding(horizontal = 8.dp, 4.dp), color = Color.Gray.copy(.4f))
        Btn(Icons.Rounded.ArrowForward, "橡皮", vm.currentTool == DrawingTool.Eraser) { vm.selectTool(DrawingTool.Eraser) }

        // 填充开关
        Box(Modifier.size(36.dp).clip(CircleShape)
            .background(if (vm.fillShapes) vm.strokeColor else Color.Transparent)
            .border(if (vm.fillShapes) 2.dp else 0.dp, if (vm.fillShapes) vm.strokeColor else Color.Gray, CircleShape)
            .clickable { vm.toggleFill() }, Alignment.Center) {
            Icon(Icons.Rounded.Face, null,
                tint = if (vm.fillShapes) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
        }

        Divider(Modifier.padding(horizontal = 8.dp, 4.dp), color = Color.Gray.copy(.4f))

        // 自动识别开关
        Box(Modifier.size(36.dp).clip(CircleShape)
            .background(if (vm.autoShape) Color(0xFF448AFF).copy(.3f) else Color.Transparent)
            .border(1.dp, if (vm.autoShape) Color(0xFF82B1FF) else Color.Gray, CircleShape)
            .clickable { vm.toggleAutoShape() }, Alignment.Center) {
            Icon(Icons.Filled.Face, null,
                tint = if (vm.autoShape) Color(0xFF82B1FF) else Color.Gray, modifier = Modifier.size(18.dp))
        }

        Divider(Modifier.padding(horizontal = 8.dp, 4.dp), color = Color.Gray.copy(.4f))

        // 颜色
        colors.forEach { c ->
            Box(Modifier.size(22.dp).clip(CircleShape).background(c)
                .border(if (vm.strokeColor == c) 2.dp else 0.dp, Color.White, CircleShape)
                .clickable { vm.setColor(c) })
        }

        Spacer(Modifier.height(4.dp))

        // 笔宽指示
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF333333)).clickable {
            // 循环笔宽
            val widths = listOf(4f, 8f, 12f, 18f)
            val idx = widths.indexOfFirst { it == vm.strokeWidths }
            vm.setStrokeWidth(widths[(idx + 1) % widths.size])
        }, Alignment.Center) {
            Box(Modifier.size(((vm.strokeWidths / 2 + 4).dp).coerceAtMost(24.dp))
                .clip(CircleShape).background(vm.strokeColor))
        }

        Divider(Modifier.padding(horizontal = 8.dp, 4.dp), color = Color.Gray.copy(.4f))
        Btn(Icons.Default.KeyboardArrowLeft, "撤销") { vm.undo() }
        Btn(Icons.Default.KeyboardArrowRight, "重做") { vm.redo() }
        Spacer(Modifier.weight(1f))
        Btn(Icons.Rounded.Delete, "清空") { vm.clearAll() }
        Btn(Icons.Rounded.Refresh, "复位") { vm.boardView?.resetView() }
    }
}

@Composable
private fun Btn(icon: ImageVector, desc: String, selected: Boolean = false, onClick: () -> Unit) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
        .background(if (selected) Color(0xFF448AFF).copy(.35f) else Color.Transparent)
        .clickable(onClick = onClick), Alignment.Center) {
        Icon(icon, desc, tint = if (selected) Color(0xFF82B1FF) else Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
