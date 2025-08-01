package com.github.rdafm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import com.github.rdafm.ui.FmRadioScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // echo 1 > /sys/devices/virtual/pwrcfg_class/dl_fmaudio_en/state
        // echo 1 > /sys/devices/virtual/pwrcfg_class/dl_amp_mute/state
        setContent {
            MaterialTheme(darkColorScheme()) {
                Box(Modifier.fillMaxSize(1f)) {
                    FmRadioScreen()
                }
            }
        }
    }
}

