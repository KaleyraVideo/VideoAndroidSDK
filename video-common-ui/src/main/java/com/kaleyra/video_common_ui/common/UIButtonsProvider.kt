package com.kaleyra.video_common_ui.common

import kotlinx.coroutines.flow.StateFlow

interface UIButton

interface UIButtonsProvider<T : UIButton> {
    /**
     * Current Call UI buttons
     */
    val buttons: StateFlow<Set<T>>

    /**
     * Buttons Provider represents an optional callback that is invoked when ui buttons hierarchy is about to being displayed on ths UI.
     * This callback allows to return an updated set of UI Buttons if some call button must be added or removed in the integration use case.
     */
    var buttonsProvider: ((MutableSet<T>) -> Set<T>)?
}