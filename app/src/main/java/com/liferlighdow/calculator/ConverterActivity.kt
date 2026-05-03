package com.liferlighdow.calculator

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.conscrypt.Conscrypt
import org.json.JSONObject
import java.security.Security
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.*

class ConverterActivity : AppCompatActivity() {

    private lateinit var etInput: EditText
    private lateinit var autoFrom: MaterialAutoCompleteTextView
    private lateinit var autoTo: MaterialAutoCompleteTextView
    private lateinit var tvConvertedValue: TextView
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvUpdateInfo: TextView

    private val exchangeRates = mutableMapOf<String, Double>()
    private var lastRatesUpdate: String = ""

    private lateinit var units: MutableMap<String, List<String>>
    private lateinit var categoryMap: Map<Int, String>

    // Internal keys for temperature to avoid locale issues
    private val TEMP_CELSIUS = "TEMP_C"
    private val TEMP_FAHRENHEIT = "TEMP_F"
    private val TEMP_KELVIN = "TEMP_K"

    private val conversionFactors = mapOf(
        "公尺 (m)" to 1.0, "公里 (km)" to 1000.0, "公分 (cm)" to 0.01, "公釐 (mm)" to 0.001,
        "英哩 (mi)" to 1609.34, "碼 (yd)" to 0.9144, "英呎 (ft)" to 0.3048, "英吋 (in)" to 0.0254,
        "天文單位 (AU)" to 149597870700.0, "光年 (ly)" to 9.4607304725808e15,
        "平方公釐 (mm²)" to 1e-6, "平方公分 (cm²)" to 0.0001, "平方公尺 (m²)" to 1.0, "公畝 (a)" to 100.0, "公頃 (ha)" to 10000.0, "平方公里 (km²)" to 1000000.0,
        "坪" to 3.305785, "甲" to 9699.17, "分" to 969.917, "才" to 0.091827, "英畝 (acre)" to 4046.856, "平方英呎 (ft²)" to 0.092903,
        "度 (°)" to 1.0, "弧度 (rad)" to 180.0 / PI, "毫弧度 (mrad)" to 0.18 / PI, "梯度 (grad)" to 0.9, "圓周 (rev)" to 360.0, "密位 (mil)" to 0.05625, "點 (pt)" to 11.25, "分點 (bin)" to 1.40625, "分 (')" to 1.0/60.0, "秒 (\")" to 1.0/3600.0,
        "公斤 (kg)" to 1.0, "公克 (g)" to 0.001, "毫克 (mg)" to 1e-6, "微克 (µg)" to 1e-9, "公噸 (t)" to 1000.0, "磅 (lb)" to 0.453592, "盎司 (oz)" to 0.0283495, "台斤" to 0.6, "台兩" to 0.0375, "克拉 (ct)" to 0.0002,
        "帕斯卡 (Pa)" to 1.0, "百帕 (hPa)" to 100.0, "仟帕 (kPa)" to 1000.0, "百萬帕 (MPa)" to 1000000.0, "巴 (bar)" to 100000.0, "毫巴 (mbar)" to 100.0, "標準大氣壓 (atm)" to 101325.0, "托 (Torr)" to 133.322, "毫米汞柱 (mmHg)" to 133.322, "英吋汞柱 (inHg)" to 3386.39, "磅每平方英吋 (psi)" to 6894.76, "公斤重每平方公分 (kgf/cm²)" to 98066.5, "水柱公尺 (mH2O)" to 9806.65, "達因每平方公分 (dyn/cm²) " to 0.1,
        "立方公分 (cm³)" to 0.001, "立方公尺 (m³)" to 1000.0, "立方公里 (km³)" to 1e12,
        "公升 (L)" to 1.0, "毫升 (mL)" to 0.001, "公秉 (kL)" to 1000.0, "加侖 (gal)" to 3.78541, "品脫 (pt)" to 0.473176,
        "公尺/秒 (m/s)" to 1.0, "公里/小時 (km/h)" to 1.0/3.6, "英哩/小時 (mph)" to 0.44704, "節 (kn)" to 0.514444, "馬赫 (Mach)" to 340.3, "英尺每秒 (ft/s)" to 0.3048, "英吋每秒 (ips)" to 0.0254, "公分每秒 (cm/s)" to 0.01, "公里每秒 (km/s)" to 1000.0, "光速 (c)" to 299792458.0,
        "秒 (s)" to 1.0, "毫秒 (ms)" to 0.001, "微秒 (μs)" to 1e-6, "奈秒 (ns)" to 1e-9, "皮秒 (ps)" to 1e-12, "飛秒 (fs)" to 1e-15, "阿秒 (as)" to 1e-18, "仄秒 (zs)" to 1e-21, "么秒 (ys)" to 1e-24,
        "普朗克時間" to 5.391e-44, "分 (min)" to 60.0, "刻 (Quarter)" to 900.0, "時 (h)" to 3600.0, "時辰" to 7200.0, "日 (d)" to 86400.0, "週 (w)" to 604800.0, "旬" to 864000.0, "月 (M)" to 2629746.0, "季 (q)" to 7889238.0, "年 (y)" to 31556952.0, "年代 (decade)" to 315569520.0, "世紀 (century)" to 3155695200.0,
        "須臾" to 2880.0, "世" to 946708560.0, "紀" to 378683424.0, "代" to 788923800.0, "宙" to 3.1556952e16, "甲子" to 1893417120.0,
        "位元 (bit)" to 1.0, "位元組 (Byte)" to 8.0, "KB" to 8.0 * 1024, "MB" to 8.0 * 1024.0.pow(2), "GB" to 8.0 * 1024.0.pow(3), "TB" to 8.0 * 1024.0.pow(4), "PB" to 8.0 * 1024.0.pow(5), "EB" to 8.0 * 1024.0.pow(6), "ZB" to 8.0 * 1024.0.pow(7), "YB" to 8.0 * 1024.0.pow(8), "RB" to 8.0 * 1024.0.pow(9), "QB" to 8.0 * 1024.0.pow(10),
        "安培 (A)" to 1.0, "毫安培 (mA)" to 0.001, "伏特 (V)" to 1.0, "歐姆 (Ω)" to 1.0, "瓦特 (W)" to 1.0,
        "牛頓 (N)" to 1.0, "公斤重 (kgf)" to 9.80665, "磅力 (lbf)" to 4.44822, "公克重 (gf)" to 0.00980665, "達因 (dyn)" to 1e-5, "磅達 (pdl)" to 0.138255, "噸重 (tf)" to 9806.65, "千牛頓 (kN)" to 1000.0,
        "勒克斯 (lx)" to 1.0, "呎燭光 (fc)" to 10.7639,
        "焦耳 (J)" to 1.0, "兆焦耳 (MJ)" to 1e6, "拍焦耳 (PJ)" to 1e15, "卡 (cal)" to 4.184, "大卡 (kcal)" to 4184.0, "瓦特小時 (Wh)" to 3600.0, "千瓦小時 (kWh)" to 3.6e6, "電子伏트 (eV)" to 1.602176634e-19, "爾格 (erg)" to 1e-7, "英國熱量單位 (BTU)" to 1055.056, "德熱姆 (th)" to 1.055056e8, "桶油當量 (boe)" to 6.1178632e9, "噸煤當量 (tce)" to 2.93076e10, "英尺磅 (ft·lb)" to 1.355818
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_converter)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initData()
        etInput = findViewById(R.id.etInput)
        autoFrom = findViewById(R.id.autoFrom)
        autoTo = findViewById(R.id.autoTo)
        tvConvertedValue = findViewById(R.id.tvConvertedValue)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        pbLoading = findViewById(R.id.pbLoading)
        tvUpdateInfo = findViewById(R.id.tvUpdateInfo)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        setupCategorySelection()
        setupListeners()
        updateSpinners("Length")
    }

    private fun initData() {
        categoryMap = mapOf(
            R.id.chipLength to "Length", R.id.chipCurrency to "Currency", R.id.chipArea to "Area",
            R.id.chipAngle to "Angle", R.id.chipWeight to "Weight", R.id.chipTemp to "Temperature",
            R.id.chipPressure to "Pressure", R.id.chipVolume to "Volume", R.id.chipSpeed to "Speed",
            R.id.chipTime to "Time", R.id.chipDigital to "Storage", R.id.chipElectric to "Electric",
            R.id.chipForce to "Force", R.id.chipOptics to "Optics", R.id.chipEnergy to "Energy"
        )

        units = mutableMapOf(
            "Length" to listOf("公尺 (m)", "公里 (km)", "公分 (cm)", "公釐 (mm)", "英哩 (mi)", "碼 (yd)", "英呎 (ft)", "英吋 (in)", "天文單位 (AU)", "光年 (ly)"),
            "Currency" to listOf("TWD", "USD", "JPY", "EUR", "HKD", "CNY", "KRW", "GBP", "AUD", "CAD"),
            "Area" to listOf("平方公釐 (mm²)", "平方公分 (cm²)", "平方公尺 (m²)", "公畝 (a)", "公頃 (ha)", "平方公里 (km²)", "坪", "甲", "分", "才", "英畝 (acre)", "平方英呎 (ft²)"),
            "Angle" to listOf("度 (°)", "弧度 (rad)", "毫弧度 (mrad)", "梯度 (grad)", "圓周 (rev)", "密位 (mil)", "點 (pt)", "分點 (bin)", "分 (')", "秒 (\")"),
            "Weight" to listOf("公斤 (kg)", "公克 (g)", "毫克 (mg)", "微克 (µg)", "公噸 (t)", "磅 (lb)", "盎司 (oz)", "台斤", "台兩", "克拉 (ct)"),
            "Temperature" to listOf(TEMP_CELSIUS, TEMP_FAHRENHEIT, TEMP_KELVIN),
            "Pressure" to listOf("帕斯卡 (Pa)", "百帕 (hPa)", "仟帕 (kPa)", "百萬帕 (MPa)", "巴 (bar)", "毫巴 (mbar)", "標準大氣壓 (atm)", "托 (Torr)", "毫米汞柱 (mmHg)", "英吋汞柱 (inHg)", "磅每平方英吋 (psi)", "公斤重每平方公分 (kgf/cm²)", "水柱公尺 (mH2O)", "達因每平方公分 (dyn/cm²)"),
            "Volume" to listOf("立方公分 (cm³)", "立方公尺 (m³)", "立方公里 (km³)", "公升 (L)", "毫升 (mL)", "公秉 (kL)", "加侖 (gal)", "品脫 (pt)"),
            "Speed" to listOf("公尺/秒 (m/s)", "公里/小時 (km/h)", "英哩/小時 (mph)", "節 (kn)", "馬赫 (Mach)", "英尺每秒 (ft/s)", "英吋每秒 (ips)", "公分每秒 (cm/s)", "公里每秒 (km/s)", "光速 (c)"),
            "Time" to listOf("普朗克時間", "么秒 (ys)", "仄秒 (zs)", "阿秒 (as)", "飛秒 (fs)", "皮秒 (ps)", "奈秒 (ns)", "微秒 (μs)", "毫秒 (ms)", "秒 (s)", "分 (min)", "刻 (Quarter)", "時 (h)", "時辰", "日 (d)", "週 (w)", "旬", "月 (M)", "季 (q)", "年 (y)", "年代 (decade)", "世紀 (century)", "須臾", "世", "紀", "代", "宙", "甲子"),
            "Storage" to listOf("位元 (bit)", "位元組 (Byte)", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "RB", "QB"),
            "Electric" to listOf("安培 (A)", "毫安培 (mA)", "伏特 (V)", "歐姆 (Ω)", "瓦特 (W)"),
            "Force" to listOf("牛頓 (N)", "公斤重 (kgf)", "磅力 (lbf)", "公克重 (gf)", "達因 (dyn)", "磅達 (pdl)", "噸重 (tf)", "千牛頓 (kN)"),
            "Optics" to listOf("勒克斯 (lx)", "呎燭光 (fc)"),
            "Energy" to listOf("焦耳 (J)", "兆焦耳 (MJ)", "拍焦耳 (PJ)", "卡 (cal)", "大卡 (kcal)", "瓦特小時 (Wh)", "千瓦小時 (kWh)", "電子伏特 (eV)", "爾格 (erg)", "英國熱量單位 (BTU)", "德熱姆 (th)", "桶油當量 (boe)", "噸煤當量 (tce)", "英尺磅 (ft·lb)")
        )
    }

    private fun setupCategorySelection() {
        chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = categoryMap[checkedIds.firstOrNull()] ?: "Length"
            updateSpinners(category)
            if (category == "Currency" && exchangeRates.isEmpty()) {
                fetchExchangeRates()
            }
        }
    }

    private fun updateSpinners(category: String) {
        val list = units[category] ?: return
        
        val displayList = if (category == "Temperature") {
            list.map { key ->
                when(key) {
                    TEMP_CELSIUS -> getString(R.string.unit_celsius)
                    TEMP_FAHRENHEIT -> getString(R.string.unit_fahrenheit)
                    TEMP_KELVIN -> getString(R.string.unit_kelvin)
                    else -> key
                }
            }
        } else list

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, displayList)
        autoFrom.setAdapter(adapter)
        autoTo.setAdapter(adapter)
        
        if (displayList.size >= 2) {
            autoFrom.setText(displayList[0], false)
            autoTo.setText(displayList[1], false)
        }
        
        tvUpdateInfo.visibility = if (category == "Currency") View.VISIBLE else View.GONE
        performConversion()
    }

    private fun fetchExchangeRates() {
        pbLoading.visibility = View.VISIBLE
        tvUpdateInfo.text = getString(R.string.update_info_connecting)
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val clientBuilder = OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        try { Security.insertProviderAt(Conscrypt.newProvider(), 1) } catch (e: Exception) {}
                    }

                    val client = clientBuilder.build()
                    val request = Request.Builder().url("https://open.er-api.com/v6/latest/USD").build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        val body = response.body?.string() ?: throw Exception("Empty")
                        val json = JSONObject(body)
                        val ratesObj = json.getJSONObject("rates")
                        val resultMap = mutableMapOf<String, Double>()
                        val codesList = mutableListOf<String>()
                        val keys = ratesObj.keys()
                        while(keys.hasNext()){
                            val key = keys.next()
                            resultMap[key] = ratesObj.getDouble(key)
                            codesList.add(key)
                        }
                        codesList.sort()
                        val updateTime = json.optString("time_last_update_utc", "").take(16)
                        Triple(resultMap, codesList, updateTime)
                    }
                }
                
                exchangeRates.clear()
                exchangeRates.putAll(result.first)
                units["Currency"] = result.second
                lastRatesUpdate = result.third
                tvUpdateInfo.text = getString(R.string.update_info_format, lastRatesUpdate)
                
                if (chipGroupCategory.checkedChipId == R.id.chipCurrency) {
                    updateSpinners("Currency")
                }
            } catch (e: Exception) {
                tvUpdateInfo.text = getString(R.string.update_info_failed, e.localizedMessage ?: "Unknown")
            } finally {
                pbLoading.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { performConversion() }
            override fun afterTextChanged(s: Editable?) {}
        })

        val autocompleteListener = AdapterView.OnItemClickListener { _, _, _, _ -> performConversion() }
        autoFrom.onItemClickListener = autocompleteListener
        autoTo.onItemClickListener = autocompleteListener
    }

    private fun performConversion() {
        val inputStr = etInput.text.toString()
        if (inputStr.isEmpty()) { tvConvertedValue.text = "0"; return }

        val value = inputStr.toDoubleOrNull() ?: 0.0
        val fromText = autoFrom.text.toString()
        val toText = autoTo.text.toString()

        val category = categoryMap[chipGroupCategory.checkedChipId] ?: "General"

        val result = when (category) {
            "Currency" -> {
                if (exchangeRates.isNotEmpty()) {
                    val fromRate = exchangeRates[fromText] ?: 1.0
                    val toRate = exchangeRates[toText] ?: 1.0
                    value * (toRate / fromRate)
                } else value
            }
            "Temperature" -> {
                val fromKey = when(fromText) {
                    getString(R.string.unit_celsius) -> TEMP_CELSIUS
                    getString(R.string.unit_fahrenheit) -> TEMP_FAHRENHEIT
                    getString(R.string.unit_kelvin) -> TEMP_KELVIN
                    else -> fromText
                }
                val toKey = when(toText) {
                    getString(R.string.unit_celsius) -> TEMP_CELSIUS
                    getString(R.string.unit_fahrenheit) -> TEMP_FAHRENHEIT
                    getString(R.string.unit_kelvin) -> TEMP_KELVIN
                    else -> toText
                }
                convertTemperature(value, fromKey, toKey)
            }
            else -> {
                val fromFactor = conversionFactors[fromText] ?: 1.0
                val toFactor = conversionFactors[toText] ?: 1.0
                value * (fromFactor / toFactor)
            }
        }
        displayResult(result)
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        if (from == to) return value
        val celsius = when (from) {
            TEMP_CELSIUS -> value
            TEMP_FAHRENHEIT -> (value - 32.0) * 5.0 / 9.0
            TEMP_KELVIN -> value - 273.15
            else -> value
        }
        return when (to) {
            TEMP_CELSIUS -> celsius
            TEMP_FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
            TEMP_KELVIN -> celsius + 273.15
            else -> celsius
        }
    }

    private fun displayResult(result: Double) {
        tvConvertedValue.text = if (result == 0.0) "0"
        else if (abs(result) >= 1e12 || (abs(result) < 1e-6)) String.format(Locale.US, "%.6e", result)
        else if (result % 1.0 == 0.0) String.format(Locale.US, "%.0f", result)
        else String.format(Locale.US, "%.10f", result).trimEnd('0').trimEnd('.')
    }
}
