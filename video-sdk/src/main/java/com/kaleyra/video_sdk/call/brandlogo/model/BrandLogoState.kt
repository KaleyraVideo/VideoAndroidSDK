package com.kaleyra.video_sdk.call.brandlogo.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.uistate.UiState

@Stable
data class BrandLogoState(
    val logo: Logo = Logo(),
    val callStateUi: CallStateUi = CallStateUi.Disconnected
): UiState

internal fun BrandLogoState.hasLogo(isDarkTheme: Boolean) = with(logo) {
    if (isDarkTheme) dark != null && dark != android.net.Uri.EMPTY
    else light != null && light != android.net.Uri.EMPTY
}