package com.kaleyra.video_sdk.extensions

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle

internal fun ComponentActivity.isAtLeastResumed() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)