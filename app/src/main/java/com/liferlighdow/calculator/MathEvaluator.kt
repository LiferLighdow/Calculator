package com.liferlighdow.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

object MathEvaluator {
    private val mc = MathContext.DECIMAL128

    fun evaluate(expression: String, xValue: Double = 0.0, useDegrees: Boolean = true): Double {
        return try {
            evaluateBigDecimal(expression, BigDecimal(xValue.toString()), useDegrees)?.toDouble() ?: Double.NaN
        } catch (e: Exception) {
            Double.NaN
        }
    }

    fun evaluateBigDecimal(expression: String, xValue: BigDecimal = BigDecimal.ZERO, useDegrees: Boolean = true): BigDecimal? {
        return try {
            object : Any() {
                var pos = -1
                var ch = 0

                fun nextChar() {
                    ch = if (++pos < expression.length) expression[pos].code else -1
                }

                fun eat(c: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == c) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): BigDecimal {
                    nextChar()
                    val x = parseExpression()
                    while (ch == ' '.code) nextChar()
                    if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                    return x
                }

                fun parseExpression(): BigDecimal {
                    var x = parseTerm()
                    while (true) {
                        if (eat('+'.code)) x = x.add(parseTerm(), mc)
                        else if (eat('-'.code)) x = x.subtract(parseTerm(), mc)
                        else return x
                    }
                }

                fun parseTerm(): BigDecimal {
                    var x = parseFactor()
                    while (true) {
                        if (eat('*'.code)) x = x.multiply(parseFactor(), mc)
                        else if (eat('/'.code)) {
                            val divisor = parseFactor()
                            x = if (divisor.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else x.divide(divisor, mc)
                        } else return x
                    }
                }

                fun parseFactor(): BigDecimal {
                    while (ch == ' '.code) nextChar()
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return parseFactor().negate()
                    var x: BigDecimal
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                        while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code || ch == 'e'.code || ch == 'E'.code || ((ch == '+'.code || ch == '-'.code) && pos > 0 && expression[pos - 1].lowercaseChar() == 'e')) nextChar()
                        x = BigDecimal(expression.substring(startPos, pos))
                    } else if (ch == 'x'.code) {
                        nextChar()
                        x = xValue
                    } else if (ch.toChar().isLetter() || ch == '√'.code || ch == '∛'.code) {
                        while (ch.toChar().isLetter() || ch == '√'.code || ch == '∛'.code || (ch in '0'.code..'9'.code && pos > startPos)) nextChar()
                        val name = expression.substring(startPos, pos).lowercase()
                        
                        if (name == "pi" || name == "π") {
                            x = BigDecimal("3.1415926535897932384626433832795028841971693993751")
                        } else if (name == "e") {
                            x = BigDecimal("2.7182818284590452353602874713526624977572470936999")
                        } else if (name == "phi" || name == "φ") {
                            x = BigDecimal("1.618033988749895")
                        } else if (name == "gamma" || name == "γ") {
                            x = BigDecimal("0.577215664901532")
                        } else if (name == "i") {
                            x = BigDecimal.ZERO 
                        } else {
                            val args = mutableListOf<BigDecimal>()
                            if (eat('('.code)) {
                                args.add(parseExpression())
                                while (eat(','.code)) args.add(parseExpression())
                                eat(')'.code)
                            } else {
                                args.add(parseFactor())
                            }
                            
                            val a = if (args.isNotEmpty()) args[0] else BigDecimal.ZERO
                            val b = if (args.size > 1) args[1] else BigDecimal.ZERO
                            
                            val resDouble = when (name) {
                                "sin" -> {
                                    val angle = if (useDegrees) a.toDouble() * PI / 180.0 else a.toDouble()
                                    sin(angle)
                                }
                                "cos" -> {
                                    val angle = if (useDegrees) a.toDouble() * PI / 180.0 else a.toDouble()
                                    cos(angle)
                                }
                                "tan" -> {
                                    val angle = if (useDegrees) a.toDouble() * PI / 180.0 else a.toDouble()
                                    tan(angle)
                                }
                                "sec" -> {
                                    val angle = if (useDegrees) a.toDouble() * PI / 180.0 else a.toDouble()
                                    1.0 / cos(angle)
                                }
                                "csc" -> {
                                    val angle = if (useDegrees) a.toDouble() * PI / 180.0 else a.toDouble()
                                    1.0 / sin(angle)
                                }
                                "cot" -> {
                                    val angle = if (useDegrees) a.toDouble() * PI / 180.0 else a.toDouble()
                                    1.0 / tan(angle)
                                }
                                "asin" -> {
                                    val v = a.toDouble()
                                    if (v < -1.0 || v > 1.0) Double.NaN else {
                                        val res = asin(v)
                                        if (useDegrees) res * 180.0 / PI else res
                                    }
                                }
                                "acos" -> {
                                    val v = a.toDouble()
                                    if (v < -1.0 || v > 1.0) Double.NaN else {
                                        val res = acos(v)
                                        if (useDegrees) res * 180.0 / PI else res
                                    }
                                }
                                "atan" -> {
                                    val res = atan(a.toDouble())
                                    if (useDegrees) res * 180.0 / PI else res
                                }
                                "sinh" -> sinh(a.toDouble())
                                "cosh" -> cosh(a.toDouble())
                                "tanh" -> tanh(a.toDouble())
                                "asinh" -> ln(a.toDouble() + sqrt(a.toDouble() * a.toDouble() + 1.0))
                                "acosh" -> if (a.toDouble() < 1.0) Double.NaN else ln(a.toDouble() + sqrt(a.toDouble() * a.toDouble() - 1.0))
                                "atanh" -> if (abs(a.toDouble()) >= 1.0) Double.NaN else 0.5 * ln((1.0 + a.toDouble()) / (1.0 - a.toDouble()))
                                "log" -> log10(a.toDouble())
                                "ln" -> ln(a.toDouble())
                                "log2" -> log2(a.toDouble())
                                "sqrt", "√" -> if (a.toDouble() < 0.0) Double.NaN else sqrt(a.toDouble())
                                "cbrt", "∛" -> if (a.toDouble() < 0.0) -java.lang.Math.cbrt(-a.toDouble()) else java.lang.Math.cbrt(a.toDouble())
                                "abs" -> abs(a.toDouble())
                                "ceil" -> ceil(a.toDouble())
                                "floor" -> floor(a.toDouble())
                                "gcd" -> {
                                    if (args.isEmpty()) 0.0 else {
                                        var resVal = args[0].toBigInteger().abs()
                                        for (i in 1 until args.size) {
                                            resVal = resVal.gcd(args[i].toBigInteger().abs())
                                        }
                                        resVal.toDouble()
                                    }
                                }
                                "lcm" -> {
                                    if (args.isEmpty()) 0.0 else {
                                        var resVal = args[0].toBigInteger().abs()
                                        for (i in 1 until args.size) {
                                            val v = args[i].toBigInteger().abs()
                                            if (resVal == java.math.BigInteger.ZERO || v == java.math.BigInteger.ZERO) {
                                                resVal = java.math.BigInteger.ZERO
                                                break
                                            }
                                            resVal = (resVal.multiply(v)).divide(resVal.gcd(v))
                                        }
                                        resVal.toDouble()
                                    }
                                }
                                "ncr", "c" -> if (args.size < 2) 0.0 else nCr(a.toDouble(), b.toDouble())
                                "npr", "p" -> if (args.size < 2) 0.0 else nPr(a.toDouble(), b.toDouble())
                                "nhr", "h" -> if (args.size < 2) 0.0 else nCr(a.toDouble() + b.toDouble() - 1, b.toDouble())
                                else -> a.toDouble()
                            }
                            x = if (resDouble.isNaN()) throw ArithmeticException("NaN") else BigDecimal(resDouble.toString())
                        }
                    } else {
                        throw RuntimeException("Unexpected character: " + ch.toChar())
                    }
                    if (eat('!'.code)) {
                        x = BigDecimal(gamma(x.toDouble() + 1.0).toString())
                    }
                    
                    // Degree symbol support: if present, treat as degree regardless of global mode
                    if (eat('°'.code)) {
                        val rad = x.toDouble() * PI / 180.0
                        x = BigDecimal(rad.toString())
                        while (eat('°'.code)) { } 
                    }

                    if (eat('^'.code)) {
                        val exponent = parseFactor()
                        x = try {
                            x.pow(exponent.toInt(), mc)
                        } catch (e: Exception) {
                            val resPow = x.toDouble().pow(exponent.toDouble())
                            if (resPow.isNaN()) throw ArithmeticException("NaN") else BigDecimal(resPow.toString())
                        }
                    }
                    return x
                }
            }.parse()
        } catch (e: Exception) {
            null
        }
    }

    private fun nCr(n: Double, r: Double): Double {
        if (r < 0 || r > n) return 0.0
        var rVal = r
        if (rVal > n / 2) rVal = n - rVal
        var res = 1.0
        for (i in 1..rVal.toInt()) res = res * (n - rVal + i) / i
        return res
    }

    private fun nPr(n: Double, r: Double): Double {
        if (r < 0 || r > n) return 0.0
        var res = 1.0
        for (i in 0 until r.toInt()) res *= (n - i)
        return res
    }

    private fun gamma(z: Double): Double {
        if (z < 0.5) return PI / (sin(PI * z) * gamma(1.0 - z))
        val x = z - 1.0
        val p = doubleArrayOf(
            676.5203681218851, -1259.1392167224028,
            771.32342877765313, -176.61502916214059,
            12.507343278686905, -0.13857109526572012,
            9.9843695780195716e-6, 1.5056327351493116e-7
        )
        var y = 0.99999999999980993
        for (i in p.indices) {
            y += p[i] / (x + i + 1)
        }
        val t = x + p.size.toDouble() - 0.5
        return sqrt(2.0 * PI) * t.pow(x + 0.5) * exp(-t) * y
    }
}
