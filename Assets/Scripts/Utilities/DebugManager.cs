using UnityEngine;
using SimsZernex.Core;
using SimsZernex.Character;

namespace SimsZernex.Utilities
{
    /// <summary>
    /// Système de debug avancé avec affichage en temps réel
    /// </summary>
    public class DebugManager : MonoBehaviour
    {
        private bool showDebugInfo = true;
        private bool showSimStats = true;
        private bool showPerformance = true;
        private float fps = 0f;
        private float updateTime = 0f;

        public static DebugManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        private void Update()
        {
            // Calculer FPS
            updateTime += Time.deltaTime;
            if (updateTime >= 1f)
            {
                fps = Mathf.Round(1f / Time.deltaTime);
                updateTime = 0f;
            }

            // Toggle debug avec F3
            if (Input.GetKeyDown(KeyCode.F3))
                showDebugInfo = !showDebugInfo;
        }

        private void OnGUI()
        {
            if (!showDebugInfo) return;

            GUI.skin.label.fontSize = 12;
            GUILayout.BeginArea(new Rect(10, 10, 400, 300));
            
            GUILayout.Label($"FPS: {fps}", GUILayout.Width(100));
            GUILayout.Label($"Temps jeu: {GameManager.Instance?.TimeManager.CurrentGameTime}");
            
            if (GameManager.Instance?.ActiveSim != null)
            {
                var sim = GameManager.Instance.ActiveSim;
                GUILayout.Label($"\n=== {sim.CharacterData.name} ===");
                GUILayout.Label($"Besoins: {sim.Needs}");
                GUILayout.Label($"Humeur: {sim.Mood.CurrentMood} ({sim.Mood.MoodValue:F1})");
                GUILayout.Label($"Santé: {sim.Health:F1} | Énergie: {sim.Energy:F1}");
                GUILayout.Label($"Position: {sim.transform.position}");
            }

            GUILayout.Label("\n[F3] Toggle Debug | [ESC] Pause");
            GUILayout.EndArea();
        }

        public void LogGameEvent(string category, string message)
        {
            Debug.Log($"[{category}] {message}");
        }
    }
}
