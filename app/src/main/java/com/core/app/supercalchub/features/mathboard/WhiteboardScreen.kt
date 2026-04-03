package com.core.app.supercalchub.features.mathboard

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteboardScreen(vm: BoardViewModel, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var editingTitle by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it && vm.saveBoard()) Toast.makeText(ctx, "已保存到相册", Toast.LENGTH_SHORT).show() }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            // ── 顶栏 ──
            TopAppBar(
                title = {
                    if (editingTitle) {
                        AndroidView(factory = { c ->
                            android.widget.EditText(c).apply {
                                setText(vm.title); setTextColor(Color.Black.hashCode())
                                textSize = 17f; isSingleLine = true; setSelectAllOnFocus(true)
                                setOnEditorActionListener { _, _, _ -> vm.title = text.toString(); editingTitle = false; true }
                                setOnFocusChangeListener { _, f -> if (!f) { vm.title = text.toString(); editingTitle = false } }
                            }
                        }, update = { e -> if (!e.hasFocus() && e.text.toString() != vm.title) e.setText(vm.title) },
                            modifier = Modifier.height(40.dp).clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF0F0F0)).padding(horizontal = 8.dp))
                    } else {
                        Text(vm.title, fontSize = 17.sp, modifier = Modifier.clickable { editingTitle = true })
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { vm.requestExit() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { editingTitle = true }) { Icon(Icons.Rounded.Edit, "编辑") }
                    IconButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (vm.saveBoard()) Toast.makeText(ctx, "已保存到相册", Toast.LENGTH_SHORT).show()
                        } else permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }) { Icon(Icons.Rounded.Star, "保存") }
                    IconButton(onClick = {
                        Toast.makeText(ctx, "分享", Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Rounded.Share, "分享") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )

            // ── 画布区域 ──
            Box(Modifier.weight(1f)) {
                AndroidView(
                    factory = { c ->
                        MathBoardView(c).also { v ->
                            vm.boardView = v
                            v.onTextTap = { vm.handleTextTap(it) }
                            v.onPolygonTap = { vm.handlePolygonTap(it) }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // 右侧浮动工具栏
                ToolSidebar(vm, Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp))
            }
        }

        // 底部面板
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            TextEntryPanel(vm)
            PolygonCustomizer(vm)
        }

        // 退出弹窗
        if (vm.showExitDialog) {
            AlertDialog(
                onDismissRequest = { vm.dismissExit() },
                title = { Text("提示") },
                text = { Text("确认要退出数学白板吗？") },
                confirmButton = {
                    Button(onClick = { vm.dismissExit(); onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("确认") }
                },
                dismissButton = { OutlinedButton(onClick = { vm.dismissExit() }) { Text("取消") } }
            )
        }
    }
}
