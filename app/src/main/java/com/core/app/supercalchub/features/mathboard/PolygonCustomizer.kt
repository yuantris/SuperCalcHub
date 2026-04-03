package com.core.app.supercalchub.features.mathboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PolygonCustomizer(vm: BoardViewModel) {
    val s = vm.polygonSettings
    AnimatedVisibility(
        visible = vm.showPolygonPanel,
        enter = slideInVertically { it }, exit = slideOutVertically { it }
    ) {
        Column(Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("自定义多边形", style = MaterialTheme.typography.titleSmall, color = Color.DarkGray)
            Box(Modifier.fillMaxWidth().height(120.dp)
                .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp)), Alignment.Center) {
                Canvas(Modifier.size(100.dp)) {
                    val c = Offset(size.width / 2, size.height / 2); val r = size.width / 2 * 0.8f
                    val path = polyPath(c, r, s.sides)
                    if (s.fill) drawPath(path, s.fillColor)
                    drawPath(path, s.strokeColor, style = Stroke(s.strokeWidth))
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("填充颜色"); Spacer(Modifier.weight(1f))
                Switch(checked = s.fill, onCheckedChange = { vm.updatePolygon(s.copy(fill = it)) })
            }
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("边数"); Text("${s.sides}", color = Color(0xFF2196F3))
                }
                Slider(value = s.sides.toFloat(), onValueChange = { vm.updatePolygon(s.copy(sides = it.toInt())) },
                    valueRange = 3f..20f, steps = 16)
            }
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("粗细"); Text("${s.strokeWidth.toInt()}", color = Color(0xFF2196F3))
                }
                Slider(value = s.strokeWidth, onValueChange = { vm.updatePolygon(s.copy(strokeWidth = it)) },
                    valueRange = 1f..30f)
            }
        }
    }
}

private fun polyPath(c: Offset, r: Float, n: Int): androidx.compose.ui.graphics.Path {
    val p = androidx.compose.ui.graphics.Path()
    for (i in 0 until n) {
        val a = (2.0 * Math.PI / n * i - Math.PI / 2.0).toFloat()
        val x = c.x + r * kotlin.math.cos(a); val y = c.y + r * kotlin.math.sin(a)
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close(); return p
}
