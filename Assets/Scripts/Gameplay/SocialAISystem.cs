using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système d'IA social - Conversations, interactions entre Sims
    /// </summary>
    public class SocialAISystem : MonoBehaviour
    {
        public static SocialAISystem Instance { get; private set; }

        private Dictionary<SimCharacter, SimCharacter> currentConversations = new Dictionary<SimCharacter, SimCharacter>();

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void InitiateConversation(SimCharacter initiator, SimCharacter target)
        {
            if (currentConversations.ContainsKey(initiator)) return;

            currentConversations[initiator] = target;
            currentConversations[target] = initiator;

            Debug.Log($"[SocialAI] {initiator.CharacterData.name} discute avec {target.CharacterData.name}");
            
            // Impact sur les besoins
            initiator.Needs.ReduceSocial(-15f);
            target.Needs.ReduceSocial(-15f);

            // Impact sur la relation
            float relationChange = Random.Range(-10f, 20f);
            initiator.RelationshipManager.ChangeRelationship(target, relationChange);
            target.RelationshipManager.ChangeRelationship(initiator, relationChange);
        }

        public void EndConversation(SimCharacter sim)
        {
            if (currentConversations.TryGetValue(sim, out var other))
            {
                currentConversations.Remove(sim);
                currentConversations.Remove(other);
                Debug.Log($"[SocialAI] Fin de conversation entre {sim.CharacterData.name} et {other.CharacterData.name}");
            }
        }

        public bool IsInConversation(SimCharacter sim)
        {
            return currentConversations.ContainsKey(sim);
        }
    }
}
