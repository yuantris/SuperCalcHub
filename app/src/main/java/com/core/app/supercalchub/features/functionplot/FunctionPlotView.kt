package com.core.app.supercalchub.features.functionplot

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import kotlin.math.*

class FunctionPlotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ===== 坐标系统参数 =====
    private var unitSize = 80f
    private var scale = 1f
    private var translateX = 0f
    private var translateY = 0f
    private var viewWidth = 0f
    private var viewHeight = 0f

    private var lastMinX = 0.0
    private var lastMaxX = 0.0
    private var lastTranslateX = 0f
    private var lastScale = 0f

    // ===== 缩放限制参数 =====
    private var minScale = 0.1f
    private var maxScale = 50f
    private var scaleFactorMultiplier = 1.0f
    private var isScaleLimited = true

    // ===== 画笔定义 =====
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2.5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        isFakeBoldText = true
    }
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val functionColors = listOf(
        Color.BLUE, Color.parseColor("#D32F2F"), Color.parseColor("#388E3C"),
        Color.parseColor("#FFA000"), Color.parseColor("#7B1FA2")
    )

    // ===== 数据模型 =====
    private data class PlotFunction(
        val expression: Expression,
        val originalString: String,
        val asymptotes: MutableList<Double> = mutableListOf(),
        val points: MutableList<PointF> = mutableListOf(),
        val zeros: MutableList<PointF> = mutableListOf(),
        val extremes: MutableList<PointF> = mutableListOf(),
        var color: Int = Color.BLUE
    )

    private var functions: List<PlotFunction> = emptyList()
    private var trackedPoint: PointF? = null
    private var trackedFunctionIndex: Int = -1
    private var needsRecalculate = true

    // ===== 手势 =====
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private val scroller: OverScroller
    private var isScaling = false

    // ⭐ 缩放期间使用低精度快速采样，缩放结束后高精度重采样
    private var isQuickMode = false

    init {
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                isQuickMode = true
                return true
            }
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
                isQuickMode = false
                needsRecalculate = true
                invalidate()
            }
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val rawScaleFactor = detector.scaleFactor

                // ⭐ 限制缩放速度，让缩放更平滑
                val adjustedScaleFactor = 1f + (rawScaleFactor - 1f) * scaleFactorMultiplier
                val oldScale = scale

                // ⭐ 应用缩放限制
                var newScale = scale * adjustedScaleFactor
                val isAtLimit = if (isScaleLimited) {
                    when {
                        newScale < minScale -> {
                            newScale = minScale
                            true
                        }
                        newScale > maxScale -> {
                            newScale = maxScale
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }

                scale = newScale

                // 如果已经到达限制，调整焦点计算
                val focusX = detector.focusX
                val focusY = detector.focusY
                val mathX = (focusX - viewWidth / 2 - translateX) / (unitSize * oldScale)
                val mathY = -(focusY - viewHeight / 2 - translateY) / (unitSize * oldScale)

                translateX = focusX - viewWidth / 2 - mathX * unitSize * scale
                translateY = focusY - viewHeight / 2 + mathY * unitSize * scale

                // 缩放时只做快速重绘
                invalidate()
                return !isAtLimit // 如果到达限制，返回false表示不继续处理
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                if (!scroller.isFinished) scroller.abortAnimation()
                return true
            }
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                trackedPoint = null
                translateX -= distanceX
                translateY -= distanceY

                val currentUnit = unitSize * scale
                val currentMinX = (-viewWidth / 2 - translateX) / currentUnit
                val currentMaxX = (viewWidth / 2 - translateX) / currentUnit

                // ⭐ 超出预计算范围 70% 时才重采样
                val range = lastMaxX - lastMinX
                if (currentMinX < lastMinX + range * 0.15 || currentMaxX > lastMaxX - range * 0.15) {
                    needsRecalculate = true
                }

                invalidate()
                return true
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                scroller.abortAnimation()
                scroller.fling(
                    translateX.toInt(), translateY.toInt(),
                    velocityX.toInt(), velocityY.toInt(),
                    Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE
                )
                postInvalidateOnAnimation()
                return true
            }
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }
        })
        scroller = OverScroller(context)
    }

    // =============================
    // API
    // =============================
    fun setFunction(expression: String) {
        functions = emptyList()
        addFunction(expression)
    }

    fun addFunction(expression: String) {
        try {
            val exp = ExpressionBuilder(expression)
                .variable("x")
                .function(SqrtFunction())   // ⭐ 注册 sqrt
                .function(AbsFunction())    // ⭐ 注册 abs
                .function(TanFunction())    // ⭐ 注册 tan
                .build()
            val color = functionColors[functions.size % functionColors.size]
            val asymptotes = analyzeAsymptotes(expression)
            val newFunc = PlotFunction(
                expression = exp,
                originalString = expression,
                asymptotes = asymptotes,
                color = color
            )
            functions = functions + newFunc
            needsRecalculate = true
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ⭐ 新增：设置缩放限制
    fun setScaleLimits(min: Float = 0.1f, max: Float = 50f) {
        // 确保限制合理
        if (min > 0 && max > min) {
            // 需要修改类的属性为 var
            minScale = min
            maxScale = max

            // 调整当前缩放比例到限制范围内
            scale = scale.coerceIn(minScale, maxScale)
            invalidate()
        }
    }

    // ⭐ 新增：设置缩放速度
    fun setScaleSpeed(speed: Float) {
        scaleFactorMultiplier = speed.coerceIn(0.1f, 2.0f)
    }

    // ⭐ 新增：启用/禁用缩放限制
    fun setScaleLimitEnabled(enabled: Boolean) {
        isScaleLimited = enabled
        if (enabled) {
            scale = scale.coerceIn(minScale, maxScale)
            invalidate()
        }
    }

    // ⭐ 新增：获取当前缩放比例
    fun getCurrentScale(): Float = scale

    // ⭐ 新增：缩放到指定比例
    fun zoomTo(targetScale: Float, pivotX: Float? = null, pivotY: Float? = null) {
        val oldScale = scale
        scale = targetScale.coerceIn(minScale, maxScale)

        // 如果指定了缩放中心点
        if (pivotX != null && pivotY != null) {
            val mathX = (pivotX - viewWidth / 2 - translateX) / (unitSize * oldScale)
            val mathY = -(pivotY - viewHeight / 2 - translateY) / (unitSize * oldScale)
            translateX = pivotX - viewWidth / 2 - mathX * unitSize * scale
            translateY = pivotY - viewHeight / 2 + mathY * unitSize * scale
        }

        needsRecalculate = true
        invalidate()
    }

    // ⭐ 新增：重置到默认缩放
    fun resetZoom() {
        zoomTo(1f, viewWidth / 2, viewHeight / 2)
    }

    fun resetView() {
        scale = 1f
        translateX = 0f
        translateY = 0f
        trackedPoint = null
        needsRecalculate = true
        invalidate()
    }

    // =============================
    // 自定义函数
    // =============================

    inner class SqrtFunction : Function("sqrt", 1) {
        override fun apply(vararg args: Double): Double {
            val x = args[0]
            return if (x >= 0) sqrt(x) else Double.NaN
        }
    }

    inner class AbsFunction : Function("abs", 1) {
        override fun apply(vararg args: Double): Double {
            return abs(args[0])
        }
    }

    inner class TanFunction : Function("tan", 1) {
        override fun apply(vararg args: Double): Double {
            val x = args[0]
            // 检查是否在渐近线附近
            val pi = Math.PI
            val remainder = (x - pi / 2) % pi
            if (abs(remainder) < 1e-10 || abs(remainder - pi) < 1e-10) {
                return Double.NaN
            }
            return tan(x)
        }
    }

    // =============================
    // 渐近线分析
    // =============================
    private fun analyzeAsymptotes(expression: String): MutableList<Double> {
        val asymptotes = mutableListOf<Double>()

        // 1/x 类型
        if (expression.contains("/x") || expression.matches(Regex(".*1\\s*/\\s*x.*"))) {
            asymptotes.add(0.0)
        }

        // 1/(x-a) 类型
        val reciprocalPattern = Regex("""1\s*/\s*\(\s*x\s*([+-]\s*\d+\.?\d*)\s*\)""")
        val match = reciprocalPattern.find(expression)
        if (match != null) {
            val offset = match.groupValues[1].replace(" ", "").toDoubleOrNull()
            asymptotes.add(if (offset != null) -offset else 0.0)
        }

        // log(x) 类型
        if (expression.contains("log")) {
            asymptotes.add(0.0)
        }

        // ⭐ sqrt(x) 不需要渐近线，但需要标记定义域边界
        // x=0 是 sqrt 的定义域起点，不是渐近线

        return asymptotes
    }

    private fun getTanAsymptotesInRange(xMin: Double, xMax: Double): List<Double> {
        val asymptotes = mutableListOf<Double>()
        val pi = Math.PI
        val nMin = floor((xMin - pi / 2) / pi).toInt()
        val nMax = ceil((xMax - pi / 2) / pi).toInt()
        for (n in nMin..nMax) {
            val asymptote = pi / 2 + n * pi
            if (asymptote in xMin..xMax) {
                asymptotes.add(asymptote)
            }
        }
        return asymptotes
    }

    // =============================
    // 核心计算
    // =============================
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
        needsRecalculate = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (scroller.computeScrollOffset()) {
            translateX = scroller.currX.toFloat()
            translateY = scroller.currY.toFloat()

            val currentUnit = unitSize * scale
            val currentMinX = (-viewWidth / 2 - translateX) / currentUnit
            val currentMaxX = (viewWidth / 2 - translateX) / currentUnit

            val range = lastMaxX - lastMinX
            if (currentMinX < lastMinX + range * 0.15 || currentMaxX > lastMaxX - range * 0.15) {
                needsRecalculate = true
            }

            postInvalidateOnAnimation()
        }

        // ⭐ 缩放过程中不重采样，保证流畅
        if (needsRecalculate && viewWidth > 0 && viewHeight > 0 && !isScaling) {
            recalculateFunctions()
            needsRecalculate = false
        }

        canvas.drawColor(Color.WHITE)
        val currentUnit = unitSize * scale

        canvas.save()
        canvas.translate(viewWidth / 2 + translateX, viewHeight / 2 + translateY)
        canvas.scale(currentUnit, -currentUnit)

        drawGrid(canvas, currentUnit)
        drawAxis(canvas, currentUnit)

        functions.forEach { func ->
            drawFunctionPath(canvas, func, currentUnit)
        }

        drawFeaturePoints(canvas, currentUnit)
        canvas.restore()

        drawTouchMarker(canvas, currentUnit)
        drawInfoText(canvas)
    }

    private fun recalculateFunctions() {
        if (viewWidth == 0f || viewHeight == 0f) return

        val currentUnit = unitSize * scale

        val expandFactor = 10.0
        val screenMinX = (-viewWidth / 2 - translateX) / currentUnit
        val screenMaxX = (viewWidth / 2 - translateX) / currentUnit
        val screenRangeX = screenMaxX - screenMinX

        val minMathX = screenMinX - screenRangeX * (expandFactor - 1) / 2
        val maxMathX = screenMaxX + screenRangeX * (expandFactor - 1) / 2

        lastMinX = minMathX
        lastMaxX = maxMathX
        lastTranslateX = translateX
        lastScale = scale

        val pixelSize = 1.0 / currentUnit
        val baseStep = (pixelSize * 0.8).coerceIn(0.0001, 0.5)

        // ⭐ Y 轴可见范围
        val screenMinY = (-viewHeight / 2f + translateY) / currentUnit
        val screenMaxY = (viewHeight / 2f + translateY) / currentUnit
        val yMargin = (screenMaxY - screenMinY) * 0.5f  // 50% 余量就够了
        val visibleMinY = (screenMinY - yMargin).toDouble()
        val visibleMaxY = (screenMaxY + yMargin).toDouble()

        functions.forEach { func ->
            func.points.clear()
            func.zeros.clear()
            func.extremes.clear()

            val isSqrt = func.originalString.contains("sqrt")
            val isTan = func.originalString.contains("tan")

            if (isSqrt) {
                val startX = maxOf(minMathX, 0.0)
                val endX = maxMathX
                if (startX < endX) {
                    sampleSqrtFunction(func, startX, endX, baseStep, visibleMinY, visibleMaxY)
                }
            } else {
                val allAsymptotes = func.asymptotes.toMutableList()
                if (isTan) {
                    allAsymptotes.addAll(getTanAsymptotesInRange(minMathX, maxMathX))
                }
                allAsymptotes.sort()

                // ⭐⭐⭐ 核心改动：safeGap 改小！
                // 只需要一个极小的间隙防止除以零，剩下的交给 Y 轴裁剪
                val safeGap = 1e-6  // 固定极小值，约等于 0

                val intervals = calculateSafeIntervals(minMathX, maxMathX, allAsymptotes, safeGap)

                for ((startX, endX) in intervals) {
                    sampleInterval(func, startX, endX, baseStep, visibleMinY, visibleMaxY)
                }
            }

            detectZerosAndExtremes(func)
        }
    }


    private fun sampleSqrtFunction(
        func: PlotFunction,
        xStart: Double,
        xEnd: Double,
        baseStep: Double,
        visibleMinY: Double,
        visibleMaxY: Double
    ) {
        var x = xStart
        var hasValidPoints = false

        while (x <= xEnd) {
            try {
                val y = func.expression.setVariable("x", x).evaluate()

                if (y.isFinite() && y >= visibleMinY && y <= visibleMaxY) {
                    func.points.add(PointF(x.toFloat(), y.toFloat()))
                    hasValidPoints = true
                } else {
                    if (hasValidPoints) {
                        func.points.add(PointF(Float.NaN, Float.NaN))
                        hasValidPoints = false
                    }
                }
            } catch (e: Exception) {
                if (hasValidPoints) {
                    func.points.add(PointF(Float.NaN, Float.NaN))
                    hasValidPoints = false
                }
            }

            // ⭐ 靠近 x=0 加密采样
            val stepMultiplier = if (x < baseStep * 10) 0.1 else 1.0
            x += maxOf(baseStep * stepMultiplier, 1e-9)
        }

        // 强制确保 (0, 0) 被采样
        try {
            val yAtZero = func.expression.setVariable("x", 0.0).evaluate()
            if (yAtZero.isFinite() && abs(yAtZero) < 1e-4) {
                val firstValidIndex = func.points.indexOfFirst { !it.x.isNaN() }
                if (firstValidIndex >= 0) {
                    val firstPoint = func.points[firstValidIndex]
                    if (abs(firstPoint.x) < baseStep * 2) {
                        func.points[firstValidIndex] = PointF(0f, 0f)
                    } else {
                        func.points.add(firstValidIndex, PointF(0f, 0f))
                    }
                } else {
                    func.points.add(PointF(0f, 0f))
                }
                func.zeros.add(PointF(0f, 0f))
            }
        } catch (e: Exception) { }
    }

    private fun sampleInterval(
        func: PlotFunction,
        xStart: Double,
        xEnd: Double,
        baseStep: Double,
        visibleMinY: Double,
        visibleMaxY: Double
    ) {
        var x = xStart
        var hasValidPoints = false

        while (x <= xEnd) {
            try {
                val y = func.expression.setVariable("x", x).evaluate()

                if (y.isFinite() && y >= visibleMinY && y <= visibleMaxY) {
                    // ⭐ 有效点：在屏幕 Y 范围内
                    func.points.add(PointF(x.toFloat(), y.toFloat()))
                    hasValidPoints = true
                } else {
                    // ⭐ Y 超出范围 → 断开
                    if (hasValidPoints) {
                        func.points.add(PointF(Float.NaN, Float.NaN))
                        hasValidPoints = false
                    }
                }
            } catch (e: Exception) {
                if (hasValidPoints) {
                    func.points.add(PointF(Float.NaN, Float.NaN))
                    hasValidPoints = false
                }
            }

            // ⭐⭐⭐ 自适应步长：靠近渐近线时大幅加密采样
            // 对于 1/x 和 log(x)，渐近线在 x=0
            val stepMultiplier = when {
                abs(x) < 0.001 -> 0.01   // 非常靠近渐近线
                abs(x) < 0.01  -> 0.05
                abs(x) < 0.1   -> 0.2
                abs(x) < 1.0   -> 0.5
                else -> 1.0
            }

            x += maxOf(baseStep * stepMultiplier, 1e-9)
        }

        if (hasValidPoints) {
            func.points.add(PointF(Float.NaN, Float.NaN))
        }
    }

    private fun calculateSafeIntervals(
        xMin: Double,
        xMax: Double,
        asymptotes: List<Double>,
        safeGap: Double
    ): List<Pair<Double, Double>> {
        val intervals = mutableListOf<Pair<Double, Double>>()
        var currentStart = xMin

        for (asymptote in asymptotes) {
            if (asymptote in xMin..xMax) {
                if (currentStart < asymptote - safeGap) {
                    intervals.add(Pair(currentStart, asymptote - safeGap))
                }
                currentStart = asymptote + safeGap
            }
        }

        if (currentStart < xMax) {
            intervals.add(Pair(currentStart, xMax))
        }

        return intervals.filter { it.first < it.second }
    }

    private fun detectZerosAndExtremes(func: PlotFunction) {
        func.zeros.clear()
        func.extremes.clear()
        if (func.points.size < 1) return

        val screenTolerance = 3.0f / (unitSize * scale)

        for (i in 0 until func.points.size) {
            val p = func.points[i]
            if (p.x.isNaN() || p.y.isNaN()) continue

            // 1. --- 边界零点检测 (修复 sqrt(x) ) ---
            // 如果是路径的第一个有效点，或者前一个点是 NaN（断裂处）
            val isStartOfSegment = (i == 0 || func.points[i - 1].x.isNaN())
            val isEndOfSegment = (i == func.points.size - 1 || func.points[i + 1].x.isNaN())

            if (isStartOfSegment || isEndOfSegment) {
                // 在边界处，如果 y 非常接近 0，则判定为零点
                if (abs(p.y) < 1e-6f) {
                    addFeaturePoint(func.zeros, p, screenTolerance)
                }
            }

            // 2. --- 中间段穿轴检测 (x^3, exp(x) 正常工作) ---
            if (i < func.points.size - 1) {
                val nextP = func.points[i + 1]
                if (!nextP.x.isNaN()) {
                    if (p.y * nextP.y < 0 && abs(p.y - nextP.y) < 10) {
                        val midX = (p.x + nextP.x) / 2f
                        addFeaturePoint(func.zeros, PointF(midX, 0f), screenTolerance)
                    }
                }
            }

            // 3. --- 切点检测 (x^2) ---
            if (i > 0 && i < func.points.size - 1) {
                val prevP = func.points[i - 1]
                val nextP = func.points[i + 1]
                if (!prevP.x.isNaN() && !nextP.x.isNaN()) {
                    // 如果当前点 y 为 0，且两边符号相同且非零（说明是切点）
                    if (p.y == 0f && prevP.y * nextP.y > 0) {
                        addFeaturePoint(func.zeros, p, screenTolerance)
                    }

                    // 4. --- 极值判定 ---
                    if ((p.y > prevP.y && p.y > nextP.y) || (p.y < prevP.y && p.y < nextP.y)) {
                        addFeaturePoint(func.extremes, p, screenTolerance)
                    }
                }
            }
        }
    }

    private fun addFeaturePoint(list: MutableList<PointF>, newPoint: PointF, tolerance: Float) {
        // 关键：基于当前缩放比例去重，彻底解决“放大后一堆点”的问题
        if (list.none { abs(it.x - newPoint.x) < tolerance }) {
            list.add(newPoint)
        }
    }


    private fun handleTap(x: Float, y: Float) {
        val currentUnit = unitSize * scale
        val tapMathX = (x - viewWidth / 2 - translateX) / currentUnit
        val tapMathY = -(y - viewHeight / 2 - translateY) / currentUnit

        var minDist = Float.MAX_VALUE
        var closestPoint: PointF? = null
        var closestIndex = -1

        functions.forEachIndexed { index, func ->
            try {
                val exactY = func.expression.setVariable("x", tapMathX.toDouble()).evaluate()
                if (exactY.isFinite()) {
                    val dist = abs(tapMathY - exactY.toFloat())
                    if (dist < minDist && dist * currentUnit < 100f) {
                        minDist = dist
                        closestPoint = PointF(tapMathX.toFloat(), exactY.toFloat())
                        closestIndex = index
                    }
                }
            } catch (_: Exception) { }
        }

        trackedPoint = closestPoint
        trackedFunctionIndex = if (closestPoint != null) closestIndex else -1
        invalidate()
    }

    // =============================
    // 绘制
    // =============================
    private fun drawFunctionPath(canvas: Canvas, func: PlotFunction, currentUnit: Float) {
        if (func.points.size < 2) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = func.color
            strokeWidth = 4f / currentUnit
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        var path = Path()
        var segmentPoints = 0

        for (point in func.points) {
            if (point.x.isNaN() || point.y.isNaN()) {
                if (segmentPoints >= 2) {
                    canvas.drawPath(path, paint)
                }
                path = Path()
                segmentPoints = 0
            } else {
                if (segmentPoints == 0) {
                    path.moveTo(point.x, point.y)
                    segmentPoints = 1
                } else {
                    path.lineTo(point.x, point.y)
                    segmentPoints++
                }
            }
        }

        if (segmentPoints >= 2) {
            canvas.drawPath(path, paint)
        }
    }

    private fun drawGrid(canvas: Canvas, currentUnit: Float) {
        val targetPixelInterval = 80f
        var rawStep = targetPixelInterval / currentUnit
        val mag = 10.0.pow(floor(log10(rawStep.toDouble()))).toFloat()
        val norm = rawStep / mag
        val mathStep = when {
            norm < 2 -> mag * 1
            norm < 5 -> mag * 2
            else -> mag * 5
        }

        gridPaint.strokeWidth = 1f / currentUnit

        val halfWidth = viewWidth / 2f / currentUnit
        val halfHeight = viewHeight / 2f / currentUnit
        val centerX = -translateX / currentUnit
        val centerY = translateY / currentUnit

        val leftMath = centerX - halfWidth
        val rightMath = centerX + halfWidth
        val bottomMath = centerY - halfHeight
        val topMath = centerY + halfHeight

        var x = floor(leftMath / mathStep) * mathStep
        while (x <= rightMath) {
            canvas.drawLine(x, bottomMath, x, topMath, gridPaint)
            x += mathStep
        }

        var y = floor(bottomMath / mathStep) * mathStep
        while (y <= topMath) {
            canvas.drawLine(leftMath, y, rightMath, y, gridPaint)
            y += mathStep
        }
    }

    private fun drawAxis(canvas: Canvas, currentUnit: Float) {
        axisPaint.strokeWidth = 2.5f / currentUnit

        val halfWidth = viewWidth / 2f / currentUnit
        val halfHeight = viewHeight / 2f / currentUnit
        val centerX = -translateX / currentUnit
        val centerY = translateY / currentUnit

        val leftMath = centerX - halfWidth
        val rightMath = centerX + halfWidth
        val bottomMath = centerY - halfHeight
        val topMath = centerY + halfHeight

        canvas.drawLine(leftMath, 0f, rightMath, 0f, axisPaint)
        canvas.drawLine(0f, bottomMath, 0f, topMath, axisPaint)

        val arrowSize = 10f / currentUnit
        canvas.drawLine(rightMath - arrowSize, arrowSize, rightMath, 0f, axisPaint)
        canvas.drawLine(rightMath - arrowSize, -arrowSize, rightMath, 0f, axisPaint)
        canvas.drawLine(-arrowSize, topMath - arrowSize, 0f, topMath, axisPaint)
        canvas.drawLine(arrowSize, topMath - arrowSize, 0f, topMath, axisPaint)
    }

    private fun drawFeaturePoints(canvas: Canvas, currentUnit: Float) {
        val radius = 8f / currentUnit
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f / currentUnit
        }

        functions.forEach { func ->
            // ⭐ 零点：红色，带白边确保可见
            pointPaint.color = Color.RED
            func.zeros.forEach { zero ->
                canvas.drawCircle(zero.x, zero.y, radius, pointPaint)
                canvas.drawCircle(zero.x, zero.y, radius, strokePaint)
            }

            // 极值点：绿色
            pointPaint.color = Color.GREEN
            func.extremes.forEach { extreme ->
                canvas.drawCircle(extreme.x, extreme.y, radius, pointPaint)
                canvas.drawCircle(extreme.x, extreme.y, radius, strokePaint)
            }
        }
    }

    private fun drawTouchMarker(canvas: Canvas, currentUnit: Float) {
        val point = trackedPoint ?: return
        val index = trackedFunctionIndex

        val sx = viewWidth / 2 + translateX + point.x * currentUnit
        val sy = viewHeight / 2 + translateY - point.y * currentUnit

        canvas.drawLine(0f, sy, viewWidth, sy, markerPaint)
        canvas.drawLine(sx, 0f, sx, viewHeight, markerPaint)

        val label = "(${String.format("%.2f", point.x)}, ${String.format("%.2f", point.y)})"
        val textWidth = textPaint.measureText(label)
        val textHeight = textPaint.textSize

        val labelX = minOf(sx + 20, viewWidth - textWidth - 20)
        val labelY = maxOf(sy - 20, textHeight + 10)

        canvas.drawRect(
            labelX - 10, labelY - textHeight - 5,
            labelX + textWidth + 10, labelY + 10,
            Paint().apply { color = Color.WHITE; setShadowLayer(5f, 0f, 0f, Color.GRAY) }
        )

        textPaint.color = if (index >= 0 && index < functions.size) functions[index].color else Color.BLACK
        canvas.drawText(label, labelX, labelY, textPaint)
    }

    private fun drawInfoText(canvas: Canvas) {
        val infoText = functions.joinToString("\n") { "f(x)=${it.originalString}" }
        if (infoText.isNotEmpty()) {
            val measureText = textPaint.measureText(infoText)
            canvas.drawText(infoText, viewWidth - measureText - 20f, viewHeight - 100f, textPaint)
        }
        // ⭐ 添加缩放比例显示
        drawScaleInfo(canvas)
    }

    // ⭐ 新增方法：绘制缩放信息
    private fun drawScaleInfo(canvas: Canvas) {
        // 保存画笔状态
        val originalTextSize = textPaint.textSize
        val originalFakeBold = textPaint.isFakeBoldText
        val originalColor = textPaint.color

        // 设置缩放信息专用样式
        textPaint.textSize = 40f
        textPaint.isFakeBoldText = true
        textPaint.color = Color.parseColor("#FF4444") // 红色，醒目

        val scalePercent = (scale * 100).toInt()
        val scaleText = "缩放: ${scalePercent}%"

        // 如果接近限制，显示警告颜色
        if (scale <= minScale * 1.5f) {
            textPaint.color = Color.parseColor("#FF8800") // 橙色警告
            val limitText = " (已达最小缩放)"
            val combinedText = scaleText + limitText
            val textWidth = textPaint.measureText(combinedText)
            canvas.drawText(combinedText, viewWidth - textWidth - 20f, viewHeight - 40f, textPaint)
        } else if (scale >= maxScale * 0.8f) {
            textPaint.color = Color.parseColor("#FF8800") // 橙色警告
            val limitText = " (接近最大缩放)"
            val combinedText = scaleText + limitText
            val textWidth = textPaint.measureText(combinedText)
            canvas.drawText(combinedText, viewWidth - textWidth - 20f, viewHeight - 40f, textPaint)
        } else {
            val textWidth = textPaint.measureText(scaleText)
            canvas.drawText(scaleText, viewWidth - textWidth - 20f, viewHeight - 40f, textPaint)
        }

        // ⭐ 添加缩放指示器（矩形条）
        drawScaleIndicator(canvas)

        // 恢复画笔状态
        textPaint.textSize = originalTextSize
        textPaint.isFakeBoldText = originalFakeBold
        textPaint.color = originalColor
    }

    // ⭐ 新增方法：绘制缩放比例指示器
    private fun drawScaleIndicator(canvas: Canvas) {
        val indicatorWidth = 150f
        val indicatorHeight = 8f
        val padding = 20f
        val x = viewWidth - indicatorWidth - padding
        val y = viewHeight - padding

        // 背景条
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
        }
        canvas.drawRoundRect(x, y, x + indicatorWidth, y + indicatorHeight, 4f, 4f, bgPaint)

        // 当前缩放条
        val progress = (scale - minScale) / (maxScale - minScale)
        val currentWidth = indicatorWidth * progress.coerceIn(0f, 1f)

        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when {
                progress < 0.1f -> Color.parseColor("#FF8800") // 橙色
                progress < 0.3f -> Color.parseColor("#4CAF50") // 绿色
                progress < 0.7f -> Color.parseColor("#2196F3") // 蓝色
                progress < 0.9f -> Color.parseColor("#FF9800") // 橙色
                else -> Color.parseColor("#F44336") // 红色
            }
        }
        canvas.drawRoundRect(x, y, x + currentWidth, y + indicatorHeight, 4f, 4f, progressPaint)

        // 指示器边框
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#BDBDBD")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(x, y, x + indicatorWidth, y + indicatorHeight, 4f, 4f, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        if (scaleDetector.isInProgress) return true
        return gestureDetector.onTouchEvent(event)
    }
}