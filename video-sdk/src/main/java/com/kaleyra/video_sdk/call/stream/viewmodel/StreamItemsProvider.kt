package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal interface StreamItemsProvider {

    val streams: List<StreamUi>

    fun buildStreamItems(): List<StreamItem>
}