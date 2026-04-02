package com.core.app.supercalchub.core.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * 分数类
 * 支持分数的加减乘除运算，结果可转换为小数
 */
data class Fraction(
    val numerator: BigInteger,
    val denominator: BigInteger = BigInteger.ONE
) {
    init {
        require(denominator != BigInteger.ZERO) { "分母不能为零" }
    }
    
    /**
     * 便捷构造函数
     */
    constructor(numerator: Int, denominator: Int = 1) : this(
        numerator.toBigInteger(),
        denominator.toBigInteger()
    )
    
    constructor(numerator: Long, denominator: Long = 1L) : this(
        numerator.toBigInteger(),
        denominator.toBigInteger()
    )
    
    // 自动简化为最简分数
    private val simplified: Fraction by lazy {
        simplify()
    }
    
    /**
     * 加法
     */
    operator fun plus(other: Fraction): Fraction {
        val lcm = lcm(denominator, other.denominator)
        val newNumerator = numerator * (lcm / denominator) + 
                          other.numerator * (lcm / other.denominator)
        return Fraction(newNumerator, lcm).simplify()
    }
    
    /**
     * 减法
     */
    operator fun minus(other: Fraction): Fraction {
        val lcm = lcm(denominator, other.denominator)
        val newNumerator = numerator * (lcm / denominator) - 
                          other.numerator * (lcm / other.denominator)
        return Fraction(newNumerator, lcm).simplify()
    }
    
    /**
     * 乘法
     */
    operator fun times(other: Fraction): Fraction {
        return Fraction(
            numerator * other.numerator,
            denominator * other.denominator
        ).simplify()
    }
    
    /**
     * 除法
     */
    operator fun div(other: Fraction): Fraction {
        require(other.numerator != BigInteger.ZERO) { "除数不能为零" }
        return Fraction(
            numerator * other.denominator,
            denominator * other.numerator
        ).simplify()
    }
    
    /**
     * 取负
     */
    operator fun unaryMinus(): Fraction {
        return Fraction(-numerator, denominator)
    }
    
    /**
     * 转换为小数
     */
    fun toDecimal(scale: Int = 10): BigDecimal {
        return BigDecimal(numerator)
            .divide(BigDecimal(denominator), scale, RoundingMode.HALF_UP)
            .stripTrailingZeros()
    }
    
    /**
     * 转换为双精度浮点数
     */
    fun toDouble(): Double {
        return toDecimal(15).toDouble()
    }
    
    /**
     * 化简分数
     */
    fun simplify(): Fraction {
        val gcd = gcd(numerator.abs(), denominator.abs())
        val sign = if (denominator < BigInteger.ZERO) -1 else 1
        return Fraction(
            numerator.divide(gcd) * sign.toBigInteger(),
            denominator.abs().divide(gcd)
        )
    }
    
    /**
     * 判断是否为真分数
     */
    fun isProper(): Boolean {
        return numerator.abs() < denominator.abs()
    }
    
    /**
     * 判断是否为整数
     */
    fun isInteger(): Boolean {
        return denominator == BigInteger.ONE
    }
    
    /**
     * 获取整数部分
     */
    fun getIntegerPart(): BigInteger {
        return numerator.divide(denominator)
    }
    
    /**
     * 获取分数部分
     */
    fun getFractionalPart(): Fraction {
        val integerPart = getIntegerPart()
        return Fraction(
            numerator - integerPart * denominator,
            denominator
        ).simplify()
    }
    
    /**
     * 获取带分数表示
     */
    fun toMixedNumber(): String {
        if (isInteger()) return numerator.toString()
        val integerPart = getIntegerPart()
        val fractionalPart = getFractionalPart()
        
        return if (integerPart != BigInteger.ZERO) {
            "$integerPart ${fractionalPart.numerator}/${fractionalPart.denominator}"
        } else {
            "${fractionalPart.numerator}/${fractionalPart.denominator}"
        }
    }
    
    override fun toString(): String {
        val simplified = simplify()
        return if (simplified.isInteger()) {
            simplified.numerator.toString()
        } else {
            "${simplified.numerator}/${simplified.denominator}"
        }
    }
    
    companion object {
        /**
         * 从字符串解析分数
         * 支持格式: "3/4", "1 1/2", "0.75"
         */
        fun parse(input: String): Fraction {
            val trimmed = input.trim()
            
            // 处理带分数: "1 1/2"
            if (trimmed.contains(" ")) {
                val parts = trimmed.split(" ")
                require(parts.size == 2) { "无效的分数格式" }
                val integerPart = parts[0].toBigInteger()
                val fractionalPart = parse(parts[1])
                return Fraction(integerPart) + fractionalPart
            }
            
            // 处理普通分数: "3/4"
            if (trimmed.contains("/")) {
                val parts = trimmed.split("/")
                require(parts.size == 2) { "无效的分数格式" }
                return Fraction(
                    parts[0].trim().toBigInteger(),
                    parts[1].trim().toBigInteger()
                )
            }
            
            // 处理小数: "0.75"
            if (trimmed.contains(".")) {
                val decimal = BigDecimal(trimmed)
                val scale = decimal.scale()
                val denominator = BigInteger.TEN.pow(scale)
                val numerator = decimal.multiply(BigDecimal(denominator)).toBigInteger()
                return Fraction(numerator, denominator).simplify()
            }
            
            // 处理整数
            return Fraction(trimmed.toBigInteger())
        }
        
        /**
         * 最大公约数
         */
        private fun gcd(a: BigInteger, b: BigInteger): BigInteger {
            return if (b == BigInteger.ZERO) a else gcd(b, a % b)
        }
        
        /**
         * 最小公倍数
         */
        private fun lcm(a: BigInteger, b: BigInteger): BigInteger {
            return a.multiply(b).divide(gcd(a, b))
        }
    }
}

/**
 * 分数运算步骤
 */
data class FractionStep(
    val description: String,
    val expression: String,
    val result: Fraction
) {
    override fun toString(): String {
        return "$description: $expression = $result"
    }
}

/**
 * 分数计算结果
 */
sealed class FractionResult {
    data class Success(
        val fraction: Fraction,
        val decimal: BigDecimal,
        val steps: List<FractionStep>,
        val originalExpression: String
    ) : FractionResult()
    
    data class Error(
        val message: String,
        val originalExpression: String
    ) : FractionResult()
}