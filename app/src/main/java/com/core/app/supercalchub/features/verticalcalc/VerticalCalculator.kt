package com.core.app.supercalchub.features.verticalcalc

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * 竖式计算器
 * 支持整数、小数的竖式运算，展示完整计算过程
 */
class VerticalCalculator {
    
    /**
     * 计算竖式
     * @param num1 第一个数
     * @param num2 第二个数
     * @param operator 运算符
     * @return 竖式计算结果
     */
    fun calculateVertical(
        num1: BigDecimal,
        num2: BigDecimal,
        operator: Char
    ): VerticalResult {
        return when (operator) {
            '+' -> add(num1, num2)
            '-' -> subtract(num1, num2)
            '×', '*' -> multiply(num1, num2)
            '÷', '/' -> divide(num1, num2)
            else -> VerticalResult.Error("不支持的运算符: $operator")
        }
    }
    
    /**
     * 加法竖式
     */
    private fun add(num1: BigDecimal, num2: BigDecimal): VerticalResult {
        val result = num1.add(num2)
        val steps = mutableListOf<VerticalStep>()
        
        // 对齐小数点
        val maxScale = maxOf(num1.scale(), num2.scale())
        val alignedNum1 = num1.setScale(maxScale, RoundingMode.UNNECESSARY)
        val alignedNum2 = num2.setScale(maxScale, RoundingMode.UNNECESSARY)
        
        steps.add(VerticalStep(
            stepNumber = 0,
            description = "对齐小数点",
            display = formatVertical(alignedNum1, alignedNum2, '+'),
            carry = null
        ))
        
        // 逐位计算
        val num1Str = alignedNum1.toPlainString().replace(".", "").padStart(maxOf(alignedNum1.toPlainString().length, alignedNum2.toPlainString().length), '0')
        val num2Str = alignedNum2.toPlainString().replace(".", "").padStart(num1Str.length, '0')
        
        var carry = 0
        val resultDigits = StringBuilder()
        
        for (i in num1Str.length - 1 downTo 0) {
            val digit1 = num1Str[i].digitToIntOrNull() ?: 0
            val digit2 = num2Str[i].digitToIntOrNull() ?: 0
            val sum = digit1 + digit2 + carry
            carry = sum / 10
            resultDigits.insert(0, (sum % 10))
            
            if (carry > 0) {
                steps.add(VerticalStep(
                    stepNumber = steps.size,
                    description = "第${num1Str.length - i}位计算: $digit1 + $digit2 + 进位${if (carry > 1) carry - 1 else 0} = $sum，进位$carry",
                    display = "",
                    carry = carry
                ))
            }
        }
        
        steps.add(VerticalStep(
            stepNumber = steps.size,
            description = "最终结果",
            display = formatVertical(alignedNum1, alignedNum2, '+', result),
            carry = null
        ))
        
        return VerticalResult.Success(
            operand1 = num1,
            operand2 = num2,
            operator = '+',
            result = result,
            steps = steps,
            formattedResult = formatNumber(result)
        )
    }
    
