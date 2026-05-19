package com.liferlighdow.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import kotlin.math.*

class GeometryActivity : AppCompatActivity() {

    private lateinit var chipGroupShape: ChipGroup
    private lateinit var etInput1: TextInputEditText
    private lateinit var etInput2: TextInputEditText
    private lateinit var etInput3: TextInputEditText
    private lateinit var tilInput1: TextInputLayout
    private lateinit var tilInput2: TextInputLayout
    private lateinit var tilInput3: TextInputLayout
    private lateinit var tvResult1: TextView
    private lateinit var tvResult2: TextView
    private lateinit var tvResult1Label: TextView
    private lateinit var tvResult2Label: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_geometry)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        updateUIForShape()
        calculate()
    }

    private fun initViews() {
        chipGroupShape = findViewById(R.id.chipGroupShape)
        etInput1 = findViewById(R.id.etInput1)
        etInput2 = findViewById(R.id.etInput2)
        etInput3 = findViewById(R.id.etInput3)
        tilInput1 = findViewById(R.id.tilInput1)
        tilInput2 = findViewById(R.id.tilInput2)
        tilInput3 = findViewById(R.id.tilInput3)
        tvResult1 = findViewById(R.id.tvResult1)
        tvResult2 = findViewById(R.id.tvResult2)
        tvResult1Label = findViewById(R.id.tvResult1Label)
        tvResult2Label = findViewById(R.id.tvResult2Label)
        
        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        chipGroupShape.setOnCheckedStateChangeListener { _, _ ->
            updateUIForShape()
            calculate()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { calculate() }
            override fun afterTextChanged(s: Editable?) {}
        }
        etInput1.addTextChangedListener(watcher)
        etInput2.addTextChangedListener(watcher)
        etInput3.addTextChangedListener(watcher)
    }

    private fun getCurrentShape(): String {
        return when (chipGroupShape.checkedChipId) {
            R.id.chipCircle -> "Circle"
            R.id.chipSquare -> "Square"
            R.id.chipTriangle -> "Triangle"
            R.id.chipRectangle -> "Rectangle"
            R.id.chipSphere -> "Sphere"
            R.id.chipCylinder -> "Cylinder"
            R.id.chipSector -> "Sector"
            R.id.chipPyramid -> "Pyramid"
            R.id.chipPrism -> "Prism"
            R.id.chipCone -> "Cone"
            R.id.chipPolygon -> "Polygon"
            else -> "Circle"
        }
    }

    private fun updateUIForShape() {
        val shape = getCurrentShape()

        // Default visibility
        tilInput2.visibility = View.GONE
        tilInput3.visibility = View.GONE

        // Update labels and visibility
        when (shape) {
            "Circle" -> {
                tilInput1.hint = getString(R.string.label_radius)
                tvResult1Label.text = getString(R.string.label_area)
                tvResult2Label.text = getString(R.string.label_perimeter)
            }
            "Square" -> {
                tilInput1.hint = getString(R.string.label_side)
                tvResult1Label.text = getString(R.string.label_area)
                tvResult2Label.text = getString(R.string.label_perimeter)
            }
            "Triangle" -> {
                tilInput1.hint = getString(R.string.label_base)
                tilInput2.hint = getString(R.string.label_height)
                tilInput2.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_area)
                tvResult2Label.text = getString(R.string.label_perimeter)
            }
            "Rectangle" -> {
                tilInput1.hint = getString(R.string.label_width)
                tilInput2.hint = getString(R.string.label_height)
                tilInput2.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_area)
                tvResult2Label.text = getString(R.string.label_perimeter)
            }
            "Sphere" -> {
                tilInput1.hint = getString(R.string.label_radius)
                tvResult1Label.text = getString(R.string.label_surface_area)
                tvResult2Label.text = getString(R.string.label_volume)
            }
            "Cylinder" -> {
                tilInput1.hint = getString(R.string.label_radius)
                tilInput2.hint = getString(R.string.label_height)
                tilInput2.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_surface_area)
                tvResult2Label.text = getString(R.string.label_volume)
            }
            "Sector" -> {
                tilInput1.hint = getString(R.string.label_radius)
                tilInput2.hint = getString(R.string.label_angle)
                tilInput2.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_area)
                tvResult2Label.text = getString(R.string.label_arc_length)
            }
            "Pyramid" -> {
                tilInput1.hint = getString(R.string.label_side)
                tilInput2.hint = getString(R.string.label_height)
                tilInput3.hint = getString(R.string.label_sides_count)
                tilInput2.visibility = View.VISIBLE
                tilInput3.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_surface_area)
                tvResult2Label.text = getString(R.string.label_volume)
            }
            "Prism" -> {
                tilInput1.hint = getString(R.string.label_side)
                tilInput2.hint = getString(R.string.label_height)
                tilInput3.hint = getString(R.string.label_sides_count)
                tilInput2.visibility = View.VISIBLE
                tilInput3.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_surface_area)
                tvResult2Label.text = getString(R.string.label_volume)
            }
            "Cone" -> {
                tilInput1.hint = getString(R.string.label_radius)
                tilInput2.hint = getString(R.string.label_height)
                tilInput2.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_surface_area)
                tvResult2Label.text = getString(R.string.label_volume)
            }
            "Polygon" -> {
                tilInput1.hint = getString(R.string.label_side)
                tilInput2.hint = getString(R.string.label_sides_count)
                tilInput2.visibility = View.VISIBLE
                tvResult1Label.text = getString(R.string.label_area)
                tvResult2Label.text = getString(R.string.label_perimeter)
            }
        }
    }

    private fun calculate() {
        val val1 = etInput1.text.toString().toDoubleOrNull() ?: 0.0
        val val2 = etInput2.text.toString().toDoubleOrNull() ?: 0.0
        val val3 = etInput3.text.toString().toDoubleOrNull() ?: 0.0
        
        val shape = getCurrentShape()
        val isTwoInputs = tilInput2.visibility == View.VISIBLE
        val isThreeInputs = tilInput3.visibility == View.VISIBLE

        if (shape == "Sector") {
            if (val2 > 360.0) {
                tilInput2.error = getString(R.string.error_angle_limit)
            } else {
                tilInput2.error = null
            }
        } else {
            tilInput2.error = null
        }
        
        if (val1 <= 0 || (isTwoInputs && val2 <= 0) || (isThreeInputs && val3 <= 0)) {
            tvResult1.text = "---"
            tvResult2.text = "---"
            return
        }

        var res1 = 0.0
        var res2 = 0.0

        when (shape) {
            "Circle" -> {
                res1 = PI * val1 * val1
                res2 = 2 * PI * val1
            }
            "Square" -> {
                res1 = val1 * val1
                res2 = 4 * val1
            }
            "Triangle" -> {
                res1 = 0.5 * val1 * val2
                res2 = val1 + 2 * sqrt((val1 / 2).pow(2) + val2.pow(2))
            }
            "Rectangle" -> {
                res1 = val1 * val2
                res2 = 2 * (val1 + val2)
            }
            "Sphere" -> {
                res1 = 4 * PI * val1 * val1
                res2 = (4.0 / 3.0) * PI * val1.pow(3)
            }
            "Cylinder" -> {
                res1 = 2 * PI * val1 * (val1 + val2)
                res2 = PI * val1 * val1 * val2
            }
            "Sector" -> {
                val angle = min(val2, 360.0)
                res1 = PI * val1 * val1 * (angle / 360.0)
                res2 = 2 * PI * val1 * (angle / 360.0)
            }
            "Pyramid" -> {
                // Regular n-gonal pyramid: val1=side, val2=height, val3=n
                val baseArea = (val3 * val1 * val1) / (4 * tan(PI / val3))
                val apothem = val1 / (2 * tan(PI / val3))
                val slantHeight = sqrt(apothem * apothem + val2 * val2)
                res1 = baseArea + (val3 * val1 * slantHeight) / 2
                res2 = (1.0 / 3.0) * baseArea * val2
            }
            "Prism" -> {
                // Regular n-gonal prism: val1=side, val2=height, val3=n
                val baseArea = (val3 * val1 * val1) / (4 * tan(PI / val3))
                res1 = 2 * baseArea + val3 * val1 * val2
                res2 = baseArea * val2
            }
            "Cone" -> {
                res1 = PI * val1 * val1 + PI * val1 * sqrt(val1 * val1 + val2 * val2)
                res2 = (1.0 / 3.0) * PI * val1 * val1 * val2
            }
            "Polygon" -> {
                // Regular n-gon: val1=side, val2=n
                res1 = (val2 * val1 * val1) / (4 * tan(PI / val2))
                res2 = val1 * val2
            }
        }

        tvResult1.text = String.format(Locale.US, "%.2f", res1)
        tvResult2.text = String.format(Locale.US, "%.2f", res2)
    }
}
