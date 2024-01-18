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

/**
 * Immutable Set
 * @param out T type T
 * @property value Set<T> input set of type T
 * @constructor
 */
@Immutable
data class ImmutableSet<out T>(val value: Set<T> = setOf()) {

    /**
     * Returns the count of the element in the immutable set
     * @return Int
     */
    fun count() = value.count()
}
