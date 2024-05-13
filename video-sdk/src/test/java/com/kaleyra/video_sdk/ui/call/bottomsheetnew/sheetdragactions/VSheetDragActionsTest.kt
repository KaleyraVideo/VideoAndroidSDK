package com.kaleyra.video_sdk.ui.call.bottomsheetnew.sheetdragactions

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.VSheetDragActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.VSheetDragHorizontalPadding
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.VSheetDragVerticalPadding
import com.kaleyra.video_sdk.call.callactionnew.CallActionDefaults
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VSheetDragActionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testItemsPlacement() {
        val itemsPerColumn = 2
        val height = CallActionDefaults.minButtonSize * itemsPerColumn + SheetItemsSpacing
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction(),
                        ChatAction()
                    )
                ),
                itemsPerColumn = itemsPerColumn,
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                modifier = Modifier.height(height)
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithContentDescription(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithContentDescription(camera).getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithContentDescription(mic).getBoundsInRoot()
        val childBounds4 = composeTestRule.onNodeWithContentDescription(chat).getBoundsInRoot()
        childBounds1.top.assertIsEqualTo(childBounds2.bottom + VSheetDragVerticalPadding, "child 1 top bound")
        childBounds1.bottom.assertIsEqualTo(rootBounds.bottom, "child 1 bottom bound")
        childBounds2.top.assertIsEqualTo(rootBounds.top, "child 2 top bound")
        childBounds3.top.assertIsEqualTo(childBounds4.bottom + VSheetDragVerticalPadding, "child 3 top bound")
        childBounds3.bottom.assertIsEqualTo(rootBounds.bottom, "child 3 bottom bound")
        childBounds4.top.assertIsEqualTo(rootBounds.top, "child 4 top bound")
        childBounds3.left.assertIsEqualTo(childBounds1.right + VSheetDragHorizontalPadding, "child 3 left bound")
        childBounds4.left.assertIsEqualTo(childBounds2.right + VSheetDragHorizontalPadding, "child 4 left bound")
    }

    @Test
    fun testOnMicClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(MicAction())),
                onHangUpClick = { },
                onMicToggled = { isClicked = it },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnCameraClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(CameraAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { isClicked = it },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnScreenShareClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(ScreenShareAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { isClicked = true },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFlipCameraClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(FlipCameraAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { isClicked = true },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnAudioClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(AudioAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { isClicked = true },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnChatClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(ChatAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { isClicked = true },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFileShareClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(FileShareAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { isClicked = true },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnWhiteboardClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(WhiteboardAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { isClicked = true },
                onVirtualBackgroundClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnVirtualBackgroundClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            VSheetDragActions(
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                onHangUpClick = { },
                onMicToggled = { },
                onCameraToggled = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { isClicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }
}