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

package com.kaleyra.video_sdk.ui.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video.configuration.Configuration
import com.kaleyra.video.configuration.Environment
import com.kaleyra.video.configuration.Region
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.model.mockAudioDevices
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.bottomsheet.AudioOutputComponentTag
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetComponent
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetContentState
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetState
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.CallActionsComponentTag
import com.kaleyra.video_sdk.call.bottomsheet.CollapsedLineWidth
import com.kaleyra.video_sdk.call.bottomsheet.ExpandedLineWidth
import com.kaleyra.video_sdk.call.bottomsheet.FileShareComponentTag
import com.kaleyra.video_sdk.call.bottomsheet.LineState
import com.kaleyra.video_sdk.call.bottomsheet.LineTag
import com.kaleyra.video_sdk.call.bottomsheet.ScreenShareComponentTag
import com.kaleyra.video_sdk.call.bottomsheet.VirtualBackgroundComponentTag
import com.kaleyra.video_sdk.call.bottomsheet.WhiteboardComponentTag
import com.kaleyra.video_sdk.call.recording.model.RecordingStateUi
import com.kaleyra.video_sdk.call.recording.model.RecordingTypeUi
import com.kaleyra.video_sdk.call.recording.model.RecordingUi
import com.kaleyra.video_sdk.call.screen.CallScreen
import com.kaleyra.video_sdk.call.screen.CallScreenState
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.screen.rememberCallScreenState
import com.kaleyra.video_sdk.call.screen.view.CallScreenAppBarTag
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.thumbnail.ThumbnailTag
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.model.mockVirtualBackgrounds
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardHideRequestMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardShowRequestMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.ui.ComposeViewModelsMockTest
import com.kaleyra.video_sdk.ui.findBackButton
import com.kaleyra.video_sdk.ui.performDoubleClick
import com.kaleyra.video_sdk.ui.performVerticalSwipe
import com.kaleyra.video_sdk.ui.pressBack
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallScreenTest: ComposeViewModelsMockTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callUiState by mutableStateOf(CallUiState(callState = CallStateUi.Connected, areCallActionsReady = true))

    private var sheetState by mutableStateOf(spyk(BottomSheetState(BottomSheetValue.Expanded)))

    private var sheetContentState by mutableStateOf(spyk(BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)))

    private var whiteboardRequest by mutableStateOf<WhiteboardRequest?>(null)

    private var shouldShowFileShareComponent by mutableStateOf(false)

    private var sideEffect by mutableStateOf(suspend { })

    private var callScreenState: CallScreenState? = null

    private var thumbnailClickedStreamId: String? = null

    private var thumbnailDoubleClickedStreamId: String? = null

    private var backPressed = false

    private var finishActivity = false

    private var fileShareDisplayed = false

    private var whiteboardDisplayed = false

    @Before
    fun setUp() {
        KaleyraVideo.configure(Configuration("appId", Environment.Production, Region.Europe))
        composeTestRule.setContent {
            CallScreen(
                callUiState = callUiState,
                whiteboardRequest = whiteboardRequest,
                callScreenState = rememberCallScreenState(
                    sheetState = sheetState,
                    sheetContentState = sheetContentState,
                    shouldShowFileShareComponent = shouldShowFileShareComponent
                ).also {
                    callScreenState = spyk(it)
                },
                onThumbnailStreamClick = { thumbnailClickedStreamId = it },
                onThumbnailStreamDoubleClick = { thumbnailDoubleClickedStreamId = it },
                onBackPressed = { backPressed = true },
                onConfigurationChange = { },
                onFullscreenStreamClick = { },
                onFileShareVisibility = { fileShareDisplayed = it },
                onWhiteboardVisibility = { whiteboardDisplayed = it },
                onAskInputPermissions = { },
                onCallEndedBack = { finishActivity = true },
                onUserFeedback = { _,_ -> },
            )
            LaunchedEffect(sideEffect) {
                sideEffect.invoke()
            }
        }
    }

    @After
    fun tearDown() {
        callScreenState = null
        callUiState = CallUiState(callState = CallStateUi.Connected, areCallActionsReady = true)
        sheetState = spyk(BottomSheetState(BottomSheetValue.Expanded))
        shouldShowFileShareComponent = false
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        thumbnailClickedStreamId = null
        thumbnailDoubleClickedStreamId = null
        fileShareDisplayed = false
        whiteboardDisplayed = false
        backPressed = false
        finishActivity = false
    }

    @Test
    fun sheetCollapsed_lineIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(
            CollapsedLineWidth
        )
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsed_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(
            ExpandedLineWidth
        )
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    @Test
    fun sheetNotCollapsableAndHalfExpanded_lineIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, isCollapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(
            CollapsedLineWidth
        )
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsableAndExpanded_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, isCollapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(
            ExpandedLineWidth
        )
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    @Test
    fun sheetCollapsed_userClicksLine_sheetHalfExpand() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun audioOutputComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.Whiteboard)
    }

    @Test
    fun virtualBackgroundComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.VirtualBackground)
    }

    private fun expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialComponent, LineState.Expanded)
        composeTestRule.waitForIdle()
        assertEquals(initialComponent, sheetContentState.currentComponent)
        composeTestRule.pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertIsDisplayed()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun callActionsComponentExpanded_userPerformsBack_sheetIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertIsDisplayed()
        composeTestRule.pressBack()
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun callActionsComponentExpandedAndNotCollapsable_userPerformsBack_sheetIsHalfExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, isCollapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertIsDisplayed()
        composeTestRule.pressBack()
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
    }

    @Test
    fun callActionsComponentHalfExpandedAndNotCollapsable_userPerformsBack_activityIsFinishing() {
        callUiState = CallUiState()
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, isCollapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.waitForIdle()
        composeTestRule.pressBack()
        assertEquals(true, composeTestRule.activity.isFinishing)
    }

    @Test
    fun callActionsComponentCollapsed_userPerformsBack_activityIsFinishing() {
        callUiState = CallUiState()
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.waitForIdle()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        assertEquals(true, composeTestRule.activity.isFinishing)
    }

    @Test
    fun callStateEnded_userPerformsBack_finishActivityInvoked() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.waitForIdle()
        composeTestRule.pressBack()
        assertEquals(true, finishActivity)
    }

    @Test
    fun userClicksWhiteboardAction_sheetIsExpanded() {
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        userClicksAction_sheetIsExpanded(whiteboard)
    }

    @Test
    fun userClicksScreenShareAction_sheetIsExpanded() {
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        userClicksAction_sheetIsExpanded(screenShare)
    }

    @Test
    fun userClicksFileShareAction_sheetIsExpanded() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        userClicksAction_sheetIsExpanded(fileShare)
    }

    @Test
    fun userClicksAudioOutputAction_sheetIsExpanded() {
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        userClicksAction_sheetIsExpanded(audioOutput)
    }

    @Test
    fun userClicksVirtualBackgroundAction_sheetIsExpanded() {
        val virtualBackground = composeTestRule.activity.getString(R.string.kaleyra_call_action_virtual_background)
        userClicksAction_sheetIsExpanded(virtualBackground)
    }

    private fun userClicksAction_sheetIsExpanded(actionDescription: String) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithContentDescription(actionDescription).performClick()
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun audioOutputComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.Whiteboard)
    }

    @Test
    fun virtualBackgroundComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.VirtualBackground)
    }

    private fun sheetHalfExpanding_callActionsComponentDisplayed(initialComponent: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialComponent, LineState.Collapsed())
        sideEffect = sheetState::halfExpand
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetComponent.CallActions, sheetContentState.currentComponent)
    }

    @Test
    fun audioOutputComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.Whiteboard)
    }

    @Test
    fun virtualBackgroundComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.Whiteboard)
    }

    private fun sheetCollapsing_callActionsComponentDisplayed(initialComponent: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialComponent, LineState.Collapsed())
        sideEffect = sheetState::collapse
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetComponent.CallActions, sheetContentState.currentComponent)
    }

    @Test
    fun userClicksAudioOutputAction_audioOutputIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route),
            targetComponentTag = AudioOutputComponentTag,
            targetComponent = BottomSheetComponent.AudioOutput
        )
    }

    @Test
    fun userClicksScreenShareAction_screenShareIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share),
            targetComponentTag = ScreenShareComponentTag,
            targetComponent = BottomSheetComponent.ScreenShare
        )
    }

    @Test
    fun userClicksWhiteboardAction_whiteboardIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard),
            targetComponentTag = WhiteboardComponentTag,
            targetComponent = BottomSheetComponent.Whiteboard
        )
    }

    @Test
    fun userClicksFileShareAction_fileShareIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share),
            targetComponentTag = FileShareComponentTag,
            targetComponent = BottomSheetComponent.FileShare
        )
    }

    @Test
    fun userClicksVirtualBackgroundAction_virtualBackgroundIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_virtual_background),
            targetComponentTag = VirtualBackgroundComponentTag,
            targetComponent = BottomSheetComponent.VirtualBackground
        )
    }

    private fun userClicksAction_componentIsDisplayed(
        actionContentDescription: String,
        targetComponentTag: String,
        targetComponent: BottomSheetComponent
    ) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithContentDescription(actionContentDescription).performClick()
        composeTestRule.onNodeWithTag(targetComponentTag).assertIsDisplayed()
        assertEquals(targetComponent, sheetContentState.currentComponent)
    }

    @Test
    fun sheetCollapsed_callActionsNotVisible() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertDoesNotExist()
    }

    @Test
    fun fileShareComponentExpanded_fileShareAppBarDisplayed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithTag(CallScreenAppBarTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun whiteboardComponentExpanded_whiteboardAppBarDisplayed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.Whiteboard, LineState.Expanded)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithTag(CallScreenAppBarTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
    }

    @Test
    fun callActionsComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.CallActions)
    }

    @Test
    fun audioOutputComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.ScreenShare)
    }

    @Test
    fun virtualBackgroundComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.VirtualBackground)
    }

    private fun componentExpanded_appBarNotDisplayed(component: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(component, LineState.Expanded)
        composeTestRule.onNodeWithTag(CallScreenAppBarTag).assertDoesNotExist()
    }

    @Test
    fun sheetHidden_thumbnailStreamsAreNotDisplayed() {
        callUiState = CallUiState(CallStateUi.Disconnected, thumbnailStreams = ImmutableList(listOf(
            streamUiMock
        )))
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.Hidden, areVisible = false)
    }

    @Test
    fun sheetCollapsed_thumbnailStreamsAreDisplayed() {
        callUiState = CallUiState(CallStateUi.Connected, areCallActionsReady = true, thumbnailStreams = ImmutableList(listOf(
            streamUiMock
        )))
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.Collapsed, areVisible = true)
    }

    @Test
    fun sheetHalfExpanded_thumbnailStreamsAreDisplayed() {
        callUiState = CallUiState(CallStateUi.Connected, areCallActionsReady = true, thumbnailStreams = ImmutableList(listOf(
            streamUiMock
        )))
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.HalfExpanded, areVisible = true)
    }

    @Test
    fun sheetExpanded_thumbnailStreamsAreDisplayed() {
        callUiState = CallUiState(CallStateUi.Connected, areCallActionsReady = true, thumbnailStreams = ImmutableList(listOf(
            streamUiMock
        )))
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.Expanded, areVisible = true)
    }

    private fun checkThumbnailStreamsVisibility(sheetValue: BottomSheetValue, areVisible: Boolean) {
        sheetState = BottomSheetState(initialValue = sheetValue)
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(sheetValue, sheetStateValue)
            composeTestRule.onAllNodesWithTag(ThumbnailTag).assertCountEquals(if (areVisible) 1 else 0)
        }
    }

    @Test
    fun audioOutputComponent_userClicksAudioDevice_sheetHalfExpand() {
        mockkConstructor(AudioOutputViewModel::class)
        every { anyConstructed<AudioOutputViewModel>().uiState } returns MutableStateFlow(
            AudioOutputUiState(audioDeviceList = mockAudioDevices, playingDeviceId = "id")
        )
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.AudioOutput, LineState.Collapsed())
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun screenShareComponent_userClicksScreenShareDevice_sheetHalfExpand() {
        mockkConstructor(ScreenShareViewModel::class)
        every { anyConstructed<ScreenShareViewModel>().uiState } returns MutableStateFlow(
            ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)))
        )
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.ScreenShare, LineState.Collapsed())
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun virtualBackgroundComponent_userClicksVirtualBackground_sheetHalfExpand() {
        mockkConstructor(VirtualBackgroundViewModel::class)
        every { anyConstructed<VirtualBackgroundViewModel>().uiState } returns MutableStateFlow(
            VirtualBackgroundUiState(backgroundList = mockVirtualBackgrounds)
        )
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.VirtualBackground, LineState.Collapsed())
        val none = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_none)
        composeTestRule.onNodeWithText(none).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun callStateConnected_sheetIsNotHidden() {
        callUiState = CallUiState(callState = CallStateUi.Connected, areCallActionsReady = true)
        composeTestRule.waitForIdle()
        assertNotEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun callStateDialing_sheetIsNotHidden() {
        callUiState = CallUiState(callState = CallStateUi.Dialing, areCallActionsReady = true)
        composeTestRule.waitForIdle()
        assertNotEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun callStateReconnecting_sheetIsNotHidden() {
        callUiState = CallUiState(callState = CallStateUi.Reconnecting, areCallActionsReady = true)
        composeTestRule.waitForIdle()
        assertNotEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun callStateRingingAndIsConnectingFalse_sheetIsHidden() {
        callUiState = CallUiState(callState = CallStateUi.Ringing, areCallActionsReady = true)
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun callStateConnecting_sheetIsHalfExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        callUiState = CallUiState(callState = CallStateUi.Connecting, areCallActionsReady = true)
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
    }

    @Test
    fun callStateDisconnected_sheetIsHidden() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected)
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Hidden, sheetState.currentValue)
    }

    @Test
    fun shouldShowFileShareComponentTrue_fileShareComponentDisplayed() {
        shouldShowFileShareComponent = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(FileShareComponentTag).assertIsDisplayed()
        assertEquals(BottomSheetComponent.FileShare, sheetContentState.currentComponent)
    }

    @Test
    fun shouldShowFileShareComponentTrue_fileShareIsDisplayed() {
        shouldShowFileShareComponent = true
        composeTestRule.waitForIdle()
        assertEquals(true, fileShareDisplayed)
    }

    @Test
    fun fileShareComponent_onFileShareVisibilityInvoked() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
        composeTestRule.waitForIdle()
        assertEquals(true, fileShareDisplayed)
    }

    @Test
    fun whiteboardComponent_onWhiteboardVisibilityInvoked() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.Whiteboard, LineState.Expanded)
        composeTestRule.waitForIdle()
        assertEquals(true, whiteboardDisplayed)
    }

    // todo understand why this fails
