package com.kaleyra.video_common_ui.common

import com.kaleyra.video_common_ui.model.FloatingMessage
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

    fun present(floatingMessage: FloatingMessage)
}

internal class CallUIFloatingMessagePresenter(val scope: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())) : FloatingMessagePresenter {

    private val _floatingMessages = MutableSharedFlow<FloatingMessage?>(replay = 1)
    override val floatingMessages: SharedFlow<FloatingMessage?> = _floatingMessages.asSharedFlow()

    override fun present(floatingMessage: FloatingMessage) {
        _floatingMessages.tryEmit(floatingMessage)

        floatingMessage.onDismissed = {
            scope.launch { dismissMessage(floatingMessage) }
        }

        val updateFloatingMessage = {
            scope.launch { _floatingMessages.emit(null) }
            scope.launch { _floatingMessages.emit(floatingMessage) }
        }
        floatingMessage.onBodyUpdated = { updateFloatingMessage() }
        floatingMessage.onButtonUpdated = { updateFloatingMessage() }

        _floatingMessages.tryEmit(floatingMessage)
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