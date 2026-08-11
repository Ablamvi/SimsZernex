package com.simszernex.game.model

// Métiers disponibles
data class Career(
    val id: String,
    val name: String,
    val description: String,
    val baseSalary: Int,
    val faction: String = "Civil"
)

object CareerData {
    val careers = listOf(
        Career("civil", "Civil", "Vie libre, petits boulots.", 280),
        Career("starfleet", "Starfleet", "Académie, vaisseaux, grades.", 350, "Starfleet"),
        Career("police", "Police", "Maintien de l'ordre, enquêtes.", 320, "Police"),
        Career("science", "Scientifique", "Recherche, centrale, laboratoires.", 340, "Science"),
        Career("medical", "Médecin", "Soigner, hôpital, prestige.", 360, "Civil"),
        Career("merchant", "Commerçant", "Vente, boutiques, import-export.", 300, "Civil"),
        Career("explorer", "Explorateur", "Forêts, anomalies, découvertes.", 310, "Civil"),
        Career("artist", "Artiste", "Création, illusions, renommée.", 290, "Civil"),
        Career("hacker", "Netrunner", "District Néon, données, implants.", 330, "Criminal"),
        Career("diplomat", "Diplomate", "Nexus, factions, négociations.", 370, "Dimensional")
    )
}

// Objets et propriétés chers
data class LuxuryItem(
    val id: String,
    val name: String,
    val category: String,          // property, vehicle, luxury, status, mana
    val price: Int,
    val description: String,
    val effect: String = ""
)

object LuxuryData {
    val items = listOf(
        // Mana
        LuxuryItem("mana_small", "Cristal de Mana (petit)", "mana", 120, "Restaure 35 Mana", "+35 Mana"),
        LuxuryItem("mana_medium", "Cristal de Mana (moyen)", "mana", 280, "Restaure 80 Mana", "+80 Mana"),
        LuxuryItem("mana_large", "Cœur de Mana", "mana", 650, "Restaure tout le Mana + augmente max", "+Max Mana"),
        LuxuryItem("mana_battery", "Batterie Arcane", "mana", 1400, "Max Mana permanent +25", "+25 Max Mana"),

        // Propriétés très chères
        LuxuryItem("villa_azur", "Villa Côte d'Azur", "property", 4500, "Villa luxueuse avec piscine", "Propriété"),
        LuxuryItem("penthouse", "Penthouse Néon", "property", 8200, "Sommet de tour cyber", "Propriété"),
        LuxuryItem("manoir", "Manoir des Brumes", "property", 12500, "Manoir magique ancien", "Propriété"),
        LuxuryItem("island", "Île Privée Zernex", "property", 28000, "Île entière à ton nom", "Propriété légendaire"),
        LuxuryItem("station_private", "Station Orbitale Privée", "property", 45000, "Ta propre station dans l'espace", "Propriété ultime"),

        // Véhicules / Transport
        LuxuryItem("speeder", "Speeder Urbain", "vehicle", 3200, "Transport rapide en ville", "Déplacements"),
        LuxuryItem("shuttle", "Navette Personnelle", "vehicle", 9800, "Navette orbitale privée", "Espace"),
        LuxuryItem("yacht", "Yacht des Nuages", "vehicle", 15000, "Yacht volant de luxe", "Prestige"),

        // Luxe & Statut
        LuxuryItem("watch_quantum", "Montre Quantique", "luxury", 1800, "Objet de prestige", "Statut"),
        LuxuryItem("cloak_legend", "Cape de Légende", "luxury", 4500, "Cape rare des Fées", "Statut + Fairy"),
        LuxuryItem("crown_shard", "Fragment de Couronne", "status", 8000, "Symbole de pouvoir souterrain", "Statut Underground"),
        LuxuryItem("star_medal_gold", "Médaille d'Amiral Honoraire", "status", 12000, "Reconnaissance Starfleet ultime", "Statut Starfleet"),
        LuxuryItem("origin_relic", "Relique Originelle", "status", 22000, "Artefact lié à Écho Originel", "Prestige max")
    )
}
