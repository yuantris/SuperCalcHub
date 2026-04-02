package com.core.app.supercalchub.core.calculator

import com.core.app.supercalchub.core.math.Fraction
import com.core.app.supercalchub.core.math.FractionResult

/**
 * 基础计算器接口
 * 定义计算器的基本操作
 */
interface BaseCalculator {
    
    /**
     * 计算表达式
     * @param expression 数学表达式
     * @param variables 变量映射
     * @return 计算结果
     */
    fun calculate(
        expression: String,
        variables: Map<String, Double> = emptyMap()
    ): CalculationResult
    
    /**
     * 验证表达式
     * @param expression 数学表达式
     * @return 是否有效
     */
    fun validate(expression: String): Boolean
    
    /**
     * 获取计算历史
     * @return 计算历史列表
     */
    fun getHistory(): List<CalculationHistory>
    
    /**
     * 清空计算历史
     */
    fun clearHistory()
    
    /**
     * 保存计算到历史
     */
    fun saveToHistory(
        expression: String,
        result: String,
        timestamp: Long = System.currentTimeMillis()
    )
}

/**
 * 计算历史记录
 */
data class CalculationHistory(
    val id: String,
    val expression: String,
    val result: String,
    val timestamp: Long,
    val type: CalculationType
)

/**
 * 计算类型
 */
enum class CalculationType {
    BASIC,          // 基础计算
    FRACTION,       // 分数计算
    EQUATION,       // 方程求解
    FUNCTION,       // 函数计算
    GEOMETRY,       // 几何计算
    SCIENTIFIC      // 科学计算
}

/**
 * 高级计算器接口
 * 扩展基础计算器功能
 */
interface AdvancedCalculator : BaseCalculator {
    
    /**
     * 计算分数表达式
     */
    fun calculateFraction(expression: String): FractionResult
    
    /**
     * 求解方程
     */
    fun solveEquation(equation: String): CalculationResult
    
    /**
     * 计算函数值
     */
    fun calculateFunction(
        function: String,
        variable: String,
        value: Double
    ): CalculationResult
    
    /**
     * 计算三角函数
     */
    fun calculateTrig(
        function: TrigFunction,
        angle: Double,
        angleUnit: AngleUnit = AngleUnit.DEGREE
    ): CalculationResult
    
    /**
     * 计算对数
     */
    fun calculateLog(
        value: Double,
        base: Double = 10.0
    ): CalculationResult
    
    /**
     * 计算阶乘
     */
    fun factorial(n: Int): CalculationResult
    
    /**
     * 计算组合数
     */
    fun combination(n: Int, r: Int): CalculationResult
    
    /**
     * 计算排列数
     */
    fun permutation(n: Int, r: Int): CalculationResult
}

/**
 * 三角函数类型
 */
enum class TrigFunction {
    SIN, COS, TAN,
    ASIN, ACOS, ATAN
}

/**
 * 角度单位
 */
enum class AngleUnit {
    DEGREE,     // 度
    RADIAN      // 弧度
}

/**
 * 计算器实现类
 */
class CalculatorImpl : AdvancedCalculator {
    
    private val expressionParser = ExpressionParser()
    private val resultFormatter = ResultFormatter()
    private val history = mutableListOf<CalculationHistory>()
    
    override fun calculate(
        expression: String,
        variables: Map<String, Double>
    ): CalculationResult {
        val result = expressionParser.evaluate(expression, variables)
        
        when (result) {
            is CalculationResult.Success -> {
                saveToHistory(
                    expression = expression,
                    result = result.formattedValue
                )
            }
            is CalculationResult.Error -> {
                // 错误处理
            }
        }
        
        return result
    }
    
    override fun validate(expression: String): Boolean {
        return expressionParser.validate(expression)
    }
    
    override fun getHistory(): List<CalculationHistory> {
        return history.toList()
    }
    
    override fun clearHistory() {
        history.clear()
    }
    
    override fun saveToHistory(
        expression: String,
        result: String,
        timestamp: Long
    ) {
        history.add(0, CalculationHistory(
            id = "${timestamp}_${expression.hashCode()}",
            expression = expression,
            result = result,
            timestamp = timestamp,
            type = CalculationType.BASIC
        ))
        
        // 限制历史记录数量
        if (history.size > 100) {
            history.removeLast()
        }
    }
    
    override fun calculateFraction(expression: String): FractionResult {
        // 实现分数计算
        return FractionResult.Error("未实现", expression)
    }
    
