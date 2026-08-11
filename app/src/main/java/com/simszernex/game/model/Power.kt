package com.simszernex.game.model

enum class PowerType {
    FLIGHT, STRENGTH, INVISIBILITY, TELEPORT, REGENERATION,
    TIME_CONTROL, MIND_READ, MAGIC_BLAST, SHIELD, PHASE,
    GRAVITY, ILLUSION, HEAL_OTHER, QUANTUM_SHIFT, ORIGIN_ECHO,
    // Fusions
    AEGIS_WINGS, QUANTUM_SHIELD, MIND_ILLUSION
}

data class Power(
    val type: PowerType,
    val name: String,
    val description: String,
    val level: Int = 1,
    val unlocked: Boolean = false,
    val manaCost: Int = 10,
    val cooldownSeconds: Int = 6,
    val stylishEffect: String = "",
    val category: String = "Général",
    val learnCost: Int = 60,
    val maxLevel: Int = 5,
    val isFusion: Boolean = false
) {
    fun leveledName(): String = if (level > 1) "$name (Niv.$level)" else name

    companion object {
        fun allPowers() = listOf(
            Power(PowerType.ORIGIN_ECHO, "Écho Originel", "Pouvoir unique. Facilite l'apprentissage de tout et permet les fusions.", 1, true, 8, 4,
                "Aura dorée-violette. Tu sens les liens entre les univers.", "Unique", 0),

            Power(PowerType.FLIGHT, "Ailes Célestes", "Vol et déplacement rapide.", 1, false, 14, 8, "Ailes de lumière.", "Spatial", 50),
            Power(PowerType.STRENGTH, "Force Titanesque", "Force surhumaine.", 1, false, 12, 7, "Le sol frémit.", "Combat", 45),
            Power(PowerType.INVISIBILITY, "Voile d'Ombre", "Invisibilité.", 1, false, 18, 10, "Corps transparent.", "Utilitaire", 55),
            Power(PowerType.TELEPORT, "Pas Dimensionnel", "Téléportation.", 1, false, 20, 9, "L'espace se déchire.", "Spatial", 60),
            Power(PowerType.REGENERATION, "Régénération Vitale", "Soigne faim et énergie.", 1, false, 15, 8, "Aura verte dorée.", "Utilitaire", 45),
            Power(PowerType.TIME_CONTROL, "Maître du Temps", "Ralentit le temps.", 1, false, 28, 14, "Monde bleuté.", "Temporel", 90),
            Power(PowerType.MIND_READ, "Œil de l'Esprit", "Lit pensées et secrets.", 1, false, 13, 7, "Yeux blancs.", "Social", 50),
            Power(PowerType.MAGIC_BLAST, "Explosion Arcane", "Onde de puissance.", 1, false, 20, 9, "Cercle rouge-violet.", "Combat", 55),
            Power(PowerType.SHIELD, "Égide Astrale", "Bouclier d'énergie.", 1, false, 16, 8, "Hexagones lumineux.", "Combat", 50),
            Power(PowerType.PHASE, "Déphasage", "Intangibilité.", 1, false, 22, 11, "Corps de brume.", "Utilitaire", 70),
            Power(PowerType.GRAVITY, "Puits Gravitationnel", "Altère la gravité.", 1, false, 24, 10, "Débris en lévitation.", "Spatial", 75),
            Power(PowerType.ILLUSION, "Mirage Mental", "Crée des illusions.", 1, false, 14, 7, "L'air ondule.", "Social", 45),
            Power(PowerType.HEAL_OTHER, "Toucher Restaurateur", "Soigne un autre Sim.", 1, false, 16, 8, "Main dorée.", "Social", 50),
            Power(PowerType.QUANTUM_SHIFT, "Décalage Quantique", "Plusieurs états à la fois.", 1, false, 35, 18, "Images multiples.", "Temporel", 110),

            // Fusions (débloquables plus tard)
            Power(PowerType.AEGIS_WINGS, "Ailes d'Égide", "Fusion Vol + Bouclier. Tu voles protégé par une égide.", 1, false, 22, 12, "Ailes + bouclier hexagonal.", "Fusion", 0, isFusion = true),
            Power(PowerType.QUANTUM_SHIELD, "Rempart Quantique", "Fusion Bouclier + Décalage. Presque intouchable.", 1, false, 30, 15, "Bouclier qui existe en plusieurs états.", "Fusion", 0, isFusion = true),
            Power(PowerType.MIND_ILLUSION, "Rêve Imposé", "Fusion Lecture + Illusion. Tu entres dans l'esprit et crées.", 1, false, 20, 11, "Le monde devient ton rêve.", "Fusion", 0, isFusion = true)
        )
    }
}
