package com.kaleyra.app_configuration.utils

import com.kaleyra.app_configuration.R
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object Api35Utils {
    fun AppCompatActivity.applyStatusBarInsetAPI35() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBarSpacing = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout())

            view.updatePadding(
                systemBarSpacing.left,
                systemBarSpacing.top,
                systemBarSpacing.right,
                systemBarSpacing.bottom
            )

            val parent = window.decorView as ViewGroup

            val colorPrimaryDarkTypedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorPrimaryDark, colorPrimaryDarkTypedValue, true)
            val colorPrimaryDark = colorPrimaryDarkTypedValue.data

            val colorSurfaceTypedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorSurface, colorSurfaceTypedValue, true)
            val colorSurface = colorSurfaceTypedValue.data

            val startDisplayCutoutBackgroundViewTag = "startDisplayCutoutBackgroundViewTag"
            parent.findViewWithTag<View>(startDisplayCutoutBackgroundViewTag)?.let {
                it.layoutParams.width = systemBarSpacing.left
            } ?: run {
                val startDisplayCutoutBackgroundView = View(this)
                startDisplayCutoutBackgroundView.tag = startDisplayCutoutBackgroundViewTag
                startDisplayCutoutBackgroundView.setBackgroundColor(colorSurface)
                parent.addView(startDisplayCutoutBackgroundView, ViewGroup.LayoutParams(systemBarSpacing.left, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            val endDisplayCutoutBackgroundViewTag = "endDisplayCutoutBackgroundViewTag"
            parent.findViewWithTag<View>(endDisplayCutoutBackgroundViewTag)?.let {
                it.layoutParams.width = systemBarSpacing.right
            } ?: run {
                val endDisplayCutoutBackgroundView = View(this)
                endDisplayCutoutBackgroundView.tag = endDisplayCutoutBackgroundViewTag
                endDisplayCutoutBackgroundView.setBackgroundColor(colorSurface)
                parent.addView(endDisplayCutoutBackgroundView, FrameLayout.LayoutParams(systemBarSpacing.right, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    gravity = Gravity.END
                })
            }

            val statusBarBackgroundViewTag = "statusBarBackgroundView"
            parent.findViewWithTag<View>(statusBarBackgroundViewTag)?.let {
                it.layoutParams.height = systemBarSpacing.top
            } ?: run {
                val statusBarBackgroundView = View(this)
                statusBarBackgroundView.tag = statusBarBackgroundViewTag
                statusBarBackgroundView.setBackgroundColor(colorPrimaryDark)
                parent.addView(statusBarBackgroundView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, systemBarSpacing.top))
            }

            val navigationBarBackgroundViewTag = "navigationBarBackgroundView"
            parent.findViewWithTag<View>(navigationBarBackgroundViewTag)?.let {
                it.layoutParams.height = systemBarSpacing.bottom
            } ?: run {
                val navigationBarBackgroundView = View(this)
                navigationBarBackgroundView.tag = navigationBarBackgroundViewTag
                navigationBarBackgroundView.setBackgroundColor(colorSurface)
                parent.addView(navigationBarBackgroundView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, systemBarSpacing.bottom).apply {
                    gravity = Gravity.BOTTOM
                })
            }

            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false

            insets
        }
    } else Unit
}