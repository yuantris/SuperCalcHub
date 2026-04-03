package com.core.app.supercalchub.features.mathboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.*

class MathBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    /* ═══════════════ 数据 ═══════════════ */
    private val elements = mutableListOf<DrawElement>()
    private val undoStack = mutableListOf<DrawElement>()

    /* ═══════════════ 当前绘制状态 ═══════════════ */
    var tools = DrawingTool.Freehand
    var color = android.graphics.Color.BLACK
    var strokeWid = 8f
    var fillOn = false
    var autoShapes = true

    private val curPts = mutableListOf<Offset>()
    private var dragA: Offset? = null
    private var dragB: Offset? = null

    /* ═══════════════ 缩放 / 平移 ═══════════════ */
    private val matrix = Matrix()
    private var scale = 1f
    private var prevSpan = 0f
    private var prevMid = Offset.Zero
    private var isPan = false
    private var drawing = false

    /* ═══════════════ 识别器 ═══════════════ */
    private val recognizer = ShapeRecognizer()

    /* ═══════════════ 回调 ═══════════════ */
    var onContentChanged: (() -> Unit)? = null
    var onTextTap: ((Offset) -> Unit)? = null
    var onPolygonTap: ((Offset) -> Unit)? = null

    /* ═══════════════ 画笔（复用避免 GC）═══════════════ */
    private val stkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val filPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT; isAntiAlias = true
    }

    init { setLayerType(LAYER_TYPE_HARDWARE, null) }

    /* ════════════════════════════════════════════
     *                   onDraw
     * ════════════════════════════════════════════ */
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.save()
        c.concat(matrix)

        // 白色底
        c.drawColor(android.graphics.Color.WHITE)

        // 已提交元素
        for (el in elements) drawEl(c, el)

        // 当前自由画笔 / 橡皮轨迹
        drawCurStroke(c)

        // 拖拽形状预览
        drawDragPreview(c)

        c.restore()
    }

    /* ── 绘制当前笔迹 ── */
    private fun drawCurStroke(c: Canvas) {
        if (curPts.size < 2) return
        if (tools == DrawingTool.Freehand) {
            stkPaint.color = color; stkPaint.strokeWidth = strokeWid
            c.drawPath(buildSmoothPath(curPts), stkPaint)
        } else if (tools == DrawingTool.Eraser) {
            // 橡皮：白色粗线覆盖 + 圆形光标
            stkPaint.color = android.graphics.Color.WHITE
            stkPaint.strokeWidth = strokeWid * 3f
            c.drawPath(buildSmoothPath(curPts), stkPaint)
            // 光标圈
            val last = curPts.last()
            val cp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; strokeWidth = 2f
                color = android.graphics.Color.GRAY
            }
            c.drawCircle(last.x, last.y, strokeWid * 1.5f, cp)
        }
    }

    /* ── 拖拽形状预览 ── */
    private fun drawDragPreview(c: Canvas) {
        val a = dragA ?: return; val b = dragB ?: return
        stkPaint.color = android.graphics.Color.argb(150f, Color(color).red,
            Color(color).green, Color(color).blue)
        stkPaint.strokeWidth = strokeWid
        when (tools) {
            DrawingTool.Line -> c.drawLine(a.x, a.y, b.x, b.y, stkPaint)
            DrawingTool.Rectangle -> {
                val r = rectF(a, b)
                if (fillOn) { filPaint.color = stkPaint.color; c.drawRect(r, filPaint) }
                c.drawRect(r, stkPaint)
            }
            DrawingTool.Circle -> {
                val r = rectF(a, b)
                if (fillOn) { filPaint.color = stkPaint.color; c.drawOval(r, filPaint) }
                c.drawOval(r, stkPaint)
            }
            DrawingTool.Trapezoid -> {
                val p = trapPath(a, b)
                if (fillOn) { filPaint.color = stkPaint.color; c.drawPath(p, filPaint) }
                c.drawPath(p, stkPaint)
            }
            else -> {}
        }
    }

    /* ── 绘制单个元素 ── */
    private fun drawEl(c: Canvas, el: DrawElement) {
        when (el) {
            is DrawElement.Freehand -> {
                if (el.points.size < 2) return
                stkPaint.color = el.color.toArgb(); stkPaint.strokeWidth = el.strokeWidth
                c.drawPath(buildSmoothPath(el.points), stkPaint)
            }
            is DrawElement.Line -> {
                stkPaint.color = el.color.toArgb(); stkPaint.strokeWidth = el.strokeWidth
                c.drawLine(el.start.x, el.start.y, el.end.x, el.end.y, stkPaint)
            }
            is DrawElement.Rectangle -> {
                val r = rectF(el.topLeft, el.bottomRight)
                stkPaint.color = el.strokeColor.toArgb(); stkPaint.strokeWidth = el.strokeWidth
                if (el.fill) { filPaint.color = el.fillColor.toArgb(); c.drawRect(r, filPaint) }
                c.drawRect(r, stkPaint)
            }
            is DrawElement.Ellipse -> {
                val r = rectF(el.topLeft, el.bottomRight)
                stkPaint.color = el.strokeColor.toArgb(); stkPaint.strokeWidth = el.strokeWidth
                if (el.fill) { filPaint.color = el.fillColor.toArgb(); c.drawOval(r, filPaint) }
                c.drawOval(r, stkPaint)
            }
            is DrawElement.Trapezoid -> {
                val p = trapPath(el.topLeft, el.bottomRight, el.topInsetRatio)
                stkPaint.color = el.strokeColor.toArgb(); stkPaint.strokeWidth = el.strokeWidth
                if (el.fill) { filPaint.color = el.fillColor.toArgb(); c.drawPath(p, filPaint) }
                c.drawPath(p, stkPaint)
            }
            is DrawElement.Polygon -> {
                val p = polyPath(el.center, el.radius, el.sides)
                stkPaint.color = el.strokeColor.toArgb(); stkPaint.strokeWidth = el.strokeWidth
                if (el.fill) { filPaint.color = el.fillColor.toArgb(); c.drawPath(p, filPaint) }
                c.drawPath(p, stkPaint)
            }
            is DrawElement.TextElement -> {
                txtPaint.color = el.color.toArgb(); txtPaint.textSize = el.fontSize
                c.drawText(el.text, el.position.x, el.position.y + el.fontSize, txtPaint)
            }
        }
    }

    /* ════════════════════════════════════════════
     *              触摸事件（核心）
     * ════════════════════════════════════════════ */
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPan = false; drawing = true
                val p = v2c(ev.getX(0), ev.getY(0))
                pDown(p)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                isPan = true; drawing = false
                cancelStroke()
                prevSpan = span(ev); prevMid = mid(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                if (ev.pointerCount >= 2 && isPan) {
                    doZoomPan(ev)
                } else if (drawing && ev.pointerCount == 1) {
                    pMove(v2c(ev.getX(0), ev.getY(0)))
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isPan && drawing) pUp(v2c(ev.getX(0), ev.getY(0)))
                drawing = false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (ev.pointerCount <= 2) isPan = false
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelStroke(); drawing = false; isPan = false
            }
        }
        return true
    }

    /* ── 指针按下 ── */
    private fun pDown(p: Offset) {
        when (tools) {
            DrawingTool.Freehand, DrawingTool.Eraser -> {
                curPts.clear(); curPts.add(p)
                if (tools == DrawingTool.Eraser) doErase(p)
            }
            DrawingTool.Text -> onTextTap?.invoke(p)
            DrawingTool.Polygon -> onPolygonTap?.invoke(p)
            else -> { dragA = p; dragB = p }
        }
        invalidate()
    }

    /* ── 指针移动 ── */
    private fun pMove(p: Offset) {
        when (tools) {
            DrawingTool.Freehand -> { curPts.add(p); invalidate() }
            DrawingTool.Eraser -> { curPts.add(p); doErase(p); invalidate() }
            else -> { dragB = p; invalidate() }
        }
    }

    /* ── 指针抬起 ── */
    private fun pUp(p: Offset) {
        when (tools) {
            DrawingTool.Freehand -> commitFreehand()
            DrawingTool.Eraser -> { curPts.clear(); invalidate() }
            DrawingTool.Text, DrawingTool.Polygon -> { /* 已在 down 触发 */ }
            else -> commitShape()
        }
    }

    /* ═══════════════ 自由画笔提交 + 自动识别 ═══════════════ */
    private fun commitFreehand() {
        if (curPts.size < 2) { curPts.clear(); return }

        val el: DrawElement = if (autoShapes) {
            recognizer.recognize(curPts.toList())?.let { applyStyle(it) }
                ?: DrawElement.Freehand(curPts.toList(), Color(color), strokeWid)
        } else {
            DrawElement.Freehand(curPts.toList(), Color(color), strokeWid)
        }
        push(el); curPts.clear(); invalidate()
    }

    /* ═══════════════ 形状拖拽提交 ═══════════════ */
    private fun commitShape() {
        val a = dragA ?: return; val b = dragB ?: return
        if ((a - b).getDistance() < 10f) { dragA = null; dragB = null; return }
        val el = when (tools) {
            DrawingTool.Line -> DrawElement.Line(a, b, Color(color), strokeWid)
            DrawingTool.Rectangle -> DrawElement.Rectangle(
                tl(a, b), br(a, b), Color(color), strokeWid, fillOn, Color(color))
            DrawingTool.Circle -> DrawElement.Ellipse(
                tl(a, b), br(a, b), Color(color), strokeWid, fillOn, Color(color))
            DrawingTool.Trapezoid -> DrawElement.Trapezoid(
                tl(a, b), br(a, b), strokeColor = Color(color), strokeWidth = strokeWid,
                fill = fillOn, fillColor = Color(color))
            else -> return
        }
        push(el); dragA = null; dragB = null; invalidate()
    }

    /* ═══════════════ 缩放 / 平移 ═══════════════ */
    private fun doZoomPan(ev: MotionEvent) {
        if (ev.pointerCount < 2) return
        val m = mid(ev); val s = span(ev)
        // 平移
        matrix.postTranslate(m.x - prevMid.x, m.y - prevMid.y)
        // 缩放
        if (prevSpan > 0) {
            val factor = s / prevSpan
            val ns = scale * factor
            if (ns in 0.2f..6f) {
                matrix.postScale(factor, factor, m.x, m.y)
                scale = ns
            }
        }
        prevSpan = s; prevMid = m; invalidate()
    }

    private fun v2c(vx: Float, vy: Float): Offset {
        val inv = Matrix(); matrix.invert(inv)
        val p = floatArrayOf(vx, vy); inv.mapPoints(p)
        return Offset(p[0], p[1])
    }

    /* ═══════════════ 橡皮擦 ═══════════════ */
    private fun doErase(p: Offset) {
        val th = 40f / max(scale, 0.3f)
        val it = elements.listIterator()
        while (it.hasNext()) {
            val el = it.next()
            val hit = when (el) {
                is DrawElement.Freehand -> el.points.any {
                    abs(it.x - p.x) < th && abs(it.y - p.y) < th
                }
                is DrawElement.Line -> distSeg(p, el.start, el.end) < th
                is DrawElement.TextElement ->
                    abs(el.position.x - p.x) < th * 3 && abs(el.position.y - p.y) < th
                is DrawElement.Rectangle -> {
                    val r = RectF(el.topLeft.x, el.topLeft.y, el.bottomRight.x, el.bottomRight.y)
                    r.contains(p.x, p.y)
                }
                is DrawElement.Ellipse -> {
                    val cx = (el.topLeft.x + el.bottomRight.x) / 2
                    val cy = (el.topLeft.y + el.bottomRight.y) / 2
                    val rx = abs(el.bottomRight.x - el.topLeft.x) / 2
                    val ry = abs(el.bottomRight.y - el.topLeft.y) / 2
                    if (rx == 0f || ry == 0f) false
                    else { val nx = (p.x - cx) / rx; val ny = (p.y - cy) / ry; nx * nx + ny * ny < 1.1f }
                }
                else -> true
            }
            if (hit) { it.remove(); undoStack.add(el); onContentChanged?.invoke(); break }
        }
    }

    /* ═══════════════ 公开 API ═══════════════ */
    fun undo() { if (elements.isNotEmpty()) { val r = elements.removeLast(); undoStack.add(r); invalidate() } }
    fun redo() { if (undoStack.isNotEmpty()) { val r = undoStack.removeLast(); elements.add(r); invalidate() } }
    fun clearAll() { undoStack.addAll(elements); elements.clear(); invalidate() }
    fun resetView() { matrix.reset(); scale = 1f; invalidate() }
    fun setTool(t: DrawingTool) { tools = t; invalidate() }
    fun setColor(c: Color) { color = c.toArgb() }
    fun setStrokeW(w: Float) { strokeWid = w }
    fun setFill(f: Boolean) { fillOn = f }
    fun setAutoShape(a: Boolean) { autoShapes = a }

    fun addText(pos: Offset, text: String, c: Color, size: Float) {
        elements.add(DrawElement.TextElement(pos, text, c, size)); invalidate()
    }
    fun addPolygon(center: Offset, r: Float, sides: Int, fill: Boolean, sw: Float, fc: Color, sc: Color) {
        elements.add(DrawElement.Polygon(center, r, sides, sc, sw, fill, fc)); invalidate()
    }
    fun captureBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bmp)); return bmp
    }

    /* ═══════════════ 内部工具 ═══════════════ */
    private fun push(el: DrawElement) { elements.add(el); undoStack.add(el); onContentChanged?.invoke() }
    private fun cancelStroke() { curPts.clear(); dragA = null; dragB = null; invalidate() }
    private fun applyStyle(el: DrawElement): DrawElement = when (el) {
        is DrawElement.Freehand -> el.copy(color = Color(color), strokeWidth = strokeWid)
        is DrawElement.Line -> el.copy(color = Color(color), strokeWidth = strokeWid)
        is DrawElement.Rectangle -> el.copy(strokeColor = Color(color), strokeWidth = strokeWid, fill = fillOn, fillColor = Color(color))
        is DrawElement.Ellipse -> el.copy(strokeColor = Color(color), strokeWidth = strokeWid, fill = fillOn, fillColor = Color(color))
        is DrawElement.Polygon -> el.copy(strokeColor = Color(color), strokeWidth = strokeWid, fill = fillOn, fillColor = Color(color))
        is DrawElement.Trapezoid -> el.copy(strokeColor = Color(color), strokeWidth = strokeWid, fill = fillOn, fillColor = Color(color))
        is DrawElement.TextElement -> el.copy(color = Color(color))
    }

    private fun buildSmoothPath(pts: List<Offset>): Path {
        val p = Path(); p.moveTo(pts[0].x, pts[0].y)
        for (i in 1 until pts.size) {
            if (i < pts.size - 1) {
                val mx = (pts[i].x + pts[i + 1].x) / 2
                val my = (pts[i].y + pts[i + 1].y) / 2
                p.quadTo(pts[i].x, pts[i].y, mx, my)
            } else p.lineTo(pts[i].x, pts[i].y)
        }
        return p
    }

    private fun rectF(a: Offset, b: Offset) = RectF(
        minOf(a.x, b.x), minOf(a.y, b.y), maxOf(a.x, b.x), maxOf(a.y, b.y)
    )
    private fun tl(a: Offset, b: Offset) = Offset(minOf(a.x, b.x), minOf(a.y, b.y))
    private fun br(a: Offset, b: Offset) = Offset(maxOf(a.x, b.x), maxOf(a.y, b.y))

    private fun trapPath(a: Offset, b: Offset, inset: Float = 0.3f): Path {
        val w = b.x - a.x; val d = w * inset
        return Path().apply {
            moveTo(a.x + d, a.y); lineTo(b.x - d, a.y)
            lineTo(b.x, b.y); lineTo(a.x, b.y); close()
        }
    }

    private fun polyPath(c: Offset, r: Float, n: Int): Path {
        val p = Path()
        for (i in 0 until n) {
            val ang = (2.0 * PI / n * i - PI / 2.0).toFloat()
            val x = c.x + r * cos(ang); val y = c.y + r * sin(ang)
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close(); return p
    }

    private fun span(ev: MotionEvent): Float {
        val dx = ev.getX(0) - ev.getX(1); val dy = ev.getY(0) - ev.getY(1)
        return sqrt(dx * dx + dy * dy)
    }
    private fun mid(ev: MotionEvent) = Offset(
        (ev.getX(0) + ev.getX(1)) / 2f, (ev.getY(0) + ev.getY(1)) / 2f
    )
    private fun distSeg(p: Offset, a: Offset, b: Offset): Float {
        val dx = b.x - a.x; val dy = b.y - a.y; val l2 = dx * dx + dy * dy
        if (l2 == 0f) return (p - a).getDistance()
        val t = ((p.x - a.x) * dx + (p.y - a.y) * dy / l2).coerceIn(0f, 1f)
        return (p - Offset(a.x + t * dx, a.y + t * dy)).getDistance()
    }
}
