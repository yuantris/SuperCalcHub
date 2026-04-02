package com.core.app.supercalchub.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.core.app.supercalchub.core.math.Fraction
import com.core.app.supercalchub.core.math.FractionCalculator
import com.core.app.supercalchub.core.math.FractionResult
import com.core.app.supercalchub.ui.navigation.NavigationUtils

/**
 * 分数计算器界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FractionScreen(navController: NavController) {
    val calculator = remember { FractionCalculator() }
    
    // 表达式各部分：分子列表、分母列表、运算符列表
    var expressionParts by remember { mutableStateOf<List<FractionPart>>(emptyList()) }
    var currentInput by remember { mutableStateOf("") }
    var isEnteringDenominator by remember { mutableStateOf(false) }
    var currentNumerator by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<FractionResult?>(null) }
    
    // 构建显示表达式
    val displayExpression = buildString {
        expressionParts.forEach { part ->
            when (part) {
                is FractionPart.Fraction -> append("${part.numerator}/${part.denominator}")
                is FractionPart.Operator -> append(part.op)
            }
        }
        if (currentNumerator.isNotEmpty()) {
            if (isEnteringDenominator) {
                append("$currentNumerator/$currentInput")
            } else {
                append(currentNumerator)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分数计算") },
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
                .padding(8.dp)
        ) {
            // 显示区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    // 表达式显示
                    Text(
                        text = displayExpression.ifEmpty { "0" },
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // 当前正在输入的分数
                    if (currentNumerator.isNotEmpty()) {
                        FractionDisplay(
                            numerator = currentNumerator,
                            denominator = if (isEnteringDenominator) currentInput.ifEmpty { "?" } else null,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    
                    // 结果显示
                    result?.let { fractionResult ->
                        when (fractionResult) {
                            is FractionResult.Success -> {
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "=",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    // 特殊情况：结果是整数
                                    if (fractionResult.fraction.isInteger()) {
                                        Text(
                                            text = fractionResult.fraction.numerator.toString(),
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else {
                                        FractionDisplay(
                                            numerator = fractionResult.fraction.numerator.toString(),
                                            denominator = fractionResult.fraction.denominator.toString(),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }
                            is FractionResult.Error -> {
                                Text(
                                    text = fractionResult.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 自定义键盘
            FractionKeyboard(
                onNumberClick = { number ->
                    if (!isEnteringDenominator) {
                        currentNumerator += number
                    } else {
                        currentInput += number
                    }
                },
                onOperatorClick = { operator ->
                    // 如果正在输入分数，先完成分数
                    if (currentNumerator.isNotEmpty()) {
                        val denom = if (isEnteringDenominator && currentInput.isNotEmpty()) currentInput else "1"
                        expressionParts = expressionParts + FractionPart.Fraction(currentNumerator, denom)
                        currentNumerator = ""
                        currentInput = ""
                        isEnteringDenominator = false
                    }
                    // 添加运算符
                    if (expressionParts.isNotEmpty() && expressionParts.last() is FractionPart.Fraction) {
                        expressionParts = expressionParts + FractionPart.Operator(operator)
                    }
                },
                onFractionClick = {
                    if (currentNumerator.isNotEmpty() && !isEnteringDenominator) {
                        isEnteringDenominator = true
                        currentInput = ""
                    }
                },
                onClear = {
                    expressionParts = emptyList()
                    currentNumerator = ""
                    currentInput = ""
                    isEnteringDenominator = false
                    result = null
                },
                onDelete = {
                    if (isEnteringDenominator && currentInput.isNotEmpty()) {
                        currentInput = currentInput.dropLast(1)
                    } else if (!isEnteringDenominator && currentNumerator.isNotEmpty()) {
                        currentNumerator = currentNumerator.dropLast(1)
                    } else if (isEnteringDenominator && currentInput.isEmpty()) {
                        isEnteringDenominator = false
                    } else if (expressionParts.isNotEmpty()) {
                        expressionParts = expressionParts.dropLast(1)
                    }
                },
                onCalculate = {
                    // 完成当前分数输入
                    if (currentNumerator.isNotEmpty()) {
                        val denom = if (isEnteringDenominator && currentInput.isNotEmpty()) currentInput else "1"
                        expressionParts = expressionParts + FractionPart.Fraction(currentNumerator, denom)
                        currentNumerator = ""
                        currentInput = ""
                        isEnteringDenominator = false
                    }
                    // 构建表达式并计算
                    val expression = buildString {
                        expressionParts.forEach { part ->
                            when (part) {
                                is FractionPart.Fraction -> append("${part.numerator}/${part.denominator}")
                                is FractionPart.Operator -> append(part.op)
                            }
                        }
                    }
                    if (expression.isNotEmpty()) {
                        result = calculator.calculate(expression)
                    }
                }
            )
        }
    }
}

/**
 * 表达式部分
 */
