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

package com.kaleyra.video_sdk.common.immutablecollections

import androidx.compose.runtime.Immutable

// Needed for compose stability to avoid recomposition
// Tried kotlinx-collections-immutable but they were not working properly
/**
 * Immutable list
 * @param out T type T
 * @property value List<T> input list
 * @constructor
 */
@Immutable
data class ImmutableList<out T>(val value: List<T> = listOf()) {

    /**
     * Returns the element at provided index in immutable list
     * @param index Int index of the requested item
     * @return T?
     */
    fun getOrNull(index: Int) = value.getOrNull(index)

    /**
     * Returns the count of the elements in the immutable list
     * @return Int
     */
    fun count() = value.count()

    /**
     * Returns true if the list is empty, false otherwise
     * @return Boolean
     */
    fun isEmpty() = value.isEmpty()

    /**
     * Returns true if the list is not empty, false otherwise
     * @return Boolean
     */
    fun isNotEmpty() = value.isNotEmpty()
}

internal fun <T> List<T>.toImmutableList() = ImmutableList(this)
