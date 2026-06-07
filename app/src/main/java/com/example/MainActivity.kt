package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.LinklyTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val action = intent?.action
    val type = intent?.type
    
    var sharedText: String? = null
    if (Intent.ACTION_SEND == action && "text/plain" == type) {
        val rawText = intent.getStringExtra(Intent.EXTRA_TEXT)
        // Extract URL
        val urlRegex = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))".toRegex()
        sharedText = rawText?.let { urlRegex.find(it)?.value } ?: rawText
    }

    setContent {
      LinklyTheme {
        AppNavigation(sharedUrl = sharedText)
      }
    }
  }
}