sealed class FractionPart {
    data class Fraction(val numerator: String, val denominator: String) : FractionPart()
    data class Operator(val op: String) : FractionPart()
}

/**
 * 分数显示组件
 * @param denominator 如果为null，则只显示分子（整数）
 */
@Composable
fun FractionDisplay(
    numerator: String,
    denominator: String?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (denominator == null) {
        // 只显示整数
        Text(
            text = numerator,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 分子
            Text(
                text = numerator,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            
            // 分数线
            Canvas(
                modifier = Modifier
                    .width(50.dp)
                    .height(2.dp)
            ) {
                drawLine(
                    color = color,
                    start = Offset(0f, 1f),
                    end = Offset(size.width, 1f),
                    strokeWidth = 2f
                )
            }
            
            // 分母
            Text(
                text = denominator,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 分数键盘
 */
@Composable
fun FractionKeyboard(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onFractionClick: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 第一行：7 8 9 c
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FracButton("7", Modifier.weight(1f), onClick = { onNumberClick("7") })
            FracButton("8", Modifier.weight(1f), onClick = { onNumberClick("8") })
            FracButton("9", Modifier.weight(1f), onClick = { onNumberClick("9") })
            FracButton("C", Modifier.weight(1f), isFunction = true, onClick = onClear)
        }
        // 第二行：4 5 6 ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FracButton("4", Modifier.weight(1f), onClick = { onNumberClick("4") })
            FracButton("5", Modifier.weight(1f), onClick = { onNumberClick("5") })
            FracButton("6", Modifier.weight(1f), onClick = { onNumberClick("6") })
            FracButton("×", Modifier.weight(1f), isOperator = true, onClick = { onOperatorClick("×") })
        }
        // 第三行：1 2 3 -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FracButton("1", Modifier.weight(1f), onClick = { onNumberClick("1") })
            FracButton("2", Modifier.weight(1f), onClick = { onNumberClick("2") })
            FracButton("3", Modifier.weight(1f), onClick = { onNumberClick("3") })
            FracButton("-", Modifier.weight(1f), isOperator = true, onClick = { onOperatorClick("-") })
        }
        // 第四行：0 ÷ + a/b
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FracButton("0", Modifier.weight(1f), onClick = { onNumberClick("0") })
            FracButton("÷", Modifier.weight(1f), isOperator = true, onClick = { onOperatorClick("÷") })
            FracButton("+", Modifier.weight(1f), isOperator = true, onClick = { onOperatorClick("+") })
            FracButton("a/b", Modifier.weight(1f), isFunction = true, onClick = onFractionClick)

        }
        // 第五行：⌫ =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FracButton("⌫", Modifier.weight(1f), isFunction = true, onClick = onDelete)
            FracButton("=", Modifier.weight(3f), isOperator = true, isAccent = true, onClick = onCalculate)
        }
    }
}

/**
 * 分数键盘按钮
 */
@Composable
fun FracButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isFunction: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isAccent -> MaterialTheme.colorScheme.tertiary
                isFunction -> MaterialTheme.colorScheme.secondary
                isOperator -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isAccent -> MaterialTheme.colorScheme.onTertiary
                isFunction -> MaterialTheme.colorScheme.onSecondary
                isOperator -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        if (text == "a/b") {
            // 显示分数图标
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("a", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Canvas(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                ) {
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, 1f),
                        end = Offset(size.width, 1f),
                        strokeWidth = 2f
                    )
                }
                Text("b", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}