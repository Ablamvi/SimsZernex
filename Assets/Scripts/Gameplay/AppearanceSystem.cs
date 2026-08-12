using UnityEngine;
using SimsZernex.Character;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système d'apparence - Vêtements, coiffures, cosmétiques
    /// </summary>
    public class AppearanceSystem : MonoBehaviour
    {
        public static AppearanceSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void ChangeClothing(SimCharacter sim, string clothingType, Color color)
        {
            Debug.Log($"[Appearance] {sim.CharacterData.name} porte maintenant un {clothingType}");
            // Modifier le mesh du Sim pour afficher les vêtements
        }

        public void ChangeHairstyle(SimCharacter sim, string hairstyle, Color hairColor)
        {
            sim.CharacterData.hairColor = hairColor;
            Debug.Log($"[Appearance] {sim.CharacterData.name} a changé sa coiffure en {hairstyle}");
        }

        public void ApplyMakeup(SimCharacter sim, string makeupType)
        {
            Debug.Log($"[Appearance] {sim.CharacterData.name} applique un maquillage {makeupType}");
        }

        public void ChangeSkinTone(SimCharacter sim, string skinTone)
        {
            sim.CharacterData.skinTone = skinTone;
            Debug.Log($"[Appearance] {sim.CharacterData.name} teint changé en {skinTone}");
        }
    }
}
