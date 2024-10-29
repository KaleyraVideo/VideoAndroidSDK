package com.kaleyra.video_sdk.call.brandlogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.call.brandlogo.model.BrandLogoState
import com.kaleyra.video_sdk.call.brandlogo.model.Logo
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrandLogoViewModel(configure: suspend () -> Configuration) : BaseViewModel<BrandLogoState>(configure) {
    override fun initialState() = BrandLogoState()

    init {
        viewModelScope.launch {
            val company = company.first()
            val call = call.first()
            company.combinedTheme.combine(call.toCallStateUi()) { theme, callStateUi -> theme to callStateUi }
                .onEach {
                    val theme = it.first
                    val callStateUi = it.second
                    _uiState.update { uiState ->
                        uiState.copy(
                            logo = Logo(theme.day.logo, theme.night.logo),
                            callStateUi = callStateUi
                        )
                    }
                }.launchIn(this)
        }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrandLogoViewModel(configure) as T
                }
            }
    }
}
