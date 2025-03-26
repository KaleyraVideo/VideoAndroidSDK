package com.kaleyra.video_sdk.ui.call.stream

import android.graphics.Matrix
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.view.core.RenderingDebounceMillis
import com.kaleyra.video_sdk.call.stream.view.core.StreamPreview
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_utils.MutableSharedStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@RunWith(RobolectricTestRunner::class)
class StreamPreviewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val context = instrumentation.context

    private var streamView by mutableStateOf<ImmutableView<VideoStreamView>?>(null)

    private var showStreamView by mutableStateOf(true)

    private var userInfos by mutableStateOf(
        ImmutableList(listOf(UserInfo("userId", "username", ImmutableUri())))
    )

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamPreview(
                streamView = streamView,
                showStreamView = showStreamView,
                userInfos = userInfos,
                avatarSize = 40.dp
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
    fun showStreamViewFalse_multiAvatarIsDisplayed() {
        userInfos = ImmutableList(
            listOf(
                UserInfo("userId1", "Alice", ImmutableUri()),
                UserInfo("userId2", "John", ImmutableUri()),
                UserInfo("userId3", "Mario", ImmutableUri())
            )
        )
        showStreamView = false
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
    }

    @Test
    fun showStreamViewFalse_multiAvatarOverflowIsDisplayed() {
        userInfos = ImmutableList(
            listOf(
                UserInfo("userId1", "Alice", ImmutableUri()),
                UserInfo("userId2", "John", ImmutableUri()),
                UserInfo("userId3", "Mario", ImmutableUri()),
                UserInfo("userId4", "Oliver", ImmutableUri()),
                UserInfo("userId5", "Federico", ImmutableUri())
            )
        )
        showStreamView = false
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("+3").assertIsDisplayed()
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
        composeTestRule.mainClock.advanceTimeBy(RenderingDebounceMillis)
        composeTestRule.onNodeWithText("U").assertIsDisplayed()

        renderingFlow.value = StreamView.State.Rendering(
            definition = MutableStateFlow(Input.Video.Quality.Definition.HD),
            matrix = MutableStateFlow(Matrix())
        )
        composeTestRule.mainClock.advanceTimeBy(RenderingDebounceMillis)
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
        composeTestRule.mainClock.advanceTimeBy(RenderingDebounceMillis)
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
    }

}