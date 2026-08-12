using UnityEngine;
using SimsZernex.Data;
using System.Collections.Generic;

namespace SimsZernex.Character
{
    /// <summary>
    /// Représente un personnage Sim - Entité jouable avec besoins, traits et IA
    /// </summary>
    public class SimCharacter : MonoBehaviour
    {
        [SerializeField] private Animator animator;
        [SerializeField] private CharacterController characterController;
        [SerializeField] private float moveSpeed = 5f;

        private SimCharacterData characterData;
        private SimNeeds needs;
        private SimMood mood;
        private SimInventory inventory;
        private SimAI simAI;
        private RelationshipManager relationshipManager;

        // États de vie
        private bool isAlive = true;
        private float energy = 100f;
        private float health = 100f;

        public SimCharacterData CharacterData => characterData;
        public SimNeeds Needs => needs;
        public SimMood Mood => mood;
        public SimInventory Inventory => inventory;
        public RelationshipManager RelationshipManager => relationshipManager;
        public bool IsAlive => isAlive;
        public float Energy => energy;
        public float Health => health;

        public void Initialize(SimCharacterData data)
        {
            characterData = data;
            needs = new SimNeeds();
            mood = new SimMood();
            inventory = new SimInventory();
            relationshipManager = new RelationshipManager();
            simAI = GetComponent<SimAI>();

            if (simAI == null)
                simAI = gameObject.AddComponent<SimAI>();

            simAI.Initialize(this);

            // Configurer le visuel
            SetupVisuals();

            Debug.Log($"[SimCharacter] {data.name} initialisé - Traits: {string.Join(", ", data.traits)}");
        }

        private void SetupVisuals()
        {
            gameObject.name = characterData.name;

            // Créer un simple mesh pour le Sim (à remplacer par un modèle 3D)
            var meshFilter = gameObject.AddComponent<MeshFilter>();
            var meshRenderer = gameObject.AddComponent<MeshRenderer>();
            
            meshFilter.mesh = CreateSimMesh();
            meshRenderer.material = new Material(Shader.Find("Standard"));
            meshRenderer.material.color = GetSkinColor();
        }

        private Mesh CreateSimMesh()
        {
            // Créer un cube simple comme placeholder
            var mesh = new Mesh();
            Vector3[] vertices = new Vector3[8]
            {
                new Vector3(0, 0, 0),
                new Vector3(0.5f, 0, 0),
                new Vector3(0.5f, 1.8f, 0),
                new Vector3(0, 1.8f, 0),
                new Vector3(0, 0, 0.5f),
                new Vector3(0.5f, 0, 0.5f),
                new Vector3(0.5f, 1.8f, 0.5f),
                new Vector3(0, 1.8f, 0.5f)
            };
            mesh.vertices = vertices;

            int[] triangles = new int[36]
            {
                0, 2, 1, 0, 3, 2,
                4, 5, 6, 4, 6, 7,
                0, 1, 5, 0, 5, 4,
                2, 3, 7, 2, 7, 6,
                0, 4, 7, 0, 7, 3,
                1, 2, 6, 1, 6, 5
            };
            mesh.triangles = triangles;
            mesh.RecalculateNormals();
            return mesh;
        }

        private Color GetSkinColor()
        {
            return characterData.skinTone switch
            {
                "Light" => new Color(0.9f, 0.8f, 0.7f),
                "Medium" => new Color(0.8f, 0.6f, 0.4f),
                "Dark" => new Color(0.5f, 0.3f, 0.2f),
                _ => new Color(0.8f, 0.7f, 0.6f)
            };
        }

        public void UpdateNeeds(float deltaTime)
        {
            if (!isAlive) return;

            needs.Update(deltaTime, characterData.traits);
            
            // Vérifier si le Sim meurt de faim ou de fatigue extrême
            if (needs.Hunger < 0 || needs.Energy < 0)
            {
                health -= deltaTime * 5f;
                if (health <= 0)
                {
                    Die("Malnutrition/Fatigue extrême");
                }
            }
        }

        public void UpdateMood()
        {
            if (!isAlive) return;
            mood.Calculate(needs, characterData.traits);
        }

        public void MoveTo(Vector3 targetPosition)
        {
            if (characterController == null) return;

            Vector3 direction = (targetPosition - transform.position).normalized;
            if (direction.magnitude > 0.1f)
            {
                characterController.SimpleMove(direction * moveSpeed);
            }
        }

        public void PerformAction(SimAction action)
        {
            if (!isAlive) return;

            switch (action.ActionType)
            {
                case ActionType.Eat:
                    needs.ReduceHunger(action.EffectValue);
                    energy -= 5f;
                    break;
                case ActionType.Sleep:
                    needs.ReduceEnergy(-action.EffectValue);
                    needs.ReduceHygiene(-5f);
                    break;
                case ActionType.Shower:
                    needs.ReduceHygiene(-action.EffectValue);
                    energy -= 10f;
                    break;
                case ActionType.Work:
                    energy -= 20f;
                    // Augmenter le skill
                    break;
                case ActionType.Relax:
                    needs.ReduceFun(action.EffectValue);
                    energy -= 5f;
                    break;
                case ActionType.Socialize:
                    needs.ReduceSocial(-action.EffectValue);
                    energy -= 10f;
                    break;
            }
        }

        public void TakeDamage(float amount)
        {
            health -= amount;
            if (health <= 0)
                Die("Blessure");
        }

        public void Die(string cause)
        {
            if (!isAlive) return;

            isAlive = false;
            Debug.Log($"[SimCharacter] {characterData.name} est décédé(e) - Cause: {cause}");
            
            // Déclencher un événement
            if (GameManager.Instance)
            {
                var deathEvent = new GameEvent(
                    EventType.Death,
                    $"{characterData.name} est décédé(e) de {cause}",
                    EventImpact.Negative
                );
                GameManager.Instance.EventManager.TriggerEvent(deathEvent);
            }

            Destroy(gameObject, 2f);
        }

        public SimCharacterData GetCharacterData()
        {
            // Sauvegarder l'état actuel
            characterData.currentHunger = needs.Hunger;
            characterData.currentEnergy = needs.Energy;
            characterData.currentHygiene = needs.Hygiene;
            return characterData;
        }

        private void OnDestroy()
        {
            if (GameManager.Instance && !isAlive)
            {
                // Nettoyer les références
            }
        }
    }

    [System.Serializable]
    public class SimAction
    {
        public ActionType ActionType;
        public float EffectValue = 20f;
        public float Duration = 1f;
    }

    public enum ActionType
    {
        Eat,
        Sleep,
        Shower,
        Work,
        Relax,
        Socialize,
        Exercise,
        Study,
        Cook,
        Clean,
        Idle
    }
}
