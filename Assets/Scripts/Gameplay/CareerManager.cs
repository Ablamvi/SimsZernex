using UnityEngine;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Gère les carrières et l'emploi des Sims
    /// </summary>
    public class CareerManager : MonoBehaviour
    {
        private Career[] availableCareers;

        private void Start()
        {
            InitializeCareers();
        }

        private void InitializeCareers()
        {
            availableCareers = new Career[]
            {
                new Career("Scientifique", "Laboratory", 1500, 0.5f, "Logic"),
                new Career("Chef Cuisinier", "Restaurant", 1200, 0.4f, "Cooking"),
                new Career("Architecte", "Office", 2000, 0.6f, "Creativity"),
                new Career("Entraîneur Fitness", "Gym", 1000, 0.3f, "Fitness"),
                new Career("Écrivain", "Office", 1300, 0.5f, "Creativity"),
                new Career("Musicien", "Recreation", 1100, 0.4f, "Creativity")
            };
        }

        public Career GetCareerByName(string name)
        {
            foreach (var career in availableCareers)
            {
                if (career.Name == name)
                    return career;
            }
            return null;
        }

        public Career[] GetAvailableCareers()
        {
            return availableCareers;
        }
    }

    [System.Serializable]
    public class Career
    {
        public string Name;
        public string Workplace;
        public int DailyPay;
        public float SkillRequirement;
        public string PrimarySkill;
        public int CurrentLevel = 1;
        public float LevelProgress = 0f;

        public Career(string name, string workplace, int pay, float skillReq, string skill)
        {
            Name = name;
            Workplace = workplace;
            DailyPay = pay;
            SkillRequirement = skillReq;
            PrimarySkill = skill;
        }

        public void Progress(float amount)
        {
            LevelProgress += amount;
            if (LevelProgress >= 100f)
            {
                CurrentLevel++;
                LevelProgress = 0f;
                DailyPay = (int)(DailyPay * 1.2f); // Augmentation de 20%
            }
        }
    }
}
