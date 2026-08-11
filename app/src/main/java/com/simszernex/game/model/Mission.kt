package com.simszernex.game.model

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val targetValue: Int = 1,
    val currentProgress: Int = 0,
    val rewardMoney: Int = 120,
    val rewardMessage: String = "",
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    val progressText: String get() = "$currentProgress / $targetValue"
    val isDone: Boolean get() = currentProgress >= targetValue
}

enum class MissionType {
    USE_POWER, FILL_NEED, TALK_TO_SIM, EARN_MONEY, VISIT_ROOM, BUY_FOOD, REACH_RELATIONSHIP, ACADEMY, SHIP_BOND
}

object MissionData {
    fun dailyMissions() = listOf(
        Mission("m1", "Équilibre Vital", "Maintiens tous tes besoins au-dessus de 55", MissionType.FILL_NEED, 1, 0, 180, "Tu maîtrises ton corps et ton esprit."),
        Mission("m2", "Éveil de Puissance", "Utilise un pouvoir 4 fois", MissionType.USE_POWER, 4, 0, 220, "La magie devient naturelle."),
        Mission("m3", "Liens Humains", "Parle à 3 Sims différents", MissionType.TALK_TO_SIM, 3, 0, 160, "Les relations sont une force."),
        Mission("m4", "Aisance Financière", "Possède au moins 5000 §", MissionType.EARN_MONEY, 5000, 0, 150, "L'argent suit ceux qui agissent."),
        Mission("m5", "Voie Stellaire", "Progresse à l'Académie ou utilise un vaisseau", MissionType.ACADEMY, 1, 0, 250, "Les étoiles se rapprochent."),
        Mission("m6", "Lien de Bord", "Renforce le lien avec un vaisseau vivant", MissionType.SHIP_BOND, 1, 0, 200, "Ton vaisseau te fait confiance.")
    )
}
