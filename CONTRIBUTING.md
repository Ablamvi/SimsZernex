# Contribuer à SimsZernex

## 🤝 Directives de Contribution

Merci de vouloir contribuer à SimsZernex! Voici comment procéder.

### Code de Conduite

- Soyez respectueux
- Accueillez les perspectives différentes
- Signalez les comportements abusifs

### Processus de Contribution

1. **Fork** le repository
2. **Branch**: `git checkout -b feature/ma-feature`
3. **Commit**: `git commit -m '[Feature] Description'`
4. **Push**: `git push origin feature/ma-feature`
5. **PR**: Ouvrir une Pull Request

### Standards de Code

#### Nommage
```csharp
public class SimCharacter      // Classes: PascalCase
private int simAge;             // Champs: camelCase
public void PerformAction()     // Méthodes: PascalCase
private const int MAX_AGE = 100; // Constantes: UPPER_SNAKE_CASE
```

#### Commentaires
```csharp
/// <summary>
/// Description de la méthode
/// </summary>
/// <param name="param1">Description du param</param>
/// <returns>Ce qu'elle retourne</returns>
public void MyMethod(int param1) { }
```

#### Indentation
- 4 espaces (pas de tabs)
- Accolades ouvrantes sur même ligne

```csharp
if (condition)
{
    DoSomething();
}
```

### Types de Contributions

#### 🐛 Bug Fixes
- [ ] Créer une issue d'abord
- [ ] Tester le fix
- [ ] Commenter le code
- [ ] Ouvrir PR

#### ✨ Features
- [ ] Discuter dans issues
- [ ] Implémenter
- [ ] Ajouter tests
- [ ] Documenter
- [ ] Ouvrir PR

#### 📚 Documentation
- [ ] Mettre à jour README/DOCS
- [ ] Ajouter commentaires de code
- [ ] Fournir exemples

### Checklist PR

- [ ] Code suit les standards
- [ ] Pas de compilation errors
- [ ] Tests passent
- [ ] Documentation à jour
- [ ] Commit messages clairs
- [ ] Pas de code inutile/débugging

### Labels Issues

- `bug` - Bug confirmé
- `enhancement` - Nouvelle feature
- `documentation` - Doc improvement
- `good first issue` - Pour débutants
- `help wanted` - Aide souhaitée

---

**Merci pour votre contribution! 🎉**
