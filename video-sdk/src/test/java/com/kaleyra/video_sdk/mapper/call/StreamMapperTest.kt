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

package com.kaleyra.video_sdk.mapper.call

import android.net.Uri
import androidx.compose.runtime.MutableState
import com.kaleyra.video.conference.*
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.ParticipantMapper
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.StreamMapper.doIHaveStreams
import com.kaleyra.video_sdk.call.mapper.StreamMapper.hasAtLeastAVideoEnabled
import com.kaleyra.video_sdk.call.mapper.StreamMapper.mapToStreamsUi
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toMyStreamsUi
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StreamMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val viewMock = mockk<VideoStreamView>()

    private val uriMock = mockk<Uri>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val audioMock = mockk<Input.Audio.My>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val streamMock3 = mockk<Stream>()

    private val myStreamMock1 = mockk<Stream.Mutable>()

    private val myStreamMock2 = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val displayNameFlow = MutableStateFlow("displayName")

    private val displayImageFlow = MutableStateFlow(uriMock)

    private val streamUi1 = StreamUi(
        id = "streamId1",
        video = VideoUi(id = "videoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
        audio = AudioUi(id = "audioId", isEnabled = true),
        userInfo = UserInfo("userId", "displayName", ImmutableUri(uriMock)),
        createdAt = 9384839L
    )

    private val streamUi2 = StreamUi(
        id = "streamId2",
        video = VideoUi(id = "videoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
        audio = AudioUi(id = "audioId", isEnabled = true),
        userInfo = UserInfo("userId", "displayName", ImmutableUri(uriMock)),
        createdAt = 948384L
    )

    private val streamUi3 = StreamUi(
        id = "streamId3",
        video = VideoUi(id = "videoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
        audio = AudioUi(id = "audioId", isEnabled = true),
        userInfo = UserInfo("userId", "displayName", ImmutableUri(uriMock)),
        createdAt = 76094743L
    )

    private val myStreamUi1 = StreamUi(
        id = "myStreamId",
        video = VideoUi(id = "myVideoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
        audio = AudioUi(id = "audioId", isEnabled = true),
        userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock)),
        isMine = true,
        createdAt = 4473847383L
    )

    private val myStreamUi2 = StreamUi(
        id = "myStreamId2",
        video = VideoUi(id = "myVideoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
        audio = AudioUi(id = "audioId", isEnabled = true),
        userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock)),
        isMine = true,
        createdAt = 85729487459L
    )

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        // only needed for toCallStateUi function
        every { audioMock.speaking } returns MutableStateFlow(false)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { list } returns listOf(participantMock1, participantMock2)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMeMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock1, myStreamMock2))
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
            every { createdAt } returns 9384839L
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
            every { createdAt } returns 948384L
        }
        with(streamMock3) {
            every { id } returns "streamId3"
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
            every { createdAt } returns 76094743L
        }
        with(myStreamMock1) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
            every { audio } returns MutableStateFlow(audioMock)
            every { createdAt } returns 4473847383L
        }
        with(myStreamMock2) {
            every { id } returns "myStreamId2"
            every { video } returns MutableStateFlow(myVideoMock)
            every { audio } returns MutableStateFlow(audioMock)
            every { createdAt } returns 85729487459L
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
        with(viewMock) {
            every { zoomLevel } returns MutableStateFlow(StreamView.ZoomLevel.Fit)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun emptyParticipantsList_toStreamsUi_emptyStreamUiList() = runTest {
        every { callParticipantsMock.list } returns listOf()

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledParticipantsList_toStreamsUi_mappedStreamUiList() = runTest {
        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun allParticipantsHaveNoStreams_toStreamsUi_emptyStreamUiList() = runTest {
        every { participantMock1.streams } returns MutableStateFlow(listOf())
        every { participantMock2.streams } returns MutableStateFlow(listOf())

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun addNewParticipant_toStreamsUi_participantStreamsUiAdded() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.list } returns listOf(participantMock1)

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        val newCallParticipantsMock = mockk<CallParticipants> {
            every { me } returns participantMeMock
            every { list } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun removeParticipant_toStreamsUi_participantStreamsUiRemoved() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        val newCallParticipantsMock = mockk<CallParticipants> {
            every { me } returns participantMeMock
            every { list } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun meParticipantInParticipantsList_toStreamUi_localParticipantStreamUiIsMineTrue() = runTest {
        every { callParticipantsMock.list } returns listOf(participantMock1, participantMeMock)

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            myStreamUi1.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock))),
            myStreamUi2.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock))),
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun updateParticipantDisplayName_toStreamsUi_participantStreamUiUpdated() = runTest {
        val displayNameParticipant1 = MutableStateFlow("displayName1")
        every { participantMock1.combinedDisplayName } returns displayNameParticipant1

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        displayNameParticipant1.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayNameModified", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayNameModified", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateParticipantDisplayImage_toStreamsUi_participantStreamUiUpdated() = runTest {
        val displayImageParticipant1 = MutableStateFlow(uriMock)
        every { participantMock1.combinedDisplayImage } returns displayImageParticipant1

        val result = callMock.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        val newUriMock = mockk<Uri>()
        displayImageParticipant1.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(newUriMock))),
            streamUi2.copy(userInfo = UserInfo("userId1", "displayName1", ImmutableUri(newUriMock))),
            streamUi3.copy(userInfo = UserInfo("userId2", "displayName2", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyStreamList_toMyStreamsUi_emptyList() = runTest {
        every { participantMeMock.streams } returns MutableStateFlow(listOf())

        val result = callMock.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledStreamList_toMyStreamsUi() = runTest {
        val result = callMock.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf(
            myStreamUi1.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock))),
            myStreamUi2.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun updateMyDisplayName_toMyStreamsUi() = runTest {
        val myDisplayName = MutableStateFlow("displayName1")
        every { participantMeMock.combinedDisplayName } returns myDisplayName

        val result = callMock.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf(
            myStreamUi1.copy(userInfo = UserInfo("myUserId", "displayName1", ImmutableUri(uriMock))),
            myStreamUi2.copy(userInfo = UserInfo("myUserId", "displayName1", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        myDisplayName.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf(
            myStreamUi1.copy(userInfo = UserInfo("myUserId", "displayNameModified", ImmutableUri(uriMock))),
            myStreamUi2.copy(userInfo = UserInfo("myUserId", "displayNameModified", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateMyDisplayImage_toMyStreamsUi_mappedStreamUiUpdated() = runTest {
        val myDisplayImage = MutableStateFlow(uriMock)
        every { participantMeMock.combinedDisplayImage } returns myDisplayImage

        val result = callMock.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf(
            myStreamUi1.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock))),
            myStreamUi2.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        val newUriMock = mockk<Uri>()
        myDisplayImage.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            myStreamUi1.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(newUriMock))),
            myStreamUi2.copy(userInfo = UserInfo("myUserId", "myDisplayName", ImmutableUri(newUriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyList_mapToStreamsUi_emptyMappedList() = runTest {
        val streams = MutableStateFlow(listOf<Stream>())
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun emptyListToFilledList_mapToStreamsUi_filledMapperList() = runTest {
        val streams = MutableStateFlow(listOf<Stream>())
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)

        streams.value = listOf(streamMock1)
        val newActual = result.first()
        val newExpected = listOf(streamUi1)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun filledListToEmptyList_mapToStreamsUi_emptyMappedList() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1)
        Assert.assertEquals(expected, actual)

        streams.value = listOf()
        val newActual = result.first()
        val newExpected = listOf<StreamUi>()
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun filledList_mapToStreamsUi_filledMappedList() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun removeElementsFromList_mapToStreamsUi_mappedListWithElementsRemoved() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update streams list
        streams.value = listOf(streamMock1)
        val newActual = result.first()
        val newExpected = listOf(streamUi1)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun addElementsToList_mapToStreamsUi_mappedListWithElementsAdded() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1)
        Assert.assertEquals(expected, actual)

        // Update streams list
        streams.value = listOf(streamMock1, streamMock2, streamMock3)
        val newActual = result.first()
        val newExpected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateStreamVideo_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val modifiedStreamVideoFlow = MutableStateFlow(videoMock)
        val modifiedStreamMock = mockk<Stream> {
            every { id } returns "modifiedStreamId"
            every { this@mockk.video } returns modifiedStreamVideoFlow
            every { this@mockk.audio } returns MutableStateFlow(audioMock)
            every { this@mockk.createdAt } returns 434552L
        }
        val modifiedStreamUi = StreamUi(
            id = "modifiedStreamId",
            video = VideoUi(id = "videoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
            audio = AudioUi(id = "audioId", isEnabled = true),
            userInfo = UserInfo("userId", "displayName", ImmutableUri(uriMock)),
            createdAt = 434552L
        )

        val streams = MutableStateFlow(listOf(streamMock1, modifiedStreamMock, streamMock3))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, modifiedStreamUi, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update stream video
        val newStreamVideoMock = mockk<Input.Video.Camera>(relaxed = true) {
            every { id } returns "videoId2"
            every { this@mockk.view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(Input.Enabled.None)
        }
        modifiedStreamVideoFlow.value = newStreamVideoMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1,
            modifiedStreamUi.copy(
                video = VideoUi(
                    id = "videoId2",
                    view =  ImmutableView(viewMock),
                    zoomLevelUi = VideoUi.ZoomLevelUi.Fit,
                    isEnabled = false
                )
            ),
            streamUi3
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateStreamAudio_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val modifiedStreamAudioFlow = MutableStateFlow(audioMock)
        val modifiedStreamMock = mockk<Stream> {
            every { id } returns "modifiedStreamId"
            every { this@mockk.video } returns MutableStateFlow(videoMock)
            every { this@mockk.audio } returns modifiedStreamAudioFlow
            every { this@mockk.createdAt } returns 434552L
        }
        val modifiedStreamUi = StreamUi(
            id = "modifiedStreamId",
            video = VideoUi(id = "videoId", view = ImmutableView(viewMock), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true),
            audio = AudioUi(id = "audioId", isEnabled = true, isMutedForYou = false),
            userInfo = UserInfo("userId", "displayName", ImmutableUri(uriMock)),
            createdAt = 434552L
        )

        val streams = MutableStateFlow(listOf(streamMock1, modifiedStreamMock, streamMock3))
        val result = streams.mapToStreamsUi(
            isLocalParticipant = false,
            userId = "userId",
            displayNameFlow,
            displayImageFlow
        )
        val actual = result.first()
        val expected = listOf(streamUi1, modifiedStreamUi, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update stream audio
        val newStreamAudioMock = mockk<Input.Audio.My>(relaxed = true) {
            every { id } returns "audioId2"
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
            every { speaking } returns MutableStateFlow(false)
        }
        modifiedStreamAudioFlow.value = newStreamAudioMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1,
            modifiedStreamUi.copy(
                audio = AudioUi(id = "audioId2", isEnabled = true, isMutedForYou = false)
            ),
            streamUi3
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateUserDisplayName_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update display name
        displayNameFlow.value = "newDisplayName"
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId", "newDisplayName", ImmutableUri(uriMock))),
            streamUi2.copy(userInfo = UserInfo("userId", "newDisplayName", ImmutableUri(uriMock))),
            streamUi3.copy(userInfo = UserInfo("userId", "newDisplayName", ImmutableUri(uriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateUserDisplayImage_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(false, "userId", displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update display name
        val newUriMock = mockk<Uri>()
        displayImageFlow.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(userInfo = UserInfo("userId", "displayName", ImmutableUri(newUriMock))),
            streamUi2.copy(userInfo = UserInfo("userId", "displayName", ImmutableUri(newUriMock))),
            streamUi3.copy(userInfo = UserInfo("userId", "displayName", ImmutableUri(newUriMock)))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyList_hasAtLeastAVideoEnabled_false() = runTest {
        val flow = flowOf(listOf<StreamUi>())
        val result = flow.hasAtLeastAVideoEnabled()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun participantHasVideoEnabled_hasAtLeastAVideoEnabled_true() = runTest {
        val flow = flowOf(listOf(StreamUi(id = "streamId", userInfo = UserInfo("userId1", "username", ImmutableUri(uriMock)), video = VideoUi(id = "videoId", isEnabled = true))))
        val result = flow.hasAtLeastAVideoEnabled()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun participantHasVideoDisabled_hasAtLeastAVideoEnabled_false() = runTest {
        val flow = flowOf(listOf(StreamUi(id = "streamId", userInfo = UserInfo("userId1", "username", ImmutableUri(uriMock)), video = VideoUi(id = "videoId", isEnabled = false))))
        val result = flow.hasAtLeastAVideoEnabled()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun meHasOneStream_doIHaveStreams_true() = runTest {
        mockkObject(ParticipantMapper)
        with(ParticipantMapper) {
            every { callMock.toMe() } returns flowOf(participantMeMock)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(mockk()))
        val result = callMock.doIHaveStreams()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun meHasNoStreams_doIHaveStreams_false() = runTest {
        mockkObject(ParticipantMapper)
        with(ParticipantMapper) {
            every { callMock.toMe() } returns flowOf(participantMeMock)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf())
        val result = callMock.doIHaveStreams()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }
}
