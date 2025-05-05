package com.kaleyra.video_sdk.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.fileshare.model.mockSignDocumentFile
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsContent
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsItemDividerTag
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsItemTag
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignDocumentsContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(emptyList<SignDocumentUi>()))

    private var actualSignDocumentUi: SignDocumentUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentsContent(
                items = items,
                onItemClick = { actualSignDocumentUi = it },
                lazyGridState = rememberLazyGridState()
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(emptyList())
        actualSignDocumentUi = null
    }

    @Test
    fun itemDividerCountIsNumberOfItemsMinusOne() {
        items = ImmutableList(listOf(mockSignDocumentFile.copy(id = "1"), mockSignDocumentFile.copy(id = "2"), mockSignDocumentFile.copy(id = "3")))
        composeTestRule.onAllNodesWithTag(SignDocumentsItemDividerTag).assertCountEquals(2)
    }

    @Test
    fun userClicksItem_onItemActionClickInvoked() {
        val signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Pending)
        items = ImmutableList(listOf(signDocument))
        val clickableChildren = composeTestRule
            .onNodeWithTag(SignDocumentsItemTag)
            .onChildren()
            .filter(hasClickAction())
        clickableChildren.onFirst().performClick()
        assertEquals(signDocument, actualSignDocumentUi)
    }

    @Test
    fun userClicksItemAction_onItemActionClickInvoked() {
        val signDocument = mockSignDocumentFile.copy(signState = SignDocumentUi.SignStateUi.Pending)
        items = ImmutableList(listOf(signDocument))
        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.resources.getString(com.kaleyra.video_sdk.R.string.kaleyra_signature_sign))
            .performClick()

        assertEquals(signDocument, actualSignDocumentUi)
    }
}