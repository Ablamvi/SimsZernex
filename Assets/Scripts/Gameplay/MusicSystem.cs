using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système d'instruments et de musique pour les Sims
    /// </summary>
    public class MusicSystem : MonoBehaviour
    {
        public static MusicSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void PlayInstrument(SimCharacter sim, InstrumentType instrument)
        {
            Debug.Log($"[MusicSystem] {sim.CharacterData.name} joue du {instrument}");
            sim.Needs.ReduceFun(-20f);
            SkillSystem.Instance.IncreaseSkill(sim, SkillType.Creativity, 5f);
        }

        public void TakeLesson(SimCharacter sim, SimCharacter teacher, InstrumentType instrument)
        {
            Debug.Log($"[MusicSystem] {sim.CharacterData.name} prend cours de {instrument} avec {teacher.CharacterData.name}");
            SkillSystem.Instance.IncreaseSkill(sim, SkillType.Creativity, 10f);
        }

        public void PracticeInstrument(SimCharacter sim, InstrumentType instrument, float duration)
        {
            Debug.Log($"[MusicSystem] {sim.CharacterData.name} s'entraîne au {instrument} pendant {duration} minutes");
            for (int i = 0; i < duration; i++)
            {
                SkillSystem.Instance.IncreaseSkill(sim, SkillType.Creativity, 1f);
            }
        }
    }

    public enum InstrumentType
    {
        Piano,
        Guitar,
        Violin,
        Drums,
        Flute,
        Trumpet,
        Harp,
        Saxophone
    }
}
