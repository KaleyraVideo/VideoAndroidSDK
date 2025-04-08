package com.kaleyra.video_sdk.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.model.mockSignDocumentFile
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUiState
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsItemTag
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignDocumentsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var signDocumentsUiState by mutableStateOf(SignDocumentUiState())

    private var largeScreen by mutableStateOf(false)

    private var onItemClicked = false

    private var onBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentsComponent(
                uiState = signDocumentsUiState,
                userMessageComponent = { Text("User message") },
                onItemClick = { onItemClicked = true },
                onBackPressed = { onBackPressed = true },
                isLargeScreen = largeScreen,
            )
        }
    }

    @After
    fun tearDown() {
        signDocumentsUiState = SignDocumentUiState()
        onItemClicked = false
        onBackPressed = false
    }

    @Test
    fun signDocumentsTopAppBarShown() {
        val sign = composeTestRule.activity.getString(R.string.kaleyra_signature_sign)
        composeTestRule.onNodeWithText(sign).assertIsDisplayed()
    }

    @Test
    fun signDocumentsAppBarCloseButtonShown() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
    }

    @Test
    fun emptyItems_noItemsUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_signature_no_documents_shared)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        signDocumentsUiState = signDocumentsUiState.copy(signDocuments = ImmutableList(listOf(mockSignDocumentFile)))
        composeTestRule.onNodeWithText(title).assertIsNotDisplayed()
    }

    @Test
    fun oneSignDocument_itemDisplayed() {
        signDocumentsUiState = SignDocumentUiState(signDocuments = ImmutableList(listOf(mockSignDocumentFile)))
        composeTestRule.onNodeWithTag(SignDocumentsItemTag).assertIsDisplayed()
    }

    @Test
    fun oneSignDocumentPendingClicked_onItemClicked() {
        signDocumentsUiState = SignDocumentUiState(signDocuments = ImmutableList(listOf(mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Pending))))
        val sign = composeTestRule.activity.resources.getString(R.string.kaleyra_signature_sign)
        composeTestRule.onNodeWithContentDescription(sign).performClick()
        Assert.assertTrue(onItemClicked)
    }

    @Test
    fun oneSignDocumentSigningClicked_onItemClicked() {
        signDocumentsUiState = SignDocumentUiState(signDocuments = ImmutableList(listOf(mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Signing))))
        val pending = composeTestRule.activity.resources.getString(R.string.kaleyra_signature_state_signing)
        composeTestRule.onAllNodesWithContentDescription(pending).onFirst().performClick()
        Assert.assertTrue(onItemClicked)
    }

    @Test
    fun oneSignDocumentCompletedClicked_onItemClicked() {
        signDocumentsUiState = SignDocumentUiState(signDocuments = ImmutableList(listOf(mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Completed))))
        val completed = composeTestRule.activity.resources.getString(R.string.kaleyra_signature_state_completed)
        composeTestRule.onAllNodesWithContentDescription(completed).onFirst().performClick()
        Assert.assertTrue(onItemClicked)
    }

    @Test
    fun oneSignDocumentFailedClicked_onItemClicked() {
        signDocumentsUiState = SignDocumentUiState(signDocuments = ImmutableList(listOf(mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Failed(Throwable())))))
        val failed = composeTestRule.activity.resources.getString(R.string.kaleyra_fileshare_retry)
        composeTestRule.onAllNodesWithContentDescription(failed).onFirst().performClick()
        Assert.assertTrue(onItemClicked)
    }

    @Test
    fun appBarCloseButtonClicked_onBackPressed() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertTrue(onBackPressed)
    }

    @Test
    fun smallScreen_userMessageComponentIsDisplayed() {
        largeScreen = false
        composeTestRule.onNodeWithText("User message").assertIsDisplayed()
    }

    @Test
    fun largeScreen_userMessageComponentDoesNotExists() {
        largeScreen = true
        composeTestRule.onNodeWithText("User message").assertDoesNotExist()
    }
}