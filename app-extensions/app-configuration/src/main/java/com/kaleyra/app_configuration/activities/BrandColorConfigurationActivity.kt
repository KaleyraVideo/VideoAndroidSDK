package com.kaleyra.app_configuration.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import codes.side.andcolorpicker.converter.setFromColorInt
import codes.side.andcolorpicker.converter.toColorInt
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerHSLColor
import codes.side.andcolorpicker.view.picker.ColorSeekBar
import com.kaleyra.app_configuration.R
import com.kaleyra.app_configuration.utils.hideKeyboard

class BrandColorConfigurationActivity : BaseConfigurationActivity() {

    companion object {
        const val PICK_CUSTOM_BRAND_COLOR = 796
        const val CUSTOM_COLOR = "CUSTOM_COLOR"
        fun showForResult(activity: Activity, requestCode: Int, @ColorInt customColor: Int? = null) {
            activity.startActivityForResult(buildIntent(activity, customColor), PICK_CUSTOM_BRAND_COLOR)
        }

        private fun buildIntent(context: Context?, @ColorInt customColor: Int? = null): Intent {
            val intent = Intent(context, BrandColorConfigurationActivity::class.java)
            intent.putExtra(CUSTOM_COLOR, customColor)
            return intent
        }
    }

    private lateinit var remote: RadioButton
    private lateinit var custom: RadioButton
    private lateinit var colorEditText: EditText
    private var currentColor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_color_configuration)
        configureBrandColorRadioGroup()
        val customColor = intent.getIntExtra(CUSTOM_COLOR, -1)
        val customColorInt = customColor.takeIf { it != -1 } ?: Color.MAGENTA
        val hasCustomColor = customColor != -1
        if (hasCustomColor) {
            currentColor = customColor.toHex()
            custom.isChecked = true
        } else {
            currentColor = Color.MAGENTA.toHex()
            remote.isChecked = true
        }
        configureColorPicker(customColorInt)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun configureBrandColorRadioGroup() {
        remote = findViewById(R.id.brand_color_remote)
        custom = findViewById(R.id.brand_color_custom)
        val radioGroup = findViewById<RadioGroup>(R.id.brand_color_radio_group)
        val remoteSummary = findViewById<TextView>(R.id.brand_color_remote_summary)
        val customSummary = findViewById<TextView>(R.id.brand_color_custom_summary)
        val customColorGroup = findViewById<ViewGroup>(R.id.custom_color_group)
        remoteSummary.setOnClickListener { remote.performClick() }
        customSummary.setOnClickListener { custom.performClick() }
        radioGroup.setOnCheckedChangeListener { _, checkedId: Int ->
            when (checkedId) {
                remote.id -> {
                    hideKeyboard()
                    customColorGroup.visibility = View.GONE
                }

                custom.id -> {
                    customColorGroup.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun configureColorPicker(customColor: Int) {
        val colorSample = findViewById<View>(R.id.brand_color_sample)
        val hueSeekBar = findViewById<HSLColorPickerSeekBar>(R.id.hueSeekBar)
        val saturationSeekBar = findViewById<HSLColorPickerSeekBar>(R.id.saturationSeekBar)
        val lightnessSeekBar = findViewById<HSLColorPickerSeekBar>(R.id.lightnessSeekBar)
        colorEditText = findViewById(R.id.brand_color_custom_value)
        val group = PickerGroup<IntegerHSLColor>().also {
            it.registerPickers(
                hueSeekBar,
                saturationSeekBar,
                lightnessSeekBar,
            )
        }
         val colorHsl = IntegerHSLColor()
        colorHsl.setFromColorInt(customColor)
        colorSample.background = ColorDrawable(customColor)
        group.setColor(colorHsl)
        colorEditText.setText(customColor.toHex())

        colorEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (currentColor != null && currentColor == s.toString()) return

                val input = s.toString()
                colorEditText.removeTextChangedListener(this)
                if (input.isNotEmpty() && input.first() != '#') {
                    colorEditText.setText("#$input")
                    colorEditText.setSelection(input.length + 1)
                }
                colorEditText.addTextChangedListener(this)
                if (!validateHexColor(input)) return
                kotlin.runCatching {
                    val fallbackColor = currentColor?.let { Color.parseColor(it) }
                    currentColor = s.toString()
                    val colorInt = Color.parseColor(currentColor)
                    colorSample.background = ColorDrawable(colorInt)
                    val colorHsl = IntegerHSLColor()
                    var hasParsedColor = true
                    runCatching { colorHsl.setFromColorInt(colorInt) }.onFailure { hasParsedColor = false }
                    if (!hasParsedColor && fallbackColor != null) runCatching {
                        fallbackColor.let { colorHsl.setFromColorInt(it) }
                        hasParsedColor = true
                    }.onFailure { hasParsedColor = false }
                    if (hasParsedColor) group.setColor(colorHsl)
                }.onFailure {
                    println(it)
                }
            }

        })
        hueSeekBar.mode = HSLColorPickerSeekBar.Mode.MODE_HUE
        hueSeekBar.coloringMode = HSLColorPickerSeekBar.ColoringMode.PURE_COLOR

        group.addListener(object : ColorSeekBar.DefaultOnColorPickListener<ColorSeekBar<IntegerHSLColor>, IntegerHSLColor>() {
            override fun onColorPicked(picker: ColorSeekBar<IntegerHSLColor>, color: IntegerHSLColor, value: Int, fromUser: Boolean) {
                val hexString = color.toColorInt().toHex()
                if (currentColor == hexString) return
                colorEditText.setText(hexString)
            }
            override fun onColorChanged(picker: ColorSeekBar<IntegerHSLColor>, color: IntegerHSLColor, value: Int) {
                colorSample.background = ColorDrawable(color.toColorInt())
            }
        })
    }

    private fun validateHexColor(input: String): Boolean {
        colorEditText.error = null
        if (input.isEmpty()) return false
        if (input.length != 7) {
            colorEditText.error = getString(R.string.brand_color_invalid_hex_color_format)
            return false
        }
        if (input.first() != '#') {
            colorEditText.error = getString(R.string.brand_color_invalid_hex_color_format)
            return false
        }
        try {
            Color.parseColor(input)
            return true
        } catch (e: IllegalArgumentException) {
            colorEditText.error = getString(R.string.brand_color_invalid_hex_color)
            return false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.save -> {
                saveSettings()
                finish()
            }

            R.id.clear_all -> {
                remote!!.isChecked = true
            }

            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveSettings() {
        val resultDataIntent = Intent()
        resultDataIntent.putExtra(CUSTOM_COLOR, if (custom!!.isChecked) Color.parseColor(currentColor) else null)
        setResult(2, resultDataIntent)
    }

    private fun hasChangedSettings(): Boolean = true

    override fun onBackPressed() {
        if (hasChangedSettings()) {
            androidx.appcompat.app.AlertDialog.Builder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setMessage(R.string.pref_settings_save_confirmation_message)
                .setPositiveButton(R.string.pref_settings_save_confirmation_message_confirmation) { dialog, _ ->
                    dialog.dismiss()
                    saveSettings()
                    finish()
                }
                .setNegativeButton(R.string.pref_settings_save_confirmation_message_cancel) { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .show()
        } else {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed()
        }
    }
}

fun Int.toHex() = String.format("#%06X", (0xFFFFFF and this))