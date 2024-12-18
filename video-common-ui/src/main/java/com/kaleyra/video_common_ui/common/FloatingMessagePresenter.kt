package com.kaleyra.video_common_ui.common

import com.kaleyra.video_common_ui.model.FloatingMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import kotlin.coroutines.resume

interface FloatingMessagePresenter {
    val floatingMessages: SharedFlow<WeakReference<FloatingMessage?>>

    fun present(floatingMessage: FloatingMessage)
}

internal class CallUIFloatingMessagePresenter(val scope: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())) : FloatingMessagePresenter {

    private val _floatingMessages = MutableSharedFlow<WeakReference<FloatingMessage?>>(replay = 1)
    override val floatingMessages: SharedFlow<WeakReference<FloatingMessage?>> = _floatingMessages.asSharedFlow()

    override fun present(floatingMessage: FloatingMessage) {
        println("presenting: ${floatingMessage.body}")
        val weakFloatingMessage = WeakReference(floatingMessage)

        _floatingMessages.tryEmit(weakFloatingMessage)

        weakFloatingMessage.get()?.onDismissed = {
            scope.launch { dismissMessage(weakFloatingMessage.get()) }
        }

        val updateFloatingMessage = {
            println("updateFloatingMessage")
            scope.launch { _floatingMessages.emit(WeakReference(null)) }
            scope.launch { _floatingMessages.emit(weakFloatingMessage) }
        }
        weakFloatingMessage.get()?.onBodyUpdated = { updateFloatingMessage() }
        weakFloatingMessage.get()?.onButtonUpdated = { updateFloatingMessage() }

        _floatingMessages.tryEmit(weakFloatingMessage)
    }

    private suspend fun dismissMessage(floatingMessage: FloatingMessage?): Boolean = suspendCancellableCoroutine { continuation ->
        floatingMessage ?: return@suspendCancellableCoroutine
        _floatingMessages.replayCache.firstOrNull()?.takeIf { it.get()?.id == floatingMessage.id }?.let {
            scope.launch { _floatingMessages.emit(WeakReference(null)) }
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }
        continuation.resume(false)
    }
}