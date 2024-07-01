package com.kaleyra.video_sdk.common.snackbarm3.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AccessibilityManager
import com.kaleyra.video_sdk.common.snackbarm3.model.StackedSnackbarHostMessagesHandler

@Composable
fun rememberStackedSnackbarHostState(accessibilityManager: AccessibilityManager) = remember { StackedSnackbarHostMessagesHandler(accessibilityManager) }
