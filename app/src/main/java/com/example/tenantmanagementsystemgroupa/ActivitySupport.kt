package com.example.tenantmanagementsystemgroupa

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

internal fun Activity.applySystemInsets(root: View) {
    val left = root.paddingLeft
    val top = root.paddingTop
    val right = root.paddingRight
    val bottom = root.paddingBottom
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                WindowInsetsCompat.Type.displayCutout() or
                WindowInsetsCompat.Type.ime()
        )
        view.setPadding(
            left + insets.left, top + insets.top,
            right + insets.right, bottom + insets.bottom
        )
        windowInsets
    }
    ViewCompat.requestApplyInsets(root)
}

internal fun Activity.openExternalIntent(intent: Intent, @StringRes unavailableMessage: Int) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, unavailableMessage, Toast.LENGTH_SHORT).show()
    }
}
