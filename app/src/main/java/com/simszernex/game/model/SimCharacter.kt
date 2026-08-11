package com.simszernex.game.model

data class InventoryItem(
    val id: String,
    val name: String,
    val type: String,
    val quantity: Int = 1,
    val description: String = "",
    val price: Int = 0
)

data class SimCharacter(
    val id: String = "player",
    val name: String = "Alex",
    val age: Int = 25,
    val gender: String = "Neutre",
    val appearance: String = "Moderne",
    val personality: List<String> = listOf("Curieux", "Ambitieux"),
    val needs: Needs = Needs(),
    val money: Int = 8000,
    val powers: List<Power> = Power.allPowers(),
    val inventory: List<InventoryItem> = listOf(
        InventoryItem("phone", "Téléphone Quantique", "tool", 1, "Communique entre univers"),
        InventoryItem("keys", "Clés Multivers", "tool", 1)
    ),
    val currentRoom: String = "Salon",
    val isUsingPower: Boolean = false,
    val currentPower: PowerType? = null,
    val relationshipStatus: String = "Célibataire",
    val spouseId: String? = null,
    val hasChildren: Boolean = false,
    val children: List<String> = emptyList(),
    val childHasOriginEcho: Boolean = false,   // Lignée
    val mana: Int = 130,
    val maxMana: Int = 130,
    val career: String = "Civil",
    val careerLevel: Int = 0,
    val starfleetRank: Int = 0,
    val starfleetRankName: String = "Aucun",
    val specialPower: String = "Écho Originel",
    val ownedHouses: List<String> = listOf("home"),
    val ownedAI: List<String> = emptyList(),
    val academyProgress: Int = 0,
    val knowledge: Int = 0,
    val powerLevels: Map<String, Int> = emptyMap(),  // type name -> level
    val unlockedFusions: List<String> = emptyList(),
    val activeShipId: String? = null,
    val journal: List<JournalEntry> = emptyList(),
    val majorEventsResolved: List<String> = emptyList(),
    val worldFlags: Map<String, Boolean> = emptyMap()  // pour conséquences durables
) {
    fun unlockedPowers(): List<Power> = powers.filter { it.unlocked }

    fun unlockPower(type: PowerType): SimCharacter {
        val updated = powers.map { if (it.type == type) it.copy(unlocked = true) else it }
        return copy(powers = updated)
    }

    fun addItem(item: InventoryItem): SimCharacter {
        val existing = inventory.find { it.id == item.id }
        return if (existing != null) {
            copy(inventory = inventory.map {
                if (it.id == item.id) it.copy(quantity = it.quantity + item.quantity) else it
            })
        } else copy(inventory = inventory + item)
    }

    fun rankTitle(): String = when (starfleetRank) {
        0 -> "Civil"
        1 -> "Cadet"
        2 -> "Enseigne"
        3 -> "Lieutenant"
        4 -> "Lieutenant Commander"
        5 -> "Commander"
        6 -> "Capitaine"
        7 -> "Commodore"
        8 -> "Contre-Amiral"
        9 -> "Vice-Amiral"
        10 -> "Amiral"
        else -> "Légende Stellaire"
    }

    fun powerLevel(type: PowerType): Int = powerLevels[type.name] ?: 1

    fun withPowerLevelUp(type: PowerType): SimCharacter {
        val current = powerLevel(type)
        return copy(powerLevels = powerLevels + (type.name to (current + 1).coerceAtMost(5)))
    }

    fun addJournal(day: Int, title: String, text: String): SimCharacter {
        return copy(journal = (journal + JournalEntry(day, title, text)).takeLast(30))
    }
}
