package com.github.rdafm.ui

import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.service.fm.FmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FmRadioScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT


    val sp = remember { context.getSharedPreferences("fm", Context.MODE_PRIVATE) }
    val fmReceiver = remember { FmReceiver() }
    var volume by remember { mutableStateOf(sp.getFloat("volume", 0f)) }
    var freq by remember { mutableStateOf(sp.getInt("freq", 9870)) }
    var power by remember { mutableStateOf(fmReceiver.radioIsOn) }
    var loading by remember { mutableStateOf(false) }
    var volumeSliderState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val freqStars = remember { mutableStateListOf<Int>() }

    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val storedList = sp.getStringSet("freqStars", null) ?: emptySet()
            storedList.mapIndexedTo(freqStars) { index, value -> value.toInt() }
        }
        launch(Dispatchers.IO) {
            snapshotFlow { freq }
                .sample(100)
                .collect {
                    fmReceiver.tuneRadio(it)
                    sp.edit { putInt("freq", it) }
                }
        }
        launch(Dispatchers.IO) {
            snapshotFlow { volume }
                .sample(100)
                .collect {
                    fmReceiver.setFMVolume((it * 10).toInt())
                    sp.edit { putFloat("volume", it) }
                }
        }
    }

    fun launch(block: suspend () -> Unit) {
        scope.launch(Dispatchers.IO) {
            loading = true
            try {
                block()
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("${e.message}")
            } finally {
                loading = false
            }
        }
    }
    BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
//        sheetSwipeEnabled = false,
        sheetContent = {
            Scaffold(topBar = {
                TopAppBar(
                    { Text("收藏的电台") },
                    navigationIcon = {
                        IconButton({
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }) {
                            Icon(Icons.Rounded.ExpandMore, null)
                        }
                    })
            }) {
                LazyColumn(Modifier.padding(it)) {
                    items(freqStars.sorted()) { freqValue ->
                        ListItem(
                            {
                                Text("FM %.1fMHz".format(freqValue / 100F))
                            }, Modifier.clickable {
                                freq = freqValue
                            },
                            colors = ListItemDefaults.colors().copy(
                                containerColor = if (freqValue == freq) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
                            )
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround // 确保内容有足够的空间
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    { volumeSliderState = !volumeSliderState },
                    enabled = power && !loading,
                ) {
                    Icon(Icons.Rounded.VolumeUp, null)
                }
                AnimatedContent(volumeSliderState, Modifier.weight(1f, fill = false)) { it ->
                    if (it) {
                        Slider(
                            volume,
                            { volume = it },
                            Modifier
                                .weight(1f)
                                .height(28.dp),
                            enabled = power && !loading
                        )
                    }
                }
                FilledTonalIconToggleButton(
                    freq in freqStars,
                    {
                        if (freq in freqStars) {
                            freqStars.remove(freq)
                        } else {
                            freqStars.add(freq)
                        }
                        val destination = mutableSetOf<String>()
                        freqStars.mapIndexedTo(destination) { index, value -> value.toString() }
                        sp.edit { putStringSet("freqStars", destination) }
                    },
                    enabled = power && !loading,
                ) {
                    if (freq in freqStars) {
                        Icon(Icons.Rounded.Star, null)
                    } else {
                        Icon(Icons.Rounded.StarBorder, null)
                    }
                }
                Spacer(Modifier.size(0.dp))
                FilledTonalIconButton(
                    {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    enabled = power && !loading,
                ) {
                    Icon(Icons.Rounded.List, null)
                }
            }
            Text(
                text = if (isPortrait) "FM\n%.1fMHz".format(freq / 100F) else "FM %.1fMHz".format(freq / 100F),
                lineHeight = 72.sp,
                autoSize = TextAutoSize.StepBased(maxFontSize = 72.sp),
                textAlign = TextAlign.Start,
                maxLines = if (isPortrait) 2 else 1,
                modifier = Modifier.padding(16.dp, 0.dp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            Slider(
                value = freq.toFloat(),
                onValueChange = { newValue -> freq = newValue.toInt() },
                valueRange = 8750f..10800f,
                steps = 204, // 108.0 - 87.5 = 20.5; 20.5 / 0.1 = 205 steps, -1 for actual steps
                modifier = Modifier.fillMaxWidth(),
                enabled = power && !loading
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    { launch { freq = fmReceiver.seekStation(FmReceiver.SCAN_MODE_DOWN) } },
                    enabled = power && !loading,
                ) {
                    Icon(Icons.Rounded.FastRewind, null)
                }
                FilledTonalIconButton(
                    { freq -= 10 },
                    enabled = power && !loading,
                ) {
                    Icon(Icons.Rounded.SkipPrevious, null)
                }
                FilledIconButton(
                    {
                        launch {
                            val state = if (power) fmReceiver.turnOffRadio() else fmReceiver.turnOnRadio()
                            if (state == 0) {
                                power = !power
                                if (power) {
                                    fmReceiver.tuneRadio(freq)
                                    fmReceiver.setFMVolume((volume * 10).toInt())
                                }
                            } else {
                                scaffoldState.snackbarHostState.showSnackbar("FM收音机打开失败 错误代码:${state}")
                            }
                        }
                    },
                    Modifier.size(36.dp + 16.dp),
                ) {
                    if (loading) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1000, easing = LinearEasing)
                            )
                        )
                        Icon(
                            Icons.Rounded.Autorenew, null,
                            Modifier
                                .size(36.dp)
                                .graphicsLayer { rotationZ = rotation })
                    } else if (power) {
                        Icon(Icons.Rounded.Pause, null, Modifier.size(36.dp))
                    } else {
                        Icon(Icons.Rounded.PlayArrow, null, Modifier.size(36.dp))
                    }
                }
                FilledTonalIconButton(
                    { freq += 10 },
                    enabled = power && !loading,
                ) {
                    Icon(Icons.Rounded.SkipNext, null)
                }
                FilledTonalIconButton(
                    { launch { freq = fmReceiver.seekStation(FmReceiver.SCAN_MODE_UP) } },
                    enabled = power && !loading,
                ) {
                    Icon(Icons.Rounded.FastForward, null)
                }
            }

        }
    }
}



