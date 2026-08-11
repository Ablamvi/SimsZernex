using UnityEngine;
using SimsZernex.Character;
using SimsZernex.World;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Contrôle les interactions 3D entre Sims et objets du monde
    /// </summary>
    public class InteractionSystem : MonoBehaviour
    {
        private SimCharacter selectedSim;
        private Vector3 targetPosition;
        private bool isPathfinding = false;

        private void Update()
        {
            if (Input.GetMouseButtonDown(0))
            {
                HandleMouseClick();
            }
        }

        private void HandleMouseClick()
        {
            Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
            
            if (Physics.Raycast(ray, out RaycastHit hit))
            {
                // Vérifier si on a cliqué sur un Sim
                var sim = hit.collider.GetComponent<SimCharacter>();
                if (sim != null)
                {
                    SelectSim(sim);
                    return;
                }

                // Vérifier si on a cliqué sur un objet interactif
                var interactiveObj = hit.collider.GetComponent<InteractiveObject>();
                if (interactiveObj != null && selectedSim != null)
                {
                    InteractWithObject(selectedSim, interactiveObj);
                    return;
                }

                // Sinon, déplacer le Sim sélectionné
                if (selectedSim != null)
                {
                    targetPosition = hit.point;
                    isPathfinding = true;
                }
            }
        }

        private void SelectSim(SimCharacter sim)
        {
            selectedSim = sim;
            Debug.Log($"[InteractionSystem] {sim.CharacterData.name} sélectionné");
        }

        private void InteractWithObject(SimCharacter sim, InteractiveObject obj)
        {
            Debug.Log($"[InteractionSystem] {sim.CharacterData.name} interagit avec {obj.ObjectName}");
            obj.UseObject(sim);

            // Simuler l'interaction
            switch (obj.InteractiveType)
            {
                case InteractiveType.Bed:
                    sim.PerformAction(new SimAction { ActionType = ActionType.Sleep, Duration = 8f });
                    break;
                case InteractiveType.Shower:
                    sim.PerformAction(new SimAction { ActionType = ActionType.Shower, EffectValue = 30f });
                    break;
                case InteractiveType.Toilet:
                    sim.PerformAction(new SimAction { ActionType = ActionType.Work });
                    break;
                case InteractiveType.Fridge:
                case InteractiveType.Stove:
                    sim.PerformAction(new SimAction { ActionType = ActionType.Eat, EffectValue = 40f });
                    break;
                case InteractiveType.TV:
                case InteractiveType.Sofa:
                    sim.PerformAction(new SimAction { ActionType = ActionType.Relax, EffectValue = 20f });
                    break;
            }

            Invoke(nameof(StopInteraction), 3f);
        }

        private void StopInteraction()
        {
            if (selectedSim != null)
            {
                // Les interactions se terminent automatiquement
            }
        }
    }
}
