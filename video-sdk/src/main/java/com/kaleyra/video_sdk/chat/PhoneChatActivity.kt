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

package com.kaleyra.video_sdk.chat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.kaleyra.video_common_ui.ChatActivity
import com.kaleyra.video_common_ui.NavBackComponent
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToPreviousOrMainActivity
import com.kaleyra.video_sdk.call.utils.Android12ChatActivityTasksFixService
import com.kaleyra.video_sdk.chat.screen.ChatScreen
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel

internal class PhoneChatActivity : ChatActivity(), ServiceConnection {

    override val viewModel: PhoneChatViewModel by viewModels {
        PhoneChatViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    }

    private val isAndroid12 = Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S.inc()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ChatScreen(onBackPressed = this::onBackPressed, viewModel = viewModel)
        }

        // fixes the resuming of a task on android 12
        // https://issuetracker.google.com/issues/207397151#comment17
        if (isAndroid12) {
            Intent(this, Android12ChatActivityTasksFixService::class.java).also { intent ->
                startService(intent)
                bindService(intent, this, Context.BIND_AUTO_CREATE)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (isAndroid12) runCatching { unbindService(this) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToPreviousOrMainActivity(
            this@PhoneChatActivity::class.simpleName!!,
            NavBackComponent.CHAT
        )
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) = Unit

    override fun onServiceDisconnected(name: ComponentName?) = Unit
}
