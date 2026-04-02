package com.core.app.supercalchub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.core.app.supercalchub.core.math.EquationSolver
import com.core.app.supercalchub.core.math.EquationResult
import com.core.app.supercalchub.ui.navigation.NavigationUtils

/**
 * 解方程界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquationScreen(navController: NavController) {
    val solver = remember { EquationSolver() }
    
    var selectedType by remember { mutableStateOf(EquationType.LINEAR) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("解方程") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 方程类型选择
            Text(
                text = "选择方程类型",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EquationType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label, fontSize = 12.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 根据类型显示不同的输入界面
            when (selectedType) {
                EquationType.LINEAR -> LinearEquationSolver(solver)
                EquationType.QUADRATIC -> QuadraticEquationSolver(solver)
                EquationType.SYSTEM -> LinearSystemSolver(solver)
            }
        }
    }
}

/**
 * 方程类型
 */
enum class EquationType(val label: String) {
    LINEAR("一元一次"),
    QUADRATIC("一元二次"),
    SYSTEM("二元一次方程组")
}

/**
 * 一元一次方程求解器：ax + b = 0
 */
@Composable
fun LinearEquationSolver(solver: EquationSolver) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "一元一次方程：ax + b = 0",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = a,
                    onValueChange = { a = it },
                    label = { Text("a") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = b,
                    onValueChange = { b = it },
                    label = { Text("b") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    try {
                        val aVal = a.toDoubleOrNull() ?: 0.0
                        val bVal = b.toDoubleOrNull() ?: 0.0
                        result = solver.solveLinearEquation(aVal, bVal)
                    } catch (e: Exception) {
                        result = EquationResult.Error(e.message ?: "计算错误")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = a.isNotEmpty()
            ) {
                Text("求解")
            }
            
            result?.let { EquationResultDisplay(it) }
        }
    }
}

/**
 * 一元二次方程求解器：ax² + bx + c = 0
 */
@Composable
fun QuadraticEquationSolver(solver: EquationSolver) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "一元二次方程：ax² + bx + c = 0",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = a,
                    onValueChange = { a = it },
                    label = { Text("a") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = b,
                    onValueChange = { b = it },
                    label = { Text("b") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = c,
                    onValueChange = { c = it },
                    label = { Text("c") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 示例按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { a = "1"; b = "-5"; c = "6" }) {
                    Text("x²-5x+6=0")
                }
                TextButton(onClick = { a = "1"; b = "0"; c = "-4" }) {
                    Text("x²-4=0")
                }
            }
            
            Button(
                onClick = {
                    try {
                        val aVal = a.toDoubleOrNull() ?: 0.0
                        val bVal = b.toDoubleOrNull() ?: 0.0
                        val cVal = c.toDoubleOrNull() ?: 0.0
                        result = solver.solveQuadraticEquation(aVal, bVal, cVal)
                    } catch (e: Exception) {
                        result = EquationResult.Error(e.message ?: "计算错误")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = a.isNotEmpty()
            ) {
                Text("求解")
            }
            
            result?.let { EquationResultDisplay(it) }
        }
    }
}

/**
 * 二元一次方程组求解器
 * a1*x + b1*y = c1
 * a2*x + b2*y = c2
 */
@Composable
fun LinearSystemSolver(solver: EquationSolver) {
    var a1 by remember { mutableStateOf("") }
    var b1 by remember { mutableStateOf("") }
    var c1 by remember { mutableStateOf("") }
    var a2 by remember { mutableStateOf("") }
    var b2 by remember { mutableStateOf("") }
    var c2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "二元一次方程组",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "a₁x + b₁y = c₁",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "a₂x + b₂y = c₂",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 第一个方程
            Text("方程 1:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = a1,
                    onValueChange = { a1 = it },
                    label = { Text("a₁") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = b1,
                    onValueChange = { b1 = it },
                    label = { Text("b₁") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = c1,
                    onValueChange = { c1 = it },
                    label = { Text("c₁") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 第二个方程
            Text("方程 2:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = a2,
                    onValueChange = { a2 = it },
                    label = { Text("a₂") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = b2,
                    onValueChange = { b2 = it },
                    label = { Text("b₂") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = c2,
                    onValueChange = { c2 = it },
                    label = { Text("c₂") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 示例按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { 
                    a1 = "1"; b1 = "1"; c1 = "5"
                    a2 = "2"; b2 = "-1"; c2 = "1"
                }) {
                    Text("示例1")
                }
                TextButton(onClick = { 
                    a1 = "3"; b1 = "2"; c1 = "12"
                    a2 = "1"; b2 = "1"; c2 = "5"
                }) {
                    Text("示例2")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    try {
                        result = solver.solveLinearSystem(
                            a1.toDoubleOrNull() ?: 0.0,
                            b1.toDoubleOrNull() ?: 0.0,
                            c1.toDoubleOrNull() ?: 0.0,
                            a2.toDoubleOrNull() ?: 0.0,
                            b2.toDoubleOrNull() ?: 0.0,
                            c2.toDoubleOrNull() ?: 0.0
                        )
                    } catch (e: Exception) {
                        result = EquationResult.Error(e.message ?: "计算错误")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = a1.isNotEmpty() && a2.isNotEmpty()
            ) {
                Text("求解")
            }
            
            result?.let { EquationResultDisplay(it) }
        }
    }
}

/**
 * 方程结果展示
 */
@Composable
fun EquationResultDisplay(result: EquationResult) {
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            when (result) {
                is EquationResult.SingleSolution -> {
                    Text(
                        text = "解：x = ${result.formattedSolution}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                is EquationResult.DoubleSolution -> {
                    Text(
                        text = "解：x = ${result.formattedSolution}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "（两个相等的实根）",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is EquationResult.TwoSolutions -> {
                    Text(
                        text = "解：",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "x₁ = ${result.formattedSolution1}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "x₂ = ${result.formattedSolution2}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is EquationResult.ComplexSolutions -> {
                    Text(
                        text = "复数解：",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "x₁ = ${result.formattedSolution1}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "x₂ = ${result.formattedSolution2}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is EquationResult.LinearSystemSolution -> {
                    Text(
                        text = "解：",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "x = ${result.formattedX}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "y = ${result.formattedY}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is EquationResult.InfiniteSolutions -> {
                    Text(
                        text = "方程有无穷多解",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                is EquationResult.NoSolution -> {
                    Text(
                        text = "方程无解",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is EquationResult.Error -> {
                    Text(
                        text = "错误：${result.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}