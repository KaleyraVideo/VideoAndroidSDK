package com.kaleyra.video_common_ui.utils.extensions

import android.content.Intent
import com.kaleyra.video_common_ui.NavBackComponent

/**
 * Key used to add NavBackComponent value to the restarting intent of launcher activity
 */
const val KALEYRA_NAV_BACK_KEY = "kaleyra_nav_back"
internal fun Intent.addBackButtonFlag(navBackComponent: NavBackComponent) = putExtra(KALEYRA_NAV_BACK_KEY, navBackComponent.name)
