package com.kaleyra.video_common_ui.common

import com.kaleyra.video.utils.logger.PHONE_BOX
import com.kaleyra.video_common_ui.model.FloatingMessage
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Represents a presenter responsible for managing the presentation of floating messages on call UI.
 *
 * This interface defines the contract for displaying and dismissing floating messages
 * within the application's user interface.
 */
interface FloatingMessagePresenter {

    /**
     * A [SharedFlow] emitting the currently presented floating message, or `null` if no message is presented.
     *
     * Observers of this flow can be notified about changes in the presented message,
     * allowing them to update the UI accordingly.
     */
    val floatingMessages: SharedFlow<FloatingMessage?>

    /**
     * Presents a floating message to the user.
     *
     * @param message The [FloatingMessage] to be presented.
     */
    fun present(message: FloatingMessage)

    /**
     * Dismisses a floating message.
     *
     * @param message The [FloatingMessage] to be dismissed.
     */
    fun dismiss(message: FloatingMessage)
}

internal class CallUIFloatingMessagePresenter(
    val scope: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()),
    val isGlassesSDK: Boolean = false,
    val logger: PriorityLogger? = null,
) : FloatingMessagePresenter {

    private val _floatingMessages = MutableSharedFlow<FloatingMessage?>(replay = 1)
    override val floatingMessages: SharedFlow<FloatingMessage?> = _floatingMessages.asSharedFlow()

    override fun present(message: FloatingMessage) {
        if (isGlassesSDK) {
            logger?.warn(
                logTarget = PHONE_BOX,
                message = "Floating messages are not supported yet on Kaleyra Video Glasses SDK.\n" +
                    "Please use Kaleyra Video SDK (phone and tablet UIs) in order to display floating messages.")
            return
        }
        _floatingMessages.tryEmit(message)

        message.onDismissed = {
            scope.launch { dismissMessage(message) }
        }

        val updateFloatingMessage = {
            scope.launch { _floatingMessages.emit(null) }
            scope.launch { _floatingMessages.emit(message) }
        }
        message.onBodyUpdated = { updateFloatingMessage() }
        message.onButtonUpdated = { updateFloatingMessage() }

        _floatingMessages.tryEmit(message)
    }

    override fun dismiss(message: FloatingMessage) {
        if (isGlassesSDK) {
            logger?.warn(
                logTarget = PHONE_BOX,
                message = "Floating messages are not supported yet on Kaleyra Video Glasses SDK.\n" +
                    "Please use Kaleyra Video SDK (phone and tablet UIs) in order to display floating messages.")
            return
        }
        message.dismiss()
    }

    private suspend fun dismissMessage(floatingMessage: FloatingMessage?): Boolean = suspendCancellableCoroutine { continuation ->
        floatingMessage ?: return@suspendCancellableCoroutine
        _floatingMessages.replayCache.firstOrNull()?.takeIf { it.id == floatingMessage.id }?.let {
            scope.launch { _floatingMessages.emit(null) }
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }
        continuation.resume(false)
    }
}