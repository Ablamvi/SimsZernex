package com.maisonvie.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.maisonvie.game.ui.GameScreen
import com.maisonvie.game.ui.theme.MaisonVieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaisonVieTheme {
                GameScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
