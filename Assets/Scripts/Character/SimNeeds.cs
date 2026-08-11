using UnityEngine;
using SimsZernex.Core;

namespace SimsZernex.Character
{
    /// <summary>
    /// Gère les 6 besoins principaux du Sim: Faim, Énergie, Hygiène, Fun, Social, Vessie
    /// </summary>
    public class SimNeeds
    {
        public float Hunger { get; private set; } = 75f;      // 0 = mourant de faim
        public float Energy { get; private set; } = 75f;      // 0 = mort de fatigue
        public float Hygiene { get; private set; } = 75f;     // 0 = très sale
        public float Fun { get; private set; } = 50f;         // 0 = déprimé
        public float Social { get; private set; } = 50f;      // 0 = très seul
        public float Bladder { get; private set; } = 75f;     // 0 = urgence

        private GameSettings gameSettings;
        private float decayMultiplier = 1f;

        public SimNeeds()
        {
            gameSettings = Resources.Load<GameSettings>("GameSettings");
        }

        public void Update(float deltaTime, string[] traits)
        {
            // Appliquer les modificateurs de traits
            decayMultiplier = 1f;
            if (System.Array.Exists(traits, t => t == "Gourmand"))
                decayMultiplier += 0.3f;
            if (System.Array.Exists(traits, t => t == "Énergique"))
                decayMultiplier -= 0.2f;
            if (System.Array.Exists(traits, t => t == "Perfectionniste"))
                decayMultiplier -= 0.1f; // Moins de besoins

            float decayRate = (gameSettings?.NeedDecayRate ?? 0.5f) * decayMultiplier;

            // Diminuer les besoins
            Hunger = Mathf.Max(0, Hunger - decayRate * deltaTime);
            Energy = Mathf.Max(0, Energy - decayRate * 0.3f * deltaTime);  // L'énergie décroît moins vite
            Hygiene = Mathf.Max(0, Hygiene - decayRate * 0.5f * deltaTime);
            Fun = Mathf.Max(0, Fun - decayRate * 0.4f * deltaTime);
            Social = Mathf.Max(0, Social - decayRate * 0.3f * deltaTime);
            Bladder = Mathf.Max(0, Bladder - decayRate * 0.6f * deltaTime);
        }

        // Réducteurs de besoins (values positives réduisent le besoin)
        public void ReduceHunger(float amount) => Hunger = Mathf.Min(100, Hunger + amount);
        public void ReduceEnergy(float amount) => Energy = Mathf.Min(100, Energy + amount);
        public void ReduceHygiene(float amount) => Hygiene = Mathf.Min(100, Hygiene + amount);
        public void ReduceFun(float amount) => Fun = Mathf.Min(100, Fun + amount);
        public void ReduceSocial(float amount) => Social = Mathf.Min(100, Social + amount);
        public void ReduceBladder(float amount) => Bladder = Mathf.Min(100, Bladder + amount);

        // Vérifier les seuils critiques
        public bool IsHungry => Hunger < 30;
        public bool IsTired => Energy < 30;
        public bool IsDirty => Hygiene < 30;
        public bool IsUnhappy => Fun < 30;
        public bool IsLonely => Social < 30;
        public bool NeedsBathroom => Bladder < 30;

        public float GetAverageNeed()
        {
            return (Hunger + Energy + Hygiene + Fun + Social + Bladder) / 6f;
        }

        public override string ToString()
        {
            return $"Faim: {Hunger:F1} | Énergie: {Energy:F1} | Hygiène: {Hygiene:F1} | Fun: {Fun:F1} | Social: {Social:F1} | Vessie: {Bladder:F1}";
        }
    }

    /// <summary>
    /// Gère l'humeur basée sur les besoins et les traits
    /// </summary>
    public class SimMood
    {
        public MoodType CurrentMood { get; private set; } = MoodType.Fine;
        public float MoodValue { get; private set; } = 0f; // -100 à 100
        public string MoodReason { get; private set; } = "Neutre";

        public void Calculate(SimNeeds needs, string[] traits)
        {
            float moodScore = 50f; // Base neutre

            // Impact des besoins
            moodScore += (needs.Hunger / 100f) * 20f;
            moodScore += (needs.Energy / 100f) * 20f;
            moodScore += (needs.Hygiene / 100f) * 15f;
            moodScore += (needs.Fun / 100f) * 15f;
            moodScore += (needs.Social / 100f) * 15f;
            moodScore += (needs.Bladder / 100f) * 15f;

            // Traits affectant l'humeur
            if (System.Array.Exists(traits, t => t == "Optimiste"))
                moodScore += 15f;
            if (System.Array.Exists(traits, t => t == "Pessimiste"))
                moodScore -= 15f;
            if (System.Array.Exists(traits, t => t == "Grincheux") && needs.Energy < 50)
                moodScore -= 25f;

            MoodValue = Mathf.Clamp(moodScore - 50f, -100f, 100f);

            // Déterminer le type d'humeur
            CurrentMood = MoodValue switch
            {
                < -60 => MoodType.Miserable,
                < -30 => MoodType.Sad,
                < -10 => MoodType.Moody,
                < 10 => MoodType.Fine,
                < 30 => MoodType.Happy,
                < 60 => MoodType.VeryHappy,
                _ => MoodType.Ecstatic
            };

            // Raison
            if (needs.IsHungry) MoodReason = "Affamé";
            else if (needs.IsTired) MoodReason = "Fatigué";
            else if (needs.IsDirty) MoodReason = "Pas hygiénique";
            else if (needs.IsLonely) MoodReason = "Seul";
            else MoodReason = CurrentMood.ToString();
        }
    }

    public enum MoodType
    {
        Miserable,
        Sad,
        Moody,
        Fine,
        Happy,
        VeryHappy,
        Ecstatic
    }

    /// <summary>
    /// Gère l'inventaire du Sim
    /// </summary>
    public class SimInventory
    {
        private System.Collections.Generic.List<Item> items = new System.Collections.Generic.List<Item>();
        private int maxCapacity = 50;
        private float currentWeight = 0f;

        public void AddItem(Item item)
        {
            if (items.Count < maxCapacity && currentWeight + item.Weight <= 100f)
            {
                items.Add(item);
                currentWeight += item.Weight;
            }
        }

        public void RemoveItem(Item item)
        {
            if (items.Remove(item))
                currentWeight -= item.Weight;
        }

        public int GetItemCount() => items.Count;
    }

    [System.Serializable]
    public class Item
    {
        public string Name;
        public float Weight = 1f;
        public ItemType Type;
        public float Value = 10f;
    }

    public enum ItemType
    {
        Food,
        Beverage,
        Clothing,
        Furniture,
        Toy,
        Book,
        Tool,
        Collectible
    }
}
