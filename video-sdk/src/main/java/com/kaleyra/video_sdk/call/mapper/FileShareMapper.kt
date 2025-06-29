/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal object FileShareMapper {

    fun CallUI.toSharedFilesUi(): Flow<Set<SharedFileUi>> {
        val sharedFiles = hashMapOf<String, SharedFileUi>()

        return combine(sharedFolder.files, this.toMe()) { f, m -> Pair(f, m) }
            .flatMapLatest { (files, me) ->
                if (files.isEmpty()) flowOf(setOf())
                else files
                    .map { it.mapToSharedFileUi(me.userId) }
                    .merge()
                    .transform { sharedFileUi ->
                        sharedFiles[sharedFileUi.id] = sharedFileUi

                        val values = sharedFiles.values
                        if (values.size == files.size) {
                            emit(values.filter { it.state !is SharedFileUi.State.Cancelled }.toSet())
                        }
                    }
            }.distinctUntilChanged()
    }

    fun Call.toOtherFilesCreationTimes(): Flow<List<Long>> {
        return combine(
            toMe(),
            sharedFolder.files
        ) { me, files ->
            files
                .filter { it.sender.userId != me.userId }
                .map { it.creationTime }
        }
    }

    fun Call.toMySignDocumentsCreationTimes(): Flow<List<Long>> {
        return combine(
            toMe(),
            sharedFolder.signDocuments
        ) { me, signDocuments ->
            signDocuments
                .filter { it.sender.userId != me.userId }
                .map { it.creationTime }
        }
    }

    fun Call.toMySignDocuments(): Flow<List<SignDocument>> {
        return combine(
            toMe(),
            sharedFolder.signDocuments
        ) { me, signDocuments ->
            signDocuments.filter { it.sender.userId != me.userId }
        }
    }

    private fun SharedFileUi.isCancelledUpload(): Boolean =
        isMine && state == SharedFileUi.State.Cancelled

    private fun Call.toMe(): Flow<CallParticipant.Me> =
        this.participants
            .mapNotNull { it.me }
            .distinctUntilChanged()

    fun SharedFile.mapToSharedFileUi(myUserId: String): Flow<SharedFileUi> {
        val sharedFile = this@mapToSharedFileUi
        val uri = ImmutableUri(sharedFile.uri)
        return combine(
            sharedFile.sender.combinedDisplayName,
            sharedFile.state.map { it.mapToSharedFileUiState(sharedFile.size) }
        ) { displayName, state ->
            SharedFileUi(
                id = sharedFile.id,
                name = sharedFile.name,
                uri = uri,
                size = sharedFile.size,
                sender = displayName ?: sharedFile.sender.userId,
                time = sharedFile.creationTime,
                state = state,
                isMine = sharedFile.sender.userId == myUserId
            )
        }.distinctUntilChanged()
    }

    fun SharedFile.State.mapToSharedFileUiState(fileSize: Long): SharedFileUi.State {
        return when (this) {
            SharedFile.State.Available -> SharedFileUi.State.Available
            SharedFile.State.Cancelled -> SharedFileUi.State.Cancelled
            is SharedFile.State.Error -> SharedFileUi.State.Error
            is SharedFile.State.InProgress -> SharedFileUi.State.InProgress((this.progress / fileSize.toFloat()).coerceIn(0.0f, 1.0f))
            SharedFile.State.Pending -> SharedFileUi.State.Pending
            is SharedFile.State.Success -> SharedFileUi.State.Success(ImmutableUri(this.uri))
        }
    }
}