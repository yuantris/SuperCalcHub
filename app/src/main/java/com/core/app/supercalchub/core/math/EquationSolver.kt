package com.core.app.supercalchub.core.math

import org.apache.commons.math3.analysis.solvers.BisectionSolver
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.analysis.solvers.NewtonSolver
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.abs

/**
 * 方程求解器
 * 支持一元一次方程、一元二次方程、二元一次方程组等
 */
class EquationSolver {
    
    /**
     * 求解一元一次方程: ax + b = 0
     * @return 方程的解
     */
    fun solveLinearEquation(a: Double, b: Double): EquationResult {
        return if (a == 0.0) {
            if (b == 0.0) {
                EquationResult.InfiniteSolutions("方程有无穷多解")
            } else {
                EquationResult.NoSolution("方程无解")
            }
        } else {
            val solution = -b / a
            EquationResult.SingleSolution(
                solution = solution,
                formattedSolution = formatNumber(solution),
                description = "x = -b/a = -($b)/($a) = ${formatNumber(solution)}"
            )
        }
    }
    
    /**
     * 求解一元二次方程: ax² + bx + c = 0
     * @return 方程的解
     */
    fun solveQuadraticEquation(a: Double, b: Double, c: Double): EquationResult {
        if (a == 0.0) {
            return solveLinearEquation(b, c)
        }
        
        val discriminant = b * b - 4 * a * c
        val steps = mutableListOf<String>()
        steps.add("判别式 Δ = b² - 4ac = $b² - 4×$a×$c = ${formatNumber(discriminant)}")
        
        return when {
            discriminant > 0 -> {
                val sqrtD = Math.sqrt(discriminant)
                val x1 = (-b + sqrtD) / (2 * a)
                val x2 = (-b - sqrtD) / (2 * a)
                steps.add("Δ > 0，方程有两个不相等的实根")
                steps.add("x₁ = (-b + √Δ) / (2a) = ${formatNumber(x1)}")
                steps.add("x₂ = (-b - √Δ) / (2a) = ${formatNumber(x2)}")
                
                EquationResult.TwoSolutions(
                    solution1 = x1,
                    solution2 = x2,
                    formattedSolution1 = formatNumber(x1),
                    formattedSolution2 = formatNumber(x2),
                    description = steps.joinToString("\n")
                )
            }
            discriminant == 0.0 -> {
                val x = -b / (2 * a)
                steps.add("Δ = 0，方程有两个相等的实根")
                steps.add("x = -b / (2a) = ${formatNumber(x)}")
                
                EquationResult.DoubleSolution(
                    solution = x,
                    formattedSolution = formatNumber(x),
                    description = steps.joinToString("\n")
                )
            }
            else -> {
                val realPart = -b / (2 * a)
                val imaginaryPart = Math.sqrt(-discriminant) / (2 * a)
                steps.add("Δ < 0，方程有两个复数根")
                steps.add("x₁ = ${formatNumber(realPart)} + ${formatNumber(imaginaryPart)}i")
                steps.add("x₂ = ${formatNumber(realPart)} - ${formatNumber(imaginaryPart)}i")
                
                EquationResult.ComplexSolutions(
                    realPart = realPart,
                    imaginaryPart = imaginaryPart,
                    formattedSolution1 = "${formatNumber(realPart)} + ${formatNumber(imaginaryPart)}i",
                    formattedSolution2 = "${formatNumber(realPart)} - ${formatNumber(imaginaryPart)}i",
                    description = steps.joinToString("\n")
                )
            }
        }
    }
    
