package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
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

class HSheetDragContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testItemsPlacement() {
        val itemsPerRow = 2
        val width = CallActionDefaults.MinButtonSize * itemsPerRow + SheetItemsSpacing
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        val fileshare = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        WhiteboardAction(),
                        FileShareAction(),
                        ChatAction()
                    )
                ),
                labels = false,
                itemsPerRow = itemsPerRow,
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
                modifier = Modifier.width(width)
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithContentDescription(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithContentDescription(whiteboard).getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithContentDescription(fileshare).getBoundsInRoot()
        val childBounds4 = composeTestRule.onNodeWithContentDescription(chat).getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(rootBounds.left, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + HSheetDragHorizontalPadding, "child 2 left bound")
        childBounds2.right.assertIsEqualTo(rootBounds.right, "child 2 right bound")
        childBounds3.left.assertIsEqualTo(rootBounds.left, "child 3 left bound")
        childBounds3.top.assertIsEqualTo(childBounds1.bottom + VSheetDragVerticalPadding, "child 3 top bound")
        childBounds4.left.assertIsEqualTo(childBounds3.right + HSheetDragHorizontalPadding, "child 4 left bound")
        childBounds4.right.assertIsEqualTo(rootBounds.right, "child 4 right bound")
        childBounds4.top.assertIsEqualTo(childBounds2.bottom + VSheetDragVerticalPadding, "child 4 top bound")
    }

    @Test
    fun testLastItemOnOneRow() {
        val itemsPerRow = 3
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        WhiteboardAction()
                    )
                ),
                itemsPerRow = itemsPerRow,
                onHangUpClick = { },
                onMicToggled = { },
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
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithText(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText(whiteboard).getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(rootBounds.left, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + HSheetDragHorizontalPadding, "child 2 left bound")
        childBounds2.right.assertIsEqualTo(rootBounds.right, "child 2 right bound")
    }

    @Test
    fun testOnMicClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            HSheetDragContent(
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
            HSheetDragContent(
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
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFlipCameraClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnAudioClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnChatClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFileShareClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnWhiteboardClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnVirtualBackgroundClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            HSheetDragContent(
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
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }
}