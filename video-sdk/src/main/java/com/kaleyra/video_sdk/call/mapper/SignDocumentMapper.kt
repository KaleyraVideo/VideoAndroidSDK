package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

@OptIn(ExperimentalCoroutinesApi::class)
internal object SignDocumentMapper {

    fun CallUI.toSignDocumentUi(): Flow<Set<SignDocumentUi>> {
        val signDocuments = hashMapOf<String, SignDocumentUi>()

        return sharedFolder.signDocuments
            .flatMapLatest {
                if (it.isEmpty()) flowOf(setOf())
                else it
                    .map { it.toSignDocumentUi() }
                    .merge()
                    .transform { signDocumentUI ->
                        signDocuments[signDocumentUI.id] = signDocumentUI
                        val values = signDocuments.values
                        emit(values.toSet())
                    }
            }.distinctUntilChanged()
    }

    fun SignDocument.toSignDocumentUi(): Flow<SignDocumentUi> = combine(
        sender.combinedDisplayName,
        signState.map { it.toSignStateUi() }
    ) { displayName, signState ->
        SignDocumentUi(
            id = id,
            name = name,
            uri = ImmutableUri(uri),
            sender = displayName ?: sender.userId,
            creationTime = creationTime,
            signView = ImmutableView(signView),
            signState = signState
        )
    }.distinctUntilChanged()

    fun Flow<Set<SignDocument>>.toSignDocumentsUi(): Flow<Set<SignDocumentUi>> = map {
        it.map {
            SignDocumentUi(
                id = it.id,
                name = it.name,
                uri = ImmutableUri(it.uri),
                sender = it.sender.userId,
                creationTime = it.creationTime,
                signView = ImmutableView(it.signView),
                signState = it.signState.value.toSignStateUi()
            )
        }.toSet()
    }

    fun SignDocument.SignState.toSignStateUi() = when (this) {
        SignDocument.SignState.Pending -> SignDocumentUi.SignStateUi.Pending
        SignDocument.SignState.Signing -> SignDocumentUi.SignStateUi.Signing
        SignDocument.SignState.Completed -> SignDocumentUi.SignStateUi.Completed
        else -> SignDocumentUi.SignStateUi.Failed((this@toSignStateUi as SignDocument.SignState.Error).throwable)
    }
}
