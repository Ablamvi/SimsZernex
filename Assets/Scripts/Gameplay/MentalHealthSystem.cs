using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de santé mentale, stress et psychologie
    /// </summary>
    public class MentalHealthSystem : MonoBehaviour
    {
        public static MentalHealthSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void ApplyStress(SimCharacter sim, float amount)
        {
            // Augmenter le stress
            sim.Mood.MoodValue -= amount * 0.1f;
            Debug.Log($"[MentalHealth] {sim.CharacterData.name} subit un stress de {amount}");
        }

        public void RelieveStress(SimCharacter sim, float amount)
        {
            sim.Mood.MoodValue += amount * 0.1f;
            Debug.Log($"[MentalHealth] {sim.CharacterData.name} se sent mieux de {amount}");
        }

        public void TriggerMentalBreakdown(SimCharacter sim)
        {
            var breakdownEvent = new GameEvent(
                EventType.Custom,
                $"{sim.CharacterData.name} a une crise nerveuse!",
                EventImpact.Negative
            );
            GameManager.Instance.EventManager.TriggerEvent(breakdownEvent);
        }
    }
}
