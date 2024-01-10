package com.kaleyra.video_sdk.call.participantspanel

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantPanelTopBarComponent
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ParticipantPanelTopBarComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testParticipantPanelTopAppBarMultipleInCallUsersHeaderDisplayed() {
        val inCallStreamUi =  ImmutableList((0..5).map {
            streamUiMock
        })
        val inCallStreamUiCount = inCallStreamUi.value.count()
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
               watermarkInfo = WatermarkInfo(
                   "brand",
                   Logo(
                       light = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"),
                       dark = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"))),
                inCallStreamUi = inCallStreamUi,
                onClose = {},
                isDarkTheme = true
            )
        }
        val headerText = "$inCallStreamUiCount ${composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_participants,inCallStreamUiCount)}"

        composeTestRule.onNodeWithText(headerText).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelTopAppBarSingleInCallUsersHeaderDisplayed() {
        val inCallStreamUi =  ImmutableList(listOf(streamUiMock))
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
                watermarkInfo = WatermarkInfo(
                    "brand",
                    Logo(
                        light = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"),
                        dark = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"))),
                inCallStreamUi = inCallStreamUi,
                onClose = {},
                isDarkTheme = true
            )
        }
        val headerText = "1 ${composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_participants, 1)}"

        composeTestRule.onNodeWithText(headerText).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelTopAppBarNoUsersInCallHeaderNotExists() {
        val inCallStreamUi =  ImmutableList(listOf<StreamUi>())
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
                watermarkInfo = WatermarkInfo(
                    "brand",
                    Logo(
                        light = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"),
                        dark = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"))),
                inCallStreamUi = inCallStreamUi,
                onClose = {},
                isDarkTheme = true
            )
        }
        val headerText = "0 ${composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_participants, 0)}"
        composeTestRule.onNodeWithText(headerText).assertDoesNotExist()
    }

    @Test
    fun testParticipantPanelTopAppBarCloseButtonDisplayed() {
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
                watermarkInfo = WatermarkInfo(
                    "brand",
                    Logo(
                        light = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"),
                        dark = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"))),
                inCallStreamUi = ImmutableList(listOf()),
                onClose = {},
                isDarkTheme = true
            )
        }
        val closeButtonContentDescription = composeTestRule.activity.resources.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(closeButtonContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelTopAppBarWatermarkWithLogoDisplayed() {
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
                watermarkInfo = WatermarkInfo(
                    "brand",
                    Logo(
                        light = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"),
                        dark = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"))),
                inCallStreamUi = ImmutableList(listOf()),
                onClose = {},
                isDarkTheme = true
            )
        }
        val companyLogoContentDescription = composeTestRule.activity.resources.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogoContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelTopAppBarWatermarkWithNoLogoDisplayed() {
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
                watermarkInfo = WatermarkInfo(
                    text = "brand",
                    logo = null),
                inCallStreamUi = ImmutableList(listOf()),
                onClose = {},
                isDarkTheme = true
            )
        }
        val companyLogoContentDescription = composeTestRule.activity.resources.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogoContentDescription).assertDoesNotExist()
    }

    @Test
    fun testParticipantPanelTopAppBarCloseButtonClickedCallbackCalled() {
        var clicked = false
        composeTestRule.setContent {
            ParticipantPanelTopBarComponent(
                watermarkInfo = WatermarkInfo(
                    text = "brand",
                    logo = null),
                inCallStreamUi = ImmutableList(listOf()),
                onClose = { clicked = true },
                isDarkTheme = true
            )
        }
        val closeButtonContentDescription = composeTestRule.activity.resources.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(closeButtonContentDescription).performClick()
        Assert.assertEquals(true, clicked)
    }
}