package com.kaleyra.video_sdk.ui.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.fileshare.model.mockSignDocumentFile
import com.kaleyra.video_sdk.call.signature.SignDocumentViewComponent
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUiState
import com.kaleyra.video_sdk.call.signature.view.SignViewTag
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SignDocumentViewComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var onDismissed = false

    private var signDocumentViewModel = mockk<SignDocumentsViewModel>(relaxed = true)

    private var uiState = MutableStateFlow(SignDocumentUiState(signDocuments = ImmutableList(listOf(mockSignDocumentFile)), ongoingSignDocumentUi = mockSignDocumentFile))

    @Before
    fun setUp() {
        every { signDocumentViewModel.uiState } returns uiState
        composeTestRule.setContent {
            SignDocumentViewComponent(
                signDocumentsViewModel = signDocumentViewModel,
                onDispose = {},
                onBackPressed = { onDismissed = true },
                onDocumentSigned = {},
                onUserMessageActionClick = {},
                isTesting = true
            )
        }
    }

    @After
    fun tearDown() {
        onDismissed = false
    }

    @Test
    fun testSignViewAdded() {
        composeTestRule.onNodeWithTag(SignViewTag)
    }

    @Test
    fun backButtonClicked_signCanceled() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.resources.getString(com.kaleyra.video_sdk.R.string.kaleyra_close)).performClick()
        verify { signDocumentViewModel.cancelSign(mockSignDocumentFile) }
        Assert.assertTrue(onDismissed)
    }

    @Test
    fun noSigningDocument_signCanceled() {
        uiState.value = SignDocumentUiState()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignViewTag).assertDoesNotExist()
    }
}