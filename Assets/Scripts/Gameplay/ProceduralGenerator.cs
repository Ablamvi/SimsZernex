using UnityEngine;
using SimsZernex.Character;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de génération procédurale pour personnages, maisons, événements
    /// </summary>
    public class ProceduralGenerator : MonoBehaviour
    {
        public static SimCharacterData GenerateRandomSim()
        {
            var data = new SimCharacterData
            {
                name = GetRandomFirstName(),
                familyName = GetRandomLastName(),
                age = Random.Range(18, 65),
                gender = Random.value > 0.5f ? Gender.Female : Gender.Male,
                skinTone = GetRandomSkinTone(),
                traits = GetRandomTraits(),
                aspiration = (Aspiration)Random.Range(0, System.Enum.GetNames(typeof(Aspiration)).Length),
                favoriteFood = GetRandomFood(),
                favoriteMusic = GetRandomMusic(),
                money = Random.Range(15000, 50000)
            };
            return data;
        }

        private static string GetRandomFirstName()
        {
            string[] firstNames = new string[]
            {
                "Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "George", "Hannah",
                "Ivan", "Julia", "Kevin", "Laura", "Michael", "Natalie", "Oscar", "Paula"
            };
            return firstNames[Random.Range(0, firstNames.Length)];
        }

        private static string GetRandomLastName()
        {
            string[] lastNames = new string[]
            {
                "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas"
            };
            return lastNames[Random.Range(0, lastNames.Length)];
        }

        private static string GetRandomSkinTone()
        {
            string[] tones = new string[] { "Light", "Medium", "Dark" };
            return tones[Random.Range(0, tones.Length)];
        }

        private static string[] GetRandomTraits()
        {
            string[] allTraits = new string[]
            {
                "Amiécal", "Grincheux", "Optimiste", "Pessimiste", "Ambitieux", "Paresseux",
                "Énergique", "Somnolent", "Gourmand", "Perfectionniste", "Sociable", "Solitaire"
            };

            var selectedTraits = new List<string>();
            int traitCount = Random.Range(2, 5);
            for (int i = 0; i < traitCount; i++)
            {
                string trait = allTraits[Random.Range(0, allTraits.Length)];
                if (!selectedTraits.Contains(trait))
                    selectedTraits.Add(trait);
            }

            return selectedTraits.ToArray();
        }

        private static string GetRandomFood()
        {
            string[] foods = new string[] { "Pizza", "Steak", "Sushi", "Burger", "Salade", "Pates", "Poulet" };
            return foods[Random.Range(0, foods.Length)];
        }

        private static string GetRandomMusic()
        {
            string[] musics = new string[] { "Pop", "Rock", "Classique", "Jazz", "Electro", "HipHop", "Country" };
            return musics[Random.Range(0, musics.Length)];
        }
    }
}
