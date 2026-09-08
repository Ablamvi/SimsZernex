package com.maisonvie.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.maisonvie.game.engine.NativeGameBridge
import kotlinx.coroutines.delay

/**
 * Full game screen for the playable large-house build: a 3D house rendered natively,
 * need bars (Faim / Énergie / Hygiène) that deplete over time.
 * The entire 3D view is touch-controlled: tap the floor to walk, or tap a
 * piece of furniture to walk there and perform its action automatically.
 */
@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    if (!NativeGameBridge.libraryLoaded) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Le moteur natif (C++/OpenGL) n'a pas pu être chargé sur cet appareil.\n" +
                    "Vérifie que l'APK a bien été compilé avec le support NDK (arm64-v8a).",
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val holder = remember { SurfaceHolderRef() }

    var hunger by remember { mutableFloatStateOf(70f) }
    var energy by remember { mutableFloatStateOf(70f) }
    var hygiene by remember { mutableFloatStateOf(70f) }
    var engineReady by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> GameSurfaceView(ctx).also { holder.view = it } }
        )

        // --- HUD: need bars, top of screen ---
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .width(220.dp)
        ) {
            NeedBar("Faim", hunger)
            NeedBar("Énergie", energy)
            NeedBar("Hygiène", hygiene)
        }

        // Touch controls replace the old tutorial/joystick: tap the floor to
        // walk, or tap a bed/shower/kitchen/etc. to walk there and interact.
        LaunchedEffect(Unit) {
            while (!engineReady) {
                holder.view?.nativeHandle?.takeIf { it != 0L }?.let {
                    engineReady = NativeGameBridge.nativeIsReady(it)
                }
                delay(100)
            }
        }
        if (!engineReady) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chargement de la maison…", color = Color.White)
            }
        }
    }

    // Poll native gameplay state ~30 times per second to drive the HUD.
    LaunchedEffect(Unit) {
        val needsBuf = FloatArray(3)
        while (true) {
            holder.view?.nativeHandle?.takeIf { it != 0L }?.let { handle ->
                NativeGameBridge.nativeGetNeeds(handle, needsBuf)
                hunger = needsBuf[0]
                energy = needsBuf[1]
                hygiene = needsBuf[2]
            }
            delay(33)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> holder.view?.onPause()
                Lifecycle.Event.ON_RESUME -> holder.view?.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            holder.view?.destroyNative()
        }
    }
}

private class SurfaceHolderRef {
    var view: GameSurfaceView? = null
}

@Composable
private fun NeedBar(label: String, value: Float) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        )
    }
}

