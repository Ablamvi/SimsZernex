using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;
using SimsZernex.World;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de mode de vie - Habitudes quotidiennes, routines et préférences
    /// </summary>
    public class LifestyleSystem : MonoBehaviour
    {
        public static LifestyleSystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void SetDailyRoutine(SimCharacter sim, DailyRoutine routine)
        {
            Debug.Log($"[LifestyleSystem] {sim.CharacterData.name} a une nouvelle routine quotidienne");
        }

        public void EatFavoriteMeal(SimCharacter sim)
        {
            Debug.Log($"[LifestyleSystem] {sim.CharacterData.name} mange son plat préféré: {sim.CharacterData.favoriteFood}");
            sim.Needs.ReduceHunger(-40f);
            sim.Mood.MoodValue += 15f;
        }

        public void ListenToFavoriteMusic(SimCharacter sim)
        {
            Debug.Log($"[LifestyleSystem] {sim.CharacterData.name} écoute sa musique préférée: {sim.CharacterData.favoriteMusic}");
            sim.Needs.ReduceFun(-30f);
            sim.Mood.MoodValue += 20f;
        }

        public void TravelToLocation(SimCharacter sim, Location destination)
        {
            Debug.Log($"[LifestyleSystem] {sim.CharacterData.name} voyage vers {destination.LocationName}");
            sim.Needs.ReduceEnergy(-10f);
            sim.transform.position = destination.GetRandomPositionInLocation();
        }

        public void TakeVacation(SimCharacter sim, int days)
        {
            Debug.Log($"[LifestyleSystem] {sim.CharacterData.name} prend des vacances de {days} jours!");
            for (int i = 0; i < days; i++)
            {
                sim.Needs.ReduceStress(20f);
            }
        }
    }

    [System.Serializable]
    public class DailyRoutine
    {
        public string Name;
        public int WakeUpTime = 7;
        public int SleepTime = 23;
        public ActionType[] ScheduledActivities;

        public DailyRoutine(string name)
        {
            Name = name;
        }
    }
}
