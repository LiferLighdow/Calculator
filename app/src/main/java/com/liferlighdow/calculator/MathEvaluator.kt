package com.liferlighdow.calculator

import kotlin.math.*

object MathEvaluator {
    fun evaluate(expression: String, xValue: Double = 0.0): Double {
        return object : Any() {
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

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                while (ch == ' '.code) nextChar()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                while (ch == ' '.code) nextChar()
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code || ch == 'e'.code || ch == 'E'.code || ((ch == '+'.code || ch == '-'.code) && pos > 0 && expression[pos - 1].lowercaseChar() == 'e')) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else if (ch == 'x'.code) {
                    nextChar()
                    x = xValue
                } else if (ch.toChar().isLetter() || ch == '√'.code || ch == '∛'.code) {
                    while (ch.toChar().isLetter() || ch == '√'.code || ch == '∛'.code || (ch in '0'.code..'9'.code && pos > startPos)) nextChar()
                    val name = expression.substring(startPos, pos).lowercase()
                    
                    if (name == "pi" || name == "π") {
                        x = PI
                    } else if (name == "e") {
                        x = E
                    } else if (name == "phi" || name == "φ") {
                        x = 1.618033988749895
                    } else if (name == "gamma" || name == "γ") {
                        x = 0.577215664901532
                    } else if (name == "i") {
                        x = Double.NaN
                    } else {
                        val args = mutableListOf<Double>()
                        if (eat('('.code)) {
                            args.add(parseExpression())
                            while (eat(','.code)) args.add(parseExpression())
                            eat(')'.code)
                        } else {
                            args.add(parseFactor())
                        }
                        
                        val a = if (args.isNotEmpty()) args[0] else 0.0
                        val b = if (args.size > 1) args[1] else 0.0
                        
                        x = when (name) {
                            "sin" -> sin(a * PI / 180.0)
                            "cos" -> cos(a * PI / 180.0)
                            "tan" -> tan(a * PI / 180.0)
                            "sec" -> 1.0 / cos(a * PI / 180.0)
                            "csc" -> 1.0 / sin(a * PI / 180.0)
                            "cot" -> 1.0 / tan(a * PI / 180.0)
                            "asin" -> {
                                // 如果真的大於 1.0 太多（例如 1.001），才視為 NaN
                                if (abs(a) > 1.00000001) Double.NaN
                                else asin(a.coerceIn(-1.0, 1.0)) * 180.0 / PI
                            }
                            "acos" -> {
                                if (abs(a) > 1.00000001) Double.NaN
                                else acos(a.coerceIn(-1.0, 1.0)) * 180.0 / PI
                            }
                            "atan" -> atan(a) * 180.0 / PI
                            "sinh" -> sinh(a)
                            "cosh" -> cosh(a)
                            "tanh" -> tanh(a)
                            "asinh" -> ln(a + sqrt(a * a + 1.0))
                            "acosh" -> {
                                if (a < 1.0) Double.NaN
                                else ln(a + sqrt(a * a - 1.0))
                            }
                            "atanh" -> {
                                if (abs(a) >= 1.0) Double.NaN
                                else 0.5 * ln((1.0 + a) / (1.0 - a))
                            }
                            "log" -> log10(a)
                            "ln" -> ln(a)
                            "log2" -> log2(a)
                            "sqrt", "√" -> if (a < 0.0) Double.NaN else sqrt(a)
                            "cbrt", "∛" -> if (a < 0.0) -java.lang.Math.cbrt(-a) else java.lang.Math.cbrt(a)
                            "abs" -> abs(a)
                            "ceil" -> ceil(a)
                            "floor" -> floor(a)
                            "gcd" -> {
                                if (args.isEmpty()) 0.0 else {
                                    var resVal = abs(args[0].toLong())
                                    for (i in 1 until args.size) {
                                        var v = abs(args[i].toLong())
                                        while (v != 0L) {
                                            val t = v
                                            v = resVal % v
                                            resVal = t
                                        }
                                    }
                                    resVal.toDouble()
                                }
                            }
                            "lcm" -> {
                                if (args.isEmpty()) 0.0 else {
                                    var resVal = abs(args[0].toLong())
                                    for (i in 1 until args.size) {
                                        val v = abs(args[i].toLong())
                                        if (resVal == 0L || v == 0L) { resVal = 0L; break }
                                        var x1 = resVal
                                        var y1 = v
                                        while (y1 != 0L) { val t = y1; y1 = x1 % y1; x1 = t }
                                        resVal = (resVal / x1) * v
                                    }
                                    resVal.toDouble()
                                }
                            }
                            "ncr", "c" -> if (args.size < 2) 0.0 else nCr(a, b)
                            "npr", "p" -> if (args.size < 2) 0.0 else nPr(a, b)
                            "nhr", "h" -> if (args.size < 2) 0.0 else nCr(a + b - 1, b)
                            else -> if (args.isNotEmpty()) args[0] else 0.0
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }
                if (eat('!'.code)) {
                    x = gamma(x + 1.0)
                }
                while (eat('°'.code)) { /* degree symbol ignored as we are in degree mode */ }
                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }.parse()
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
