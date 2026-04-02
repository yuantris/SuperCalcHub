package com.core.app.supercalchub.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.core.app.supercalchub.core.calculator.CalculatorImpl
import com.core.app.supercalchub.data.AppDatabase
import com.core.app.supercalchub.data.CalculationHistoryEntity
import com.core.app.supercalchub.ui.navigation.AppRoutes
import com.core.app.supercalchub.ui.navigation.NavigationUtils
import kotlinx.coroutines.launch

/**
 * 标准计算器界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CalculatorScreen(
    navController: NavController,
    database: AppDatabase? = null
) {
    val calculator = remember { CalculatorImpl() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isScientificMode by remember { mutableStateOf(false) }
    var isCalculated by remember { mutableStateOf(false) }
    var isDegreeMode by remember { mutableStateOf(true) }
    
    // 预计算功能 - 实时计算（仅当表达式包含运算符且完整时）
    LaunchedEffect(expression, isDegreeMode) {
        if (expression.isNotEmpty() && !isCalculated && calculator.containsOperator(expression) && calculator.isExpressionComplete(expression)) {
            try {
                val completeExpression = calculator.autoCompleteParentheses(expression)
                val evalResult = calculator.evaluateExpression(completeExpression, isDegreeMode)
                result = evalResult
            } catch (e: Exception) {
                result = ""
            }
        } else if (expression.isEmpty() || !calculator.containsOperator(expression) || !calculator.isExpressionComplete(expression)) {
            result = ""
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "功能菜单",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val menuItems = listOf(
                    Pair(AppRoutes.CALCULATOR, "计算器"),
                    Pair(AppRoutes.FRACTION, "分数计算"),
                    Pair(AppRoutes.EQUATION, "解方程"),
                    Pair(AppRoutes.VERTICAL_CALC, "竖式计算"),
                    Pair(AppRoutes.FUNCTION_PLOT, "函数绘制"),
                    Pair(AppRoutes.HISTORY, "计算历史"),
                    Pair(AppRoutes.SETTINGS, "设置")
                )
                
                menuItems.forEach { (route, title) ->
                    NavigationDrawerItem(
                        label = { Text(title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            NavigationUtils.safeNavigate(navController, route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isScientificMode) "科学计算器" else "标准计算器") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                    actions = {
                        TextButton(onClick = { isScientificMode = !isScientificMode }) {
                            Text(if (isScientificMode) "标准" else "科学")
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
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        // 科学模式显示DEG/RAD
                        if (isScientificMode) {
                            Text(
                                text = if (isDegreeMode) "DEG" else "RAD",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        if (isCalculated) {
                            // 点等于后：表达式变小，结果变大且前有等号
                            Text(
                                text = expression,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "= $result",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // 预计算状态：表达式大，结果小
                            Text(
                                text = expression.ifEmpty { "0" },
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (result.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = result,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 科学模式下的角度切换
                if (isScientificMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { isDegreeMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDegreeMode) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Text("角度 DEG")
                        }
                        Button(
                            onClick = { isDegreeMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDegreeMode) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        ) {
                            Text("弧度 RAD")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 按钮网格
                AnimatedContent(
                    targetState = isScientificMode,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
                    }
                ) { isScientific ->
                    if (isScientific) {
                        ScientificCalculatorButtons(
                            isDegreeMode = isDegreeMode,
                            onInput = { input ->
                                if (isCalculated) {
                                    if (input in "+-×÷^") {
                                        expression = result + input
                                    } else {
                                        expression = input
                                    }
                                    isCalculated = false
                                } else {
                                    expression += input
                                }
                            },
                            onClear = { 
                                expression = ""
                                result = ""
                                isCalculated = false
                            },
                            onDelete = { 
                                if (isCalculated) {
                                    expression = ""
                                    result = ""
                                    isCalculated = false
                                } else if (expression.isNotEmpty()) {
                                    expression = expression.dropLast(1)
                                }
                            },
                            onCalculate = {
                                val completeExpression = calculator.autoCompleteParentheses(expression)
                                val calcResult = calculator.evaluateExpression(completeExpression, isDegreeMode)
                                result = calcResult
                                expression = completeExpression
                                isCalculated = true
                                // 保存历史记录
                                if (result != "Error" && database != null) {
                                    scope.launch {
                                        database.calculationHistoryDao().insertHistory(
                                            CalculationHistoryEntity(
                                                expression = completeExpression,
                                                result = result,
                                                type = if (isScientificMode) "SCIENTIFIC" else "BASIC"
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        StandardCalculatorButtons(
                            onInput = { input ->
                                if (isCalculated) {
                                    if (input in "+-×÷") {
                                        expression = result + input
                                    } else {
                                        expression = input
                                    }
                                    isCalculated = false
                                } else {
                                    expression += input
                                }
                            },
                            onClear = { 
                                expression = ""
                                result = ""
                                isCalculated = false
                            },
                            onDelete = { 
                                if (isCalculated) {
                                    expression = ""
                                    result = ""
                                    isCalculated = false
                                } else if (expression.isNotEmpty()) {
                                    expression = expression.dropLast(1)
                                }
                            },
                            onCalculate = {
                                val completeExpression = calculator.autoCompleteParentheses(expression)
                                val calcResult = calculator.evaluateExpression(completeExpression, isDegreeMode)
                                result = calcResult
                                expression = completeExpression
                                isCalculated = true
                                // 保存历史记录
                                if (result != "Error" && database != null) {
                                    scope.launch {
                                        database.calculationHistoryDao().insertHistory(
                                            CalculationHistoryEntity(
                                                expression = completeExpression,
                                                result = result,
                                                type = "BASIC"
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 标准计算器按钮网格 - 等于号占2格宽度
 */
