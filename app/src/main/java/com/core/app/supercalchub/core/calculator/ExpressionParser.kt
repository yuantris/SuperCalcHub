package com.core.app.supercalchub.core.calculator

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import net.objecthunter.exp4j.operator.Operator
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

/**
 * 表达式解析器
 * 支持基本数学运算、三角函数、指数函数等
 */
class ExpressionParser {
    
    private val customFunctions = listOf(
        // 三角函数（角度制）
        object : Function("sin", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.sin(Math.toRadians(args[0]))
            }
        },
        object : Function("cos", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.cos(Math.toRadians(args[0]))
            }
        },
        object : Function("tan", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.tan(Math.toRadians(args[0]))
            }
        },
        // 反三角函数（返回角度）
        object : Function("asin", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.toDegrees(Math.asin(args[0]))
            }
        },
        object : Function("acos", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.toDegrees(Math.acos(args[0]))
            }
        },
        object : Function("atan", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.toDegrees(Math.atan(args[0]))
            }
        },
        // 对数函数
        object : Function("log", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.log10(args[0])
            }
        },
        object : Function("ln", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.log(args[0])
            }
        },
        // 平方根
        object : Function("sqrt", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.sqrt(args[0])
            }
        },
        // 绝对值
        object : Function("abs", 1) {
            override fun apply(vararg args: Double): Double {
                return Math.abs(args[0])
            }
        }
    )
    
    private val customOperators = emptyList<Operator>()
    
    /**
     * 解析并计算表达式
     * @param expression 数学表达式
     * @param variables 变量映射
     * @return 计算结果
     */
    fun evaluate(
        expression: String,
        variables: Map<String, Double> = emptyMap()
    ): CalculationResult {
        return try {
            // 将显示符号转换为exp4j支持的符号
            val processedExpression = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("mod", "%")
            
            val builder = ExpressionBuilder(processedExpression)
            
            // 添加自定义函数
            customFunctions.forEach { builder.function(it) }
            
            // 添加变量
            variables.forEach { (name, _) -> 
                builder.variable(name) 
            }
            
            val exp = builder.build()
            val value = exp.evaluate()
            
            CalculationResult.Success(
                value = value,
                formattedValue = formatNumber(value),
                originalExpression = expression
            )
        } catch (e: Exception) {
            CalculationResult.Error(
                message = e.message ?: "计算错误",
                originalExpression = expression
            )
        }
    }
    
    /**
     * 验证表达式是否有效
     */
    fun validate(expression: String): Boolean {
        return try {
            // 将显示符号转换为exp4j支持的符号
            val processedExpression = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("mod", "%")
            
            val builder = ExpressionBuilder(processedExpression)
            customFunctions.forEach { builder.function(it) }
            builder.build()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 格式化数字
     */
    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            BigDecimal(value)
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
        }
    }
    
    /**
     * 获取表达式中的变量列表
     */
    fun extractVariables(expression: String): Set<String> {
        // 将显示符号转换为exp4j支持的符号
        val processedExpression = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("mod", "%")
        
        val builder = ExpressionBuilder(processedExpression)
        customFunctions.forEach { builder.function(it) }
        val exp = builder.build()
        return exp.variableNames ?: emptySet()
    }
    
    /**
     * 检查表达式是否包含运算符或函数
     */
    fun containsOperator(expr: String): Boolean {
        return expr.any { it in "+-×÷*/^%" } || 
               expr.contains("sin") || expr.contains("cos") || expr.contains("tan") ||
               expr.contains("asin") || expr.contains("acos") || expr.contains("atan") ||
               expr.contains("log") || expr.contains("ln") || expr.contains("sqrt")
    }
    
    /**
     * 检查表达式是否完整（不以运算符结尾）
     */
    fun isExpressionComplete(expr: String): Boolean {
        if (expr.isEmpty()) return false
        val lastChar = expr.trimEnd().last()
        return lastChar !in "+-×÷*/^(%"
    }
    
    /**
     * 自动补全括号
     */
    fun autoCompleteParentheses(expr: String): String {
        var openCount = 0
        for (char in expr) {
            when (char) {
                '(' -> openCount++
                ')' -> openCount--
            }
        }
        return expr + ")".repeat(maxOf(0, openCount))
    }
    
    /**
     * 求值表达式（支持角度/弧度模式）
     * @param expression 表达式
     * @param isDegreeMode 是否为角度模式
     * @return 计算结果字符串
     */
    fun evaluateExpression(expression: String, isDegreeMode: Boolean = true): String {
        return try {
            val processed = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())
            
            val result = eval(processed, isDegreeMode)
            if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                String.format("%.10f", result).trimEnd('0').trimEnd('.')
            }
        } catch (e: Exception) {
            "Error"
        }
    }
    
    /**
     * 递归下降解析表达式
     */
    private fun eval(expr: String, isDegree: Boolean): Double {
        return object {
            var pos = -1
            var ch = 0
            
            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }
            
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }
            
            fun parse(): Double {
                nextChar()
                val result = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected character: ${ch.toChar()}")
                return result
            }
            
            fun parseExpression(): Double {
                var result = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> result += parseTerm()
                        eat('-'.code) -> result -= parseTerm()
                        else -> return result
                    }
                }
            }
            
            fun parseTerm(): Double {
                var result = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> result *= parseFactor()
                        eat('/'.code) -> result /= parseFactor()
                        else -> return result
                    }
                }
            }
            
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                
                var result: Double
                val startPos = pos
                
                if (eat('('.code)) {
                    result = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    result = expr.substring(startPos, pos).toDouble()
                } else if (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code) {
                    while (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code) nextChar()
                    val func = expr.substring(startPos, pos)
                    if (eat('('.code)) {
                        result = parseExpression()
                        eat(')'.code)
                    } else {
                        result = parseFactor()
                    }
                    result = when (func) {
                        "sin" -> if (isDegree) sin(Math.toRadians(result)) else sin(result)
                        "cos" -> if (isDegree) cos(Math.toRadians(result)) else cos(result)
                        "tan" -> if (isDegree) tan(Math.toRadians(result)) else tan(result)
                        "asin" -> if (isDegree) Math.toDegrees(asin(result)) else asin(result)
                        "acos" -> if (isDegree) Math.toDegrees(acos(result)) else acos(result)
                        "atan" -> if (isDegree) Math.toDegrees(atan(result)) else atan(result)
                        "sqrt" -> sqrt(result)
                        "log" -> log10(result)
                        "ln" -> ln(result)
                        "abs" -> abs(result)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }
                
                if (eat('^'.code)) {
                    result = result.pow(parseFactor())
                }
                
                // 百分比运算
                if (eat('%'.code)) {
                    result /= 100.0
                }
                
                return result
            }
        }.parse()
    }
}

/**
 * 计算结果
 */
sealed class CalculationResult {
    data class Success(
        val value: Double,
        val formattedValue: String,
        val originalExpression: String
    ) : CalculationResult()
    
    data class Error(
        val message: String,
        val originalExpression: String
    ) : CalculationResult()
}