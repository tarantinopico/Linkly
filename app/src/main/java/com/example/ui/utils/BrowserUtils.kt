package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun openUrl(context: Context, url: String, useInternalBrowser: Boolean) {
    try {
        val uri = Uri.parse(url)
        if (useInternalBrowser) {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(context, uri)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
