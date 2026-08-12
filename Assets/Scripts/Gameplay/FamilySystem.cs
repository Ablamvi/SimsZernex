using UnityEngine;
using SimsZernex.Character;
using SimsZernex.Core;

namespace SimsZernex.Gameplay
{
    /// <summary>
    /// Système de relations familiales et arbre généalogique
    /// </summary>
    public class FamilySystem : MonoBehaviour
    {
        private System.Collections.Generic.Dictionary<SimCharacter, FamilyNode> familyTrees = new System.Collections.Generic.Dictionary<SimCharacter, FamilyNode>();

        public static FamilySystem Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }

        public void CreateFamilyTree(SimCharacter founder)
        {
            if (!familyTrees.ContainsKey(founder))
            {
                familyTrees[founder] = new FamilyNode(founder);
                Debug.Log($"[FamilySystem] Arbre généalogique créé pour {founder.CharacterData.name}");
            }
        }

        public void AddChild(SimCharacter parent, SimCharacter child)
        {
            foreach (var tree in familyTrees.Values)
            {
                if (tree.FindMember(parent) != null)
                {
                    tree.FindMember(parent).AddChild(child);
                    familyTrees[child] = tree;
                    Debug.Log($"[FamilySystem] {child.CharacterData.name} ajouté comme enfant de {parent.CharacterData.name}");
                    break;
                }
            }
        }

        public void MarrySims(SimCharacter sim1, SimCharacter sim2)
        {
            Debug.Log($"[FamilySystem] {sim1.CharacterData.name} et {sim2.CharacterData.name} sont mariés!");
            sim1.RelationshipManager.ChangeRelationship(sim2, 100f);
            sim2.RelationshipManager.ChangeRelationship(sim1, 100f);
        }

        public System.Collections.Generic.List<SimCharacter> GetFamily(SimCharacter sim)
        {
            if (familyTrees.TryGetValue(sim, out var tree))
            {
                return tree.GetAllMembers();
            }
            return new System.Collections.Generic.List<SimCharacter>();
        }
    }

    public class FamilyNode
    {
        public SimCharacter Member { get; set; }
        public FamilyNode Parent { get; set; }
        public System.Collections.Generic.List<FamilyNode> Children { get; set; } = new System.Collections.Generic.List<FamilyNode>();

        public FamilyNode(SimCharacter member)
        {
            Member = member;
        }

        public void AddChild(SimCharacter child)
        {
            Children.Add(new FamilyNode(child));
        }

        public FamilyNode FindMember(SimCharacter target)
        {
            if (Member == target) return this;
            
            foreach (var child in Children)
            {
                var found = child.FindMember(target);
                if (found != null) return found;
            }
            return null;
        }

        public System.Collections.Generic.List<SimCharacter> GetAllMembers()
        {
            var members = new System.Collections.Generic.List<SimCharacter> { Member };
            foreach (var child in Children)
            {
                members.AddRange(child.GetAllMembers());
            }
            return members;
        }
    }
}
