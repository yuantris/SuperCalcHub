package com.core.app.supercalchub.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.core.app.supercalchub.features.functionplot.FunctionPlotView
import com.core.app.supercalchub.ui.navigation.NavigationUtils

/**
 * 函数绘制界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionPlotScreen(navController: NavController) {
    var functionExpression by remember { mutableStateOf("sin(x)") }
    var plotView by remember { mutableStateOf<FunctionPlotView?>(null) }

    // 预设函数列表
    val presetFunctions = listOf(
        "sin(x)" to "sin(x)",
        "cos(x)" to "cos(x)",
        "tan(x)" to "tan(x)",
        "x²" to "x^2",
        "x³" to "x^3",
        "√x" to "sqrt(x)",
        "1/x" to "1/x",
        "ln(x)" to "ln(x)",
        "log(x)" to "log(x)",
        "eˣ" to "exp(x)",
        "|x|" to "abs(x)",
        "2x+1" to "2*x+1"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("函数绘制") },
                navigationIcon = {
                    IconButton(onClick = { NavigationUtils.safePopBack(navController) }) {
                        Text("←")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 函数绘制区域 - 背景
            AndroidView(
                factory = { context ->
                    FunctionPlotView(context).apply {
                        plotView = this
                        setFunction(functionExpression)
                    }
                },
                update = { view ->
                    view.setFunction(functionExpression)
                },
                modifier = Modifier.fillMaxSize()
            )

            // 输入区域 - 覆盖在上方
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = functionExpression,
                        onValueChange = { functionExpression = it },
                        label = { Text("f(x) =") },
                        placeholder = { Text("sin(x), x^2, 2*x+1") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "快捷函数",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(presetFunctions) { (label, expr) ->
                            AssistChip(
                                onClick = {
                                    functionExpression = expr
                                    plotView?.setFunction(expr)
                                },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            plotView?.setFunction(functionExpression)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("绘制", fontSize = 16.sp)
                    }
                }
            }

            // 控制按钮
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                    SmallFloatingActionButton(
                        onClick = { plotView?.setScale(1.5f) },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text("+", fontSize = 20.sp)
                    }

                    SmallFloatingActionButton(
                        onClick = { plotView?.setScale(0.67f) },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text("-", fontSize = 20.sp)
                    }

                    SmallFloatingActionButton(
                        onClick = {
                            plotView?.resetView()
                            plotView?.setFunction(functionExpression)
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text("⟲", fontSize = 16.sp)
                    }
                }

        }
    }
}