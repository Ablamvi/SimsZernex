package com.simszernex.game.model

data class HouseObject(
    val id: String,
    val name: String,
    val room: String,
    val type: ObjectType,
    val description: String,
    val actionLabel: String,
    val hungerEffect: Float = 0f,
    val energyEffect: Float = 0f,
    val hygieneEffect: Float = 0f,
    val funEffect: Float = 0f,
    val socialEffect: Float = 0f,
    val bladderEffect: Float = 0f,
    val moneyCost: Int = 0,
    val isTool: Boolean = false,
    val isMaterial: Boolean = false
)

enum class ObjectType {
    FURNITURE, FOOD, TOOL, MATERIAL, DECORATION, SPECIAL
}

object HouseData {
    val rooms = listOf(
        "Salon", "Cuisine", "Chambre", "Salle de bain",
        "Bureau", "Garage", "Jardin", "Salle de magie"
    )

    val objects = listOf(
        // Salon
        HouseObject("sofa", "Canapé Luxe", "Salon", ObjectType.FURNITURE, "Un grand canapé confortable", "S'asseoir / Se reposer", energyEffect = 15f, funEffect = 10f),
        HouseObject("tv", "Télévision 4K", "Salon", ObjectType.FURNITURE, "Grande télé pour se divertir", "Regarder la télé", funEffect = 25f, energyEffect = -5f),
        HouseObject("bookshelf", "Bibliothèque", "Salon", ObjectType.FURNITURE, "Livres et savoir", "Lire", funEffect = 15f, energyEffect = -5f),

        // Cuisine
        HouseObject("fridge", "Réfrigérateur Premium", "Cuisine", ObjectType.FURNITURE, "Stocke toute ta nourriture", "Prendre de la nourriture", hungerEffect = 30f, moneyCost = 0),
        HouseObject("stove", "Cuisinière Pro", "Cuisine", ObjectType.FURNITURE, "Pour cuisiner de bons plats", "Cuisiner", hungerEffect = 40f, funEffect = 10f),
        HouseObject("coffee", "Machine à café", "Cuisine", ObjectType.FURNITURE, "Café pour l'énergie", "Boire un café", energyEffect = 20f, hungerEffect = 5f),

        // Chambre
        HouseObject("bed", "Lit King Size", "Chambre", ObjectType.FURNITURE, "Le meilleur lit pour récupérer", "Dormir", energyEffect = 60f, hygieneEffect = -5f),
        HouseObject("wardrobe", "Armoire", "Chambre", ObjectType.FURNITURE, "Change de tenue", "Changer de vêtements", funEffect = 5f, hygieneEffect = 5f),

        // Salle de bain
        HouseObject("shower", "Douche à l'italienne", "Salle de bain", ObjectType.FURNITURE, "Douche luxueuse", "Se doucher", hygieneEffect = 50f, energyEffect = 5f),
        HouseObject("toilet", "Toilettes", "Salle de bain", ObjectType.FURNITURE, "Besoin urgent", "Utiliser les toilettes", bladderEffect = 80f),
        HouseObject("sink", "Lavabo", "Salle de bain", ObjectType.FURNITURE, "Se laver les mains / visage", "Se laver", hygieneEffect = 15f),

        // Bureau
        HouseObject("computer", "Ordinateur Gaming", "Bureau", ObjectType.FURNITURE, "Travail et divertissement", "Utiliser l'ordinateur", funEffect = 20f, energyEffect = -10f),
        HouseObject("desk", "Bureau Design", "Bureau", ObjectType.FURNITURE, "Pour travailler", "Travailler", moneyCost = -50, energyEffect = -15f, funEffect = -5f),

        // Garage - Outils & Matériaux
        HouseObject("toolbox", "Boîte à outils Pro", "Garage", ObjectType.TOOL, "Tous les outils nécessaires", "Utiliser les outils", isTool = true, funEffect = 10f),
        HouseObject("workbench", "Établi", "Garage", ObjectType.TOOL, "Fabriquer et réparer", "Bricoler", isTool = true, funEffect = 15f, energyEffect = -10f),
        HouseObject("wood", "Bois de qualité", "Garage", ObjectType.MATERIAL, "Matériau de construction", "Utiliser le bois", isMaterial = true),
        HouseObject("metal", "Métal renforcé", "Garage", ObjectType.MATERIAL, "Matériau solide", "Utiliser le métal", isMaterial = true),
        HouseObject("paint", "Peinture premium", "Garage", ObjectType.MATERIAL, "Pour décorer", "Peindre", isMaterial = true, funEffect = 10f),

        // Jardin
        HouseObject("garden_chair", "Chaise de jardin", "Jardin", ObjectType.FURNITURE, "Profiter du soleil", "Se détendre", energyEffect = 10f, funEffect = 15f),
        HouseObject("grill", "Barbecue", "Jardin", ObjectType.FURNITURE, "Cuisiner dehors", "Faire un barbecue", hungerEffect = 35f, funEffect = 20f, socialEffect = 10f),

        // Salle de magie (spéciale pour les pouvoirs)
        HouseObject("magic_circle", "Cercle Magique", "Salle de magie", ObjectType.SPECIAL, "Amplifie tes pouvoirs", "Activer le cercle", funEffect = 20f, energyEffect = -10f),
        HouseObject("crystal", "Cristal d'énergie", "Salle de magie", ObjectType.SPECIAL, "Recharge ta magie", "Méditer", energyEffect = 25f, funEffect = 15f),
        HouseObject("spellbook", "Grimoire Ancien", "Salle de magie", ObjectType.SPECIAL, "Apprends de nouveaux sorts", "Étudier la magie", funEffect = 10f, energyEffect = -15f)
    )

    fun objectsInRoom(room: String) = objects.filter { it.room == room }
}
