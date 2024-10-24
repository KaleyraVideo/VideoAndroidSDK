package com.kaleyra.video_sdk.call.brandlogo.view

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel

@Composable
fun BrandLogoComponent(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    viewModel: BrandLogoViewModel = viewModel(factory = BrandLogoViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()
    val brandLogoUri = if (isDarkTheme) uiState.logo.dark else uiState.logo.light
    if (brandLogoUri == null || brandLogoUri == Uri.EMPTY) return

    BrandLogoComponent(modifier, alignment, brandLogoUri)
}

@Composable
fun BrandLogoComponent(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    brandLogoUri: Uri
) {
    if (brandLogoUri == Uri.EMPTY) return
    AsyncImage(
        modifier = modifier,
        model = brandLogoUri,
        alignment = alignment,
        contentDescription = stringResource(id = R.string.kaleyra_company_logo),
        contentScale = ContentScale.Fit,
    )
}
