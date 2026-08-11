package com.simszernex.game.world

data class WorldLocation(
    val id: String,
    val name: String,
    val universe: String,                 // Univers principal
    val type: LocationType,
    val description: String,
    val dangerLevel: Int = 0,
    val isUnlocked: Boolean = true,
    val requiredCareer: String? = null,
    val requiredRank: Int = 0,
    val flavor: String = "",
    val isShop: Boolean = false,
    val isAcademy: Boolean = false,
    val isHousing: Boolean = false,
    val price: Int = 0
)

enum class LocationType {
    HOME, CITY, SPACEPORT, STARSHIP, RESEARCH, UNDERGROUND, WILD, DIMENSIONAL, ACADEMY, SHOP, FAIRY, CYBER, NEXUS
}

object WorldData {

    // ========== 5 UNIVERS + Maison de base ==========
    val universes = listOf(
        "Résidence Zernex",
        "Fédération Stellaire",
        "Forêt des Fées",
        "District Néon",
        "Royaumes Souterrains",
        "Nexus Dimensionnel"
    )

    val locations = listOf(
        // === UNIVERS 0 : Résidence (base) ===
        WorldLocation("home", "Résidence Principale", "Résidence Zernex", LocationType.HOME, "Ta maison de départ. Tu peux en acheter d'autres.", 0, true, flavor = "Le seul endroit vraiment à toi... pour l'instant."),
        WorldLocation("home_garden", "Jardin Privé", "Résidence Zernex", LocationType.HOME, "Jardin calme avec une source magique.", 0, true),
        WorldLocation("home_buy_1", "Villa Côte d'Azur", "Résidence Zernex", LocationType.HOME, "Villa luxueuse avec vue. Maison achetable.", 1, true, isHousing = true, price = 4500, flavor = "Piscine, terrasse et silence."),
        WorldLocation("home_buy_2", "Penthouse Néon", "Résidence Zernex", LocationType.HOME, "Appartement ultra-moderne au sommet d'une tour.", 2, true, isHousing = true, price = 8200),
        WorldLocation("home_buy_3", "Manoir des Brumes", "Résidence Zernex", LocationType.HOME, "Grand manoir ancien lié à la magie.", 3, true, isHousing = true, price = 12000),

        // === UNIVERS 1 : Fédération Stellaire (Star Trek-like) ===
        WorldLocation("spaceport", "Aéroport Spatial Orion", "Fédération Stellaire", LocationType.SPACEPORT, "Porte vers les étoiles.", 3, true, flavor = "Le point de départ de toutes les carrières spatiales."),
        WorldLocation("academy", "Académie Starfleet", "Fédération Stellaire", LocationType.ACADEMY, "École pour devenir membre de Starfleet. Cours, examens, grades.", 2, true, isAcademy = true, flavor = "Ici naissent les futurs capitaines."),
        WorldLocation("academy_sim", "Simulateur de Combat & Commandement", "Fédération Stellaire", LocationType.ACADEMY, "Entraînement intensif.", 3, true, isAcademy = true),
        WorldLocation("starship_horizon", "Vaisseau Étoile « Horizon »", "Fédération Stellaire", LocationType.STARSHIP, "Croiseur d'exploration. Pont, moteurs, infirmerie.", 4, false, "Starfleet", 2, "Les moteurs quantiques vibrent sous tes pieds."),
        WorldLocation("starship_aurora", "Vaisseau Étoile « Aurora »", "Fédération Stellaire", LocationType.STARSHIP, "Vaisseau rapide de reconnaissance.", 5, false, "Starfleet", 4),
        WorldLocation("starship_command", "Pont de Commandement Supérieur", "Fédération Stellaire", LocationType.STARSHIP, "Réservé aux hauts gradés.", 3, false, "Starfleet", 7),
        WorldLocation("orbital_station", "Station Nexus Orbitale", "Fédération Stellaire", LocationType.SPACEPORT, "Station diplomatique et commerciale.", 3, true),
        WorldLocation("ai_shop", "Boutique IA Intégrées", "Fédération Stellaire", LocationType.SHOP, "Achète des IA de bord, assistants et modules.", 1, true, isShop = true, flavor = "Une bonne IA peut changer une mission."),

        // === UNIVERS 2 : Forêt des Fées ===
        WorldLocation("fairy_gate", "Portail des Lucioles", "Forêt des Fées", LocationType.FAIRY, "Entrée de la forêt enchantée.", 2, true, flavor = "L'air goûte à la magie pure."),
        WorldLocation("fairy_village", "Village des Fées", "Forêt des Fées", LocationType.FAIRY, "Petites maisons lumineuses et marchés magiques.", 1, true),
        WorldLocation("fairy_shop", "Boutique des Enchantements", "Forêt des Fées", LocationType.SHOP, "Potions, ailes, charmes et objets féeriques.", 1, true, isShop = true),
        WorldLocation("fairy_queen_court", "Cour de la Reine Fée", "Forêt des Fées", LocationType.FAIRY, "Lieu de pouvoir et de négociations anciennes.", 4, true, flavor = "On ne ment pas ici sans conséquences."),
        WorldLocation("fairy_lake", "Lac des Reflets", "Forêt des Fées", LocationType.FAIRY, "Voir des fragments de destin.", 3, true),
        WorldLocation("fairy_academy", "Cercle d'Apprentissage Féerique", "Forêt des Fées", LocationType.ACADEMY, "Apprendre la magie douce et les pactes.", 2, true, isAcademy = true),

        // === UNIVERS 3 : District Néon (Cyber) ===
        WorldLocation("neon_plaza", "Place Néon", "District Néon", LocationType.CYBER, "Cœur de la ville cybernétique.", 3, true),
        WorldLocation("neon_market", "Marché Noir Augmenté", "District Néon", LocationType.SHOP, "Implants, données, armes et infos.", 5, true, isShop = true),
        WorldLocation("neon_club", "Club Eclipse", "District Néon", LocationType.CYBER, "Musique, contacts et secrets.", 2, true),
        WorldLocation("neon_lab", "Labo Cybernétique", "District Néon", LocationType.RESEARCH, "Améliorations corporelles et IA.", 4, true),
        WorldLocation("neon_tower", "Tour des Corporations", "District Néon", LocationType.CYBER, "Pouvoir et surveillance.", 4, true),

        // === UNIVERS 4 : Royaumes Souterrains ===
        WorldLocation("under_gate", "Descente des Anciens", "Royaumes Souterrains", LocationType.UNDERGROUND, "Entrée vers les profondeurs.", 3, true),
        WorldLocation("under_city", "Cité Souterraine", "Royaumes Souterrains", LocationType.UNDERGROUND, "Civilisation cachée.", 4, true),
        WorldLocation("under_market", "Marché des Ombres", "Royaumes Souterrains", LocationType.SHOP, "Objets rares et interdits.", 5, true, isShop = true),
        WorldLocation("under_throne", "Trône de la Reine des Profondeurs", "Royaumes Souterrains", LocationType.UNDERGROUND, "Siège du pouvoir souterrain.", 6, true),
        WorldLocation("under_archives", "Archives Interdites", "Royaumes Souterrains", LocationType.UNDERGROUND, "Secrets du monde de surface et d'ailleurs.", 3, true),

        // === UNIVERS 5 : Nexus Dimensionnel ===
        WorldLocation("nexus_core", "Cœur du Nexus", "Nexus Dimensionnel", LocationType.NEXUS, "Carrefour de toutes les réalités.", 7, true, flavor = "Ici, les univers se touchent."),
        WorldLocation("nexus_market", "Bazar Interdimensionnel", "Nexus Dimensionnel", LocationType.SHOP, "Objets venus d'innombrables mondes.", 4, true, isShop = true),
        WorldLocation("nexus_anomaly", "Anomalie Temporelle Stable", "Nexus Dimensionnel", LocationType.DIMENSIONAL, "Étudier le temps et les possibilités.", 8, true),
        WorldLocation("nexus_library", "Bibliothèque des Possibles", "Nexus Dimensionnel", LocationType.NEXUS, "Tous les savoirs... presque.", 2, true),
        WorldLocation("nexus_gate_hub", "Hub des Portails", "Nexus Dimensionnel", LocationType.NEXUS, "Voyager rapidement entre univers.", 3, true),

        // Lieux classiques restants
        WorldLocation("downtown", "Centre-Ville", "Résidence Zernex", LocationType.CITY, "Vie urbaine classique.", 2, true),
        WorldLocation("police_hq", "QG de la Police", "Résidence Zernex", LocationType.CITY, "Forces de l'ordre.", 3, true),
        WorldLocation("hospital", "Hôpital Central", "Résidence Zernex", LocationType.CITY, "Soins et recherches.", 1, true),
        WorldLocation("nuclear_plant", "Centrale Aurora", "Résidence Zernex", LocationType.RESEARCH, "Énergie et danger.", 7, true, "Scientifique"),
        WorldLocation("forest", "Forêt des Murmures", "Résidence Zernex", LocationType.WILD, "Nature et magie ancienne.", 2, true)
    )
}