    override fun solveEquation(equation: String): CalculationResult {
        // 实现方程求解
        return CalculationResult.Error("未实现", equation)
    }
    
    override fun calculateFunction(
        function: String,
        variable: String,
        value: Double
    ): CalculationResult {
        val expression = function.replace(variable, value.toString())
        return calculate(expression)
    }
    
    override fun calculateTrig(
        function: TrigFunction,
        angle: Double,
        angleUnit: AngleUnit
    ): CalculationResult {
        val radians = when (angleUnit) {
            AngleUnit.DEGREE -> Math.toRadians(angle)
            AngleUnit.RADIAN -> angle
        }
        
        val result = when (function) {
            TrigFunction.SIN -> Math.sin(radians)
            TrigFunction.COS -> Math.cos(radians)
            TrigFunction.TAN -> Math.tan(radians)
            TrigFunction.ASIN -> Math.toDegrees(Math.asin(angle))
            TrigFunction.ACOS -> Math.toDegrees(Math.acos(angle))
            TrigFunction.ATAN -> Math.toDegrees(Math.atan(angle))
        }
        
        val expression = "${function.name}(${resultFormatter.format(angle, 4)}${if (angleUnit == AngleUnit.DEGREE) "°" else ""})"
        
        return CalculationResult.Success(
            value = result,
            formattedValue = resultFormatter.format(result),
            originalExpression = expression
        )
    }
    
    override fun calculateLog(value: Double, base: Double): CalculationResult {
        val result = Math.log(value) / Math.log(base)
        val expression = if (base == 10.0) "log(${resultFormatter.format(value)})" else "log_${resultFormatter.format(base)}(${resultFormatter.format(value)})"
        
        return CalculationResult.Success(
            value = result,
            formattedValue = resultFormatter.format(result),
            originalExpression = expression
        )
    }
    
    override fun factorial(n: Int): CalculationResult {
        require(n >= 0) { "阶乘仅适用于非负整数" }
        
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        
        return CalculationResult.Success(
            value = result.toDouble(),
            formattedValue = result.toString(),
            originalExpression = "$n!"
        )
    }
    
    override fun combination(n: Int, r: Int): CalculationResult {
        require(n >= r && r >= 0) { "n必须大于等于r，且r必须非负" }
        
        val factorialN = factorial(n)
        val factorialR = factorial(r)
        val factorialNR = factorial(n - r)
        
        return when {
            factorialN is CalculationResult.Success && 
            factorialR is CalculationResult.Success && 
            factorialNR is CalculationResult.Success -> {
                val result = factorialN.value / (factorialR.value * factorialNR.value)
                CalculationResult.Success(
                    value = result,
                    formattedValue = result.toLong().toString(),
                    originalExpression = "C($n,$r)"
                )
            }
            else -> CalculationResult.Error("计算阶乘失败", "C($n,$r)")
        }
    }
    
    override fun permutation(n: Int, r: Int): CalculationResult {
        require(n >= r && r >= 0) { "n必须大于等于r，且r必须非负" }
        
        val factorialN = factorial(n)
        val factorialNR = factorial(n - r)
        
        return when {
            factorialN is CalculationResult.Success && 
            factorialNR is CalculationResult.Success -> {
                val result = factorialN.value / factorialNR.value
                CalculationResult.Success(
                    value = result,
                    formattedValue = result.toLong().toString(),
                    originalExpression = "P($n,$r)"
                )
            }
            else -> CalculationResult.Error("计算阶乘失败", "P($n,$r)")
        }
    }
    
    /**
     * 检查表达式是否包含运算符
     */
    fun containsOperator(expr: String): Boolean {
        return expressionParser.containsOperator(expr)
    }
    
    /**
     * 检查表达式是否完整（不以运算符结尾）
     */
    fun isExpressionComplete(expr: String): Boolean {
        return expressionParser.isExpressionComplete(expr)
    }
    
    /**
     * 自动补全括号
     */
    fun autoCompleteParentheses(expr: String): String {
        return expressionParser.autoCompleteParentheses(expr)
    }
    
    /**
     * 求值表达式
     * @param expression 表达式
     * @param isDegreeMode 是否为角度模式
     * @return 计算结果字符串
     */
    fun evaluateExpression(expression: String, isDegreeMode: Boolean = true): String {
        return expressionParser.evaluateExpression(expression, isDegreeMode)
    }
}