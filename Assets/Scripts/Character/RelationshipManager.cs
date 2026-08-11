using UnityEngine;
using System.Collections.Generic;
using SimsZernex.Core;

namespace SimsZernex.Character
{
    /// <summary>
    /// Gère les relations entre Sims
    /// </summary>
    public class RelationshipManager
    {
        private Dictionary<SimCharacter, Relationship> relationships = new Dictionary<SimCharacter, Relationship>();

        public void AddRelationship(SimCharacter otherSim, float friendshipLevel = 50f)
        {
            if (!relationships.ContainsKey(otherSim))
            {
                relationships[otherSim] = new Relationship { FriendshipLevel = friendshipLevel };
            }
        }

        public Relationship GetRelationship(SimCharacter otherSim)
        {
            return relationships.TryGetValue(otherSim, out var rel) ? rel : null;
        }

        public void ChangeRelationship(SimCharacter otherSim, float amount)
        {
            if (relationships.TryGetValue(otherSim, out var rel))
            {
                rel.FriendshipLevel = Mathf.Clamp(rel.FriendshipLevel + amount, -100f, 100f);
            }
        }

        public List<SimCharacter> GetFriends(float threshold = 50f)
        {
            var friends = new List<SimCharacter>();
            foreach (var kvp in relationships)
            {
                if (kvp.Value.FriendshipLevel > threshold)
                    friends.Add(kvp.Key);
            }
            return friends;
        }
    }

    [System.Serializable]
    public class Relationship
    {
        public float FriendshipLevel = 50f;     // -100 à 100
        public float RomanceLevel = 0f;         // 0 à 100 (si applicable)
        public RelationshipStatus Status = RelationshipStatus.Acquaintance;
        public System.DateTime SinceDate = System.DateTime.Now;

        public void Update(float deltaTime)
        {
            // Les relations évoluent avec le temps
            if (FriendshipLevel > 75 && Status == RelationshipStatus.Acquaintance)
                Status = RelationshipStatus.Friend;
            if (FriendshipLevel > 90 && Status == RelationshipStatus.Friend)
                Status = RelationshipStatus.BestFriend;
            if (FriendshipLevel < 25)
                Status = RelationshipStatus.Rival;
        }
    }

    public enum RelationshipStatus
    {
        Stranger,
        Acquaintance,
        Friend,
        BestFriend,
        Rival,
        Enemy,
        Spouse,
        Parent,
        Child,
        Sibling
    }
}
