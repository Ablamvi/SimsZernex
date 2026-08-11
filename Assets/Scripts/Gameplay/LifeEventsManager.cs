using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Gère les événements de vie: mariages, naissances, décès, amiennes
    /// </summary>
    public class LifeEventsManager : MonoBehaviour
    {
        private List<SimCharacter> sims = new List<SimCharacter>();

        public static LifeEventsManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void RegisterSim(SimCharacter sim)
        {
            if (!sims.Contains(sim))
                sims.Add(sim);
        }

        public void TriggerMarriage(SimCharacter sim1, SimCharacter sim2)
        {
            Debug.Log($"[LifeEventsManager] {sim1.CharacterData.name} et {sim2.CharacterData.name} se marient!");
            
            var marriageEvent = new GameEvent(
                EventType.Marriage,
                $"{sim1.CharacterData.name} et {sim2.CharacterData.name} se marient!",
                EventImpact.Positive
            );
            GameManager.Instance.EventManager.TriggerEvent(marriageEvent);

            // Lier les deux Sims familialement
            sim1.RelationshipManager.ChangeRelationship(sim2, 50f);
            sim2.RelationshipManager.ChangeRelationship(sim1, 50f);
        }

        public void TriggerBirth(SimCharacter mother, SimCharacter father)
        {
            Debug.Log($"[LifeEventsManager] {mother.CharacterData.name} accouche!");
            
            // Créer un nouveau Sim bébé
            var babyData = new SimCharacterData
            {
                name = "Bébé",
                familyName = mother.CharacterData.familyName,
                age = 0,
                skinTone = Random.value > 0.5f ? mother.CharacterData.skinTone : father.CharacterData.skinTone
            };

            var birthEvent = new GameEvent(
                EventType.Birth,
                $"{mother.CharacterData.name} accouche d'un(e) {babyData.name}!",
                EventImpact.Positive
            );
            GameManager.Instance.EventManager.TriggerEvent(birthEvent);
        }

        public void TriggerDeath(SimCharacter sim, string cause)
        {
            sims.Remove(sim);
            Debug.Log($"[LifeEventsManager] {sim.CharacterData.name} est décédé(e)");
        }

        public void TriggerPromotion(SimCharacter sim, Career career)
        {
            var promotionEvent = new GameEvent(
                EventType.Promotion,
                $"{sim.CharacterData.name} a obtenu une promotion au niveau {career.CurrentLevel}!",
                EventImpact.Positive
            );
            GameManager.Instance.EventManager.TriggerEvent(promotionEvent);
        }

        public void TriggerIllness(SimCharacter sim)
        {
            var illnessEvent = new GameEvent(
                EventType.Illness,
                $"{sim.CharacterData.name} est tombé(e) malade!",
                EventImpact.Negative
            );
            GameManager.Instance.EventManager.TriggerEvent(illnessEvent);
        }
    }
}
