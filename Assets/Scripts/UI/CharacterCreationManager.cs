using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;
using SimsZernex.Data;

namespace SimsZernex.UI
{
    /// <summary>
    /// Gère le menu de création de personnage
    /// </summary>
    public class CharacterCreationManager : MonoBehaviour
    {
        private SimCharacterData characterData;

        public void CreateNewCharacter()
        {
            // Créer un personnage par défaut ou avec des options personnalisées
            characterData = new SimCharacterData
            {
                name = "MonSim",
                familyName = "Fam",
                age = 25,
                gender = Gender.Female,
                traits = new string[] { "Amiéral", "Énergique" },
                houseSeed = Random.Range(0, 999999)
            };

            // Démarrer le jeu
            GameManager.Instance.CreateNewGame(characterData);
        }

        public void CreateRandomCharacter()
        {
            characterData = ProceduralGenerator.GenerateRandomSim();
            characterData.houseSeed = Random.Range(0, 999999);
            GameManager.Instance.CreateNewGame(characterData);
        }

        public void SetCharacterName(string name)
        {
            if (characterData != null)
                characterData.name = name;
        }

        public void SetCharacterGender(int genderIndex)
        {
            if (characterData != null)
                characterData.gender = (Gender)genderIndex;
        }

        public void SetCharacterTrait(string trait)
        {
            if (characterData != null)
            {
                // Ajouter trait
            }
        }
    }
}
