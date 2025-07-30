package com.github.rdafm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.rdafm.ui.theme.RdafmTheme
import com.service.fm.FmReceiver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //echo 1 > /sys/devices/virtual/pwrcfg_class/dl_fmaudio_en
            val fmReceiver = FmReceiver()
            fmReceiver.turnOnRadio()
            fmReceiver.tuneRadio(9750)
//            fmReceiver.muteAudio(false)
            fmReceiver.setFMVolume(1)
//            fmReceiver.setAudioPath(FmReceiver.AUDIO_PATH_SPEAKER)

            RdafmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RdafmTheme {
        Greeting("Android")
    }
}