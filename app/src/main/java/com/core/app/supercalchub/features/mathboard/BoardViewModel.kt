package com.core.app.supercalchub.features.mathboard

import android.R.attr.strokeWidth
import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BoardViewModel(app: Application) : AndroidViewModel(app) {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var title by mutableStateOf(sdf.format(Date()) + "·数学白板")
    var currentTool by mutableStateOf(DrawingTool.Freehand)
    var strokeColor by mutableStateOf(Color.Black)
    var strokeWidths by mutableFloatStateOf(8f)
    var fillShapes by mutableStateOf(false)
    var autoShape by mutableStateOf(true)
    var polygonSettings by mutableStateOf(PolygonSettings())

    var showTextPanel by mutableStateOf(false)
        private set
    var showPolygonPanel by mutableStateOf(false)
        private set
    var showExitDialog by mutableStateOf(false)
        private set

    // Text input
    var textInput by mutableStateOf("")
    var textFontSize by mutableFloatStateOf(32f)
    var textColor by mutableStateOf(Color.Black)

    // View reference
    var boardView: MathBoardView? = null

    /* ── 工具选择 ── */
    fun selectTool(t: DrawingTool) {
        currentTool = t
        boardView?.setTool(t)
        showTextPanel = t == DrawingTool.Text
        showPolygonPanel = t == DrawingTool.Polygon
    }
    fun setColor(c: Color) { strokeColor = c; textColor = c; boardView?.setColor(c) }
    fun setStrokeWidth(w: Float) { strokeWidths = w; boardView?.setStrokeW(w) }
    fun toggleFill() { fillShapes = !fillShapes; boardView?.setFill(fillShapes) }
    fun toggleAutoShape() { autoShape = !autoShape; boardView?.setAutoShape(autoShape) }
    fun updatePolygon(s: PolygonSettings) { polygonSettings = s }

    /* ── 撤销 / 重做 / 清空 ── */
    fun undo() { boardView?.undo() }
    fun redo() { boardView?.redo() }
    fun clearAll() { boardView?.clearAll() }

    /* ── 文字 / 多边形放置 ── */
    fun handleTextTap(pos: Offset) {
        if (textInput.isBlank()) return
        boardView?.addText(pos, textInput.trim(), textColor, textFontSize)
        textInput = ""
    }
    fun handlePolygonTap(pos: Offset) {
        val s = polygonSettings
        boardView?.addPolygon(pos, 80f, s.sides, s.fill, s.strokeWidth, s.fillColor, s.strokeColor)
    }

    /* ── 弹窗 ── */
    fun requestExit() { showExitDialog = true }
    fun dismissExit() { showExitDialog = false }

    /* ── 保存到相册 ── */
    fun saveBoard(): Boolean {
        val ctx = getApplication<Application>()
        val bmp = boardView?.captureBitmap() ?: return false
        val resolver = ctx.contentResolver
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "MathWhiteboard")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: return false
        resolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cv.clear(); cv.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, cv, null, null)
        }
        return true
    }
}
