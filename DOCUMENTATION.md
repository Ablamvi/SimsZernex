# SimsZernex 3D - Documentation Complète

## 📖 Table des Matières
1. [Installation](#installation)
2. [Architecture](#architecture)
3. [Systèmes Clés](#systèmes-clés)
4. [Guide de Développement](#guide-de-développement)
5. [Optimisation & Performance](#optimisation--performance)

---

## Installation

### Prérequis
- **Unity 2022 LTS** ou supérieur
- **.NET 6+**
- **C# 10+**

### Étapes
1. Cloner le repository
   ```bash
   git clone https://github.com/Ablamvi/SimsZernex.git
   cd SimsZernex
   ```

2. Ouvrir dans Unity
   - File → Open Project
   - Sélectionner le dossier racine

3. Configurer les scènes
   - Créer `Assets/Scenes/MainMenu.unity`
   - Créer `Assets/Scenes/GameWorld.unity`
   - Créer `Assets/Scenes/CharacterCreation.unity`

4. Ajouter GameManager
   - Dans GameWorld.unity, créer un GameObject vide
   - Ajouter les composants: `GameManager`, `TimeManager`, `CityManager`, `EventManager`

---

## Architecture

### Hiérarchie des Namespaces

```
SimsZernex/
├── Core/
│   ├── GameManager          (orchestrateur principal)
│   ├── TimeManager          (gestion du temps)
│   ├── EventManager         (événements globaux)
│   └── GameSettings         (configuration)
│
├── Character/
│   ├── SimCharacter         (Sim 3D jouable)
│   ├── SimNeeds             (besoins & humeur)
│   ├── SimAI                (IA autonome)
│   ├── RelationshipManager  (relations)
│   └── SimCharacterData     (données)
│
├── World/
│   ├── House                (maison + pièces)
│   ├── Room                 (pièce avec objets)
│   ├── InteractiveObject    (lit, toilettes, TV)
│   ├── CityManager          (gestion de ville)
│   └── Location             (lieux visités)
│
├── Gameplay/
│   ├── CareerManager        (carrières)
│   ├── EconomyManager       (argent & transactions)
│   ├── SkillSystem          (compétences)
│   ├── QuestSystem          (quêtes)
│   ├── LifeEventsManager    (mariages, naissances)
│   ├── SupernaturalPowers   (magie & pouvoirs)
│   ├── PathfindingSystem    (navigation A*)
│   ├── SocialAISystem       (conversations)
│   └── WeatherSystem        (météo)
│
├── UI/
│   ├── GameManager          (HUD temps réel)
│   ├── MainMenuManager      (menu principal)
│   ├── CameraController     (caméra 3D)
│   └── CharacterCreation    (création perso)
│
├── Data/
│   ├── GameDataManager      (sauvegarde JSON)
│   └── SimCharacterData     (structure données)
│
└── Utilities/
    ├── DebugManager         (affichage debug)
    └── PerformanceManager   (profiling)
```

### Flux de Données

```
GameManager (maestro)
    ↓
┌─ TimeManager      → Actions par jour/heure
├─ CityManager      → Lieux & Sims
├─ EventManager     → Tous les événements
└─ ActiveSim
    ↓
    ├─ SimCharacter
    │  ├─ SimNeeds (Faim, Énergie, Hygiène, Fun, Social, Vessie)
    │  ├─ SimMood  (Calcul d'humeur basé sur besoins)
    │  ├─ SimAI    (Prend décisions autonomes)
    │  └─ RelationshipManager (Relations avec d'autres Sims)
    │
    ├─ House (Maison du Sim)
    │  └─ Room[] (Chambre, Cuisine, Salon, Salle de bain)
    │     └─ InteractiveObject[] (Lit, Toilettes, TV, Frigo)
    │
    └─ EconomyManager (Argent & Carrière)
```

---

## Systèmes Clés

### 1️⃣ Système de Besoins

```csharp
// 6 besoins décroissent naturellement
Needs.Hunger        // 0-100 (0 = mort de faim)
Needs.Energy        // 0-100 (0 = mort de fatigue)
Needs.Hygiene       // 0-100 (0 = très sale)
Needs.Fun           // 0-100 (0 = déprimé)
Needs.Social        // 0-100 (0 = très seul)
Needs.Bladder       // 0-100 (0 = urgence)
```

**Traits affectent la décroissance:**
- `Gourmand` → Faim +30%
- `Énergique` → Énergie -20%
- `Perfectionniste` → Tous -10%

### 2️⃣ Système d'IA Autonome

**Décision toutes les 5 secondes:**
```
Priorité:
1. Besoins CRITIQUES (faim < 30, fatigue < 30)
2. Hygiène critique
3. Besoin bathroom
4. Social/Seul
5. Activités normales (travail, loisir)
```

### 3️⃣ Système d'Économie

```csharp
// Transactions tracées
EconomyManager.AddMoney(sim, 500, "Salaire quotidien");
EconomyManager.SpendMoney(sim, 100, "Loyer");
var history = EconomyManager.GetTransactionHistory(sim);
```

### 4️⃣ Pathfinding A*

```csharp
// Navigation intelligente
PathfindingSystem.FindPath(sim, startPos, goalPos, out path);
// Sim suit le chemin optimisé
```

### 5️⃣ Système d'Événements

```csharp
// Events globaux
var marriageEvent = new GameEvent(
    EventType.Marriage,
    "Alice et Bob se marient!",
    EventImpact.Positive
);
GameManager.Instance.EventManager.TriggerEvent(marriageEvent);
```

---

## Guide de Développement

### Ajouter un Nouveau Trait

```csharp
// Dans SimNeeds.cs, Update():
if (System.Array.Exists(traits, t => t == "MonTrait"))
    decayMultiplier += 0.2f; // Impact du trait
```

### Ajouter une Nouvelle Carrière

```csharp
// Dans CareerManager.cs:
availableCareers = new Career[]
{
    // ...
    new Career("Astronaute", "Laboratory", 3000, 0.8f, "Logic")
};
```

### Ajouter un Objet Interactif

```csharp
// Dans Room.cs:
private void AddDefaultObject(string name, InteractiveType type)
{
    var objGo = new GameObject(name);
    var interactiveObj = objGo.AddComponent<InteractiveObject>();
    interactiveObj.Initialize(name, type, 50);
}
```

### Ajouter une Quête

```csharp
var quest = new Quest(
    "Atteindre le niveau 5 en Cuisine",
    "Améliorez vos compétences culinaires",
    1000 // Récompense
);
QuestSystem.Instance.AddQuest(sim, quest);
```

---

## Optimisation & Performance

### 1. Object Pooling

```csharp
// Créer un pool d'objets
ObjectPoolManager.Instance.CreatePool("SimEffect", prefab, 20);

// Récupérer un objet
var effect = ObjectPoolManager.Instance.GetObject("SimEffect");

// Retourner un objet
ObjectPoolManager.Instance.ReturnObject("SimEffect", effect);
```

### 2. Caching

- Dictionnaires pour accès O(1)
- Queues pour pathfinding
- Lazy initialization pour ressources lourdes

### 3. Performance Monitoring

```csharp
// Appuyer F3 pour afficher debug HUD
// Affiche: FPS, besoins, humeur, position
```

### 4. Réduction Qualité Dynamique

```csharp
// PerformanceManager détecte lag > 50ms
// Réduit automatiquement qualité graphique
```

---

## Checklist Intégration Unity

- [ ] Importer tous les scripts
- [ ] Créer GameSettings.asset dans Resources/
- [ ] Créer 3 scènes (MainMenu, CharacterCreation, GameWorld)
- [ ] Ajouter GameManager à GameWorld
- [ ] Ajouter CameraController à la caméra principale
- [ ] Configurer InputManager (ZQSD, souris)
- [ ] Ajouter des modèles 3D & textures
- [ ] Tester gameplay complet
- [ ] Profiler avec Unity Profiler
- [ ] Build & test sur cible

---

## Ressources Utiles

- **Unity Docs**: https://docs.unity.com
- **C# Documentation**: https://docs.microsoft.com/dotnet/csharp
- **GameDev Patterns**: https://en.wikipedia.org/wiki/Design_pattern

---

**Version**: 1.0.0  
**Dernière mise à jour**: 2026-08-11  
**Auteur**: Copilot  
**License**: MIT
