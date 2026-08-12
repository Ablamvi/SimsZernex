# Architecture & Design Patterns - SimsZernex

## 🏗️ Architecture Globale

### Layers

```
┌─────────────────────────────────────┐
│  PRESENTATION LAYER (UI)            │
│  HUDManager, MainMenuManager,       │
│  CameraController, CharacterCreation│
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│  APPLICATION LAYER (Game Logic)     │
│  GameManager, GameState, Gameplay   │
├─────────────────────────────────────┤
│  DOMAIN LAYER (Business Logic)      │
│  SimCharacter, House, Career,       │
│  SkillSystem, QuestSystem           │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│  INFRASTRUCTURE LAYER (Data)        │
│  GameDataManager, SimCharacterData, │
│  Save/Load, Serialization           │
└─────────────────────────────────────┘
```

---

## 🎯 Design Patterns Utilisés

### 1. Singleton Pattern

**Utilisation**: Gestionnaires globaux uniques

```csharp
public class GameManager : MonoBehaviour
{
    public static GameManager Instance { get; private set; }
    
    private void Awake()
    {
        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
            return;
        }
        Instance = this;
        DontDestroyOnLoad(gameObject);
    }
}

// Utilisation partout:
GameManager.Instance.CreateNewGame(data);
```

**Avantages**:
- ✅ Point d'accès unique
- ✅ Pas de dépendances complexes
- ✅ Lazy initialization

### 2. Observer Pattern

**Utilisation**: Événements globaux

```csharp
public event Action<GameEvent> OnEventTriggered;
public event Action<GameTime> OnTimeChanged;

// Abonnement
GameManager.Instance.TimeManager.OnTimeChanged += UpdateHUD;

// Notification
OnTimeChanged?.Invoke(currentGameTime);
```

### 3. Factory Pattern

**Utilisation**: Génération procédurale

```csharp
public static SimCharacterData GenerateRandomSim()
{
    // Créer un Sim avec propriétés aléatoires
    return new SimCharacterData { ... };
}

public static House GenerateHouse(int seed)
{
    Random.InitState(seed);
    // Générer maison unique
}
```

### 4. Strategy Pattern

**Utilisation**: Comportements d'IA

```csharp
// Différentes stratégies d'action
switch (currentAction)
{
    case ActionType.Eat:
        // Stratégie Manger
        break;
    case ActionType.Sleep:
        // Stratégie Dormir
        break;
}
```

### 5. Command Pattern

**Utilisation**: Actions Sim

```csharp
public class SimAction
{
    public ActionType ActionType;
    public float EffectValue;
    public float Duration;
}

sim.PerformAction(new SimAction { 
    ActionType = ActionType.Eat,
    EffectValue = 40f 
});
```

### 6. Object Pool Pattern

**Utilisation**: Performance des effets

```csharp
ObjectPoolManager.Instance.CreatePool("SimEffect", prefab, 20);
var effect = ObjectPoolManager.Instance.GetObject("SimEffect");
ObjectPoolManager.Instance.ReturnObject("SimEffect", effect);
```

---

## 📊 Dépendances entre Systèmes

```
GameManager (centre)
    ├─→ TimeManager
    │   ├─→ EventManager (jour/nuit events)
    │   └─→ WeatherSystem (météo liée au temps)
    │
    ├─→ CityManager
    │   ├─→ Location[]
    │   └─→ SimCharacter[] (Sims de la ville)
    │
    ├─→ EventManager
    │   └─→ Tous les systèmes (broadcast)
    │
    └─→ ActiveSim (SimCharacter)
        ├─→ SimCharacterData (données)
        ├─→ SimNeeds
        │   └─→ SimMood (calcul mood)
        ├─→ SimAI
        │   ├─→ PathfindingSystem
        │   └─→ SocialAISystem
        ├─→ RelationshipManager
        │   └─→ Autres SimCharacter
        ├─→ House
        │   ├─→ Room[]
        │   └─→ InteractiveObject[]
        └─→ Career (CareerManager)
            └─→ EconomyManager (salaire)
```

---

## 🔄 Boucles de Jeu

### Boucle Principale

```
Update() {
    timeManager.Update()           // Avancer temps
        ↓
    gameManager.UpdateGameplay()   // Mettre à jour
        ├─→ activeSim.UpdateNeeds()    // Besoins décroissent
        ├─→ activeSim.UpdateMood()     // Humeur recalculée
        └─→ simAI.MakeDecision()       // Choisir action
            ↓
        simAI.ExecuteCurrentAction()   // Exécuter
            ↓
        hudManager.UpdateSimDisplay()  // Afficher HUD
}
```

### Boucle d'Événements

```
EventQueue
    ↓
Dequeue Event
    ↓
Trigger GlobalEvent
    ↓
Tous les listeners reçoivent
    ↓
Affichage notification
```

---

## 🎬 State Machines

### GameState

```
[MainMenu]
    ↓ StartNewGame
[CharacterCreation]
    ↓ CreateCharacter
[Playing] ←→ [Paused]
    ↓ QuitToMenu
[MainMenu]
```

### SimAction State

```
[Idle]
    ↓ NeedThreshold
[Eating] → [Idle]
    ↓ Hunger > 80%
[Idle]
```

---

## 🔐 Principes SOLID

### S - Single Responsibility
✅ GameManager = orchestration  
✅ SimAI = décisions autonomes  
✅ EconomyManager = argent uniquement

### O - Open/Closed
✅ Facile d'ajouter carrières (CareerManager extensible)  
✅ Facile d'ajouter traits (SimNeeds peut être étendu)

### L - Liskov Substitution
✅ InteractiveObject peut être remplacé par ses types  
✅ Location peut être remplacé par House/Workplace

### I - Interface Segregation
✅ Chaque système expose une interface minimale  
✅ Pas de dépendances inutiles

### D - Dependency Inversion
✅ Événements plutôt que références directes  
✅ Managers découplés via EventManager

---

## 📈 Scalabilité

### Ajouter une Nouvelle Carrière
```csharp
// 1. Ajouter à CareerManager
availableCareers = new Career[] {
    new Career("Astronaute", "Laboratory", 3000, 0.8f, "Logic")
};

// 2. Fin. C'est tout!
```

### Ajouter un Nouveau Pouvoir
```csharp
// 1. Ajouter à enum
public enum SupernaturalPowerType {
    // ...
    MyNewPower
}

// 2. Implémenter dans UsePower()
case SupernaturalPowerType.MyNewPower:
    // Logique
    break;

// 3. Fin!
```

---

## 🎨 Extensibilité

### Template pour Nouveau Système

```csharp
namespace SimsZernex.Gameplay
{
    public class MySystem : MonoBehaviour
    {
        public static MySystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void DoSomething(SimCharacter sim)
        {
            Debug.Log($"[MySystem] {sim.CharacterData.name}");
        }
    }
}
```

---

**Dernière mise à jour**: 2026-08-11  
**Version**: 1.0.0
