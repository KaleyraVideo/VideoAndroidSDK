package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.mapper.VideoMapper.mapParticipantsToVideos
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class VideoMapperTest {

    private val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val streamMock1 = mockk<Stream>(relaxed = true)

    private val streamMock2 = mockk<Stream>(relaxed = true)

    private val videoMock1 = mockk<Input.Video.Camera>(relaxed = true)

    private val videoMock2 = mockk<Input.Video.Screen>(relaxed = true)

    @Before
    fun setUp() {
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1))
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock2))
        }
        with(streamMock1) {
            every { id } returns "id1"
            every { video } returns MutableStateFlow(videoMock1)
        }
        with(streamMock2) {
            every { id } returns "id2"
            every { video } returns MutableStateFlow(videoMock2)
        }
    }

    @Test
    fun emptyParticipantList_mapParticipantsToVideos_emptyList() = runTest {
        val participants = listOf(participantMock1, participantMock2)
        val result = flowOf(participants).mapParticipantsToVideos().first()
        assertEquals(listOf(videoMock1, videoMock2), result)
    }

    @Test
    fun participantsList_mapParticipantsToVideos_videoList() = runTest {
        val participants = listOf(participantMock1, participantMock2)
        val result = flowOf(participants).mapParticipantsToVideos().first()
        assertEquals(listOf(videoMock1, videoMock2), result)
    }
}