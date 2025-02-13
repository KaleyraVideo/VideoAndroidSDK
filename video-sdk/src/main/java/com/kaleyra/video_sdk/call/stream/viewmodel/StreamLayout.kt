package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.flow.Flow

/**
 * `StreamLayoutInputs` defines the input dependencies required by a stream layout.
 *
 * This interface specifies the data sources and providers that a stream layout needs
 * to function correctly. It includes information about the available streams, layout
 * constraints, and providers for generating stream items in different layouts.
 */
internal interface StreamLayoutInputs {
    /**
     * A flow of the list of available streams (`StreamUi`).
     *
     * This flow emits updates whenever the list of available streams changes.
     */
    val layoutStreams: Flow<List<StreamUi>>

    /**
     * A flow of the constraints for the stream layout (`StreamLayoutConstraints`).
     *
     * This flow emits updates whenever the layout constraints change, such as the
     * maximum number of streams allowed in different layouts.
     */
    val layoutConstraints: Flow<StreamLayoutConstraints>

    /**
     * A provider for building stream items in a mosaic layout (`MosaicStreamItemsProvider`).
     *
     * This provider is responsible for generating the `StreamItem`s that are displayed
     * when the layout is in mosaic mode.
     */
    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    /**
     * A provider for building stream items in a featured layout (`FeaturedStreamItemsProvider`).
     *
     * This provider is responsible for generating the `StreamItem`s that are displayed
     * when the layout is in a featured mode, such as when streams are pinned or in fullscreen.
     */
    val featuredStreamItemsProvider: FeaturedStreamItemsProvider
}

/**
 * `StreamLayoutOutput` defines the output provided by a stream layout.
 *
 * This interface specifies the data that a stream layout produces, which is the
 * list of `StreamItem`s to be displayed.
 */
internal interface StreamLayoutOutput {
    /**
     * A flow of the list of stream items (`StreamItem`).
     *
     * This flow emits updates whenever the list of stream items changes, reflecting
     * changes in the input data or layout constraints.
     */
    val streamItems: Flow<List<StreamItem>>
}

/**
 * `StreamLayout` combines the input and output interfaces for a stream layout.
 *
 * This interface represents a complete stream layout, encompassing both the
 * input dependencies (`StreamLayoutInputs`) and the output data (`StreamLayoutOutput`).
 *
 * Classes implementing this interface are responsible for managing the stream layout
 * based on the provided inputs and producing the corresponding stream items.
 */
internal interface StreamLayout : StreamLayoutInputs, StreamLayoutOutput