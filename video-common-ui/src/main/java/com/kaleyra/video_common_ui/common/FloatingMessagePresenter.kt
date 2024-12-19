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

interface FloatingMessagePresenter {
    val floatingMessages: SharedFlow<FloatingMessage?>

    fun present(message: FloatingMessage)

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