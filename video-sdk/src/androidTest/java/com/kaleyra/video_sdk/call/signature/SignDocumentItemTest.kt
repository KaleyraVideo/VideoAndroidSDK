package com.kaleyra.video_sdk.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.model.mockSignDocumentFile
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.view.SignDocumentItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignDocumentItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var signDocument by mutableStateOf(mockSignDocumentFile)

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentItem(
                signDocumentUi = signDocument,
                onActionClick = { isActionClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        signDocument = mockSignDocumentFile
        isActionClicked = false
    }

    @Test
    fun usersClicksAction_onActionClickInvoked() {
        val sign = composeTestRule.activity.getString(R.string.kaleyra_signature_sign)
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Pending)
        composeTestRule.onNodeWithContentDescription(sign).performClick()
        assert(isActionClicked)
    }

    @Test
    fun signStateFailed_failedMessageDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Failed(Throwable("error")))
        val error = composeTestRule.activity.getString(R.string.kaleyra_signature_state_failed)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }

    @Test
    fun signStatePending_pendingMessageDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Pending)
        val pending = composeTestRule.activity.getString(R.string.kaleyra_signature_state_pending)
        composeTestRule.onNodeWithText(pending).assertIsDisplayed()
    }

    @Test
    fun signStateSigning_signingMessageDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Signing)
        val signing = composeTestRule.activity.getString(R.string.kaleyra_signature_state_signing)
        composeTestRule.onNodeWithText(signing).assertIsDisplayed()
    }

    @Test
    fun signStateCompleted_completedMessageDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Signing)
        val completed = composeTestRule.activity.getString(R.string.kaleyra_signature_state_signing)
        composeTestRule.onNodeWithText(completed).assertIsDisplayed()
    }

    @Test
    fun signNameDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Signing)
        composeTestRule.onNodeWithText(signDocument.name).assertIsDisplayed()
    }

    @Test
    fun senderDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Signing)
        composeTestRule.onNodeWithText(signDocument.sender).assertIsDisplayed()
    }

    @Test
    fun signStateFailed_retryButtonDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Failed(Throwable("error")))
        val retry = composeTestRule.activity.getString(R.string.kaleyra_fileshare_retry)
        composeTestRule.onNodeWithContentDescription(retry).assertIsDisplayed()
    }

    @Test
    fun signStateCompleted_completedIconDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Completed)
        val completed = composeTestRule.activity.getString(R.string.kaleyra_signature_state_completed)
        composeTestRule.onAllNodesWithContentDescription(completed).onFirst().assertIsDisplayed()
    }

    @Test
    fun signStateSigning_signingIconDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Signing)
        val signing = composeTestRule.activity.getString(R.string.kaleyra_signature_state_signing)
        composeTestRule.onAllNodesWithContentDescription(signing).onFirst().assertIsDisplayed()
    }

    @Test
    fun signStatePending_signIconDisplayed() {
        signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Pending)
        val signing = composeTestRule.activity.getString(R.string.kaleyra_signature_sign)
        composeTestRule.onNodeWithContentDescription(signing).assertIsDisplayed()
    }
}