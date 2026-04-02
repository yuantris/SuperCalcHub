package com.core.app.supercalchub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.core.app.supercalchub.core.calculator.CalculatorImpl
import com.core.app.supercalchub.core.calculator.CalculationResult
import com.core.app.supercalchub.core.calculator.AngleUnit
import com.core.app.supercalchub.core.calculator.TrigFunction

/**
 * 科学计算器界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificCalculatorScreen(navController: NavController) {
    val calculator = remember { CalculatorImpl() }
    
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isDegreeMode by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("科学计算器") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // 角度/弧度模式
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (isDegreeMode) "DEG" else "RAD",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 表达式
                    Text(
                        text = expression,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 结果
                    Text(
                        text = result,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 模式切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isDegreeMode = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDegreeMode) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("DEG")
                }
                Button(
                    onClick = { isDegreeMode = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isDegreeMode) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("RAD")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 科学计算按钮网格
            ScientificCalculatorButtons(
                onNumberClick = { number ->
                    expression += number
                },
                onOperatorClick = { operator ->
                    expression += operator
                },
                onFunctionClick = { function ->
                    expression += function
                },
                onClear = {
                    expression = ""
                    result = ""
                },
                onDelete = {
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                    }
                },
                onCalculate = {
                    val calcResult = calculator.calculate(expression)
                    result = when (calcResult) {
                        is CalculationResult.Success -> calcResult.formattedValue
                        is CalculationResult.Error -> calcResult.message
                    }
                },
                isDegreeMode = isDegreeMode
            )
        }
    }
}

/**
 * 科学计算器按钮网格
 */
@Composable
fun ScientificCalculatorButtons(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onFunctionClick: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onCalculate: () -> Unit,
    isDegreeMode: Boolean
) {
    // 科学计算器按钮布局
    val buttons = listOf(
        // 第一行：函数
        "sin", "cos", "tan", "C",
        // 第二行：函数
        "asin", "acos", "atan", "⌫",
        // 第三行：运算符
        "(", ")", "^", "÷",
        // 第四行：数字和运算符
        "7", "8", "9", "×",
        // 第五行：数字和运算符
        "4", "5", "6", "-",
        // 第六行：数字和运算符
        "1", "2", "3", "+",
        // 第七行：数字和运算符
        "0", ".", "π", "=",
        // 第八行：更多函数
        "log", "ln", "√", "x²",
        // 第九行：更多函数
        "e", "%", "mod", "1/x"
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(buttons) { button ->
            ScientificCalculatorButton(
                text = button,
                onClick = {
                    when (button) {
                        "C" -> onClear()
                        "⌫" -> onDelete()
                        "=" -> onCalculate()
                        "+", "-", "×", "÷", "^", "(", ")", "%" -> onOperatorClick(button)
                        "π" -> onNumberClick("3.14159265358979")
                        "e" -> onNumberClick("2.718281828459")
                        "sin", "cos", "tan", "asin", "acos", "atan" -> {
                            val func = if (isDegreeMode) "($button(" else "($button("
                            onFunctionClick(func)
                        }
                        "log" -> onFunctionClick("log(")
                        "ln" -> onFunctionClick("ln(")
                        "√" -> onFunctionClick("sqrt(")
                        "x²" -> onOperatorClick("^2")
                        "1/x" -> onFunctionClick("1/(")
                        "mod" -> onOperatorClick("%")
                        else -> onNumberClick(button)
                    }
                },
                modifier = Modifier.aspectRatio(1f),
                isFunction = button in listOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "√", "x²", "1/x", "mod"),
                isOperator = button in listOf("+", "-", "×", "÷", "^", "=", "C", "⌫", "(", ")", "%")
            )
        }
    }
}

/**
 * 单个科学计算器按钮
 */
@Composable
fun ScientificCalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFunction: Boolean = false,
    isOperator: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isFunction -> MaterialTheme.colorScheme.tertiary
                isOperator -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isFunction -> MaterialTheme.colorScheme.onTertiary
                isOperator -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = text,
            fontSize = when (text.length) {
                1 -> 16.sp
                2 -> 14.sp
                3 -> 12.sp
                else -> 10.sp
            },
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}