    /**
     * 减法竖式
     */
    private fun subtract(num1: BigDecimal, num2: BigDecimal): VerticalResult {
        val result = num1.subtract(num2)
        val steps = mutableListOf<VerticalStep>()
        
        // 对齐小数点
        val maxScale = maxOf(num1.scale(), num2.scale())
        val alignedNum1 = num1.setScale(maxScale, RoundingMode.UNNECESSARY)
        val alignedNum2 = num2.setScale(maxScale, RoundingMode.UNNECESSARY)
        
        steps.add(VerticalStep(
            stepNumber = 0,
            description = "对齐小数点",
            display = formatVertical(alignedNum1, alignedNum2, '-'),
            carry = null
        ))
        
        // 逐位计算
        val num1Str = alignedNum1.toPlainString().replace(".", "").padStart(maxOf(alignedNum1.toPlainString().length, alignedNum2.toPlainString().length), '0')
        val num2Str = alignedNum2.toPlainString().replace(".", "").padStart(num1Str.length, '0')
        
        var borrow = 0
        val resultDigits = StringBuilder()
        
        for (i in num1Str.length - 1 downTo 0) {
            var digit1 = num1Str[i].digitToIntOrNull() ?: 0
            val digit2 = num2Str[i].digitToIntOrNull() ?: 0
            
            digit1 -= borrow
            borrow = 0
            
            if (digit1 < digit2) {
                digit1 += 10
                borrow = 1
            }
            
            val diff = digit1 - digit2
            resultDigits.insert(0, diff)
            
            if (borrow > 0) {
                steps.add(VerticalStep(
                    stepNumber = steps.size,
                    description = "第${num1Str.length - i}位计算: 需要借位，$digit1 - $digit2 = $diff",
                    display = "",
                    carry = -borrow
                ))
            }
        }
        
        steps.add(VerticalStep(
            stepNumber = steps.size,
            description = "最终结果",
            display = formatVertical(alignedNum1, alignedNum2, '-', result),
            carry = null
        ))
        
        return VerticalResult.Success(
            operand1 = num1,
            operand2 = num2,
            operator = '-',
            result = result,
            steps = steps,
            formattedResult = formatNumber(result)
        )
    }
    
    /**
     * 乘法竖式
     */
    private fun multiply(num1: BigDecimal, num2: BigDecimal): VerticalResult {
        val result = num1.multiply(num2)
        val steps = mutableListOf<VerticalStep>()
        
        steps.add(VerticalStep(
            stepNumber = 0,
            description = "乘法计算",
            display = formatVertical(num1, num2, '×', result),
            carry = null
        ))
        
        return VerticalResult.Success(
            operand1 = num1,
            operand2 = num2,
            operator = '×',
            result = result,
            steps = steps,
            formattedResult = formatNumber(result)
        )
    }
    
    /**
     * 除法竖式
     */
    private fun divide(num1: BigDecimal, num2: BigDecimal): VerticalResult {
        require(num2 != BigDecimal.ZERO) { "除数不能为零" }
        
        val scale = 10
        val result = num1.divide(num2, scale, RoundingMode.HALF_UP)
        val steps = mutableListOf<VerticalStep>()
        
        steps.add(VerticalStep(
            stepNumber = 0,
            description = "除法计算",
            display = formatVertical(num1, num2, '÷', result),
            carry = null
        ))
        
        return VerticalResult.Success(
            operand1 = num1,
            operand2 = num2,
            operator = '÷',
            result = result,
            steps = steps,
            formattedResult = formatNumber(result)
        )
    }
    
    /**
     * 格式化竖式显示
     */
    private fun formatVertical(
        num1: BigDecimal,
        num2: BigDecimal,
        operator: Char,
        result: BigDecimal? = null
    ): String {
        val num1Str = formatNumber(num1)
        val num2Str = formatNumber(num2)
        val operatorStr = operator.toString()
        
        val maxWidth = maxOf(num1Str.length, num2Str.length, result?.let { formatNumber(it).length } ?: 0) + 2
        
        val sb = StringBuilder()
        sb.appendLine(num1Str.padStart(maxWidth))
        sb.appendLine("$operatorStr${num2Str.padStart(maxWidth - 1)}")
        sb.appendLine("-".repeat(maxWidth))
        result?.let {
            sb.appendLine(formatNumber(it).padStart(maxWidth))
        }
        
        return sb.toString()
    }
    
    /**
     * 格式化数字
     */
    private fun formatNumber(value: BigDecimal): String {
        return if (value.scale() <= 0) {
            value.toBigInteger().toString()
        } else {
            value.stripTrailingZeros().toPlainString()
        }
    }
}

/**
 * 竖式计算步骤
 */
data class VerticalStep(
    val stepNumber: Int,
    val description: String,
    val display: String,
    val carry: Int? // 进位/借位
)

/**
 * 竖式计算结果
 */
sealed class VerticalResult {
    data class Success(
        val operand1: BigDecimal,
        val operand2: BigDecimal,
        val operator: Char,
        val result: BigDecimal,
        val steps: List<VerticalStep>,
        val formattedResult: String
    ) : VerticalResult()
    
    data class Error(
        val message: String
    ) : VerticalResult()
}