@Composable
fun StandardCalculatorButtons(
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 第一行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton("C", Modifier.weight(1f), isOperator = true, onClick = onClear)
            CalcButton("⌫", Modifier.weight(1f), isFunction = true, onClick = onDelete)
            CalcButton("%", Modifier.weight(1f), onClick = { onInput("%") })
            CalcButton("÷", Modifier.weight(1f), isOperator = true, onClick = { onInput("÷") })
        }
        // 第二行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton("7", Modifier.weight(1f), onClick = { onInput("7") })
            CalcButton("8", Modifier.weight(1f), onClick = { onInput("8") })
            CalcButton("9", Modifier.weight(1f), onClick = { onInput("9") })
            CalcButton("×", Modifier.weight(1f), isOperator = true, onClick = { onInput("×") })
        }
        // 第三行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton("4", Modifier.weight(1f), onClick = { onInput("4") })
            CalcButton("5", Modifier.weight(1f), onClick = { onInput("5") })
            CalcButton("6", Modifier.weight(1f), onClick = { onInput("6") })
            CalcButton("-", Modifier.weight(1f), isOperator = true, onClick = { onInput("-") })
        }
        // 第四行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton("1", Modifier.weight(1f), onClick = { onInput("1") })
            CalcButton("2", Modifier.weight(1f), onClick = { onInput("2") })
            CalcButton("3", Modifier.weight(1f), onClick = { onInput("3") })
            CalcButton("+", Modifier.weight(1f), isOperator = true, onClick = { onInput("+") })
        }
        // 第五行 - 等于号占2格
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(".", Modifier.weight(1f), onClick = { onInput(".") })
            CalcButton("0", Modifier.weight(1f), onClick = { onInput("0") })
            CalcButton("=", Modifier.weight(2f), isOperator = true, isAccent = true, onClick = onCalculate)
        }
    }
}

/**
 * 科学计算器按钮网格
 */
