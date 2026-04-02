package com.core.app.supercalchub.core.math

import java.math.BigDecimal

/**
 * 分数计算器
 * 支持分数表达式的解析和计算
 */
class FractionCalculator {
    
    /**
     * 计算分数表达式
     * @param expression 分数表达式，如 "1/2 + 3/4"
     * @return 计算结果
     */
    fun calculate(expression: String): FractionResult {
        return try {
            val steps = mutableListOf<FractionStep>()
            val result = parseAndEvaluate(expression, steps)
            
            FractionResult.Success(
                fraction = result,
                decimal = result.toDecimal(),
                steps = steps,
                originalExpression = expression
            )
        } catch (e: Exception) {
            FractionResult.Error(
                message = e.message ?: "计算错误",
                originalExpression = expression
            )
        }
    }
    
    /**
     * 解析并计算表达式
     */
    private fun parseAndEvaluate(
        expression: String,
        steps: MutableList<FractionStep>
    ): Fraction {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) {
            throw IllegalArgumentException("空表达式")
        }
        
        return parseExpression(tokens, steps).first
    }
    
    /**
     * 分词器
     */
    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val expr = expression.replace(" ", "")
        
        while (i < expr.length) {
            when {
                expr[i] in "0123456789." -> {
                    val start = i
                    while (i < expr.length && expr[i] in "0123456789.") {
                        i++
                    }
                    tokens.add(expr.substring(start, i))
                }
                expr[i] == '/' -> {
                    tokens.add("/")
                    i++
                }
                expr[i] in "+-()" -> {
                    tokens.add(expr[i].toString())
                    i++
                }
                expr[i] == '*' || expr[i] == '×' -> {
                    tokens.add("*")
                    i++
                }
                expr[i] == '÷' -> {
                    tokens.add("/")
                    i++
                }
                else -> {
                    throw IllegalArgumentException("无效字符: ${expr[i]}")
                }
            }
        }
        
        return tokens
    }
    
    /**
     * 解析表达式（处理加减）
     */
    private fun parseExpression(
        tokens: List<String>,
        steps: MutableList<FractionStep>,
        startIndex: Int = 0
    ): Pair<Fraction, Int> {
        var (left, index) = parseTerm(tokens, steps, startIndex)
        
        while (index < tokens.size && (tokens[index] == "+" || tokens[index] == "-")) {
            val operator = tokens[index]
            val (right, newIndex) = parseTerm(tokens, steps, index + 1)
            
            val result = when (operator) {
                "+" -> left + right
                "-" -> left - right
                else -> throw IllegalArgumentException("无效运算符")
            }
            
            steps.add(FractionStep(
                description = "计算",
                expression = "$left $operator $right",
                result = result
            ))
            
            left = result
            index = newIndex
        }
        
        return Pair(left, index)
    }
    
    /**
     * 解析项（处理乘除）
     */
    private fun parseTerm(
        tokens: List<String>,
        steps: MutableList<FractionStep>,
        startIndex: Int
    ): Pair<Fraction, Int> {
        var (left, index) = parseFactor(tokens, steps, startIndex)
        
        while (index < tokens.size && (tokens[index] == "*" || tokens[index] == "/")) {
            val operator = tokens[index]
            val (right, newIndex) = parseFactor(tokens, steps, index + 1)
            
            val result = when (operator) {
                "*" -> left * right
                "/" -> left / right
                else -> throw IllegalArgumentException("无效运算符")
            }
            
            steps.add(FractionStep(
                description = "计算",
                expression = "$left $operator $right",
                result = result
            ))
            
            left = result
            index = newIndex
        }
        
        return Pair(left, index)
    }
    
    /**
     * 解析因子（处理括号和数字）
     */
    private fun parseFactor(
        tokens: List<String>,
        steps: MutableList<FractionStep>,
        startIndex: Int
    ): Pair<Fraction, Int> {
        if (startIndex >= tokens.size) {
            throw IllegalArgumentException("表达式不完整")
        }
        
        val token = tokens[startIndex]
        
        return when {
            token == "(" -> {
                val (result, index) = parseExpression(tokens, steps, startIndex + 1)
                if (index >= tokens.size || tokens[index] != ")") {
                    throw IllegalArgumentException("缺少右括号")
                }
                Pair(result, index + 1)
            }
            token == "-" -> {
                val (factor, index) = parseFactor(tokens, steps, startIndex + 1)
                Pair(-factor, index)
            }
            token.toDoubleOrNull() != null -> {
                // 处理小数或整数
                val decimal = BigDecimal(token)
                if (decimal.scale() == 0) {
                    Pair(Fraction(decimal.toBigInteger()), startIndex + 1)
                } else {
                    // 小数转分数
                    val scale = decimal.scale()
                    val denominator = BigDecimal.TEN.pow(scale).toBigInteger()
                    val numerator = decimal.multiply(BigDecimal.TEN.pow(scale)).toBigInteger()
                    Pair(Fraction(numerator, denominator).simplify(), startIndex + 1)
                }
            }
            token.contains("/") -> {
                // 分数格式
                Pair(Fraction.parse(token), startIndex + 1)
            }
            else -> {
                throw IllegalArgumentException("无效的token: $token")
            }
        }
    }
    
    /**
     * 验证分数表达式
     */
    fun validate(expression: String): Boolean {
        return try {
            calculate(expression)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取表达式的计算步骤
     */
    fun getSteps(expression: String): List<FractionStep> {
        return when (val result = calculate(expression)) {
            is FractionResult.Success -> result.steps
            is FractionResult.Error -> emptyList()
        }
    }
}