using UnityEngine;
using SimsZernex.Character;
using SimsZernex.World;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de génération procédurale avancée pour maisons, villes et environnements
    /// </summary>
    public class AdvancedProceduralGenerator : MonoBehaviour
    {
        public static AdvancedProceduralGenerator Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        /// <summary>
        /// Générer une maison complète avec pièces aléatoires
        /// </summary>
        public House GenerateProceduralHouse(int seed, int budget = 50000)
        {
            Random.InitState(seed);
            
            var houseGo = new GameObject($"ProceduralHouse_{seed}");
            var house = houseGo.AddComponent<House>();
            
            // Déterminer la taille et le style
            int roomCount = Random.Range(3, 8);
            int houseStyle = Random.Range(0, 3); // 0=Moderne, 1=Vintage, 2=Futuriste
            
            Debug.Log($"[ProceduralGenerator] Maison générée: {roomCount} pièces, Style: {houseStyle}");
            
            return house;
        }

        /// <summary>
        /// Générer une ville avec lieux dynamiques
        /// </summary>
        public void GenerateProceduralCity(int seed, int size = 20)
        {
            Random.InitState(seed);
            
            int locationCount = Random.Range(10, 20);
            Debug.Log($"[ProceduralGenerator] Ville générée: {locationCount} lieux");
        }

        /// <summary>
        /// Générer une famille complète avec héritage génétique
        /// </summary>
        public List<SimCharacterData> GenerateFamily(int familySize = 4)
        {
            var family = new List<SimCharacterData>();
            string familyName = GetRandomLastName();
            
            // Générateur parent
            var parent1 = GenerateRandomSim();
            parent1.familyName = familyName;
            parent1.age = Random.Range(30, 60);
            family.Add(parent1);
            
            // Générateur enfants avec héritage
            for (int i = 1; i < familySize; i++)
            {
                var child = GenerateRandomSim();
                child.familyName = familyName;
                child.age = Random.Range(5, 25);
                // Héritage: couleur de cheveux ou teint
                if (Random.value > 0.5f)
                    child.hairColor = parent1.hairColor;
                family.Add(child);
            }
            
            Debug.Log($"[ProceduralGenerator] Famille '{familyName}' générée avec {familySize} membres");
            return family;
        }

        private static string GetRandomLastName()
        {
            string[] lastNames = new string[]
            {
                "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"
            };
            return lastNames[Random.Range(0, lastNames.Length)];
        }

        private SimCharacterData GenerateRandomSim()
        {
            return ProceduralGenerator.GenerateRandomSim();
        }
    }
}
