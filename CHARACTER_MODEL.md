# Personnage 3D — MaisonVie 3D v0.4.1

Le personnage officiel du projet est maintenant `rocker_girl-compressed.glb`, intégré sous `app/src/main/assets/models/character.glb`.

- Source : GLB riggé fourni pour le projet
- Rig : 102 os
- Runtime : `character.skinnedbin`
- Skinning GPU : 4 influences par sommet
- OpenGL ES 3.0 : matrices d'os via texture RGBA32F

## Animation et interaction

Le GLB ne contenait pas de clips. Les premières animations sont donc générées procéduralement par le moteur :

- Idle : respiration et micro-mouvements
- Walk : jambes en opposition, bras bas et balancés naturellement, légère rotation du bassin et du torse
- Approche : marche automatique jusqu'au point d'interaction
- Sit : placement sur le coussin du canapé puis flexion des hanches/genoux
- Sleep : placement sur le matelas et posture allongée
- Eat : bras vers le visage et mouvement de tête
- Shower : bras levés et petits mouvements
- Work : posture devant le bureau

## Adaptation aux meubles

Chaque interaction possède maintenant un point d'approche et une pose finale séparée. Le personnage marche donc vers le meuble avant de passer à l'animation, puis est placé sur le siège/matelas quand l'action le nécessite.

Les meubles principaux ont également été enrichis visuellement : lit avec couette/oreillers, canapé avec coussins, chaises avec dossiers, cuisine détaillée, douche vitrée, lavabo et bureau.


## v0.6.0-final animation correction
Le rig fourni contient 102 os et aucun clip d'animation. La pose de repos du rig place les bras presque horizontalement. Le moteur applique donc une pose neutre explicite sur les deux upperarms avec des rotations Z opposées : les deux mains restent près des hanches en IDLE et pendant la marche, puis le mouvement de balancement est ajouté autour de cette pose.
