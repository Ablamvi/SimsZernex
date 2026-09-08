# Maison / intégration personnage — vérification

- [x] `house.glb` reste la source visuelle principale de la maison.
- [x] Les axes Y/Z du GLB Habitat ont été corrigés pour utiliser Y comme hauteur et Z comme profondeur.
- [x] Sol et murs visibles cohérents avec les limites de déplacement.
- [x] Entrée laissée ouverte dans le mur avant.
- [x] Canapé aligné avec son volume réel et son point d'assise.
- [x] Lit et matelas reposent sur le sol et correspondent aux coordonnées de jeu.
- [x] Cuisine, douche et meuble vasque replacés dans les bonnes zones.
- [x] Table et quatre chaises conservées dans leur disposition cohérente.
- [x] Bureau, WC et réfrigérateur complétés par rendu procédural sans dupliquer les meubles du GLB.
- [x] Collisions principales alignées sur les empreintes de mobilier.
- [x] États C++/Kotlin synchronisés.
- [x] `Renderer::drawHouse()` déclaré dans `Renderer.h`.
- [x] Transition canapé sans téléportation visible.
- [x] Marche avec bras abaissés et balancement opposé.

## Limite de validation
Le contrôle C++ local passe en vérification syntaxique avec les en-têtes GLES/Android simulés. La compilation Android complète dépend du SDK/NDK et de Gradle disponibles dans l'environnement CI. Une validation visuelle sur appareil reste nécessaire pour calibrer au pixel près les poses du personnage.


## v0.6.0-final
- [x] Cuisine placée dans le secteur sud-ouest.
- [x] Séjour/dîner au centre.
- [x] Chambre dans le secteur sud-est.
- [x] Salle de bain au nord-est.
- [x] Bureau au nord-ouest.
- [x] Murs intérieurs avec ouvertures de passage.
- [x] Collision murale synchronisée avec les murs visibles.
- [x] Navigation par grille pour éviter les murs.
