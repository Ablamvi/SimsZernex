# SimsZernex v0.4.0 — Sim-style house update

## Visual update
This build replaces the previous single-box furniture prototype with a lightweight stylized 3D composition system using boxes, rounded spheres and cylinders. Objects are built from multiple recognizable parts: mattress, pillows, duvet, bed frame, shower frame, basin, toilet bowl, cabinet doors, handles, microwave, monitor, lamp, sofa cushions, plant leaves and a rounded humanoid character.

## Gameplay
The existing native gameplay logic remains authoritative: movement, collisions, needs and interactions are unchanged and synchronized with the visible furniture list.

## Important
The project does not contain copied assets from The Sims. The look is an original, lightweight "life simulation" style.

# MaisonVie 3D — Étape 1

Jeu de simulation de vie en 3D natif pour Android : un personnage dans sa
maison, qui peut marcher, dormir, manger et se doucher. Rendu OpenGL ES 3.0
en C++ (comme le moteur de visualiseur 3D de Zernex Music), UI en Jetpack
Compose, même chaîne de compilation/signature (Gradle + GitHub Actions +
keystore).

**Important — à propos de la propriété intellectuelle** : ce projet est un
jeu original inspiré du genre "simulation de vie" (marcher, manger, dormir,
se laver, gérer des besoins). Je n'ai pas reproduit le code, les assets, les
noms, l'interface ou la marque de The Sims 3 / The Sims 3: Supernatural
(propriété d'Electronic Arts / Maxis) — ce serait une violation de copyright
que je ne peux pas faire, quelle que soit la façon dont c'est demandé. Tout
ce qui suit est écrit à partir de zéro.

## Ce qui fonctionne (v0.2.0)

- Une maison divisée en **deux pièces** séparées par un mur intérieur avec
  une **porte** (ouverture de 2 unités) : chambre + salle de bain d'un côté,
  cuisine + salon de l'autre.
- 4 zones meublées : **lit** (chambre), **douche** (salle de bain),
  **cuisine** (manger), **canapé** (décor pour l'instant).
- Un personnage contrôlable via un **joystick virtuel** (bas gauche), qui
  **tourne visuellement pour faire face à sa direction de marche** et a un
  léger rebond pendant qu'il marche. Collisions gérées avec les murs
  (extérieurs + intérieurs) et les meubles.
- 3 jauges de besoins qui diminuent avec le temps : **Faim**, **Énergie**,
  **Hygiène**.
- Un **bouton d'action contextuel** qui apparaît quand le personnage est
  près d'un meuble utilisable (Dormir / Manger / Se doucher) ; l'action
  recharge le besoin correspondant en quelques secondes.
- Caméra 3D qui suit le personnage.
- Tout le rendu (sol, murs, meubles, personnage) est fait en une seule
  passe de dessin instancié (`glDrawArraysInstanced`) à partir d'un seul
  cube unitaire, avec une rotation par instance (yaw) pour le personnage —
  simple et rapide, façon "blockout" de prototype.

## Ce qui n'est PAS encore fait (prochaines étapes naturelles)

- Plus de pièces (2 → 4+), couloirs, meubles supplémentaires.
- Animations de marche plus poussées, personnage plus détaillé,
  textures/matériaux au lieu de couleurs unies.
- Sauvegarde de la partie, plusieurs personnages, thème "surnaturel"
  (créatures, capacités spéciales) en tant que mécaniques originales.
- Sons, musique, effets de particules.

## Correctif notable (v0.1.0 → v0.2.0)

Le premier build échouait à la compilation Kotlin (`GameScreen.kt`) : les
imports `androidx.compose.runtime.getValue` / `setValue`, nécessaires pour
utiliser `by remember { mutableStateOf(...) }`, manquaient. Sans eux, le
compilateur ne sait pas résoudre le délégué de propriété et ça casse aussi
l'inférence de type plus loin dans le fichier (d'où les erreurs "Unresolved
reference 'x'/'y'" sur `Offset`, qui n'étaient qu'une conséquence du même
problème). C'est corrigé dans cette version.

## Architecture

```
app/src/main/cpp/          Moteur natif C++ (OpenGL ES 3.0)
  Mat4.h                   Maths 4x4 (repris du projet Zernex, générique)
  shaders.h                Shaders GLSL ES 3.0 (rendu de boîtes instanciées)
  Game.h / Game.cpp         Logique de jeu : personnage, besoins, meubles, collisions
  Renderer.h / Renderer.cpp Rendu : géométrie, caméra, construction de la scène par frame
  native-lib.cpp            Pont JNI

app/src/main/java/com/maisonvie/game/
  MainActivity.kt            Point d'entrée, héberge l'écran Compose
  engine/NativeGameBridge.kt Déclarations JNI côté Kotlin
  ui/GameSurfaceView.kt       GLSurfaceView reliée au moteur natif
  ui/GameScreen.kt            HUD, joystick, bouton d'action, boucle de sondage d'état
```

## Compiler et signer (identique au flux Zernex Music)

Le projet réutilise exactement la même mécanique que `Zernex-music` :

- `hugues-release.keystore` à la racine (même fichier que celui que tu m'as
  fourni, réutilisé tel quel pour signer ce nouveau projet).
- `app/build.gradle.kts` référence ce keystore via `signingConfigs.release`,
  avec les mêmes variables d'environnement `KEYSTORE_PASSWORD`,
  `KEY_ALIAS`, `KEY_PASSWORD` (valeurs par défaut identiques, à remplacer
  par des secrets GitHub `KEYSTORE_BASE64` si tu préfères ne pas committer
  le fichier `.keystore`).
- `.github/workflows/build-apk.yml` compile `assembleRelease`, vérifie la
  signature avec `apksigner`, publie l'APK release + un zip du code source
  en artefacts de build.

En local :

```bash
./gradlew assembleRelease
```

L'APK signé sort dans `app/build/outputs/apk/release/`.

## Nom du projet

J'ai appelé le jeu **MaisonVie 3D** (package `com.maisonvie.game`) plutôt
que d'utiliser "Sims" dans le nom, ce dernier étant une marque déposée
d'Electronic Arts. Dis-moi si tu veux un autre nom — tant que ce n'est pas
une marque existante, aucun souci pour l'adapter.


## Final build scope
This build includes the complete current gameplay foundation: furnished 3D house, skinned character, natural walk cycle with arms down, orbit camera, pinch zoom, tap-to-move, automatic furniture interaction, collision sliding, need simulation, and autonomous recovery when hunger/energy/hygiene become low. The native renderer uses GLES 3.0 and the app targets arm64-v8a, armeabi-v7a and x86_64.

### Controls
- Tap floor: character walks to the selected point.
- Tap furniture: character walks there and automatically performs the relevant action.
- One finger drag: rotate the 3D camera.
- Two finger pinch: zoom.
- No tutorial/joystick overlay is required.

### Validation
The source package was checked for JNI name consistency, house scene header parsing, character asset parsing, shader program guards, camera access synchronization, and C++ brace/structure consistency. A device/CI Android build remains the authoritative final compilation test because this environment does not contain the requested Android NDK/remote Gradle artifacts.

## v0.5.1 final visual correction

- Corrected the skinned-character scale: the rig's root already contains its
  centimeter-to-meter conversion, so the renderer now uses a world scale of
  `0.55` rather than `0.01`. This restores an approximately human-sized
  character in the same world as the house.
- Rebuilt the visible house scene from the v0.5 base and added interior
  partitions with real door openings: kitchen/living, living/bedroom,
  bedroom/bathroom and office/living divisions, while keeping the roof open
  for the isometric/orbit camera.
- Synchronized the gameplay collision walls with those visible partitions so
  the character cannot walk through them.
- Kept the furnished layout aligned to the supplied isometric reference:
  kitchen in the south-west, central living/dining area, bedroom on the east,
  bathroom on the north-east and office/storage on the north-west.


## v0.6.0-final — personnage + maison réorganisée

- Pose debout permanente avec bras relâchés vers le bas, même sans action.
- Marche procédurale avec balancement opposé des bras et des jambes.
- Les rotations d'épaule utilisent l'axe Z adapté au rig CC_Base fourni.
- Maison organisée en cuisine, séjour, chambre, salle de bain et bureau, avec murs intérieurs et ouvertures de porte.
- Les collisions suivent les murs visibles et les meubles.
- Navigation par grille locale pour contourner les murs et éviter les destinations inaccessibles.
- Les interactions sont déclenchées après une approche réellement navigable.
- Le modèle `house.glb` contient également les séparations intérieures pour rester cohérent avec `house.scene.bin`.

### Contrôle final effectué
Le script de vérification interne contrôle l'en-tête des modèles, les indices de skinning, les poids, les bornes de la scène, la présence des partitions intérieures et l'intégrité de l'archive. La syntaxe C++ a été vérifiée avec des en-têtes GLES/Android de test. La compilation Android complète doit toujours être exécutée dans l'environnement disposant du SDK/NDK et de Gradle.
