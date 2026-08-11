using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système d'objets collectibles et décorations
    /// </summary>
    public class CollectiblesSystem : MonoBehaviour
    {
        private System.Collections.Generic.Dictionary<SimCharacter, System.Collections.Generic.List<Collectible>> collections = new System.Collections.Generic.Dictionary<SimCharacter, System.Collections.Generic.List<Collectible>>();

        public static CollectiblesSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void AddCollectible(SimCharacter sim, Collectible collectible)
        {
            if (!collections.ContainsKey(sim))
                collections[sim] = new System.Collections.Generic.List<Collectible>();
            
            collections[sim].Add(collectible);
            Debug.Log($"[Collectibles] {sim.CharacterData.name} a collecté {collectible.Name}");
        }

        public System.Collections.Generic.List<Collectible> GetCollection(SimCharacter sim)
        {
            return collections.TryGetValue(sim, out var coll) ? coll : new System.Collections.Generic.List<Collectible>();
        }

        public int GetCollectionValue(SimCharacter sim)
        {
            int value = 0;
            foreach (var collectible in GetCollection(sim))
            {
                value += collectible.Value;
            }
            return value;
        }
    }

    [System.Serializable]
    public class Collectible
    {
        public string Name;
        public string Description;
        public int Value = 100;
        public CollectibleType Type;
        public Sprite Icon;

        public Collectible(string name, int value, CollectibleType type)
        {
            Name = name;
            Value = value;
            Type = type;
        }
    }

    public enum CollectibleType
    {
        Art,
        Butterfly,
        Fish,
        Gem,
        Dinosaur,
        Metals,
        Postcard,
        Photography,
        Sculpture
    }
}
