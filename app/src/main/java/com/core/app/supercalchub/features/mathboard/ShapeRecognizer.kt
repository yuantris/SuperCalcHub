package com.core.app.supercalchub.features.mathboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * 手绘笔迹 → 标准几何图形 自动识别
 *
 * 算法流程：
 * 1. Ramer-Douglas-Peucker 路径简化
 * 2. 判断路径是否闭合
 * 3. 闭合 → 检测顶点数 → 三角形 / 矩形 / 圆
 * 4. 开放 → 检测线性度 → 直线
 */
class ShapeRecognizer {

    companion object {
        private const val RDP_EPSILON = 8f
        private const val CLOSE_RATIO = 0.12f   // 路径长度的 12% 以内视为闭合
        private const val VERTEX_ANGLE = 1.8f    // 弧度，< 此值视为顶点
        private const val LINE_LINEARITY = 0.88f  // 线性度 > 此值视为直线
        private const val CIRCLE_VARIANCE = 0.14f
        private const val MIN_PTS = 12
    }

    fun recognize(raw: List<Offset>): DrawElement? {
        if (raw.size < MIN_PTS) return null

        val simplified = rdp(raw, RDP_EPSILON)
        val pathLen = pathLength(raw)
        val closed = (raw.first() - raw.last()).getDistance() < pathLen * CLOSE_RATIO

        if (!closed) {
            return detectLine(raw)
        }

        // 闭合路径：找顶点
        val closedPath = if (simplified.first() != simplified.last())
            simplified + simplified.first() else simplified
        val verts = findVertices(closedPath)

        return when (verts.size) {
            3  -> makeTriangle(verts)
            4  -> makeRectangle(verts)
            else -> detectCircle(raw)
        }
    }

    // ── 直线检测 ──────────────────────────────────────────
    private fun detectLine(pts: List<Offset>): DrawElement.Line? {
        val a = pts.first(); val b = pts.last()
        val len = (b - a).getDistance()
        if (len < 40f) return null
        val maxDev = pts.maxOf { perpDist(it, a, b) }
        if (1f - maxDev / len > LINE_LINEARITY)
            return DrawElement.Line(a, b, Color.Black, 8f)
        return null
    }

    // ── 三角形 ────────────────────────────────────────────
    private fun makeTriangle(v: List<Offset>): DrawElement.Polygon {
        val cx = v.map { it.x }.average().toFloat()
        val cy = v.map { it.y }.average().toFloat()
        val r = v.map { (it - Offset(cx, cy)).getDistance() }.average().toFloat()
        return DrawElement.Polygon(
            center = Offset(cx, cy), radius = r * 1.1f, sides = 3,
            strokeColor = Color.Black, strokeWidth = 8f,
            fill = false, fillColor = Color.Black
        )
    }

    // ── 矩形（从四个顶点的包围盒生成）─────────────────────
    private fun makeRectangle(v: List<Offset>): DrawElement.Rectangle {
        val tl = Offset(v.minOf { it.x }, v.minOf { it.y })
        val br = Offset(v.maxOf { it.x }, v.maxOf { it.y })
        return DrawElement.Rectangle(
            topLeft = tl, bottomRight = br,
            strokeColor = Color.Black, strokeWidth = 8f,
            fill = false, fillColor = Color.Black
        )
    }

    // ── 圆检测 ────────────────────────────────────────────
    private fun detectCircle(pts: List<Offset>): DrawElement.Ellipse? {
        val cx = pts.map { it.x }.average().toFloat()
        val cy = pts.map { it.y }.average().toFloat()
        val r = pts.map { (it - Offset(cx, cy)).getDistance() }.average().toFloat()
        if (r < 15f) return null
        val variance = pts.map { abs((it - Offset(cx, cy)).getDistance() - r) / r }
            .average().toFloat()
        if (variance > CIRCLE_VARIANCE) return null
        return DrawElement.Ellipse(
            topLeft = Offset(cx - r, cy - r),
            bottomRight = Offset(cx + r, cy + r),
            strokeColor = Color.Black, strokeWidth = 8f,
            fill = false, fillColor = Color.Black
        )
    }

    // ── Ramer-Douglas-Peucker 路径简化 ────────────────────
    private fun rdp(pts: List<Offset>, eps: Float): List<Offset> {
        if (pts.size <= 2) return pts
        var maxD = 0f; var idx = 0
        val s = pts.first(); val e = pts.last()
        for (i in 1 until pts.size - 1) {
            val d = perpDist(pts[i], s, e)
            if (d > maxD) { maxD = d; idx = i }
        }
        return if (maxD > eps) {
            val l = rdp(pts.subList(0, idx + 1), eps)
            val r = rdp(pts.subList(idx, pts.size), eps)
            l.dropLast(1) + r
        } else listOf(s, e)
    }

    // ── 闭合路径顶点检测 ──────────────────────────────────
    private fun findVertices(pts: List<Offset>): List<Offset> {
        if (pts.size < 4) return pts
        val n = pts.size
        val verts = mutableListOf<Offset>()
        for (i in 0 until n) {
            val prev = pts[(i - 1 + n) % n]
            val curr = pts[i]
            val next = pts[(i + 1) % n]
            val v1 = prev - curr; val v2 = next - curr
            val angle = angleBetween(v1, v2)
            if (angle < VERTEX_ANGLE) {
                if (verts.isEmpty() || (curr - verts.last()).getDistance() > 25f)
                    verts.add(curr)
            }
        }
        // 保证首顶点也被收录
        if (verts.isNotEmpty() && (verts.first() - verts.last()).getDistance() > 25f)
            verts.add(verts.first())
        return verts
    }

    // ── 几何工具 ──────────────────────────────────────────
    private fun perpDist(p: Offset, a: Offset, b: Offset): Float {
        val dx = b.x - a.x; val dy = b.y - a.y
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return (p - a).getDistance()
        return abs(dy * p.x - dx * p.y + b.x * a.y - b.y * a.x) / len
    }

    private fun pathLength(pts: List<Offset>): Float {
        var l = 0f
        for (i in 1 until pts.size) l += (pts[i] - pts[i - 1]).getDistance()
        return l
    }

    private fun angleBetween(a: Offset, b: Offset): Float {
        val dot = a.x * b.x + a.y * b.y
        val m1 = sqrt(a.x * a.x + a.y * a.y)
        val m2 = sqrt(b.x * b.x + b.y * b.y)
        if (m1 == 0f || m2 == 0f) return PI.toFloat()
        return acos((dot / (m1 * m2)).coerceIn(-1f, 1f))
    }
}
