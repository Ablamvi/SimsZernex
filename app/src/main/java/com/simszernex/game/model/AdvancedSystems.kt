package com.simszernex.game.model

data class LivingShip(
    val id: String,
    val name: String,
    val aiName: String,
    val aiPersonality: String,
    val bond: Int = 20,
    val level: Int = 1,
    val unlocked: Boolean = false,
    val specialBonus: String = ""
) {
    val mood: String
        get() = when {
            bond >= 85 -> "Dévouée"
            bond >= 60 -> "Très liée"
            bond >= 35 -> "Amicale"
            bond >= 15 -> "Neutre"
            else -> "Distante"
        }
}

object ShipData {
    val ships = listOf(
        LivingShip("horizon", "Horizon", "Aria", "Loyale", 30, 1, false, "Bonus exploration et stabilité"),
        LivingShip("aurora", "Aurora", "Nyx", "Sarcastique", 15, 1, false, "Vitesse + réparties cinglantes"),
        LivingShip("eclipse", "Eclipse", "Vesper", "Protectrice", 10, 1, false, "Boucliers renforcés"),
        LivingShip("mythos", "Mythos", "Echo", "Curieuse", 5, 1, false, "Détecte les anomalies cachées"),
        LivingShip("solace", "Solace", "Lumen", "Empathique", 8, 1, false, "Soutien moral et régénération légère")
    )
}

data class JournalEntry(val day: Int, val title: String, val text: String)

data class MajorEvent(
    val id: String,
    val title: String,
    val description: String,
    val choices: List<String>,
    val isActive: Boolean = false,
    val isResolved: Boolean = false
)

object EventData {
    val possibleEvents = listOf(
        MajorEvent("reactor_crisis", "Crise du Réacteur Aurora", "Le cœur du réacteur devient instable. Des vies sont en danger.", listOf("Stabiliser avec tes pouvoirs", "Évacuer et sacrifier la zone", "Demander l'aide de Starfleet")),
        MajorEvent("dimension_rift", "Faille Dimensionnelle", "Une faille s'ouvre près du Nexus. Des entités en sortent.", listOf("Refermer la faille", "Négocier avec les entités", "Absorber une partie de l'énergie")),
        MajorEvent("ship_lost", "Vaisseau en Détresse", "Un signal de détresse allié résonne dans l'espace.", listOf("Partir en sauvetage", "Envoyer une équipe", "Ignorer pour le moment")),
        MajorEvent("fairy_pact", "Pacte de la Reine Fée", "La Reine Fée te propose un pacte permanent.", listOf("Accepter le pacte", "Négocier les termes", "Refuser poliment")),
        MajorEvent("omega_choice", "Le Destin d'Omega-7", "Omega-7 te demande de l'aider à se libérer.", listOf("L'aider pleinement", "Le stabiliser seulement", "Le livrer aux autorités")),
        MajorEvent("ai_awakening", "Éveil d'IA", "L'IA de ton vaisseau commence à poser des questions sur sa propre existence.", listOf("L'encourager à évoluer", "Imposer des limites strictes", "Partager ton Écho Originel avec elle")),
        MajorEvent("lineage_dream", "Rêve de Lignée", "Tu rêves d'un futur où ton enfant maîtrise les univers.", listOf("Accepter la vision", "Chercher à la changer", "En parler à ton conjoint"))
    )
}

data class PowerFusion(
    val id: String,
    val name: String,
    val requiredPowers: List<PowerType>,
    val description: String,
    val unlocked: Boolean = false
)

object FusionData {
    val fusions = listOf(
        PowerFusion("aegis_wings", "Ailes d'Égide", listOf(PowerType.FLIGHT, PowerType.SHIELD), "Vole protégé par des ailes-bouclier."),
        PowerFusion("time_phase", "Fantôme Temporel", listOf(PowerType.TIME_CONTROL, PowerType.PHASE), "Intangible pendant que le temps ralentit."),
        PowerFusion("mind_heal", "Empathie Restauratrice", listOf(PowerType.MIND_READ, PowerType.HEAL_OTHER), "Lis la douleur et soigne en même temps."),
        PowerFusion("quantum_blast", "Nova Quantique", listOf(PowerType.QUANTUM_SHIFT, PowerType.MAGIC_BLAST), "Explosion multi-états."),
        PowerFusion("origin_mastery", "Maîtrise Originelle", listOf(PowerType.ORIGIN_ECHO, PowerType.QUANTUM_SHIFT), "Écho Originel atteint un nouveau seuil."),
        PowerFusion("gravity_flight", "Ascension Gravitationnelle", listOf(PowerType.FLIGHT, PowerType.GRAVITY), "Contrôle total de la gravité en vol.")
    )
}
