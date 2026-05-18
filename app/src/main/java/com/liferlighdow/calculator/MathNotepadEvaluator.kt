package com.liferlighdow.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.math.*

class MathNotepadEvaluator {
    private val mc = MathContext.DECIMAL128
    private val variables = mutableMapOf<String, BigDecimal>()
    private val assignedVariables = mutableSetOf<String>()
    private val equations = mutableListOf<Pair<String, String>>()

    data class EvaluationResult(
        val value: Double,
        val variableName: String? = null,
        val isEquation: Boolean = false,
        val error: Double = 0.0
    )

    init {
        resetConstants()
    }

    private fun resetConstants() {
        variables["pi"] = BigDecimal(PI.toString())
        variables["e"] = BigDecimal(E.toString())
        variables["π"] = BigDecimal(PI.toString())
    }

    fun clearVariables() {
        variables.clear()
        assignedVariables.clear()
        equations.clear()
        resetConstants()
    }

    fun addEquation(left: String, right: String) {
        if (isValidVariableName(left)) {
            try {
                val value = evaluateExpression(right)
                variables[left] = value
                assignedVariables.add(left)
                return
            } catch (e: Exception) {}
        }
        equations.add(left to right)
    }

    fun solveSystem() {
        val unknownVars = mutableSetOf<String>()
        equations.forEach { (l, r) ->
            unknownVars.addAll(extractVariables(l))
            unknownVars.addAll(extractVariables(r))
        }
        
        unknownVars.remove("pi")
        unknownVars.remove("e")
        unknownVars.remove("π")
        assignedVariables.forEach { unknownVars.remove(it) }
        
        val varList = unknownVars.toList()
        if (varList.isEmpty()) return

        fun computeCost(p: DoubleArray): Double {
            varList.forEachIndexed { i, name -> variables[name] = BigDecimal(p[i].toString()) }
            var totalError = 0.0
            for ((l, r) in equations) {
                try {
                    val diff = (evaluateExpression(l).subtract(evaluateExpression(r), mc)).toDouble()
                    totalError += diff * diff
                } catch (e: Exception) {}
            }
            return totalError
        }

        val startPoints = listOf(
            DoubleArray(varList.size) { 1.0 },
            DoubleArray(varList.size) { 0.0 },
            DoubleArray(varList.size) { -1.0 }
        )

        var bestP = DoubleArray(varList.size) { 1.0 }
        var minGlobalCost = Double.MAX_VALUE

        for (startP in startPoints) {
            var p = startP.copyOf()
            var rate = 0.1
            var bestLocalP = p.copyOf()
            var minLocalCost = computeCost(p)

            for (iter in 0 until 100) {
                if (minLocalCost < 1e-12) break
                
                val gradient = DoubleArray(p.size)
                val h = 1e-7
                for (i in p.indices) {
                    val oldVal = p[i]
                    p[i] += h
                    val cPlus = computeCost(p)
                    gradient[i] = (cPlus - minLocalCost) / h
                    p[i] = oldVal
                }

                val nextP = DoubleArray(p.size) { i -> p[i] - rate * gradient[i] }
                val nextCost = computeCost(nextP)
                
                if (nextCost < minLocalCost) {
                    minLocalCost = nextCost
                    bestLocalP = nextP.copyOf()
                    p = nextP
                    rate *= 1.2
                } else {
                    rate *= 0.3
                }
            }
            
            if (minLocalCost < minGlobalCost) {
                minGlobalCost = minLocalCost
                bestP = bestLocalP
            }
        }

        if (minGlobalCost < 0.1) {
            val roundedP = DoubleArray(bestP.size) { i -> round(bestP[i]) }
            if (computeCost(roundedP) < minGlobalCost + 1e-9) {
                bestP = roundedP
            }
        }
        
        varList.forEachIndexed { i, name -> variables[name] = BigDecimal(bestP[i].toString()) }
    }

    fun evaluate(expression: String): EvaluationResult {
        val cleanExpr = expression.replace("×", "*").replace("÷", "/")
        if (cleanExpr.contains("=")) {
            val parts = cleanExpr.split("=")
            val left = parts[0].trim()
            val right = parts[1].trim()
            
            if (assignedVariables.contains(left)) {
                return EvaluationResult((variables[left] ?: BigDecimal.ZERO).toDouble(), left)
            }
            
            val vars = extractVariables(left) + extractVariables(right)
            val varName = vars.firstOrNull { !assignedVariables.contains(it) && it != "pi" && it != "e" && it != "π" } ?: "x"
            return EvaluationResult((variables[varName] ?: BigDecimal.ZERO).toDouble(), varName, true)
        }
        return EvaluationResult(evaluateExpression(cleanExpr).toDouble())
    }

