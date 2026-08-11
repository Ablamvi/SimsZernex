using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.UI
{
    /// <summary>
    /// Gère l'interface HUD du jeu
    /// </summary>
    public class HUDManager : MonoBehaviour
    {
        [SerializeField] private Canvas hudCanvas;
        private SimCharacter currentSim;
        private GameTime currentGameTime;

        public static HUDManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        private void Start()
        {
            if (hudCanvas == null)
                hudCanvas = FindObjectOfType<Canvas>();

            // S'abonner aux changements de temps
            if (GameManager.Instance && GameManager.Instance.TimeManager)
            {
                GameManager.Instance.TimeManager.OnTimeChanged += UpdateTimeDisplay;
            }
        }

        public void SetActiveSim(SimCharacter sim)
        {
            currentSim = sim;
            UpdateSimDisplay();
        }

        public void UpdateSimDisplay()
        {
            if (currentSim == null) return;
            
            // Afficher les besoins
            Debug.Log($"[HUD] {currentSim.CharacterData.name} - Besoins: {currentSim.Needs}");
            Debug.Log($"[HUD] Humeur: {currentSim.Mood.CurrentMood} ({currentSim.Mood.MoodValue:F1})");
            Debug.Log($"[HUD] Énergie: {currentSim.Energy:F1} | Santé: {currentSim.Health:F1}");
        }

        private void UpdateTimeDisplay(GameTime time)
        {
            currentGameTime = time;
            Debug.Log($"[HUD] {time}");
        }

        private void OnDestroy()
        {
            if (GameManager.Instance && GameManager.Instance.TimeManager)
            {
                GameManager.Instance.TimeManager.OnTimeChanged -= UpdateTimeDisplay;
            }
        }
    }
}
