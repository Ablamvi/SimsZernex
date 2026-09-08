# MaisonVie 3D — plan d'amélioration

## Phase 1 — stabilité et cohérence (fait dans cette version)
- Corriger la déclaration C++ de `Renderer::drawHouse()` pour que la définition hors classe corresponde à `Renderer.h`.
- Synchroniser les codes d'état Kotlin/C++ : `IDLE=0`, `WALKING=1`, `APPROACHING=2`, `BUSY=3`.
- Mettre les valeurs par défaut de `Game.h` au même niveau que la maison actuelle.
- Empêcher le bouton d'action de rester actif pendant l'approche.

## Phase 2 — maison et repères (fait dans cette version)
- Corriger l'orientation du modèle Habitat : le fichier précédent avait les axes Y/Z inversés pour les meubles.
- Replacer les meubles sur le sol : canapé, lit, cuisine, douche, meuble vasque et table/chaises.
- Remplacer les anciens cubes de murs mal exportés par un vrai sol et des murs cohérents autour de la zone jouable.
- Conserver une ouverture centrale dans le mur avant pour l'entrée.
- Aligner les collisions avec les limites visuelles de la maison.
- Ajouter visuellement les éléments qui n'étaient pas présents dans le GLB : bureau, WC et réfrigérateur.

## Phase 3 — interactions réalistes (partiellement fait, priorité suivante)
- Marche avec bras abaissés et balancement opposé aux jambes.
- Approche avec freinage progressif et rotation douce.
- Canapé : déplacement vers le siège, descente progressive, maintien assis, remontée puis retour au point de sortie sans téléportation.
- Lit : transition progressive vers la position couchée et retour.
- Douche : entrée/sortie progressive.
- Prochaine étape : calibrer précisément les hauteurs de bassin/pieds sur l'écran réel Android.

## Phase 4 — animations et vie quotidienne
1. Idle : respiration, petits mouvements du poids du corps.
2. Marche : pas plus naturels, pieds mieux synchronisés au sol.
3. Canapé : assise complète avec mains sur les cuisses/accoudoirs.
4. Lit : s'allonger puis dormir, réveil et sortie du lit.
5. Cuisine/frigo : prise de nourriture et retour des mains vers la bouche.
6. Douche/lavabo : gestes plus variés et orientation correcte.
7. Bureau : s'asseoir, travailler, se lever.

## Phase 5 — gameplay
- Besoins faim/énergie/hygiène reliés aux interactions.
- Interactions disponibles uniquement lorsqu'un point est réellement atteignable.
- Blocage des actions pendant les transitions.
- Feedback HUD plus clair : `Approche...`, `Action...`, puis retour à l'état normal.

## Phase 6 — qualité et optimisation
- Vérifier les shaders sur GLES 3.0.
- Réduire les allocations par frame dans les animations.
- Nettoyer le code procédural devenu inutile après l'intégration du GLB.
- Tester `arm64-v8a` en CI et sur appareil Android réel.
- Faire une passe finale de calibration visuelle de toutes les zones d'interaction.