    private fun extractVariables(expr: String): List<String> {
        val tokens = tokenize(expr)
        val functions = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "nCr", "nPr", "P", "C", "H", "gcd", "lcm")
        val constants = setOf("pi", "e", "π")
        return tokens.filter { it[0].isLetter() && !functions.contains(it) && !constants.contains(it) }.distinct()
    }

    private fun isValidVariableName(name: String): Boolean {
        return name.isNotEmpty() && name.all { it.isLetter() || it == '_' }
    }

    private fun evaluateExpression(expr: String): BigDecimal {
        val tokens = tokenize(expr)
        val rpn = shuntingYard(tokens)
        return evaluateRPN(rpn)
    }

    private fun tokenize(expr: String): List<String> {
        val rawTokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() || c == ',' -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i++])
                    }
                    rawTokens.add(sb.toString())
                }
                c.isLetter() || c == '_' || c == 'Σ' || c == 'π' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isLetterOrDigit() || expr[i] == '_' || expr[i] == 'Σ' || expr[i] == 'π')) {
                        sb.append(expr[i++])
                    }
                    rawTokens.add(sb.toString())
                }
                "+-*/^()!".contains(c) -> {
                    rawTokens.add(c.toString())
                    i++
                }
                else -> i++
            }
        }

        val tokens = mutableListOf<String>()
        for (j in rawTokens.indices) {
            val current = rawTokens[j]
            tokens.add(current)
            if (j < rawTokens.size - 1) {
                val next = rawTokens[j + 1]
                if ((current[0].isDigit() && (next[0].isLetter() || next == "(" || next == "π")) ||
                    (current == ")" && (next[0].isLetterOrDigit() || next == "(" || next == "π")) ||
                    (current[0].isLetter() && next == "(") ||
                    (current == "!" && next[0].isLetterOrDigit())) {
                    tokens.add("*")
                }
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3, "!" to 4)
        val functions = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "nCr", "nPr", "P", "C", "H", "gcd", "lcm")

        for (token in tokens) {
            when {
                token[0].isDigit() -> output.add(token)
                functions.contains(token) -> stack.push(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty()) stack.pop()
                    if (stack.isNotEmpty() && functions.contains(stack.peek())) {
                        output.add(stack.pop())
                    }
                }
                precedence.containsKey(token) -> {
                    while (stack.isNotEmpty() && stack.peek() != "(" &&
                        precedence.getOrDefault(stack.peek(), 0) >= precedence[token]!!
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
                else -> output.add(token)
            }
        }
        while (stack.isNotEmpty()) output.add(stack.pop())
        return output
    }

    private fun evaluateRPN(rpn: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()
        for (token in rpn) {
            when {
                token[0].isDigit() -> stack.push(BigDecimal(token))
                token == "+" -> { if(stack.size < 2) return BigDecimal.ZERO; val b = stack.pop(); val a = stack.pop(); stack.push(a.add(b, mc)) }
                token == "-" -> { if(stack.size < 2) return BigDecimal.ZERO; val b = stack.pop(); val a = stack.pop(); stack.push(a.subtract(b, mc)) }
                token == "*" -> { if(stack.size < 2) return BigDecimal.ZERO; val b = stack.pop(); val a = stack.pop(); stack.push(a.multiply(b, mc)) }
                token == "/" -> { if(stack.size < 2) return BigDecimal.ZERO; val b = stack.pop(); val a = stack.pop(); stack.push(if (b.compareTo(BigDecimal.ZERO) != 0) a.divide(b, mc) else BigDecimal.ZERO) }
                token == "^" -> { 
                    if(stack.size < 2) return BigDecimal.ZERO
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(try { a.pow(b.toInt(), mc) } catch(e: Exception) { BigDecimal(a.toDouble().pow(b.toDouble()).toString()) }) 
                }
                token == "!" -> { if(stack.isEmpty()) return BigDecimal.ZERO; stack.push(BigDecimal(factorial(stack.pop().toDouble()).toString())) }
                token == "sin" -> { if(stack.isEmpty()) return BigDecimal.ZERO; stack.push(BigDecimal(sin(stack.pop().toDouble()).toString())) }
                token == "cos" -> { if(stack.isEmpty()) return BigDecimal.ZERO; stack.push(BigDecimal(cos(stack.pop().toDouble()).toString())) }
                token == "tan" -> { if(stack.isEmpty()) return BigDecimal.ZERO; stack.push(BigDecimal(tan(stack.pop().toDouble()).toString())) }
                token == "sqrt" -> { if(stack.isEmpty()) return BigDecimal.ZERO; stack.push(BigDecimal(sqrt(stack.pop().toDouble()).toString())) }
                token == "gcd" -> { if(stack.size < 2) return BigDecimal.ZERO; val b = stack.pop(); val a = stack.pop(); stack.push(BigDecimal(gcd(a.toLong(), b.toLong()).toString())) }
                token == "lcm" -> { if(stack.size < 2) return BigDecimal.ZERO; val b = stack.pop(); val a = stack.pop(); stack.push(BigDecimal(lcm(a.toLong(), b.toLong()).toString())) }
                token == "nCr" || token == "C" -> { if(stack.size < 2) return BigDecimal.ZERO; val r = stack.pop(); val n = stack.pop(); stack.push(BigDecimal(nCr(n.toDouble(), r.toDouble()).toString())) }
                token == "nPr" || token == "P" -> { if(stack.size < 2) return BigDecimal.ZERO; val r = stack.pop(); val n = stack.pop(); stack.push(BigDecimal(nPr(n.toDouble(), r.toDouble()).toString())) }
                token == "H" -> { if(stack.size < 2) return BigDecimal.ZERO; val r = stack.pop(); val n = stack.pop(); stack.push(BigDecimal(nCr(n.toDouble() + r.toDouble() - 1, r.toDouble()).toString())) }
                variables.containsKey(token) -> stack.push(variables[token]!!)
                else -> stack.push(BigDecimal.ZERO)
            }
        }
        return if (stack.isNotEmpty()) stack.pop() else BigDecimal.ZERO
    }

    private fun factorial(n: Double): Double {
        val num = n.toInt()
        if (num < 0) return 0.0
        var res = 1.0
        for (i in 1..num) res *= i
        return res
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
    
    private fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }
    
    private fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a * b) / gcd(a, b)
    }
}
