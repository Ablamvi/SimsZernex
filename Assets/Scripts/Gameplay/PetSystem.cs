using UnityEngine;
using SimsZernex.Character;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de élevage et soins d'animaux de compagnie
    /// </summary>
    public class PetSystem : MonoBehaviour
    {
        public static PetSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public Pet CreatePet(string name, PetType type)
        {
            var pet = new Pet(name, type);
            Debug.Log($"[PetSystem] Nouvel animal de compagnie: {name} ({type})");
            return pet;
        }

        public void FeedPet(Pet pet, float amount)
        {
            pet.Hunger -= amount;
            pet.Hunger = Mathf.Max(0, pet.Hunger);
            Debug.Log($"[PetSystem] {pet.Name} a mangé");
        }

        public void PlayWithPet(Pet pet, SimCharacter sim, float duration)
        {
            pet.Happiness += duration * 2f;
            pet.Hunger += duration * 0.5f;
            sim.Needs.ReduceSocial(-duration * 3f);
            Debug.Log($"[PetSystem] {sim.CharacterData.name} joue avec {pet.Name}");
        }

        public void BrushPet(Pet pet, float amount)
        {
            pet.Hygiene -= amount;
            pet.Hygiene = Mathf.Max(0, pet.Hygiene);
            Debug.Log($"[PetSystem] {pet.Name} a été brosseré(e)");
        }
    }

    [System.Serializable]
    public class Pet
    {
        public string Name;
        public PetType Type;
        public float Hunger = 50f;
        public float Happiness = 75f;
        public float Hygiene = 75f;
        public int Age = 0;
        public string Breed = "Mixed";

        public Pet(string name, PetType type)
        {
            Name = name;
            Type = type;
        }

        public void Update(float deltaTime)
        {
            Hunger += deltaTime * 0.1f;
            Happiness -= deltaTime * 0.05f;
            Hygiene -= deltaTime * 0.03f;
        }
    }

    public enum PetType
    {
        Cat,
        Dog,
        Bird,
        Rabbit,
        Snake,
        Spider,
        Llama,
        Alien
    }
}
