package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

/**
 * Represents a preview of a user in the "More Streams" item.
 *
 * This data class holds the essential information needed to display a user
 * in a list of users who are also streaming. It includes the user's unique
 * identifier, their username, and an optional avatar image.
 *
 * @property id The unique identifier of the user. This is a non-nullable String.
 * @property username The username of the user. This is a non-nullable String.
 * @property avatar An optional URI representing the user's avatar image.
 *                   If the user does not have an avatar, this will be null.
 *                   The URI is wrapped in an [ImmutableUri] to ensure immutability.
 */
@Immutable
data class MoreStreamsUserPreview(
    val id: String,
    val username: String,
    val avatar: ImmutableUri?,
)