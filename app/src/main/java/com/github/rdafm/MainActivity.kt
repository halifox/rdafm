package com.github.rdafm

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.rdafm.ui.FmRadioScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // echo 1 > /sys/devices/virtual/pwrcfg_class/dl_fmaudio_en/state
        // echo 1 > /sys/devices/virtual/pwrcfg_class/dl_amp_mute/state
        setContent {
            val context = LocalContext.current
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }

                darkTheme                                      -> darkColorScheme()
                else                                           -> lightColorScheme()
            }
            MaterialTheme(colorScheme) {
                Box(Modifier.fillMaxSize(1f)) {
                    FmRadioScreen()
                }
            }
        }
    }
}

