package com.kaleyra.video_sdk.call.stream.layoutsystem.model

/**
 * Represents the different states a [StreamItem] can be in.
 *
 * This sealed class defines the possible states for a stream item, allowing for
 * exhaustive `when` expressions when handling different stream item states.
 *
 * The states are categorized into:
 * - [Standard]: The default state of a stream item.
 * - [Featured]: States that highlight a stream item, such as pinned or fullscreen.
 */
sealed class StreamItemState {

    /**
     * Represents the standard, default state of a [StreamItem].
     *
     * In this state, the stream item is displayed normally without any special
     * highlighting or emphasis.
     */
    data object Standard : StreamItemState()

    /**
     * Represents states that highlight or feature a [StreamItem].
     *
     * This sealed class provides a hierarchy for different featured states,
     * such as [Pinned] and [Fullscreen].
     */
    sealed class Featured : StreamItemState() {

        /**
         * A companion object to allow to use the Featured class as a state.
         */
        companion object Companion : Featured()

        /**
         * Represents the pinned state of a [StreamItem].
         *
         * In this state, the stream item is highlighted and remains visible,
         * typically at the top or in a prominent position.
         */
        data object Pinned : Featured()

        /**
         * Represents the fullscreen state of a [StreamItem].
         *
         * In this state, the stream item is displayed in fullscreen mode,
         * taking up the entire screen or a significant portion of it.
         */
        data object Fullscreen : Featured()
    }
}