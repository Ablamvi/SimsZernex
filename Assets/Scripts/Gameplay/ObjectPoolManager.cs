using UnityEngine;
using SimsZernex.Character;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de cache d'objets pour améliorer les performances
    /// </summary>
    public class ObjectPoolManager : MonoBehaviour
    {
        private Dictionary<string, Queue<GameObject>> pools = new Dictionary<string, Queue<GameObject>>();
        private Dictionary<string, GameObject> prefabs = new Dictionary<string, GameObject>();

        public static ObjectPoolManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void CreatePool(string poolName, GameObject prefab, int initialSize = 10)
        {
            if (!pools.ContainsKey(poolName))
            {
                pools[poolName] = new Queue<GameObject>(initialSize);
                prefabs[poolName] = prefab;

                for (int i = 0; i < initialSize; i++)
                {
                    var obj = Instantiate(prefab);
                    obj.SetActive(false);
                    pools[poolName].Enqueue(obj);
                }
            }
        }

        public GameObject GetObject(string poolName)
        {
            if (!pools.ContainsKey(poolName))
            {
                Debug.LogWarning($"Pool '{poolName}' n'existe pas");
                return null;
            }

            GameObject obj;
            if (pools[poolName].Count > 0)
            {
                obj = pools[poolName].Dequeue();
            }
            else
            {
                obj = Instantiate(prefabs[poolName]);
            }

            obj.SetActive(true);
            return obj;
        }

        public void ReturnObject(string poolName, GameObject obj)
        {
            if (pools.ContainsKey(poolName))
            {
                obj.SetActive(false);
                pools[poolName].Enqueue(obj);
            }
        }
    }
}
