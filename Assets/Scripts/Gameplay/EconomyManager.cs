using UnityEngine;
using SimsZernex.Character;
using System.Collections.Generic;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Gère les économies, argent et finances des Sims
    /// </summary>
    public class EconomyManager : MonoBehaviour
    {
        private Dictionary<SimCharacter, int> simWallets = new Dictionary<SimCharacter, int>();
        private Dictionary<SimCharacter, List<Transaction>> transactionHistory = new Dictionary<SimCharacter, List<Transaction>>();

        public static EconomyManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void RegisterSim(SimCharacter sim, int startingMoney = 20000)
        {
            if (!simWallets.ContainsKey(sim))
            {
                simWallets[sim] = startingMoney;
                transactionHistory[sim] = new List<Transaction>();
                Debug.Log($"[EconomyManager] {sim.CharacterData.name} enregistré avec {startingMoney}§");
            }
        }

        public int GetMoney(SimCharacter sim)
        {
            return simWallets.TryGetValue(sim, out var money) ? money : 0;
        }

        public void AddMoney(SimCharacter sim, int amount, string reason)
        {
            if (!simWallets.ContainsKey(sim)) RegisterSim(sim);

            simWallets[sim] += amount;
            RecordTransaction(sim, amount, reason);
            
            if (amount > 0)
                Debug.Log($"[EconomyManager] {sim.CharacterData.name} gagne {amount}§ ({reason})");
            else
                Debug.Log($"[EconomyManager] {sim.CharacterData.name} perd {-amount}§ ({reason})");
        }

        public bool CanAfford(SimCharacter sim, int cost)
        {
            return GetMoney(sim) >= cost;
        }

        public void SpendMoney(SimCharacter sim, int amount, string reason)
        {
            if (CanAfford(sim, amount))
            {
                AddMoney(sim, -amount, reason);
            }
            else
            {
                Debug.LogWarning($"[EconomyManager] {sim.CharacterData.name} n'a pas assez d'argent pour {reason}");
            }
        }

        private void RecordTransaction(SimCharacter sim, int amount, string reason)
        {
            var transaction = new Transaction
            {
                Amount = amount,
                Reason = reason,
                Timestamp = System.DateTime.Now
            };
            transactionHistory[sim].Add(transaction);
        }

        public List<Transaction> GetTransactionHistory(SimCharacter sim)
        {
            return transactionHistory.TryGetValue(sim, out var history) ? history : new List<Transaction>();
        }
    }

    [System.Serializable]
    public class Transaction
    {
        public int Amount;
        public string Reason;
        public System.DateTime Timestamp;
    }
}
