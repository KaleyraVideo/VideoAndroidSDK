/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kaleyra.demo_video_sdk.ui.custom_views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.kaleyra.app_configuration.model.CallOptionsType
import com.kaleyra.app_utilities.utils.Utils
import com.kaleyra.demo_video_sdk.R
import com.kaleyra.video_utils.ContextRetainer
import com.robertlevonyan.views.expandable.Expandable

@SuppressLint("ViewConstructor")
class CallOptionsDialogView(
    context: Context,
    val callOptionType: CallOptionsType? = null,
    val callConfiguration: CallConfiguration? = null
) : LinearLayout(context) {

    var audioOnlyCallOptionsView: CallOptions? = null
    var audioUpgradableCallOptionsView: CallOptions? = null
    var audioVideoCallOptionsView: CallOptions? = null

    private fun setup(context: Context) {
        orientation = VERTICAL
        @SuppressLint("InflateParams") val layout = LayoutInflater.from(context).inflate(R.layout.call_options_dialog_layout, null)
        addView(layout)

        val info = findViewById<TextView>(R.id.info)
        audioOnlyCallOptionsView = CallOptions(context)
        audioOnlyCallOptionsView!!.titleView.text = context.getString(R.string.audio_only)
        audioOnlyCallOptionsView!!.callOptionsViewContainer.findViewById<View>(R.id.call_options_camera_effects).visibility = View.GONE
        audioOnlyCallOptionsView!!.callOptionsViewContainer.findViewById<View>(R.id.call_options_back_camera).visibility = View.GONE
        audioUpgradableCallOptionsView = CallOptions(context)
        audioUpgradableCallOptionsView!!.titleView.text = context.getString(R.string.audio_upgradable)
        audioVideoCallOptionsView = CallOptions(context)
        audioVideoCallOptionsView!!.titleView.text = context.getString(R.string.audio_video)
        setupCallOptionsViewCompoundClickListener(audioOnlyCallOptionsView!!)
        setupCallOptionsViewCompoundClickListener(audioUpgradableCallOptionsView!!)
        setupCallOptionsViewCompoundClickListener(audioVideoCallOptionsView!!)
        val options = layout.findViewById<LinearLayout>(R.id.options)
        options.addView(audioVideoCallOptionsView!!.callOptionsViewContainer, 2)
        options.addView(audioUpgradableCallOptionsView!!.callOptionsViewContainer, 2)
        options.addView(audioOnlyCallOptionsView!!.callOptionsViewContainer, 2)
        val selectAllCallCapabilityButton: AppCompatButton = findViewById(R.id.select_all_call_capabilities)
        val deselectAllCallCapabilityButton: AppCompatButton = findViewById(R.id.deselect_all_call_capabilities)
        val selectAllCallOptionsButton: AppCompatButton = findViewById(R.id.select_all_call_options)
        val deselectAllCallOptionsButton: AppCompatButton = findViewById(R.id.deselect_all_call_options)

        selectAllCallCapabilityButton.setOnClickListener { buttonView: View? ->
            selectedOptionsView!!.selectAllCallCapabilities()
            return@setOnClickListener
        }
        deselectAllCallCapabilityButton.setOnClickListener { buttonView: View? -> deselectAllCallCapabilities() }
        selectAllCallOptionsButton.setOnClickListener { buttonView: View? ->
            selectedOptionsView!!.selectAllCallOptions()
            return@setOnClickListener
        }
        deselectAllCallOptionsButton.setOnClickListener { buttonView: View? -> deselectAllCallOptions() }
        info.text = context.getString(R.string.select_call_type)
        deSelectAllCallTypes()
        when (callOptionType!!) {
            CallOptionsType.AUDIO_ONLY -> {
                audioOnlyCallOptionsView!!.selectingProgrammatically = true
                audioOnlyCallOptionsView!!.titleView.isChecked = true

            }

            CallOptionsType.AUDIO_UPGRADABLE -> {
                audioUpgradableCallOptionsView!!.selectingProgrammatically = true
                audioUpgradableCallOptionsView!!.titleView.isChecked = true
            }

            CallOptionsType.AUDIO_VIDEO -> {
                audioVideoCallOptionsView!!.selectingProgrammatically = true
                audioVideoCallOptionsView!!.titleView.isChecked = true
            }
        }
    }

    private val selectedOptionsView: CallOptions?
        get() = if (audioOnlyCallOptionsView!!.isChecked) audioOnlyCallOptionsView else if (audioUpgradableCallOptionsView!!.isChecked) audioUpgradableCallOptionsView else if (audioVideoCallOptionsView!!.isChecked) audioVideoCallOptionsView else null

    val isAudioOnlyCallChecked: Boolean
        get() = audioOnlyCallOptionsView!!.isChecked

    val isAudioUpgradableCallChecked: Boolean
        get() = audioUpgradableCallOptionsView!!.isChecked

    val isAudioVideoCallChecked: Boolean
        get() = audioVideoCallOptionsView!!.isChecked

    private fun deSelectAllCallTypes() {
        audioOnlyCallOptionsView!!.selectingProgrammatically = false
        audioOnlyCallOptionsView!!.titleView.isChecked = false
        audioUpgradableCallOptionsView!!.selectingProgrammatically = false
        audioUpgradableCallOptionsView!!.titleView.isChecked = false
        audioVideoCallOptionsView!!.selectingProgrammatically = false
        audioVideoCallOptionsView!!.titleView.isChecked = false
    }

    private fun deselectAllCallCapabilities() {
        audioOnlyCallOptionsView!!.deselectAllCallCapabilities()
        audioUpgradableCallOptionsView!!.deselectAllCallCapabilities()
        audioVideoCallOptionsView!!.deselectAllCallCapabilities()
    }

    private fun deselectAllCallOptions() {
        audioOnlyCallOptionsView!!.deselectAllCallOptions()
        audioUpgradableCallOptionsView!!.deselectAllCallOptions()
        audioVideoCallOptionsView!!.deselectAllCallOptions()
    }

    private fun setupCallOptionsViewCompoundClickListener(callOptionsView: CallOptions) {
        callOptionsView.titleView.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                when {
                    callOptionsView === audioOnlyCallOptionsView -> {
                        deselectAll(audioUpgradableCallOptionsView)
                        deselectAll(audioVideoCallOptionsView)
                    }

                    callOptionsView === audioUpgradableCallOptionsView -> {
                        deselectAll(audioOnlyCallOptionsView)
                        deselectAll(audioVideoCallOptionsView)
                    }

                    callOptionsView === audioVideoCallOptionsView -> {
                        deselectAll(audioUpgradableCallOptionsView)
                        deselectAll(audioOnlyCallOptionsView)
                    }
                }
            }
            callOptionsView.applyCallOptionsPreferences()
            if (isChecked) {
                if (callOptionsView.selectingProgrammatically) {
                    callOptionsView.selectingProgrammatically = false
                    return@setOnCheckedChangeListener
                }
                callOptionsView.callOptionsViewContainer.expand()
            } else {
                callOptionsView.deselectAllCallOptions()
                callOptionsView.deselectAllCallCapabilities()
                callOptionsView.titleView.isChecked = false
                callOptionsView.callOptionsViewContainer.collapse()
            }
        }
    }

    private fun deselectAll(callOptionsView: CallOptions?) {
        callOptionsView!!.titleView.isChecked = false
        callOptionsView.deselectAllCallCapabilities()
        callOptionsView.deselectAllCallOptions()
        callOptionsView.callOptionsViewContainer.collapse()
        callOptionsView.selectingProgrammatically = false
    }

    inner class CallOptions(context: Context, parent: ViewGroup? = null) {

        var callOptionsViewContainer: Expandable = LayoutInflater.from(context).inflate(R.layout.call_options_container_expandable, parent) as Expandable

        val titleView = callOptionsViewContainer.findViewById<CompoundButton>(R.id.call_options_title)

        var selectingProgrammatically = false

        @SuppressLint("CustomViewStyleable")
        private fun setup(context: Context, attrs: AttributeSet?) {
            if (attrs != null) {
                val a = context.obtainStyledAttributes(attrs, R.styleable.CallOptionsViewContainer, 0, 0)
                a.recycle()
            }
            enableOptionClickListeners()
        }

        fun applyCallOptionsPreferences() {
            callConfiguration?.let { setCallCapabilities(it) }
            enableOptionClickListeners(enableOpenUrlDialog = true)
        }

        private fun setCallCapabilities(callConfiguration: CallConfiguration) = with(callConfiguration) {
            actions.forEach {
                when (it) {
                    is ConfigAction.OpenWhiteboard -> setChecked(R.id.call_options_whiteboard, true)
                    is ConfigAction.FileShare -> setChecked(R.id.call_options_file_share, true)
                    is ConfigAction.OpenChat -> setChecked(R.id.call_options_chat, true)
                    is ConfigAction.ScreenShare -> setChecked(R.id.call_options_screen_sharing, true)
                    is ConfigAction.CameraEffects -> setChecked(R.id.call_options_camera_effects, true)
                    is ConfigAction.OpenUrl -> setChecked(R.id.call_options_custom_button, true)
                    else -> Unit
                }
            }
            setChecked(R.id.call_options_recording, options.recordingEnabled)
            setChecked(R.id.call_options_back_camera, options.backCameraAsDefault)
            setChecked(R.id.call_options_feedback, options.feedbackEnabled)
        }

        private fun enableOptionClickListeners(enableOpenUrlDialog: Boolean = false) {
            val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                if (isChecked && !titleView.isChecked) {
                    titleView.isChecked = true
                }
            }
            addCheckedChangeListener(R.id.call_options_recording, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_whiteboard, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_file_share, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_screen_sharing, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_chat, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_back_camera, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_feedback, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_camera_effects, checkedChangeListener)
            addCheckedChangeListener(R.id.call_options_custom_button, CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                if (enableOpenUrlDialog && isChecked) getCustomButtonOpenUrl(
                    context,
                    ContextRetainer.context.getString(R.string.open_url_custom_button),
                    null,
                    onOkClicked = { url ->
                        if (url.isEmpty()) buttonView?.isChecked = false
                        else _openUrl = url
                    }
                )
                checkedChangeListener.onCheckedChanged(buttonView, isChecked)
            })
        }

        private fun addCheckedChangeListener(id: Int, checkedChangeListener: CompoundButton.OnCheckedChangeListener) {
            val checkBox = callOptionsViewContainer.findViewById<CheckBox>(id)
            checkBox.setOnCheckedChangeListener(checkedChangeListener)
        }

        fun selectAllCallCapabilities() {
            selectingProgrammatically = true
            setChecked(R.id.call_options_whiteboard, true)
            setChecked(R.id.call_options_file_share, true)
            setChecked(R.id.call_options_screen_sharing, true)
            setChecked(R.id.call_options_camera_effects, true)
            setChecked(R.id.call_options_chat, true)
        }

        fun deselectAllCallCapabilities() {
            setChecked(R.id.call_options_whiteboard, false)
            setChecked(R.id.call_options_file_share, false)
            setChecked(R.id.call_options_screen_sharing, false)
            setChecked(R.id.call_options_camera_effects, false)
            with(R.id.call_options_chat) {
                if (!findViewById<CheckBox>(this).isEnabled) return
                setChecked(this, false)
            }
        }

        fun selectAllCallOptions() {
            selectingProgrammatically = true
            setChecked(R.id.call_options_recording, true)
            if (this@CallOptions !== audioOnlyCallOptionsView) setChecked(R.id.call_options_back_camera, true)
            setChecked(R.id.call_options_feedback, true)
        }

        fun deselectAllCallOptions() {
            setChecked(R.id.call_options_recording, false)
            setChecked(R.id.call_options_back_camera, false)
            setChecked(R.id.call_options_feedback, false)
        }

        private fun getCustomButtonOpenUrl(
            context: Context,
            title: String,
            message: String?,
            initialText: String = "",
            onOkClicked: (String) -> Unit
        ) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(context, com.kaleyra.app_configuration.R.style.ThemeOverlay_App_MaterialAlertDialog)
            builder.setTitle(title)
            message?.let { builder.setMessage(it) }

            val layout = LinearLayout(context)
            val padding = Utils.dpToPx(context, 16f)
            layout.setPadding(padding, padding, padding, padding)
            val input = EditText(context)
            layout.addView(input, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.setText(initialText)
            builder.setView(layout)

            builder.setPositiveButton("OK") { dialog, which ->
                val inputText = input.text.toString()
                onOkClicked(inputText)
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
                onOkClicked("")
            }

            builder.show()
        }

        val isRecordingChecked: Boolean
            get() = isChecked(R.id.call_options_recording)

        val isWhiteboardChecked: Boolean
            get() = isChecked(R.id.call_options_whiteboard)

        val isFileShareChecked: Boolean
            get() = isChecked(R.id.call_options_file_share)

        val isScreenShareChecked: Boolean
            get() = isChecked(R.id.call_options_screen_sharing)

        val isChatChecked: Boolean
            get() = isChecked(R.id.call_options_chat)

        val isOpenUrlChecked: Boolean
            get() = isChecked(R.id.call_options_custom_button)

        private var _openUrl: String? = null
        val openUrl: String?
            get() = _openUrl ?: (callConfiguration?.actions?.firstOrNull { it is ConfigAction.OpenUrl } as? ConfigAction.OpenUrl)?.url

        val isBackCameraChecked: Boolean
            get() = isChecked(R.id.call_options_back_camera)

        val isFeedbackChecked: Boolean
            get() = isChecked(R.id.call_options_feedback)

        val isCameraEffectsChecked: Boolean
            get() = isChecked(R.id.call_options_camera_effects)

        val isChecked: Boolean
            get() = titleView.isChecked

        private fun isChecked(id: Int): Boolean {
            val checkBox = callOptionsViewContainer.findViewById<CheckBox>(id)
            return checkBox.isChecked
        }

        private fun setChecked(id: Int, checked: Boolean) {
            val checkBox = callOptionsViewContainer.findViewById<CheckBox>(id)
            checkBox.isChecked = checked
            checkBox.jumpDrawablesToCurrentState()
        }

        init {
            setup(context, null)
        }
    }

    init {
        setup(context)
    }
}