    /**
     * 求解二元一次方程组:
     * a1*x + b1*y = c1
     * a2*x + b2*y = c2
     * @return 解的结果
     */
    fun solveLinearSystem(
        a1: Double, b1: Double, c1: Double,
        a2: Double, b2: Double, c2: Double
    ): EquationResult {
        val determinant = a1 * b2 - a2 * b1
        val steps = mutableListOf<String>()
        steps.add("行列式 D = a₁b₂ - a₂b₁ = $a1×$b2 - $a2×$b1 = ${formatNumber(determinant)}")
        
        return when {
            determinant != 0.0 -> {
                val x = (c1 * b2 - c2 * b1) / determinant
                val y = (a1 * c2 - a2 * c1) / determinant
                steps.add("D ≠ 0，方程组有唯一解")
                steps.add("Dx = c₁b₂ - c₂b₁ = ${formatNumber(c1 * b2 - c2 * b1)}")
                steps.add("Dy = a₁c₂ - a₂c₁ = ${formatNumber(a1 * c2 - a2 * c1)}")
                steps.add("x = Dx/D = ${formatNumber(x)}")
                steps.add("y = Dy/D = ${formatNumber(y)}")
                
                EquationResult.LinearSystemSolution(
                    x = x,
                    y = y,
                    formattedX = formatNumber(x),
                    formattedY = formatNumber(y),
                    description = steps.joinToString("\n")
                )
            }
            else -> {
                val dx = c1 * b2 - c2 * b1
                val dy = a1 * c2 - a2 * c1
                when {
                    dx == 0.0 && dy == 0.0 -> {
                        steps.add("D = 0, Dx = 0, Dy = 0，方程组有无穷多解")
                        EquationResult.InfiniteSolutions(steps.joinToString("\n"))
                    }
                    else -> {
                        steps.add("D = 0, Dx ≠ 0 或 Dy ≠ 0，方程组无解")
                        EquationResult.NoSolution(steps.joinToString("\n"))
                    }
                }
            }
        }
    }
    
    /**
     * 求解一元方程（通用方法）
     * 使用数值方法求解
     */
    fun solveEquation(
        expression: String,
        variableName: String = "x",
        lowerBound: Double = -1000.0,
        upperBound: Double = 1000.0
    ): EquationResult {
        return try {
            val function = object : UnivariateFunction {
                override fun value(x: Double): Double {
                    val exp = ExpressionBuilder(expression)
                        .variable(variableName)
                        .build()
                        .setVariable(variableName, x)
                    return exp.evaluate()
                }
            }
            
            // 使用Brent求解器
            val solver = BrentSolver(1e-10, 1e-10, 1e-10)
            val solution = solver.solve(1000, function, lowerBound, upperBound)
            
            EquationResult.SingleSolution(
                solution = solution,
                formattedSolution = formatNumber(solution),
                description = "通过数值方法求解得到 x = ${formatNumber(solution)}"
            )
        } catch (e: Exception) {
            EquationResult.NoSolution("无法找到解: ${e.message}")
        }
    }
    
    /**
     * 格式化数字
     */
    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.6f", value).trimEnd('0').trimEnd('.')
        }
    }
}

/**
 * 方程求解结果
 */
sealed class EquationResult {
    data class SingleSolution(
        val solution: Double,
        val formattedSolution: String,
        val description: String
    ) : EquationResult()
    
    data class DoubleSolution(
        val solution: Double,
        val formattedSolution: String,
        val description: String
    ) : EquationResult()
    
    data class TwoSolutions(
        val solution1: Double,
        val solution2: Double,
        val formattedSolution1: String,
        val formattedSolution2: String,
        val description: String
    ) : EquationResult()
    
    data class ComplexSolutions(
        val realPart: Double,
        val imaginaryPart: Double,
        val formattedSolution1: String,
        val formattedSolution2: String,
        val description: String
    ) : EquationResult()
    
    data class LinearSystemSolution(
        val x: Double,
        val y: Double,
        val formattedX: String,
        val formattedY: String,
        val description: String
    ) : EquationResult()
    
    data class InfiniteSolutions(
        val description: String
    ) : EquationResult()
    
    data class NoSolution(
        val description: String
    ) : EquationResult()
    
    data class Error(
        val message: String
    ) : EquationResult()
}