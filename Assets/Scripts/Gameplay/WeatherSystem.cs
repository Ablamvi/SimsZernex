using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système météo - Affecte l'humeur et les activités possibles
    /// </summary>
    public class WeatherSystem : MonoBehaviour
    {
        private WeatherType currentWeather = WeatherType.Sunny;
        private float weatherChangeTimer = 0f;
        private float weatherChangeDuration = 60f; // Change tous les 60 secondes réelles

        public static WeatherSystem Instance { get; private set; }
        public WeatherType CurrentWeather => currentWeather;

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
            weatherChangeTimer += Time.deltaTime;
            
            if (weatherChangeTimer >= weatherChangeDuration)
            {
                ChangeWeather();
                weatherChangeTimer = 0f;
            }
        }

        private void ChangeWeather()
        {
            WeatherType oldWeather = currentWeather;
            currentWeather = (WeatherType)Random.Range(0, System.Enum.GetNames(typeof(WeatherType)).Length);
            
            Debug.Log($"[Weather] Météo changée: {oldWeather} → {currentWeather}");
        }

        public float GetMoodModifier()
        {
            return currentWeather switch
            {
                WeatherType.Sunny => 10f,
                WeatherType.Cloudy => 0f,
                WeatherType.Rainy => -15f,
                WeatherType.Stormy => -25f,
                WeatherType.Snowy => -10f,
                _ => 0f
            };
        }

        public bool CanGoOutside()
        {
            return currentWeather != WeatherType.Stormy;
        }
    }

    public enum WeatherType
    {
        Sunny,
        Cloudy,
        Rainy,
        Stormy,
        Snowy,
        Foggy
    }
}
