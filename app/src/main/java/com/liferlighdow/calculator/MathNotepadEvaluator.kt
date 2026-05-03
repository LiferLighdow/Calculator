package com.liferlighdow.calculator

import java.util.*
import kotlin.math.*

class MathNotepadEvaluator {
    private val variables = mutableMapOf<String, Double>()
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
        equations.clear()
        resetConstants()
    }

    // New: Add equation to the system without solving immediately
    fun addEquation(left: String, right: String) {
        equations.add(left to right)
    }

    // Solve all collected equations as a system
    fun solveSystem() {
        val allVars = mutableSetOf<String>()
        equations.forEach { (l, r) ->
            allVars.addAll(extractVariables(l))
            allVars.addAll(extractVariables(r))
        }
        // Exclude constants
        allVars.remove("pi")
        allVars.remove("e")
        allVars.remove("π")
        
        val varList = allVars.toList()
        if (varList.isEmpty()) return

        // Cost function: sum of squared differences
        fun computeCost(p: DoubleArray): Double {
            val originalVals = varList.map { variables[it] }
            varList.forEachIndexed { i, name -> variables[name] = p[i] }
            var totalError = 0.0
            for ((l, r) in equations) {
                try {
                    val diff = evaluateExpression(l) - evaluateExpression(r)
                    totalError += diff * diff
                } catch (e: Exception) {}
            }
            // Restore (though we usually want to keep the best p)
            return totalError
        }

        // Numerical solver (Simple Gradient Descent with Momentum)
        var p = DoubleArray(varList.size) { variables[varList[it]] ?: 1.0 }
        var rate = 0.1
        var bestP = p.copyOf()
        var minCost = computeCost(p)

        for (iter in 0 until 200) {
            val currentCost = computeCost(p)
            if (currentCost < 1e-10) break
            
            val gradient = DoubleArray(p.size)
            val h = 1e-6
            for (i in p.indices) {
                val oldVal = p[i]
                p[i] += h
                val cPlus = computeCost(p)
                gradient[i] = (cPlus - currentCost) / h
                p[i] = oldVal
            }

            for (i in p.indices) {
                p[i] -= rate * gradient[i]
            }
            
            val newCost = computeCost(p)
            if (newCost < minCost) {
                minCost = newCost
                bestP = p.copyOf()
                rate *= 1.1 // Accelerate
            } else {
                rate *= 0.5 // Slow down and backtrack
                p = bestP.copyOf()
            }
        }
        
        // Final values
        varList.forEachIndexed { i, name -> variables[name] = bestP[i] }
    }

    fun evaluate(expression: String): EvaluationResult {
        val cleanExpr = expression.replace("×", "*").replace("÷", "/")
        
        if (cleanExpr.contains("=")) {
            val parts = cleanExpr.split("=")
            if (parts.size == 2) {
                val left = parts[0].trim()
                val right = parts[1].trim()
                
                if (isValidVariableName(left) && !extractVariables(right).contains(left)) {
                    val value = evaluateExpression(right)
                    variables[left] = value
                    return EvaluationResult(value, left)
                }
                
                // If it looks like an equation, it should have been solved by solveSystem()
                // But for a single line evaluation, we provide the current variable state
                val varName = extractVariables(left).firstOrNull() ?: extractVariables(right).firstOrNull() ?: "x"
                return EvaluationResult(variables[varName] ?: 0.0, varName, true)
            }
        }
        return EvaluationResult(evaluateExpression(cleanExpr))
    }

    private fun extractVariables(expr: String): List<String> {
        val tokens = tokenize(expr)
        val functions = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "nCr", "nPr")
        return tokens.filter { it[0].isLetter() && !functions.contains(it) }.distinct()
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
