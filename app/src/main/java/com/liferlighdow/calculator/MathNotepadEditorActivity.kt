package com.liferlighdow.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import java.io.File

class MathNotepadEditorActivity : AppCompatActivity() {

    private lateinit var rvLines: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var adapter: MathLineAdapter
    private val lines = mutableListOf<MathLine>()
    private var filePath: String? = null
    private val evaluator = MathNotepadEvaluator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_notepad_editor)

        filePath = intent.getStringExtra("FILE_PATH")
        val fileName = filePath?.let { File(it).nameWithoutExtension } ?: "New Notebook"
        findViewById<TextView>(R.id.tvFileName).text = fileName
        tvTotal = findViewById(R.id.tvTotal)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvLines = findViewById(R.id.rvLines)
        rvLines.layoutManager = LinearLayoutManager(this)
        
        loadLines()
        if (lines.isEmpty()) {
            lines.add(MathLine())
        }

        adapter = MathLineAdapter(lines) {
            recalculateAll()
        }
        rvLines.adapter = adapter
    }

    private fun loadLines() {
        filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.readLines().forEach {
                    lines.add(MathLine(it))
                }
            }
        }
        recalculateAll()
    }

    private fun saveLines() {
        filePath?.let { path ->
            val file = File(path)
            file.writeText(lines.joinToString("\n") { it.expression })
        }
    }

    override fun onPause() {
        super.onPause()
        saveLines()
    }

    private fun recalculateAll() {
        evaluator.clearVariables()
        
        // Phase 1: Collect assignments and equations
        for (line in lines) {
            val expr = line.expression.trim()
            if (expr.isNotEmpty() && expr.contains("=")) {
                val parts = expr.split("=")
                if (parts.size == 2) {
                    evaluator.addEquation(parts[0].trim(), parts[1].trim())
                }
            }
        }
        
        // Phase 2: Solve the system
        evaluator.solveSystem()

        // Phase 3: Evaluate and display
        var total = 0.0
        for (i in lines.indices) {
            val line = lines[i]
            if (line.expression.trim().isNotEmpty()) {
                try {
                    val evalRes = evaluator.evaluate(line.expression)
                    if (evalRes.isEquation && evalRes.variableName != null) {
                        line.result = "=> ${evalRes.variableName} = ${formatRes(evalRes.value)}"
                    } else if (evalRes.variableName != null) {
                        line.result = "= ${formatRes(evalRes.value)}"
                    } else {
                        line.result = "= ${formatRes(evalRes.value)}"
                        total += evalRes.value
                    }
                } catch (_: Exception) {
                    line.result = ""
                }
            } else {
                line.result = ""
            }
            val holder = rvLines.findViewHolderForAdapterPosition(i) as? MathLineAdapter.ViewHolder
            holder?.tvLineResult?.text = line.result
        }
        tvTotal.text = String.format(java.util.Locale.US, "%.2f", total)
    }

    private fun formatRes(v: Double): String {
        return if (v == v.toLong().toDouble()) v.toLong().toString() else String.format("%.2f", v)
    }

    data class MathLine(var expression: String = "", var result: String = "")

    class MathLineAdapter(private val lines: MutableList<MathLine>, private val onUpdate: () -> Unit) :
        RecyclerView.Adapter<MathLineAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val etExpression: EditText = view.findViewById(R.id.etExpression)
            val tvLineResult: TextView = view.findViewById(R.id.tvLineResult)
            var textWatcher: TextWatcher? = null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_math_line, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val line = lines[position]
            
            // Remove old watcher to avoid recursion or multiple triggers
            holder.textWatcher?.let { holder.etExpression.removeTextChangedListener(it) }
            
            holder.etExpression.setText(line.expression)
            holder.tvLineResult.text = line.result

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    line.expression = s.toString()
                    onUpdate()
                }
                override fun afterTextChanged(s: Editable?) {}
            }
            holder.etExpression.addTextChangedListener(watcher)
            holder.textWatcher = watcher
            
            holder.etExpression.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN) {
                    val curPos = holder.adapterPosition
                    if (curPos != RecyclerView.NO_POSITION) {
                        lines.add(curPos + 1, MathLine())
                        notifyItemInserted(curPos + 1)
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }

        override fun getItemCount() = lines.size
    }
}
