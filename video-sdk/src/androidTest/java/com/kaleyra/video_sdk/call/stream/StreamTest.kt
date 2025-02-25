package com.kaleyra.video_sdk.call.stream

import android.graphics.Matrix
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.view.core.RenderingDebouceMillis
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_utils.MutableSharedStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class StreamTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val context = instrumentation.context

    private var streamView by mutableStateOf<ImmutableView<VideoStreamView>?>(null)

    private var showStreamView by mutableStateOf(true)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Stream(
                streamView = streamView,
                userInfo = UserInfo("userId", "username", ImmutableUri()),
                isMine = false,
                isSpeaking = false,
                showStreamView = showStreamView,
            )
        }
    }

    @After
    fun tearDown() {
        streamView = null
        showStreamView = true
    }

    @Test
    fun streamViewIsDisplayed() {
        instrumentation.runOnMainSync { streamView = ImmutableView(VideoStreamView(context)) }
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun showStreamViewTrue_avatarIsNotDisplayed() {
        instrumentation.runOnMainSync { streamView = ImmutableView(VideoStreamView(context)) }
        showStreamView = true
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
    }

    @Test
    fun showStreamViewFalse_avatarIsDisplayed() {
        showStreamView = false
        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }

    @Test
    fun testAvatarIsForceDisplayedIfStreamViewIsNotRendering() {
        val renderingFlow = MutableSharedStateFlow<StreamView.State>(StreamView.State.NotRendering)
        showStreamView = true
        instrumentation.runOnMainSync {
            val videoStreamView = VideoStreamView(context)
            val property =
                videoStreamView::class.memberProperties.first { it.name == "internalState" }
            val field = property.javaField!!
            field.isAccessible = true
            field.set(videoStreamView, renderingFlow)
            streamView = ImmutableView(videoStreamView)
        }
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(RenderingDebouceMillis)
        composeTestRule.onNodeWithText("U").assertIsDisplayed()

        renderingFlow.value = StreamView.State.Rendering(
            definition = MutableStateFlow(Input.Video.Quality.Definition.HD),
            matrix = MutableStateFlow(Matrix())
        )
        composeTestRule.mainClock.advanceTimeBy(RenderingDebouceMillis)
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
    }

    @Test
    fun testAvatarIsNotForceDisplayedIfStreamViewIsRendering() {
        showStreamView = true
        instrumentation.runOnMainSync {
            val videoStreamView = VideoStreamView(context)
            val property =
                videoStreamView::class.memberProperties.first { it.name == "internalState" }
            val field = property.javaField!!
            field.isAccessible = true
            field.set(
                videoStreamView,
                MutableSharedStateFlow(
                    StreamView.State.Rendering(
                        definition = MutableStateFlow(Input.Video.Quality.Definition.HD),
                        matrix = MutableStateFlow(Matrix())
                    )
                )
            )
            streamView = ImmutableView(videoStreamView)
        }
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(RenderingDebouceMillis)
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
    }
}