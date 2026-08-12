using UnityEngine;
using SimsZernex.Character;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Gère les compétences des Sims (cuisine, logique, chaírisme, etc.)
    /// </summary>
    public class SkillSystem : MonoBehaviour
    {
        private Dictionary<SimCharacter, Dictionary<SkillType, Skill>> simSkills = new Dictionary<SimCharacter, Dictionary<SkillType, Skill>>();

        public static SkillSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void InitializeSimSkills(SimCharacter sim)
        {
            if (!simSkills.ContainsKey(sim))
            {
                simSkills[sim] = new Dictionary<SkillType, Skill>
                {
                    { SkillType.Cooking, new Skill("Cuisine", 1) },
                    { SkillType.Logic, new Skill("Logique", 1) },
                    { SkillType.Fitness, new Skill("Forme Physique", 1) },
                    { SkillType.Charisma, new Skill("Chaírisme", 1) },
                    { SkillType.Creativity, new Skill("Créativité", 1) },
                    { SkillType.Mischief, new Skill("Farces", 1) }
                };
            }
        }

        public void IncreaseSkill(SimCharacter sim, SkillType skillType, float amount)
        {
            if (!simSkills.TryGetValue(sim, out var skills)) return;
            if (skills.TryGetValue(skillType, out var skill))
            {
                skill.AddExperience(amount);
            }
        }

        public Skill GetSkill(SimCharacter sim, SkillType skillType)
        {
            if (simSkills.TryGetValue(sim, out var skills))
                return skills.TryGetValue(skillType, out var skill) ? skill : null;
            return null;
        }

        public int GetSkillLevel(SimCharacter sim, SkillType skillType)
        {
            var skill = GetSkill(sim, skillType);
            return skill?.Level ?? 0;
        }
    }

    [System.Serializable]
    public class Skill
    {
        public string Name { get; set; }
        public int Level { get; set; }
        public float Experience { get; set; }

        public Skill(string name, int level = 1)
        {
            Name = name;
            Level = level;
            Experience = 0f;
        }

        public void AddExperience(float amount)
        {
            Experience += amount;
            if (Experience >= 100f)
            {
                Level++;
                Experience = 0f;
                Debug.Log($"[Skill] {Name} augmenté au niveau {Level}");
            }
        }
    }

    public enum SkillType
    {
        Cooking,
        Logic,
        Fitness,
        Charisma,
        Creativity,
        Mischief
    }
}
