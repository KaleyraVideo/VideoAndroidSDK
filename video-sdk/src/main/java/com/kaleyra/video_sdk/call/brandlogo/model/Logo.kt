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

package com.kaleyra.video_sdk.call.brandlogo.model

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Logo image representation
 * @property light Uri? optional uri for day style
 * @property dark Uri? optional uri for dark style
 * @constructor
 */
@Immutable
data class Logo(
    val light: Uri? = null,
    val dark: Uri? = null
)