package com.simszernex.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Placeholder 3D Activity
 *
 * Pour l'instant cette Activity affiche un message et des instructions.
 * L'intégration d'un moteur 3D (Filament / glTF) sera réalisée ensuite.
 */
class ThreeDActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showInfo = remember { mutableStateOf(true) }
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showInfo.value) {
                            Text("Module 3D (placeholder)", style = MaterialTheme.typography.headlineMedium)
                            Text("L'intégration complète du rendu 3D (Filament) est disponible dans assets/3d-readme.md.", modifier = Modifier.padding(top = 12.dp))
                            Button(onClick = { showInfo.value = false }, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Afficher l'exemple de code")
                            }
                        } else {
                            Text("Exemple d'intégration Filament (voir README pour détails)")
                            Text(
                                "1) Ajouter la dépendance Filament dans app/build.gradle.kts\n2) Placer votre modèle .glb dans app/src/main/assets/models/\n3) Utiliser la classe ModelViewer (gltfio) pour charger le .glb",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Button(onClick = { showInfo.value = true }, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Retour")
                            }
                        }
                    }
                }
            }
        }
    }
}
