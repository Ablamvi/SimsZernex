using UnityEngine;
using SimsZernex.Character;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Gère les quetes et objectifs pour les Sims
    /// </summary>
    public class QuestSystem : MonoBehaviour
    {
        private Dictionary<SimCharacter, List<Quest>> simQuests = new Dictionary<SimCharacter, List<Quest>>();

        public static QuestSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void AddQuest(SimCharacter sim, Quest quest)
        {
            if (!simQuests.ContainsKey(sim))
                simQuests[sim] = new List<Quest>();

            simQuests[sim].Add(quest);
            Debug.Log($"[QuestSystem] {sim.CharacterData.name} a reçu la quête: {quest.Title}");
        }

        public void CompleteQuest(SimCharacter sim, Quest quest)
        {
            if (simQuests.TryGetValue(sim, out var quests))
            {
                if (quests.Remove(quest))
                {
                    Debug.Log($"[QuestSystem] {sim.CharacterData.name} a completé {quest.Title}");
                    // Donner des récompenses
                    EconomyManager.Instance.AddMoney(sim, quest.Reward, $"Quête: {quest.Title}");
                }
            }
        }

        public List<Quest> GetSimQuests(SimCharacter sim)
        {
            return simQuests.TryGetValue(sim, out var quests) ? quests : new List<Quest>();
        }
    }

    [System.Serializable]
    public class Quest
    {
        public string Title;
        public string Description;
        public int Reward;
        public bool IsCompleted;
        public float Progress; // 0 à 100

        public Quest(string title, string description, int reward)
        {
            Title = title;
            Description = description;
            Reward = reward;
            IsCompleted = false;
            Progress = 0f;
        }
    }
}
