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

package com.kaleyra.video_sdk

import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.MediumSizeHeight
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.MediumSizeWidth
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeDevice
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeHeight
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeWidth
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigurationExtensionsTest {

    @Test
    fun testIsAtLeastMediumSizeHeight() {
        assertEquals(true, MediumSizeHeight.isAtLeastMediumSizeHeight())
        assertEquals(true, (MediumSizeHeight + 50.dp).isAtLeastMediumSizeHeight())
        assertEquals(false, (MediumSizeHeight - 50.dp).isAtLeastMediumSizeHeight())
    }

    @Test
    fun testIsAtLeastMediumSizeWeight() {
        assertEquals(true, MediumSizeWidth.isAtLeastMediumSizeWidth())
        assertEquals(true, (MediumSizeWidth + 50.dp).isAtLeastMediumSizeWidth())
        assertEquals(false, (MediumSizeWidth - 50.dp).isAtLeastMediumSizeWidth())
    }

    @Test
    fun testIsAtLeastMediumSizeDevice() {
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth, MediumSizeHeight))
        assertEquals(false, isAtLeastMediumSizeDevice(MediumSizeWidth - 50.dp, MediumSizeHeight))
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth + 50.dp, MediumSizeHeight))
        assertEquals(false, isAtLeastMediumSizeDevice(MediumSizeWidth, MediumSizeHeight - 50.dp))
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth, MediumSizeHeight + 50.dp))
        assertEquals(false, isAtLeastMediumSizeDevice(MediumSizeWidth - 50.dp, MediumSizeHeight - 50.dp))
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth + 50.dp, MediumSizeHeight + 50.dp))
    }
}