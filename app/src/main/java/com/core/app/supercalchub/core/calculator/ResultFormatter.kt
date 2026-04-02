package com.core.app.supercalchub.core.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 结果格式化器
 * 提供多种数字格式化选项
 */
class ResultFormatter {
    
    /**
     * 格式化数字
     * @param value 数值
     * @param precision 小数位数
     * @param useScientific 是否使用科学计数法
     * @param trimZeros 是否去除末尾的零
     * @return 格式化后的字符串
     */
    fun format(
        value: Double,
        precision: Int = 10,
        useScientific: Boolean = false,
        trimZeros: Boolean = true
    ): String {
        // 处理特殊值
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        
        // 判断是否为整数
        if (value == value.toLong().toDouble() && !useScientific) {
            return value.toLong().toString()
        }
        
        return if (useScientific) {
            formatScientific(value, precision)
        } else {
            formatDecimal(value, precision, trimZeros)
        }
    }
    
    /**
     * 格式化为小数
     */
    private fun formatDecimal(
        value: Double,
        precision: Int,
        trimZeros: Boolean
    ): String {
        val decimal = BigDecimal(value).setScale(precision, RoundingMode.HALF_UP)
        return if (trimZeros) {
            decimal.stripTrailingZeros().toPlainString()
        } else {
            decimal.toPlainString()
        }
    }
    
    /**
     * 格式化为科学计数法
     */
    private fun formatScientific(value: Double, precision: Int): String {
        val decimal = BigDecimal(value).setScale(precision, RoundingMode.HALF_UP)
        val scientific = decimal.toString()
        
        return if (scientific.contains("E")) {
            scientific.replace("E", "×10^")
        } else {
            // 转换为科学计数法
            val exponent = Math.floor(Math.log10(Math.abs(value))).toInt()
            val mantissa = value / Math.pow(10.0, exponent.toDouble())
            val formattedMantissa = BigDecimal(mantissa)
                .setScale(precision, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
            "$formattedMantissa×10^$exponent"
        }
    }
    
    /**
     * 格式化为分数
     */
    fun formatAsFraction(value: Double): String {
        val tolerance = 1e-10
        var h1 = 1L
        var h2 = 0L
        var k1 = 0L
        var k2 = 1L
        var b = value
        var denominator = 1L
        
        do {
            val a = Math.floor(b)
            val aux = h1
            h1 = a.toLong() * h1 + h2
            h2 = aux
            val aux2 = k1
            k1 = a.toLong() * k1 + k2
            k2 = aux2
            b = 1.0 / (b - a)
            denominator = k1
        } while (Math.abs(value - h1.toDouble() / k1) > value * tolerance && denominator < 10000)
        
        return if (k1 == 1L) {
            h1.toString()
        } else {
            "$h1/$k1"
        }
    }
    
    /**
     * 格式化为百分比
     */
    fun formatAsPercentage(value: Double, precision: Int = 2): String {
        val percentage = value * 100
        return "${format(percentage, precision)}%"
    }
    
    /**
     * 格式化为角度
     */
    fun formatAsAngle(radians: Double): String {
        val degrees = Math.toDegrees(radians)
        return "${format(degrees, 4)}°"
    }
    
    /**
     * 格式化计算步骤
     */
    fun formatCalculationSteps(steps: List<String>): String {
        return steps.joinToString("\n") { step ->
            "• $step"
        }
    }
    
    /**
     * 格式化表达式
     * 添加空格使表达式更易读
     */
    fun formatExpression(expression: String): String {
        return expression
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("*", " × ")
            .replace("/", " ÷ ")
            .replace("(", " ( ")
            .replace(")", " ) ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    companion object {
        /**
         * 默认格式化器
         */
        val DEFAULT = ResultFormatter()
        
        /**
         * 高精度格式化器
         */
        val HIGH_PRECISION = ResultFormatter()
        
        /**
         * 科学计数法格式化器
         */
        val SCIENTIFIC = ResultFormatter()
    }
}