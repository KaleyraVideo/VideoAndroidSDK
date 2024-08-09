package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AccessibilityManager
import com.kaleyra.video_sdk.common.snackbar.model.StackedSnackbarHostMessagesHandler

@Composable
fun rememberStackedSnackbarHostState(accessibilityManager: AccessibilityManager) = remember { StackedSnackbarHostMessagesHandler(accessibilityManager) }
