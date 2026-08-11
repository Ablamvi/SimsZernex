using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de pouvoirs surnaturels pour les Sims
    /// </summary>
    public class SupernaturalPowerSystem : MonoBehaviour
    {
        public static SupernaturalPowerSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void GrantPower(SimCharacter sim, SupernaturalPowerType powerType)
        {
            Debug.Log($"[SupernaturalPowers] {sim.CharacterData.name} a obtenu le pouvoir: {powerType}");
            
            var powerEvent = new GameEvent(
                EventType.Custom,
                $"{sim.CharacterData.name} a découvert le pouvoir de {powerType}!",
                EventImpact.Positive
            );
            GameManager.Instance.EventManager.TriggerEvent(powerEvent);
        }

        public void UsePower(SimCharacter sim, SupernaturalPowerType powerType, object target)
        {
            switch (powerType)
            {
                case SupernaturalPowerType.TimeControl:
                    TimeManager.Instance.SetTimeScale(2f); // Accélérer le temps
                    break;
                case SupernaturalPowerType.Telekinesis:
                    Debug.Log($"{sim.CharacterData.name} utilise la télékinésie!");
                    break;
                case SupernaturalPowerType.MindControl:
                    if (target is SimCharacter targetSim)
                    {
                        Debug.Log($"{sim.CharacterData.name} contrôle l'esprit de {targetSim.CharacterData.name}!");
                    }
                    break;
                case SupernaturalPowerType.ElementalMagic:
                    Debug.Log($"{sim.CharacterData.name} lance une magie élémentaire!");
                    break;
                case SupernaturalPowerType.Resurrection:
                    Debug.Log($"{sim.CharacterData.name} a ressuscité quelqu'un!");
                    break;
            }
        }
    }

    public enum SupernaturalPowerType
    {
        TimeControl,
        Telekinesis,
        MindControl,
        ElementalMagic,
        Resurrection,
        Invisibility,
        Telepathy,
        DimensionalTravel
    }
}
