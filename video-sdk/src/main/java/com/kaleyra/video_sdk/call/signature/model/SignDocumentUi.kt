package com.kaleyra.video_sdk.call.signature.model

import android.view.View
import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Immutable data class representing the UI state for a document signing process.
 *
 * This class holds all the necessary information to display and manage the signing
 * of a document within the application's user interface.  It is designed to be
 * immutable to ensure data consistency and thread safety within UI rendering.
 *
 * @property id A unique identifier for the document to be signed.
 * @property name The user-friendly name of the document.
 * @property uri An [ImmutableUri] representing the location of the document.
 * @property sender The identifier (e.g., name or email) of the document's sender.
 * @property creationTime The timestamp (in milliseconds) indicating when the document
 *                         was created or received.
 * @property signView An optional [ImmutableView] containing a custom view for
 *                    displaying signature-related information or controls.
 *                    If null, a default signature view might be used.
 * @property signState The current signing state, represented by a [SignStateUi] object.
 */
@Immutable
data class SignDocumentUi(
    val id: String,
    val name: String,
    val uri: ImmutableUri,
    val sender: String,
    val creationTime: Long,
    val signView: ImmutableView<View>? = null,
    val signState: SignStateUi
) : UiState {

    /**
     * Sealed class representing the possible states of a document signing process.
     *
     * This sealed class provides a type-safe and exhaustive way to represent the
     * different stages a document goes through during signing, from pending to
     * completion or failure.
     */
    @Immutable
    sealed class SignStateUi {

        /**
         * Indicates that the document is waiting to be signed.
         *
         * This is typically the initial state of a document that requires a signature.
         */
        data object Pending: SignStateUi()

        /**
         * Indicates that the document is currently being signed.
         *
         * This state might be active while the user is interacting with a signature
         * input or while a signature request is being processed.
         */
        data object Signing: SignStateUi()

        /**
         * Indicates that the document has been successfully signed.
         *
         * This is the final state when the signing process is completed without errors.
         */
        data object Completed: SignStateUi()

        /**
         * Indicates that an error occurred during the signing process.
         *
         * @property error A [Throwable] object representing the specific error that
         *                  caused the signing to fail. This allows for detailed
         *                  error reporting and handling.
         */
        data class Failed(val error: Throwable): SignStateUi()
    }
}
