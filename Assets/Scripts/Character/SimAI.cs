using UnityEngine;
using SimsZernex.Core;

namespace SimsZernex.Character
{
    /// <summary>
    /// Système d'IA autonome pour les Sims - Prennent des décisions basées sur les besoins
    /// </summary>
    public class SimAI : MonoBehaviour
    {
        private SimCharacter sim;
        private SimNeeds needs;
        private SimMood mood;
        private ActionType currentAction = ActionType.Idle;
        private float actionTimer = 0f;
        private Vector3 targetPosition;
        private bool hasTarget = false;

        private GameSettings gameSettings;
        private float decisionInterval = 5f; // Prendre une décision toutes les 5 secondes
        private float decisionTimer = 0f;

        public ActionType CurrentAction => currentAction;

        public void Initialize(SimCharacter simCharacter)
        {
            sim = simCharacter;
            needs = simCharacter.Needs;
            mood = simCharacter.Mood;
            gameSettings = Resources.Load<GameSettings>("GameSettings");
        }

        private void Update()
        {
            if (sim == null || !sim.IsAlive) return;

            decisionTimer -= Time.deltaTime;
            if (decisionTimer <= 0)
            {
                MakeDecision();
                decisionTimer = decisionInterval;
            }

            ExecuteCurrentAction();
        }

        private void MakeDecision()
        {
            // Priorité: besoins critiques d'abord
            if (needs.IsHungry)
            {
                currentAction = ActionType.Eat;
                return;
            }

            if (needs.IsTired)
            {
                currentAction = ActionType.Sleep;
                return;
            }

            if (needs.IsDirty)
            {
                currentAction = ActionType.Shower;
                return;
            }

            if (needs.NeedsBathroom)
            {
                currentAction = ActionType.Work; // Placeholder: chercher une salle de bain
                return;
            }

            if (needs.IsLonely && Random.value > 0.5f)
            {
                currentAction = ActionType.Socialize;
                return;
            }

            if (needs.IsUnhappy)
            {
                currentAction = ActionType.Relax;
                return;
            }

            // Sinon, activités normales
            float randomValue = Random.value;
            currentAction = randomValue switch
            {
                < 0.3f => ActionType.Work,
                < 0.5f => ActionType.Relax,
                < 0.7f => ActionType.Socialize,
                < 0.85f => ActionType.Exercise,
                _ => ActionType.Idle
            };
        }

        private void ExecuteCurrentAction()
        {
            switch (currentAction)
            {
                case ActionType.Idle:
                    // Rester sur place
                    break;
                case ActionType.Eat:
                    // Trouver la cuisine et manger
                    needs.ReduceHunger(20f * Time.deltaTime);
                    actionTimer += Time.deltaTime;
                    if (actionTimer > 5f)
                    {
                        currentAction = ActionType.Idle;
                        actionTimer = 0f;
                    }
                    break;
                case ActionType.Sleep:
                    // Trouver un lit et dormir
                    needs.ReduceEnergy(-30f * Time.deltaTime);
                    actionTimer += Time.deltaTime;
                    if (actionTimer > 3f || needs.Energy > 90)
                    {
                        currentAction = ActionType.Idle;
                        actionTimer = 0f;
                    }
                    break;
                case ActionType.Shower:
                    // Trouver une salle de bain et se doucher
                    needs.ReduceHygiene(-15f * Time.deltaTime);
                    actionTimer += Time.deltaTime;
                    if (actionTimer > 2f)
                    {
                        currentAction = ActionType.Idle;
                        actionTimer = 0f;
                    }
                    break;
                case ActionType.Relax:
                    // Se reposer sur un canapé
                    needs.ReduceFun(-10f * Time.deltaTime);
                    actionTimer += Time.deltaTime;
                    if (actionTimer > 3f)
                    {
                        currentAction = ActionType.Idle;
                        actionTimer = 0f;
                    }
                    break;
                case ActionType.Socialize:
                    // Chercher d'autres Sims pour socialiser
                    needs.ReduceSocial(-15f * Time.deltaTime);
                    actionTimer += Time.deltaTime;
                    if (actionTimer > 4f)
                    {
                        currentAction = ActionType.Idle;
                        actionTimer = 0f;
                    }
                    break;
                case ActionType.Exercise:
                    // Faire du sport
                    needs.ReduceEnergy(-10f * Time.deltaTime);
                    actionTimer += Time.deltaTime;
                    if (actionTimer > 3f)
                    {
                        currentAction = ActionType.Idle;
                        actionTimer = 0f;
                    }
                    break;
            }
        }
    }
}
