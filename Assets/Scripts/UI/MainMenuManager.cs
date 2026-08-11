using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.UI
{
    /// <summary>
    /// Gère le menu principal du jeu
    /// </summary>
    public class MainMenuManager : MonoBehaviour
    {
        private Canvas menuCanvas;

        private void Start()
        {
            menuCanvas = GetComponentInChildren<Canvas>();
            if (GameManager.Instance)
            {
                GameManager.Instance.SetGameState(GameState.MainMenu);
            }
        }

        public void StartNewGame()
        {
            Debug.Log("[MainMenu] Nouvelle partie");
            GameManager.Instance.SetGameState(GameState.CharacterCreation);
        }

        public void LoadGame()
        {
            Debug.Log("[MainMenu] Charger partie");
            var saves = GameManager.Instance.DataManager.GetSaveFiles();
            if (saves.Count > 0)
            {
                GameManager.Instance.LoadGame(saves[0]);
            }
        }

        public void Settings()
        {
            Debug.Log("[MainMenu] Paramètres");
        }

        public void QuitGame()
        {
            Debug.Log("[MainMenu] Quitter");
            GameManager.Instance.QuitGame();
        }
    }
}
