package com.kaleyra.video_sdk.viewmodel.call

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.Participant
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions.getFileSize
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SignDocumentsViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SignDocumentsViewModel

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val uriMock = mockk<Uri>()

    private val senderMock = mockk<Participant>()

    private val meMock = mockk<CallParticipant.Me>()

    private val sharedFolderMock = mockk<SharedFolder>(relaxed = true)

    private val signDocument1 = mockk<SignDocument>(relaxed = true)

    private val signView = View(ApplicationProvider.getApplicationContext<Context?>().applicationContext)

    private var signDocumentUi1 = SignDocumentUi(id = "sign1", name = "name1", sender = "userId", creationTime = 123L, signView = ImmutableView(signView), signState = SignDocumentUi.SignStateUi.Pending, uri = ImmutableUri(uriMock))

    private val signDocuments = MutableStateFlow<Set<SignDocument>>(setOf())

    @Before
    fun setUp() {
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
        mockkObject(UriExtensions)
        mockkObject(ContactDetailsManager)
        every { any<Uri>().getFileSize() } returns 0
        mockkObject(CallUserMessagesProvider)
        signDocumentUi1 = signDocumentUi1.copy(signState = SignDocumentUi.SignStateUi.Pending)
        every { sharedFolderMock.signDocuments } returns signDocuments
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { sharedFolder } returns sharedFolderMock
            every { participants } returns MutableStateFlow(mockk {
                every { me } returns meMock
            })
        }
        with(meMock) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
        }
        with(senderMock) {
            every { userId } returns "userId"
            every { combinedDisplayName } returns MutableStateFlow("displayName")
        }
        with(signDocument1) {
            every { id } returns "sign1"
            every { name } returns "name1"
            every { creationTime } returns 123L
            every { uri } returns uriMock
            every { signState } returns MutableStateFlow(SignDocument.SignState.Pending)
            every { sender } returns senderMock
            every { signView } returns this@SignDocumentsViewModelTest.signView
        }
        viewModel = SignDocumentsViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSignDocumentsUpdatedOnSharedFolder_signDocumentsUiUpdated() = runTest {
        viewModel.uiState.first{ it.signDocuments.isEmpty() }
        signDocuments.value = setOf(signDocument1)
        viewModel.uiState.first{ it.signDocuments.isNotEmpty() }
    }

    @Test
    fun signCalled_ongoingSignDocumentsUpdated() = runTest {
        signDocuments.value = setOf(signDocument1)
        viewModel.uiState.first{ it.signDocuments.isNotEmpty() }
        viewModel.signDocument(signDocumentUi1)
        assertEquals(signDocumentUi1, viewModel.uiState.value.ongoingSignDocumentUi)
    }

    @Test
    fun cancelCalled_ongoingSignDocumentsUpdatedToNull() = runTest {
        signDocuments.value = setOf(signDocument1)
        viewModel.uiState.first{ it.signDocuments.isNotEmpty() }
        viewModel.signDocument(signDocumentUi1)
        assertEquals(signDocumentUi1, viewModel.uiState.value.ongoingSignDocumentUi)
        viewModel.cancelSign(signDocumentUi1)
        assertEquals(null, viewModel.uiState.value.ongoingSignDocumentUi)
    }

    @Test
    fun signCompleted_signCalled_ongoingSignDocumentsUpdatedToNull() = runTest {
        signDocumentUi1 = signDocumentUi1.copy(signState = SignDocumentUi.SignStateUi.Completed)
        every { signDocument1.signState } returns MutableStateFlow(SignDocument.SignState.Completed)
        signDocuments.value = setOf(signDocument1)
        viewModel.uiState.first{ it.signDocuments.isNotEmpty() }
        viewModel.signDocument(signDocumentUi1)
        assertEquals(null, viewModel.uiState.value.ongoingSignDocumentUi)
    }
}
