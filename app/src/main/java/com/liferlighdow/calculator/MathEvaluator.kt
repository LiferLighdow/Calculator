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
                } else if (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code || ch == '∛'.code) {
                    while (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code || ch == '∛'.code || (ch >= '0'.code && ch <= '9'.code && pos > startPos)) nextChar()
                    val func = expression.substring(startPos, pos)
                    val args = mutableListOf<Double>()
                    if (eat('('.code)) {
                        args.add(parseExpression())
                        while (eat(','.code)) args.add(parseExpression())
                        eat(')'.code)
                    } else {
                        args.add(parseFactor())
                    }
                    x = when (func) {
                        "sin" -> sin(args[0])
                        "cos" -> cos(args[0])
                        "tan" -> tan(args[0])
                        "asin" -> asin(args[0])
                        "acos" -> acos(args[0])
                        "atan" -> atan(args[0])
                        "sinh" -> sinh(args[0])
                        "cosh" -> cosh(args[0])
                        "tanh" -> tanh(args[0])
                        "log" -> log10(args[0])
                        "ln" -> ln(args[0])
                        "log2" -> log2(args[0])
                        "sqrt" -> sqrt(args[0])
                        "cbrt" -> args[0].pow(1.0 / 3.0)
                        "abs" -> abs(args[0])
                        "ceil" -> ceil(args[0])
                        "floor" -> floor(args[0])
                        "gcd" -> {
                            if (args.size < 2) 0.0 else {
                                var a = abs(args[0].toLong())
                                var b = abs(args[1].toLong())
                                while (b != 0L) {
                                    val t = b
                                    b = a % b
                                    a = t
                                }
                                a.toDouble()
                            }
                        }
                        "nCr" -> {
                            if (args.size < 2) 0.0 else {
                                val n = args[0].toInt()
                                var r = args[1].toInt()
                                if (r < 0 || r > n) 0.0 else {
                                    if (r > n / 2) r = n - r
                                    var res = 1.0
                                    for (i in 1..r) res = res * (n - r + i) / i
                                    res
                                }
                            }
                        }
                        "nPr" -> {
                            if (args.size < 2) 0.0 else {
                                val n = args[0].toInt()
                                val r = args[1].toInt()
                                if (r < 0 || r > n) 0.0 else {
                                    var res = 1.0
                                    for (i in 0 until r) res *= (n - i)
                                    res
                                }
                            }
                        }
                        else -> args[0]
                    }
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }
                if (eat('!'.code)) {
                    x = gamma(x + 1.0)
                }
                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }.parse()
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
