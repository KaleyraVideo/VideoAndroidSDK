package com.kaleyra.video_sdk.call.virtualbackground.state

import com.kaleyra.video.conference.Call
import com.kaleyra.video_utils.dispatcher.DispatcherProvider
import com.kaleyra.video_utils.dispatcher.StandardDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Interface for managing the global virtual background enabled state.
 */
interface VirtualBackgroundStateManager {

    /**
     * A [StateFlow] indicating whether any virtual background effect (blur or image) is currently enabled.
     */
    val isVirtualBackgroundEnabled: StateFlow<Boolean>

    /**
     * Sets the global virtual background enabled state.
     * @param isEnabled True if a virtual background is enabled, false otherwise.
     */
    fun setVirtualBackgroundEnabled(isEnabled: Boolean)

    /**
     * Updates the active [Call] instance being observed by the manager.
     *
     * This method should be called by components (e.g., ViewModels) that acquire
     * a [Call] instance, allowing the manager to monitor its state (e.g., for
     * disconnection events that might disable virtual backgrounds).
     *
     * @param call The current active [Call] instance.
     */
    suspend fun updateActiveCall(call: Call)
}

/**
 * Implementation of [VirtualBackgroundStateManager].
 * This class should ideally be a singleton provided via Dependency Injection.
 */
internal class VirtualBackgroundStateManagerImpl private constructor(dispatcherProvider: DispatcherProvider = StandardDispatchers) :
    VirtualBackgroundStateManager {
    private val _isVirtualBackgroundEnabled = MutableStateFlow(false) // Initial state: virtual background is not enabled
    override val isVirtualBackgroundEnabled: StateFlow<Boolean> = _isVirtualBackgroundEnabled.asStateFlow()

    private val _activeCallFlow = MutableSharedFlow<Call>(replay = 1)

    init {
        CoroutineScope(dispatcherProvider.main).launch {
            _activeCallFlow.collectLatest { call ->
                coroutineScope {
                    call.state.collect { state ->
                        if (state is Call.State.Disconnected.Ended) {
                            _isVirtualBackgroundEnabled.value = false
                        }
                    }
                }
            }
        }
    }

    override fun setVirtualBackgroundEnabled(isEnabled: Boolean) {
        _isVirtualBackgroundEnabled.value = isEnabled
    }

    override suspend fun updateActiveCall(call: Call) {
        _activeCallFlow.emit(call) // Emit the new active Call instance to the internal flow
    }

    // Companion object to hold the single instance and provide the getInstance method
    companion object {
        @Volatile
        private var INSTANCE: VirtualBackgroundStateManagerImpl? = null

        /**
         * Returns the singleton instance of [VirtualBackgroundStateManagerImpl].
         * Thread-safe double-checked locking ensures only one instance is created.
         */
        fun getInstance(): VirtualBackgroundStateManagerImpl {
            return INSTANCE
                ?: synchronized(this) { // First check (outside sync) and then synchronized block
                    INSTANCE
                        ?: VirtualBackgroundStateManagerImpl(StandardDispatchers).also { // Second check (inside sync)
                            INSTANCE = it // Initialize and assign
                        }
                }
        }

        /**
         * Creates a new, isolated instance of [VirtualBackgroundStateManagerImpl] for testing.
         * This bypasses the singleton logic and allows for clean test isolation.
         *
         * @param dispatcherProvider The [DispatcherProvider] to use for testing (e.g., a TestDispatcherProvider).
         * @return A new instance of [VirtualBackgroundStateManagerImpl].
         */
        internal fun createForTesting(dispatcherProvider: DispatcherProvider = StandardDispatchers): VirtualBackgroundStateManagerImpl {
            return VirtualBackgroundStateManagerImpl(dispatcherProvider)
        }
    }
}