@Composable
fun ScientificCalculatorButtons(
    isDegreeMode: Boolean,
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 第一行：三角函数
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton("sin", Modifier.weight(1f), onClick = { onInput("sin(") })
            SciButton("cos", Modifier.weight(1f), onClick = { onInput("cos(") })
            SciButton("tan", Modifier.weight(1f), onClick = { onInput("tan(") })
            SciButton("C", Modifier.weight(1f), isOperator = true, onClick = onClear)
            SciButton("⌫", Modifier.weight(1f), isFunction = true, onClick = onDelete)
        }
        // 第二行：反三角和括号
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton("asin", Modifier.weight(1f), onClick = { onInput("asin(") })
            SciButton("acos", Modifier.weight(1f), onClick = { onInput("acos(") })
            SciButton("atan", Modifier.weight(1f), onClick = { onInput("atan(") })
            SciButton("(", Modifier.weight(1f), onClick = { onInput("(") })
            SciButton(")", Modifier.weight(1f), onClick = { onInput(")") })
        }
        // 第三行：对数和根号
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton("log", Modifier.weight(1f), onClick = { onInput("log(") })
            SciButton("ln", Modifier.weight(1f), onClick = { onInput("ln(") })
            SciButton("√", Modifier.weight(1f), onClick = { onInput("sqrt(") })
            SciButton("÷", Modifier.weight(1f), isOperator = true, onClick = { onInput("÷") })
            SciButton("^", Modifier.weight(1f), onClick = { onInput("^") })
        }
        // 第四行：数字7-9和π
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton("7", Modifier.weight(1f), onClick = { onInput("7") })
            SciButton("8", Modifier.weight(1f), onClick = { onInput("8") })
            SciButton("9", Modifier.weight(1f), onClick = { onInput("9") })
            SciButton("×", Modifier.weight(1f), isOperator = true, onClick = { onInput("×") })
            SciButton("π", Modifier.weight(1f), onClick = { onInput("π") })
        }
        // 第五行：数字4-6和e
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton("4", Modifier.weight(1f), onClick = { onInput("4") })
            SciButton("5", Modifier.weight(1f), onClick = { onInput("5") })
            SciButton("6", Modifier.weight(1f), onClick = { onInput("6") })
            SciButton("-", Modifier.weight(1f), isOperator = true, onClick = { onInput("-") })
            SciButton("e", Modifier.weight(1f), onClick = { onInput("e") })
        }
        // 第六行：数字1-3和x²
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton("1", Modifier.weight(1f), onClick = { onInput("1") })
            SciButton("2", Modifier.weight(1f), onClick = { onInput("2") })
            SciButton("3", Modifier.weight(1f), onClick = { onInput("3") })
            SciButton("+", Modifier.weight(1f), isOperator = true, onClick = { onInput("+") })
            SciButton("x²", Modifier.weight(1f), onClick = { onInput("^2") })
        }
        // 第七行：0、小数点、=（占2格）、1/x
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SciButton(".", Modifier.weight(1f), onClick = { onInput(".") })
            SciButton("0", Modifier.weight(1f), onClick = { onInput("0") })
            SciButton("=", Modifier.weight(2f), isOperator = true, isAccent = true, onClick = onCalculate)
            SciButton("1/x", Modifier.weight(1f), onClick = { onInput("1/(") })
        }
    }
}

/**
 * 标准计算器按钮
 */
@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isFunction: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isAccent -> MaterialTheme.colorScheme.tertiary
                isOperator -> MaterialTheme.colorScheme.primary
                isFunction -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isAccent -> MaterialTheme.colorScheme.onTertiary
                isOperator -> MaterialTheme.colorScheme.onPrimary
                isFunction -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 科学计算器按钮
 */
@Composable
fun SciButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isFunction: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isAccent -> MaterialTheme.colorScheme.tertiary
                isOperator -> MaterialTheme.colorScheme.primary
                isFunction -> MaterialTheme.colorScheme.secondary
                text.length > 1 && text[0].isLetter() && text !in listOf("log", "ln") -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isAccent -> MaterialTheme.colorScheme.onTertiary
                isOperator -> MaterialTheme.colorScheme.onPrimary
                isFunction -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = when (text.length) {
                1 -> 16.sp
                2 -> 13.sp
                3 -> 11.sp
                else -> 10.sp
            },
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}