using UnityEngine;
using System.Collections.Generic;

namespace SimsZernex.Data
{
    /// <summary>
    /// Données sérialisables d'un personnage Sim
    /// </summary>
    [System.Serializable]
    public class SimCharacterData
    {
        [Header("Infos de base")]
        public string name = "Sim";
        public string familyName = "Unknown";
        public int age = 25;
        public Gender gender = Gender.Female;
        public string skinTone = "Medium";
        public Color hairColor = Color.black;
        public Color eyeColor = Color.blue;

        [Header("Traits & Personnalité")]
        public string[] traits = new string[] { "Amical" };
        public Aspiration aspiration = Aspiration.None;
        public string favoriteMusic = "Pop";
        public string favoriteFood = "Pizza";

        [Header("État de santé")]
        public float currentHunger = 75f;
        public float currentEnergy = 75f;
        public float currentHygiene = 75f;
        public float currentFun = 50f;
        public float currentSocial = 50f;
        public float currentBladder = 75f;

        [Header("Compétences")]
        public int cookingSkill = 1;
        public int logicSkill = 1;
        public int fitnessSkill = 1;
        public int charismaSkill = 1;
        public int creativitySkill = 1;

        [Header("Économie & Carrière")]
        public int money = 20000;
        public string careerTitle = "Unemployed";
        public int careerLevel = 0;
        public float careerProgress = 0f;

        [Header("Maison")]
        public int houseSeed = 0;
        public Vector3 housePosition = Vector3.zero;

        [Header("Vie Personnelle")]
        public int dayOfBirth = 1;
        public int currentDay = 1;
        public int deathDay = -1; // -1 si vivant
        public bool isPregnant = false;
        public int pregnancyDays = 0;
    }

    public enum Gender
    {
        Male,
        Female,
        NonBinary
    }

    public enum Aspiration
    {
        None,
        FamilyOriented,
        CareerFocused,
        Romantic,
        FriendlySimplicity,
        Knowledge,
        Creativity,
        Wealth,
        Supernatural
    }
}
