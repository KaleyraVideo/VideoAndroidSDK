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

package com.kaleyra.video_common_ui.theme

import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.KaleyraVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Utility functions for the Company Theme
 */
object CompanyThemeManager {

    /**
     * Mapper function to convert Company to a flow containing its CompanyUI.Theme representation
     */
    val Company.combinedTheme: Flow<CompanyUI.Theme>
        get() = theme.mapToUI(uiTheme = KaleyraVideo.theme)

    private fun Flow<Company.Theme>.mapToUI(uiTheme: CompanyUI.Theme?): Flow<CompanyUI.Theme> =
        flow {
            uiTheme?.let { emit(it) }
            map { theme ->
                val combinedTheme = uiTheme ?: CompanyUI.Theme()
                combinedTheme.copy(
                    day = CompanyUI.Theme.Style(
                        logo = combinedTheme.day.logo ?: theme.day.logo,
                        colors = combinedTheme.day.colors ?: theme.day.colors?.mapToCompanyUIColors()
                    ),
                    night = CompanyUI.Theme.Style(
                        logo = combinedTheme.night.logo ?: theme.night.logo,
                        colors = combinedTheme.night.colors ?: theme.night.colors?.mapToCompanyUIColors()
                    )
                )
            }.collect(this)
        }

    private fun Company.Theme.Style.Colors.mapToCompanyUIColors(): CompanyUI.Theme.Colors? {
        return takeIf { colors -> colors is Company.Theme.Style.Colors.Seed }?.let { colors ->
            CompanyUI.Theme.Colors.Seed((colors as Company.Theme.Style.Colors.Seed).color)
        }
    }
}
