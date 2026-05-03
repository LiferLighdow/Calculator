package com.liferlighdow.calculator

import java.util.*
import kotlin.math.*

class MathNotepadEvaluator {
    private val variables = mutableMapOf<String, Double>()
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
        variables["pi"] = PI
        variables["e"] = E
        variables["π"] = PI
    }

    fun clearVariables() {
        variables.clear()
        assignedVariables.clear()
        equations.clear()
        resetConstants()
    }

    fun addEquation(left: String, right: String) {
        // If left is a pure variable name, it's an assignment
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
        
        // Remove constants and assigned variables
        unknownVars.remove("pi")
        unknownVars.remove("e")
        unknownVars.remove("π")
        assignedVariables.forEach { unknownVars.remove(it) }
        
        val varList = unknownVars.toList()
        if (varList.isEmpty()) return

        fun computeCost(p: DoubleArray): Double {
            varList.forEachIndexed { i, name -> variables[name] = p[i] }
            var totalError = 0.0
            for ((l, r) in equations) {
                try {
                    val diff = evaluateExpression(l) - evaluateExpression(r)
                    totalError += diff * diff
                } catch (e: Exception) {}
            }
            return totalError
        }

        // Numerical solver: Try multiple starting points to avoid local minima
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

        // Polishing: Try rounding to nearest integer if cost is very low
        if (minGlobalCost < 0.1) {
            val roundedP = DoubleArray(bestP.size) { i -> round(bestP[i]) }
            if (computeCost(roundedP) < minGlobalCost + 1e-9) {
                bestP = roundedP
            }
        }
        
        varList.forEachIndexed { i, name -> variables[name] = bestP[i] }
    }

    fun evaluate(expression: String): EvaluationResult {
        val cleanExpr = expression.replace("×", "*").replace("÷", "/")
        if (cleanExpr.contains("=")) {
            val parts = cleanExpr.split("=")
            val left = parts[0].trim()
            val right = parts[1].trim()
            
            if (assignedVariables.contains(left)) {
                return EvaluationResult(variables[left] ?: 0.0, left)
            }
            
            val vars = extractVariables(left) + extractVariables(right)
            val varName = vars.firstOrNull { !assignedVariables.contains(it) && it != "pi" && it != "e" && it != "π" } ?: "x"
            return EvaluationResult(variables[varName] ?: 0.0, varName, true)
        }
        return EvaluationResult(evaluateExpression(cleanExpr))
    }

    private fun extractVariables(expr: String): List<String> {
        val tokens = tokenize(expr)
        val functions = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "nCr", "nPr")
        val constants = setOf("pi", "e", "π")
        return tokens.filter { it[0].isLetter() && !functions.contains(it) && !constants.contains(it) }.distinct()
    }

    private fun isValidVariableName(name: String): Boolean {
        return name.isNotEmpty() && name.all { it.isLetter() || it == '_' }
    }

    private fun evaluateExpression(expr: String): Double {
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
        val functions = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "nCr", "nPr")

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

    private fun evaluateRPN(rpn: List<String>): Double {
        val stack = Stack<Double>()
        for (token in rpn) {
            when {
                token[0].isDigit() -> stack.push(token.toDouble())
                token == "+" -> { if(stack.size < 2) return 0.0; val b = stack.pop(); val a = stack.pop(); stack.push(a + b) }
                token == "-" -> { if(stack.size < 2) return 0.0; val b = stack.pop(); val a = stack.pop(); stack.push(a - b) }
                token == "*" -> { if(stack.size < 2) return 0.0; val b = stack.pop(); val a = stack.pop(); stack.push(a * b) }
                token == "/" -> { if(stack.size < 2) return 0.0; val b = stack.pop(); val a = stack.pop(); stack.push(if (b != 0.0) a / b else 0.0) }
                token == "^" -> { if(stack.size < 2) return 0.0; val b = stack.pop(); val a = stack.pop(); stack.push(a.pow(b)) }
                token == "!" -> { if(stack.isEmpty()) return 0.0; stack.push(factorial(stack.pop())) }
                token == "sin" -> { if(stack.isEmpty()) return 0.0; stack.push(sin(stack.pop())) }
                token == "cos" -> { if(stack.isEmpty()) return 0.0; stack.push(cos(stack.pop())) }
                token == "tan" -> { if(stack.isEmpty()) return 0.0; stack.push(tan(stack.pop())) }
                token == "sqrt" -> { if(stack.isEmpty()) return 0.0; stack.push(sqrt(stack.pop())) }
                token == "nCr" -> { if(stack.size < 2) return 0.0; val r = stack.pop(); val n = stack.pop(); stack.push(nCr(n, r)) }
                token == "nPr" -> { if(stack.size < 2) return 0.0; val r = stack.pop(); val n = stack.pop(); stack.push(nPr(n, r)) }
                variables.containsKey(token) -> stack.push(variables[token]!!)
                else -> stack.push(0.0)
            }
        }
        return if (stack.isNotEmpty()) stack.pop() else 0.0
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
        return factorial(n) / (factorial(r) * factorial(n - r))
    }

    private fun nPr(n: Double, r: Double): Double {
        if (r < 0 || r > n) return 0.0
        return factorial(n) / factorial(n - r)
    }
}
