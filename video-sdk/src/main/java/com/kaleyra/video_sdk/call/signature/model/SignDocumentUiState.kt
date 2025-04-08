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

package com.kaleyra.video_sdk.call.signature.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Immutable data class representing the overall UI state related to document signing.
 *
 * This class encapsulates the complete state for the document signing feature,
 * including a list of documents to be signed and the currently active document
 * being signed (if any).  Its immutability ensures thread safety and predictable
 * behavior in the user interface.
 *
 * @property signDocuments An [ImmutableList] containing [SignDocumentUi] objects,
 *                          each representing a document that requires signing.
 *                          Defaults to an empty list if no documents are initially
 *                          available.
 * @property ongoingSignDocumentUi An optional [SignDocumentUi] object representing the
 *                                 document that is currently in the process of
 *                                 being signed by the user.  If null, no document
 *                                 is actively being signed.
 */
@Immutable
internal data class SignDocumentUiState(
    val signDocuments: ImmutableList<SignDocumentUi> = ImmutableList(listOf()),
    val ongoingSignDocumentUi: SignDocumentUi? = null
) : UiState

