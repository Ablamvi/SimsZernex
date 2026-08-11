using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système d'alchimie et laboratoire scientifique
    /// </summary>
    public class LaboratorySystem : MonoBehaviour
    {
        public static LaboratorySystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void ConductExperiment(SimCharacter sim, ExperimentType experimentType)
        {
            Debug.Log($"[Laboratory] {sim.CharacterData.name} conduit l'expérience: {experimentType}");
            
            switch (experimentType)
            {
                case ExperimentType.TimeWarp:
                    TimeManager.Instance.SetTimeScale(0.5f);
                    break;
                case ExperimentType.GrowthPotion:
                    Debug.Log("Potion de croissance créée!");
                    break;
                case ExperimentType.HypnosisRay:
                    Debug.Log("Rayon d'hypnose activé!");
                    break;
                case ExperimentType.FusionDevice:
                    Debug.Log("Appareil de fusion en fonctionnement!");
                    break;
            }
        }
    }

    public enum ExperimentType
    {
        TimeWarp,
        GrowthPotion,
        HypnosisRay,
        FusionDevice,
        VampireConverter,
        WerewolfSerum,
        FairyTransformation,
        AlienAbduction
    }
}
