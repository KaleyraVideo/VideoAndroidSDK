package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.flow.Flow

internal interface StreamLayoutInputs {
    val layoutStreams: Flow<List<StreamUi>>

    val layoutConstraints: Flow<StreamLayoutConstraints>

    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    val featuredStreamItemsProvider: FeaturedStreamItemsProvider
}

internal interface StreamLayoutOutput {
    val streamItems: Flow<List<StreamItem>>
}

internal interface StreamLayout: StreamLayoutInputs, StreamLayoutOutput