//    @Test
//    fun userClicksThumbnail_onThumbnailStreamClickInvoked() {
//        callUiState = CallUiState(callState = CallStateUi.Connected, thumbnailStreams = ImmutableList(listOf(streamUiMock)))
//        composeTestRule.onNodeWithTag(ThumbnailTag).performClick()
//        assertEquals(streamUiMock.id, thumbnailClickedStreamId)
//    }

    @Test
    fun userDoubleClicksThumbnail_onThumbnailStreamDoubleClickInvoked() {
        callUiState = CallUiState(callState = CallStateUi.Connected, areCallActionsReady = true, thumbnailStreams = ImmutableList(listOf(
            streamUiMock
        )))
        composeTestRule.onNodeWithTag(ThumbnailTag).performDoubleClick()
        assertEquals(streamUiMock.id, thumbnailDoubleClickedStreamId)
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        callUiState = CallUiState()
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun whiteboardComponent_sheetGesturesAreDisabled() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.Whiteboard, LineState.Expanded)
        composeTestRule.onNodeWithTag(WhiteboardComponentTag).performVerticalSwipe(-0.5f)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            composeTestRule.onNodeWithTag(WhiteboardComponentTag).assertIsDisplayed()
            assertEquals(BottomSheetValue.Expanded, currentValue)
            assertEquals(BottomSheetComponent.Whiteboard, sheetContentState.currentComponent)
        }
    }

    @Test
    fun recordingTypeOnConnectAndCallStateDialing_onConnectHelperTextIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Dialing, recording = RecordingUi(RecordingTypeUi.OnConnect, RecordingStateUi.Started))
        val text = composeTestRule.activity.getString(R.string.kaleyra_automatic_recording_disclaimer)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun recordingTypeOnDemandAndCallStateDialing_onDemandHelperTextIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Dialing, recording = RecordingUi(RecordingTypeUi.OnDemand, RecordingStateUi.Started))
        val text = composeTestRule.activity.getString(R.string.kaleyra_manual_recording_disclaimer)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showWhiteboardRequestReceived_whiteboardDisplayed() {
        whiteboardRequest = WhiteboardRequest.Show("username")
        composeTestRule.waitForIdle()
        verify { sheetContentState.navigateToComponent(BottomSheetComponent.Whiteboard) }
        assertEquals(BottomSheetComponent.Whiteboard, sheetContentState.currentComponent)
    }

    @Test
    fun showWhiteboardRequestReceived_whiteboardUserMessageDisplayed() = mockkObject(CallUserMessagesProvider) {
        every { CallUserMessagesProvider.sendUserMessage(any()) } returns Unit
        whiteboardRequest = WhiteboardRequest.Show("username")
        composeTestRule.waitForIdle()
        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg<WhiteboardShowRequestMessage> {
                assertEquals("username", it.username)
            })
        }
    }

    @Test
    fun hideWhiteboardRequestReceived_whiteboardDisplayed() {
        sheetContentState.navigateToComponent(BottomSheetComponent.Whiteboard)
        whiteboardRequest = WhiteboardRequest.Hide("username")
        composeTestRule.waitForIdle()
        verify { sheetContentState.navigateToComponent(BottomSheetComponent.CallActions) }
        assertEquals(BottomSheetComponent.CallActions, sheetContentState.currentComponent)
    }

    @Test
    fun hideWhiteboardRequestReceived_whiteboardUserMessageDisplayed() = mockkObject(CallUserMessagesProvider) {
        every { CallUserMessagesProvider.sendUserMessage(any()) } returns Unit
        sheetContentState.navigateToComponent(BottomSheetComponent.Whiteboard)
        whiteboardRequest = WhiteboardRequest.Hide("username")
        composeTestRule.waitForIdle()
        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg<WhiteboardHideRequestMessage> {
                assertEquals("username", it.username)
            })
        }
    }

}