using UnityEngine;
using SimsZernex.Character;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de pathfinding A* optimisé pour navigation 3D des Sims
    /// </summary>
    public class PathfindingSystem : MonoBehaviour
    {
        private static PathfindingSystem instance;
        private Dictionary<SimCharacter, Queue<Vector3>> activePaths = new Dictionary<SimCharacter, Queue<Vector3>>();
        private float gridCellSize = 1f;

        public static PathfindingSystem Instance
        {
            get
            {
                if (instance == null)
                    instance = FindObjectOfType<PathfindingSystem>();
                return instance;
            }
        }

        public bool FindPath(SimCharacter sim, Vector3 start, Vector3 goal, out Queue<Vector3> path)
        {
            path = new Queue<Vector3>();
            
            // Implémentation simplifiée A*
            List<Node> openSet = new List<Node>();
            HashSet<Node> closedSet = new HashSet<Node>();
            
            Node startNode = new Node(start);
            Node goalNode = new Node(goal);
            
            openSet.Add(startNode);
            
            while (openSet.Count > 0)
            {
                Node current = openSet[0];
                int currentIndex = 0;
                
                for (int i = 1; i < openSet.Count; i++)
                {
                    if (openSet[i].f < current.f)
                    {
                        current = openSet[i];
                        currentIndex = i;
                    }
                }
                
                if (Vector3.Distance(current.position, goal) < gridCellSize)
                {
                    // Chemin trouvé
                    Node temp = current;
                    while (temp != null)
                    {
                        path.Enqueue(temp.position);
                        temp = temp.parent;
                    }
                    activePaths[sim] = path;
                    return true;
                }
                
                openSet.RemoveAt(currentIndex);
                closedSet.Add(current);
                
                // Exploration des voisins
                foreach (Vector3 neighbor in GetNeighbors(current.position))
                {
                    Node neighborNode = new Node(neighbor, current);
                    if (closedSet.Contains(neighborNode)) continue;
                    
                    float newG = current.g + Vector3.Distance(current.position, neighbor);
                    bool isInOpenSet = openSet.Contains(neighborNode);
                    
                    if (newG < neighborNode.g || !isInOpenSet)
                    {
                        neighborNode.g = newG;
                        neighborNode.h = Vector3.Distance(neighbor, goal);
                        neighborNode.f = neighborNode.g + neighborNode.h;
                        
                        if (!isInOpenSet)
                            openSet.Add(neighborNode);
                    }
                }
            }
            
            return false;
        }

        private List<Vector3> GetNeighbors(Vector3 position)
        {
            var neighbors = new List<Vector3>
            {
                position + Vector3.forward * gridCellSize,
                position - Vector3.forward * gridCellSize,
                position + Vector3.right * gridCellSize,
                position - Vector3.right * gridCellSize,
                position + (Vector3.forward + Vector3.right).normalized * gridCellSize,
                position + (Vector3.forward - Vector3.right).normalized * gridCellSize,
                position + (-Vector3.forward + Vector3.right).normalized * gridCellSize,
                position + (-Vector3.forward - Vector3.right).normalized * gridCellSize
            };
            return neighbors;
        }

        public void ClearPath(SimCharacter sim)
        {
            if (activePaths.ContainsKey(sim))
                activePaths.Remove(sim);
        }

        private class Node
        {
            public Vector3 position;
            public Node parent;
            public float g = 0; // Coût depuis le début
            public float h = 0; // Heuristique jusqu'à la fin
            public float f = 0; // g + h

            public Node(Vector3 pos, Node par = null)
            {
                position = pos;
                parent = par;
            }
        }
    }
}
