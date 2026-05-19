package com.liferlighdow.calculator

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: EditText
    private lateinit var tvResult: EditText
    private lateinit var drawerLayout: DrawerLayout
    private var stateError: Boolean = false
    private var lastResult: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        drawerLayout = findViewById(R.id.drawerLayout)
        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)

        setupImmersiveMode()

        tvExpression.showSoftInputOnFocus = false
        tvResult.showSoftInputOnFocus = false
        tvResult.isFocusable = true
        tvResult.isFocusableInTouchMode = true
        tvResult.inputType = InputType.TYPE_NULL

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets -> insets }

        findViewById<MaterialButton>(R.id.btnOpenDrawer)?.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<MaterialButton>(R.id.btnGoToConverter)?.setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<MaterialButton>(R.id.btnGoToSpecialCalculators)?.setOnClickListener {
            startActivity(Intent(this, SpecialCalculatorsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<MaterialButton>(R.id.btnGoToGeometry)?.setOnClickListener {
            startActivity(Intent(this, GeometryActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<MaterialButton>(R.id.btnGoToMathNotepad)?.setOnClickListener {
            startActivity(Intent(this, MathNotepadActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        setupButtons()
    }

    private fun setupImmersiveMode() {
        val controller = ViewCompat.getWindowInsetsController(window.decorView) ?: return
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setupImmersiveMode()
    }

    private fun insertText(text: String) {
        val pos = tvExpression.selectionStart
        val old = tvExpression.text.toString()
        tvExpression.setText(StringBuilder(old).insert(pos, text).toString())
        tvExpression.setSelection(pos + text.length)
    }

    private fun setupButtons() {
        val numericIds = listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot)
        val numListener = View.OnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (stateError) { tvExpression.setText(""); stateError = false }
            if (v.id == R.id.btnDot) {
                val before = tvExpression.text.toString().substring(0, tvExpression.selectionStart)
                val lastPart = before.split('+', '-', '×', '÷', '*', '/', '(', ')', '^', ',', '!', '%', '°').last()
                if (!lastPart.contains(".")) insertText(if (before.isEmpty() || !before.last().isDigit()) "0." else ".")
            } else insertText((v as MaterialButton).text.toString())
            calculateResult(false)
        }
        numericIds.forEach { findViewById<MaterialButton>(it)?.setOnClickListener(numListener) }

        val ops = mapOf(R.id.btnAdd to "+", R.id.btnSub to "-", R.id.btnMul to "×", R.id.btnDiv to "÷", R.id.btnPercent to "%", R.id.btnPow to "^", R.id.btnSqrt to "√(", R.id.btnFact to "!", R.id.btnComma to ",")
        val opListener = View.OnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            insertText(ops[v.id] ?: "")
            calculateResult(false)
        }
        ops.keys.forEach { findViewById<MaterialButton>(it)?.setOnClickListener(opListener) }

        findViewById<MaterialButton>(R.id.btnAC)?.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            tvExpression.setText(""); tvResult.setText("0"); tvResult.tag = 0.0; stateError = false
        }

        findViewById<MaterialButton>(R.id.btnDel)?.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val text = tvExpression.text.toString(); val cursor = tvExpression.selectionStart
            if (cursor > 0) {
                val before = text.substring(0, cursor); val after = text.substring(cursor)
                val funcs = listOf("asinh(", "acosh(", "atanh(", "asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(", "log2(", "sqrt(", "cbrt(", "ceil(", "floor(", "abs(", "exp(", "sin(", "cos(", "tan(", "sec(", "csc(", "cot(", "log(", "ln(", "P(", "C(", "H(", "gcd(", "lcm(", "√(", "∛(")
                var found = false
                for (f in funcs) { if (before.endsWith(f)) { tvExpression.setText(before.dropLast(f.length) + after); tvExpression.setSelection(cursor - f.length); found = true; break } }
                if (!found) { tvExpression.setText(before.dropLast(1) + after); tvExpression.setSelection(cursor - 1) }
                calculateResult(false)
            }
        }

        findViewById<MaterialButton>(R.id.btnEqual)?.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (tvExpression.text.isNotEmpty() && !stateError) {
                calculateResult(true)
                if (!stateError && tvResult.text.toString() != "Error") {
                    val resVal = tvResult.tag as? Double ?: 0.0
                    lastResult = resVal
                    val fraction = toFraction(resVal)
                    val formattedDecimal = formatResult(resVal)
                    tvExpression.setText(formattedDecimal)
                    tvExpression.setSelection(tvExpression.text.length)
                    if (fraction != null) {
                        tvResult.setText(fraction)
                    } else {
                        tvResult.setText(formattedDecimal)
                    }
                }
            }
        }
        setupScientificButtons()
    }

    private fun setupScientificButtons() {
        val sci = mapOf(R.id.btnSin to "sin(", R.id.btnCos to "cos(", R.id.btnTan to "tan(", R.id.btnSec to "sec(", R.id.btnCsc to "csc(", R.id.btnCot to "cot(", R.id.btnAsin to "asin(", R.id.btnAcos to "acos(", R.id.btnAtan to "atan(", R.id.btnSinh to "sinh(", R.id.btnCosh to "cosh(", R.id.btnTanh to "tanh(", R.id.btnAsinh to "asinh(", R.id.btnAcosh to "acosh(", R.id.btnAtanh to "atanh(", R.id.btnLog to "log(", R.id.btnLn to "ln(", R.id.btnLog2 to "log2(", R.id.btnCbrt to "∛(", R.id.btnAbs to "abs(", R.id.btnCeil to "ceil(", R.id.btnFloor to "floor(", R.id.btnPi to "π", R.id.btnE to "e", R.id.btnPhi to "φ", R.id.btnGamma to "γ", R.id.btnAns to "Ans", R.id.btnGCD to "gcd(", R.id.btnLCM to "lcm(", R.id.btnNcr to "C(", R.id.btnNpr to "P(", R.id.btnNhr to "H(", R.id.btnDegree to "°")
        val sciListener = View.OnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (v.id == R.id.btnAns) {
                insertText(formatResult(lastResult))
            } else {
                insertText(sci[v.id] ?: "")
            }
            calculateResult(false)
            if (v.id != R.id.btnDegree) drawerLayout.closeDrawer(GravityCompat.START)
        }
        sci.keys.forEach { findViewById<MaterialButton>(it)?.setOnClickListener(sciListener) }
        findViewById<MaterialButton>(R.id.btnOpenParen)?.setOnClickListener { insertText("("); calculateResult(false) }
        findViewById<MaterialButton>(R.id.btnCloseParen)?.setOnClickListener { insertText(")"); calculateResult(false) }
        findViewById<MaterialButton>(R.id.btnDot)?.setOnLongClickListener { insertText(","); true }
    }

    private fun calculateResult(isFinal: Boolean) {
        val expr = tvExpression.text.toString()
        if (expr.isEmpty()) { tvResult.setText("0"); tvResult.tag = 0.0; return }
        try {
            val p = expr.replace("×", "*").replace("÷", "/").replace("%", "/100")
            
            // For live calculation, don't try to parse incomplete expressions
            if (!isFinal) {
                val lastChar = expr.last().toString()
                val incompleteFuncs = listOf("sin", "cos", "tan", "sec", "csc", "cot", "asin", "acos", "atan", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh", "log", "ln", "gcd", "lcm", "P", "C", "H", "abs", "sqrt", "cbrt", "ceil", "floor", "√", "∛")
                if ("+-×÷*/^ ( ,".contains(lastChar) || incompleteFuncs.any { expr.endsWith(it) }) return
            }

            val res = MathEvaluator.evaluate(p)
            tvResult.setText(formatResult(res))
            tvResult.tag = res
            stateError = false
        } catch (e: Exception) { if (isFinal) { tvResult.setText("Error"); stateError = true } }
    }

    private fun formatResult(d: Double): String {
        if (d.isNaN() || d.isInfinite()) return "Error"
        var value = d
        if (abs(value) < 1e-12) value = 0.0
        val absD = abs(value)
        return if (absD >= 1e12 || (absD < 1e-7 && absD > 0)) {
            DecimalFormat("0.######E0").format(value).lowercase()
        } else {
            val formatted = DecimalFormat("#.##########").format(value)
            if (formatted == "-0") "0" else formatted
        }
    }

    private fun toFraction(v: Double): String? {
        if (v.isInfinite() || v.isNaN() || abs(v) > 1e6 || abs(v - round(v)) < 1e-9) return null
        var x = abs(v); val sign = if (v < 0) "-" else ""; var n0 = 0L; var d0 = 1L; var n1 = 1L; var d1 = 0L; var a = floor(x).toLong(); var rem = x - a
        for (i in 0..10) {
            val n2 = a * n1 + n0; val d2 = a * d1 + d0
            if (abs(n2.toDouble() / d2.toDouble() - x) < 1e-7) return if (d2 == 1L) null else "$sign$n2/$d2"
            if (d2 > 10000) break
            n0 = n1; n1 = n2; d0 = d1; d1 = d2
            if (abs(rem) < 1e-12) break
            x = 1.0 / rem; a = floor(x).toLong(); rem = x - a
        }
        return null
    }
}
