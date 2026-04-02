package com.core.app.supercalchub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.core.app.supercalchub.features.verticalcalc.VerticalCalculator
import com.core.app.supercalchub.features.verticalcalc.VerticalResult
import com.core.app.supercalchub.ui.navigation.NavigationUtils
import java.math.BigDecimal

/**
 * 竖式计算界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalCalcScreen(navController: NavController) {
    val calculator = remember { VerticalCalculator() }
    
    var num1 by remember { mutableStateOf("") }
    var num2 by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf('+') }
    var result by remember { mutableStateOf<VerticalResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("竖式计算") },
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
        ) {
            // 输入区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 第一个数输入
                    OutlinedTextField(
                        value = num1,
                        onValueChange = { num1 = it },
                        label = { Text("第一个数") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 运算符选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf('+', '-', '×', '÷').forEach { op ->
                            Button(
                                onClick = { operator = op },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (operator == op) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(op.toString(), fontSize = 20.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 第二个数输入
                    OutlinedTextField(
                        value = num2,
                        onValueChange = { num2 = it },
                        label = { Text("第二个数") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 计算按钮
                    Button(
                        onClick = {
                            try {
                                val n1 = BigDecimal(num1)
                                val n2 = BigDecimal(num2)
                                result = calculator.calculateVertical(n1, n2, operator)
                            } catch (e: Exception) {
                                // 处理错误
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = num1.isNotEmpty() && num2.isNotEmpty()
                    ) {
                        Text("计算", fontSize = 18.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 结果显示
            result?.let { verticalResult ->
                when (verticalResult) {
                    is VerticalResult.Success -> {
                        VerticalResultDisplay(verticalResult)
                    }
                    is VerticalResult.Error -> {
                        Text(
                            text = verticalResult.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 竖式结果展示
 */
@Composable
fun VerticalResultDisplay(result: VerticalResult.Success) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // 竖式显示
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "竖式计算",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = result.steps.lastOrNull()?.display ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "结果: ${result.formattedResult}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 计算步骤
        item {
            Text(
                text = "计算步骤",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(result.steps) { step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "步骤 ${step.stepNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    step.carry?.let { carry ->
                        Text(
                            text = if (carry > 0) "进位: $carry" else "借位: ${-carry}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}