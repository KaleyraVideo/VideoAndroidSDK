package com.kaleyra.video_common_ui.theme.resource

import android.net.Uri

/**
 * Represents a Uniform Resource Identifier (URI) resource that can have different values for light and dark themes.
 *
 * This class is typically used to store URIs for resources like images or logos that need to adapt to different theme settings.
 *
 * @property light The URI to use in light theme.
 * @property dark The URI to use in dark theme.*/
data class URIResource(val light: Uri, val dark: Uri) {
    /**
     * Creates a URIResource with the same URI for both light and dark themes.
     *
     * @param uri The URI to use for both themes.
     */
    constructor(uri: Uri) : this(uri, uri)
}