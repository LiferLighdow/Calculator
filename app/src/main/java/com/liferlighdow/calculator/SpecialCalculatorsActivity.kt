package com.liferlighdow.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class SpecialCalculatorsActivity : AppCompatActivity() {

    private lateinit var tvSpecialResult: TextView
    private lateinit var tvSpecialDetail: TextView
    private lateinit var nsvResult: androidx.core.widget.NestedScrollView
    private lateinit var chipGroupSpecial: ChipGroup

    private lateinit var llBMI: LinearLayout
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton

    private lateinit var llWorldClock: LinearLayout
    private lateinit var spinnerTimeZone: Spinner

    private lateinit var llUnitPrice: LinearLayout
    private lateinit var etTotalCost: EditText
    private lateinit var etTotalQuantity: EditText

    private lateinit var llFuel: LinearLayout
    private lateinit var etDistance: EditText
    private lateinit var etFuelConsumed: EditText
    private lateinit var etFuelUnitPrice: EditText

    private lateinit var llBaseConverter: LinearLayout
    private lateinit var etDecimalInput: EditText

    private lateinit var llDiscount: LinearLayout
    private lateinit var etOriginalPrice: EditText
    private lateinit var etDiscountPercent: EditText

    private lateinit var llLoan: LinearLayout
    private lateinit var etLoanAmount: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etLoanTerm: EditText

    private lateinit var llTip: LinearLayout
    private lateinit var etBillAmount: EditText
    private lateinit var etTipPercent: EditText
    private lateinit var etSplitCount: EditText

    private lateinit var llSigma: LinearLayout
    private lateinit var etSigmaExpr: EditText
    private lateinit var etSigmaStart: EditText
    private lateinit var etSigmaEnd: EditText

    private lateinit var llNumInfo: LinearLayout
    private lateinit var etInfoInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_special_calculators)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupTimeZoneSpinner()
        setupListeners()
    }

    private fun initViews() {
        tvSpecialResult = findViewById(R.id.tvSpecialResult)
        tvSpecialDetail = findViewById(R.id.tvSpecialDetail)
        nsvResult = findViewById(R.id.nsvResult)
        chipGroupSpecial = findViewById(R.id.chipGroupSpecial)

        llBMI = findViewById(R.id.llBMI)
        etAge = findViewById(R.id.etAge)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        rgGender = findViewById(R.id.rgGender)
        rbMale = findViewById(R.id.rbMale)

        llWorldClock = findViewById(R.id.llWorldClock)
        spinnerTimeZone = findViewById(R.id.spinnerTimeZone)

        llUnitPrice = findViewById(R.id.llUnitPrice)
        etTotalCost = findViewById(R.id.etTotalCost)
        etTotalQuantity = findViewById(R.id.etTotalQuantity)

        llFuel = findViewById(R.id.llFuel)
        etDistance = findViewById(R.id.etDistance)
        etFuelConsumed = findViewById(R.id.etFuelConsumed)
        etFuelUnitPrice = findViewById(R.id.etFuelUnitPrice)

        llBaseConverter = findViewById(R.id.llBaseConverter)
        etDecimalInput = findViewById(R.id.etDecimalInput)

        llDiscount = findViewById(R.id.llDiscount)
        etOriginalPrice = findViewById(R.id.etOriginalPrice)
        etDiscountPercent = findViewById(R.id.etDiscountPercent)

        llLoan = findViewById(R.id.llLoan)
        etLoanAmount = findViewById(R.id.etLoanAmount)
        etInterestRate = findViewById(R.id.etInterestRate)
        etLoanTerm = findViewById(R.id.etLoanTerm)

        llTip = findViewById(R.id.llTip)
        etBillAmount = findViewById(R.id.etBillAmount)
        etTipPercent = findViewById(R.id.etTipPercent)
        etSplitCount = findViewById(R.id.etSplitCount)

        llSigma = findViewById(R.id.llSigma)
        etSigmaExpr = findViewById(R.id.etSigmaExpr)
        etSigmaStart = findViewById(R.id.etSigmaStart)
        etSigmaEnd = findViewById(R.id.etSigmaEnd)

        llNumInfo = findViewById(R.id.llNumInfo)
        etInfoInput = findViewById(R.id.etInfoInput)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupTimeZoneSpinner() {
        val tzs = TimeZone.getAvailableIDs().sorted()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tzs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeZone.adapter = adapter
        val def = tzs.indexOf(TimeZone.getDefault().id)
        if (def >= 0) spinnerTimeZone.setSelection(def)
    }

    private fun setupListeners() {
        chipGroupSpecial.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: R.id.chipBMIBMR
            val sections = listOf(llBMI, llWorldClock, llUnitPrice, llFuel, llBaseConverter, llDiscount, llLoan, llTip, llSigma, llNumInfo)
            sections.forEach { it.visibility = View.GONE }

            when (id) {
                R.id.chipBMIBMR -> llBMI.visibility = View.VISIBLE
                R.id.chipWorldClock -> llWorldClock.visibility = View.VISIBLE
                R.id.chipUnitPrice -> llUnitPrice.visibility = View.VISIBLE
                R.id.chipFuel -> llFuel.visibility = View.VISIBLE
                R.id.chipBaseConverter -> llBaseConverter.visibility = View.VISIBLE
                R.id.chipDiscount -> llDiscount.visibility = View.VISIBLE
                R.id.chipLoan -> llLoan.visibility = View.VISIBLE
                R.id.chipTip -> llTip.visibility = View.VISIBLE
                R.id.chipSigma -> llSigma.visibility = View.VISIBLE
                R.id.chipNumInfo -> llNumInfo.visibility = View.VISIBLE
            }
            performCalculation()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { performCalculation() }
            override fun afterTextChanged(s: Editable?) {}
        }

        val inputs = listOf(
            etAge, etHeight, etWeight, etTotalCost, etTotalQuantity,
            etDistance, etFuelConsumed, etFuelUnitPrice, etDecimalInput,
            etOriginalPrice, etDiscountPercent, etLoanAmount, etInterestRate,
            etLoanTerm, etBillAmount, etTipPercent, etSplitCount,
            etSigmaExpr, etSigmaStart, etSigmaEnd, etInfoInput
        )
        inputs.forEach { it.addTextChangedListener(watcher) }
        rgGender.setOnCheckedChangeListener { _, _ -> performCalculation() }
        spinnerTimeZone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { performCalculation() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun performCalculation() {
        nsvResult.scrollTo(0, 0)
        when (chipGroupSpecial.checkedChipId) {
            R.id.chipBMIBMR -> calculateBMIBMR()
            R.id.chipWorldClock -> calculateWorldClock()
            R.id.chipUnitPrice -> calculateUnitPrice()
            R.id.chipFuel -> calculateFuel()
            R.id.chipBaseConverter -> calculateBaseConverter()
            R.id.chipDiscount -> calculateDiscount()
            R.id.chipLoan -> calculateLoan()
            R.id.chipTip -> calculateTip()
            R.id.chipSigma -> calculateSigma()
            R.id.chipNumInfo -> calculateNumInfo()
        }
    }

    private fun calculateBMIBMR() {
        val h = etHeight.text.toString().toDoubleOrNull() ?: 0.0
        val w = etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val age = etAge.text.toString().toDoubleOrNull() ?: 0.0
        if (h > 0 && w > 0) {
            val bmi = w / (h / 100).pow(2)
            val bmr = if (rbMale.isChecked) 88.362 + (13.397 * w) + (4.799 * h) - (5.677 * age)
            else 447.593 + (9.247 * w) + (3.098 * h) - (4.330 * age)
            tvSpecialResult.text = String.format(Locale.US, "%.1f", bmi)
            tvSpecialDetail.text = getString(R.string.bmr_status_format, bmr.toInt(), getBMIStatus(bmi))
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun getBMIStatus(bmi: Double): String {
        return when {
            bmi < 18.5 -> getString(R.string.bmi_underweight)
            bmi < 25.0 -> getString(R.string.bmi_normal)
            bmi < 30.0 -> getString(R.string.bmi_overweight)
            else -> getString(R.string.bmi_obese)
        }
    }

    private fun calculateWorldClock() {
        val tzId = spinnerTimeZone.selectedItem?.toString() ?: return
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(tzId)
        tvSpecialResult.text = sdf.format(Date())
        tvSpecialDetail.text = getString(R.string.timezone_format, tzId)
    }

    private fun calculateUnitPrice() {
        val cost = etTotalCost.text.toString().toDoubleOrNull() ?: 0.0
        val qty = etTotalQuantity.text.toString().toDoubleOrNull() ?: 0.0
        if (cost > 0 && qty > 0) {
            tvSpecialResult.text = String.format(Locale.US, "%.2f", cost / qty)
            tvSpecialDetail.text = getString(R.string.unit_price_detail)
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun calculateFuel() {
        val dist = etDistance.text.toString().toDoubleOrNull() ?: 0.0
        val fuel = etFuelConsumed.text.toString().toDoubleOrNull() ?: 0.0
        val price = etFuelUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
        if (dist > 0 && fuel > 0) {
            val totalCost = fuel * price
            tvSpecialResult.text = String.format(Locale.US, "%.2f L/100km", (fuel / dist) * 100)
            tvSpecialDetail.text = getString(R.string.fuel_detail_format, totalCost.toInt(), String.format(Locale.US, "%.2f", totalCost / dist))
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun calculateBaseConverter() {
        val inputStr = etDecimalInput.text.toString()
        val input = inputStr.toDoubleOrNull()
        if (input != null) {
            val isNegative = input < 0
            val absInput = abs(input)
            val integerPart = absInput.toLong()
            val fractionalPart = absInput - integerPart

            var hexResult = java.lang.Long.toHexString(integerPart).uppercase()
            var binResult = java.lang.Long.toBinaryString(integerPart)

            if (fractionalPart > 0) {
                hexResult += "." + convertFractionToBase(fractionalPart, 16)
                binResult += "." + convertFractionToBase(fractionalPart, 2)
            }

            if (isNegative) {
                hexResult = "-$hexResult"
                binResult = "-$binResult"
            }

            tvSpecialResult.text = getString(R.string.label_hex_prefix, hexResult)
            tvSpecialDetail.text = getString(R.string.label_bin_prefix, binResult)
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun convertFractionToBase(fraction: Double, base: Int): String {
        var f = fraction
        val sb = StringBuilder()
        val precision = 12
        for (i in 0 until precision) {
            f *= base
            val digit = f.toInt()
            sb.append(Integer.toString(digit, base).uppercase())
            f -= digit
            if (f < 0.0000000001) break
        }
        return sb.toString()
    }

    private fun calculateDiscount() {
        val price = etOriginalPrice.text.toString().toDoubleOrNull() ?: 0.0
        val disc = etDiscountPercent.text.toString().toDoubleOrNull() ?: 0.0
        if (price > 0) {
            val final = price * (1 - disc / 100)
            tvSpecialResult.text = String.format(Locale.US, "%.2f", final)
            tvSpecialDetail.text = getString(R.string.discount_saved_format, String.format(Locale.US, "%.2f", price - final))
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun calculateLoan() {
        val amount = etLoanAmount.text.toString().toDoubleOrNull() ?: 0.0
        val rate = etInterestRate.text.toString().toDoubleOrNull() ?: 0.0
        val term = etLoanTerm.text.toString().toDoubleOrNull() ?: 0.0
        if (amount > 0 && rate > 0 && term > 0) {
            val r = rate / 100 / 12
            val pay = (r * amount) / (1 - (1 + r).pow(-term))
            tvSpecialResult.text = pay.toInt().toString()
            tvSpecialDetail.text = getString(R.string.loan_detail_format, (pay * term).toInt())
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun calculateTip() {
        val bill = etBillAmount.text.toString().toDoubleOrNull() ?: 0.0
        val tip = etTipPercent.text.toString().toDoubleOrNull() ?: 0.0
        val split = etSplitCount.text.toString().toIntOrNull() ?: 1
        if (bill > 0) {
            val total = bill * (1 + tip / 100)
            tvSpecialResult.text = String.format(Locale.US, "%.2f", total / max(1, split))
            tvSpecialDetail.text = getString(R.string.tip_detail_format, total.toInt())
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun calculateSigma() {
        val expr = etSigmaExpr.text.toString()
        val start = etSigmaStart.text.toString().toIntOrNull()
        val end = etSigmaEnd.text.toString().toIntOrNull()

        if (expr.isNotEmpty() && start != null && end != null) {
            try {
                var total = 0.0
                val formattedExpr = expr.lowercase().replace("i", "x")
                for (i in min(start, end)..max(start, end)) {
                    total += MathEvaluator.evaluate(formattedExpr, i.toDouble())
                }
                tvSpecialResult.text = String.format(Locale.US, "%.4f", total)
                tvSpecialDetail.text = getString(R.string.label_sigma_detail_format, start, end)
            } catch (e: Exception) {
                tvSpecialResult.text = "Error"
                tvSpecialDetail.text = e.message
            }
        } else {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
        }
    }

    private fun calculateNumInfo() {
        val inputStr = etInfoInput.text.toString().trim()
        if (inputStr.isEmpty()) {
            tvSpecialResult.text = getString(R.string.waiting_input)
            tvSpecialDetail.text = ""
            return
        }

        if (inputStr.endsWith("i")) {
            tvSpecialResult.text = getString(R.string.num_info_imaginary)
            tvSpecialDetail.text = getString(R.string.num_info_imaginary_detail)
            return
        }

        val num: Double? = try {
            val p = inputStr.lowercase()
                .replace("π", PI.toString())
                .replace("e", E.toString())
                .replace("φ", "1.6180339887")
                .replace("γ", "0.5772156649")
            MathEvaluator.evaluate(p)
        } catch (e: Exception) {
            null
        }

        if (num == null) {
            tvSpecialResult.text = getString(R.string.num_info_invalid)
            tvSpecialDetail.text = ""
            return
        }

        val sb = StringBuilder()
        tvSpecialResult.text = getString(R.string.num_info_real)

        sb.append(when {
            num > 0 -> getString(R.string.num_info_positive)
            num < 0 -> getString(R.string.num_info_negative)
            else -> getString(R.string.num_info_zero)
        }).append(" | ")

        val isInteger = abs(num - round(num)) < 1e-10
        if (isInteger) {
            val longNum = num.toLong()
            sb.append(getString(R.string.num_info_integer)).append(" | ")
            sb.append(if (longNum % 2 == 0L) getString(R.string.num_info_even) else getString(R.string.num_info_odd)).append("\n")

            if (longNum > 0) {
                if (longNum == 1L) {
                    sb.append(getString(R.string.num_info_unit))
                } else {
                    val factors = mutableListOf<Long>()
                    for (i in 1..sqrt(longNum.toDouble()).toLong()) {
                        if (longNum % i == 0L) {
                            factors.add(i)
                            if (i * i != longNum) factors.add(longNum / i)
                        }
                    }
                    factors.sort()

                    if (factors.size == 2) {
                        sb.append(getString(R.string.num_info_prime))
                    } else {
                        sb.append(getString(R.string.num_info_composite)).append("\n")
                        sb.append(getString(R.string.num_info_prime_factors)).append(primeFactorization(longNum))
                    }
                    sb.append("\n").append(getString(R.string.num_info_all_factors)).append(factors.joinToString(", "))
                }
            }
        } else {
            val fraction = toSimpleFraction(num)
            if (fraction != null) {
                sb.append(getString(R.string.num_info_rational, fraction))
            } else {
                sb.append(getString(R.string.num_info_irrational))
            }
        }

        tvSpecialDetail.text = sb.toString()
    }

    private fun primeFactorization(n: Long): String {
        var temp = n
        val factors = mutableMapOf<Long, Int>()
        var d = 2L
        while (d * d <= temp) {
            while (temp % d == 0L) {
                factors[d] = factors.getOrDefault(d, 0) + 1
                temp /= d
            }
            d++
        }
        if (temp > 1) factors[temp] = factors.getOrDefault(temp, 0) + 1
        
        return factors.entries.joinToString(" × ") { (f, p) -> if (p > 1) "$f^$p" else "$f" }
    }

    private fun toSimpleFraction(v: Double): String? {
        val x = abs(v)
        var n0 = 0L; var d0 = 1L; var n1 = 1L; var d1 = 0L
        var tempX = x
        var a = floor(tempX).toLong()
        var rem = tempX - a
        for (i in 0..8) {
            val n2 = a * n1 + n0
            val d2 = a * d1 + d0
            if (d2 > 10000) break
            if (abs(n2.toDouble() / d2.toDouble() - x) < 1e-9) {
                val sign = if (v < 0) "-" else ""
                return "$sign$n2/$d2"
            }
            n0 = n1; n1 = n2; d0 = d1; d1 = d2
            if (abs(rem) < 1e-12) break
            tempX = 1.0 / rem
            a = floor(tempX).toLong()
            rem = tempX - a
        }
        return null
    }
}
