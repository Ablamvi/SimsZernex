using UnityEngine;
using SimsZernex.Core;
using SimsZernex.Character;

namespace SimsZernex.Utilities
{
    /// <summary>
    /// Système de gestion des performances - Profiling & optimisation
    /// </summary>
    public class PerformanceManager : MonoBehaviour
    {
        private float deltaTimeSum = 0f;
        private float averageDeltaTime = 0f;
        private int frameCount = 0;

        public static PerformanceManager Instance { get; private set; }

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
            deltaTimeSum += Time.deltaTime;
            frameCount++;

            if (frameCount >= 60)
            {
                averageDeltaTime = deltaTimeSum / frameCount;
                
                if (averageDeltaTime > 0.02f) // Plus de 50ms, performance issue
                {
                    Debug.LogWarning($"[Performance] Lag détecté: {averageDeltaTime * 1000:F2}ms");
                    OptimizeIfNeeded();
                }

                deltaTimeSum = 0f;
                frameCount = 0;
            }
        }

        private void OptimizeIfNeeded()
        {
            // Réduire la qualité graphique temporairement
            QualitySettings.maxQueuedFrames = 1;
            Debug.Log("[Performance] Optimisation appliquée");
        }

        public float GetAverageDeltaTime() => averageDeltaTime;
        public float GetCurrentFPS() => 1f / Time.deltaTime;
    }
}
