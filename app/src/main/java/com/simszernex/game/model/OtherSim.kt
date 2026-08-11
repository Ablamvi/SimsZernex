package com.simszernex.game.model

data class OtherSim(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val personality: String,
    val job: String,
    val relationship: Int = 0,
    val isRomantic: Boolean = false,
    val isMarried: Boolean = false,
    val hasPower: Boolean = false,
    val powerName: String? = null,
    val currentLocationId: String = "downtown",
    val mood: String = "Neutre",
    val isAutonomous: Boolean = true,
    val rank: String = "",
    val faction: String = "Civil",
    val bio: String = "",
    val secret: String = ""
) {
    val relationshipLabel: String
        get() = when {
            relationship >= 88 -> "Âme sœur"
            relationship >= 65 -> "Très proche"
            relationship >= 35 -> "Ami proche"
            relationship >= 15 -> "Ami"
            relationship >= 0 -> "Connaissance"
            relationship >= -20 -> "Neutre"
            relationship >= -50 -> "Méfiant"
            else -> "Hostile"
        }
}

object NpcData {
    val allNpcs = listOf(
        // Maison / Proches
        OtherSim("npc_luna", "Luna Veyra", 24, "Femme", "Mystérieuse", "Médium", 22, hasPower = true, powerName = "Télékinésie", currentLocationId = "home", faction = "Civil",
            bio = "Voit des fragments d'avenir. Parle souvent aux esprits.", secret = "Elle a déjà vu ta mort dans une vision... et l'a changée."),
        OtherSim("npc_kai", "Kai Rorden", 27, "Homme", "Aventurier", "Explorateur", 14, currentLocationId = "forest", faction = "Civil",
            bio = "A parcouru des zones interdites. Porte toujours un carnet de cartes.", secret = "Il a trouvé une entrée vers la Cité Souterraine."),
        OtherSim("npc_mira", "Mira Solen", 22, "Femme", "Créative", "Artiste Digitale", 10, hasPower = true, powerName = "Illusions", currentLocationId = "downtown", faction = "Civil",
            bio = "Crée des œuvres qui bougent seules. Ses illusions deviennent parfois réelles.", secret = "Une de ses illusions a pris conscience."),

        // Police
        OtherSim("npc_rake", "Capitaine Elias Rake", 39, "Homme", "Strict", "Commissaire", 0, currentLocationId = "police_hq", rank = "Commissaire", faction = "Police",
            bio = "Ancien militaire. Applique la loi sans compromis.", secret = "Il cache un dossier sur les êtres à pouvoirs."),
        OtherSim("npc_neyra", "Sergent Neyra Voss", 29, "Femme", "Juste", "Policière d'élite", 8, hasPower = true, powerName = "Bouclier Cinétique", currentLocationId = "police_hq", rank = "Sergent", faction = "Police",
            bio = "Spécialiste des interventions à haut risque.", secret = "Elle a déjà couvert un incident lié aux pouvoirs."),
        OtherSim("npc_arms", "Agent Kellan Arms", 34, "Homme", "Froid", "Armurier", -5, currentLocationId = "police_hq", faction = "Police",
            bio = "Gère l'armement expérimental du QG.", secret = "Il vend parfois des pièces au Marché Noir."),
        OtherSim("npc_detective", "Inspecteur Lira Kane", 32, "Femme", "Perçante", "Détective", 5, currentLocationId = "downtown", faction = "Police",
            bio = "Résout les affaires que les autres abandonnent.", secret = "Elle enquête secrètement sur le Laboratoire Omega."),

        // Starfleet / Espace
        OtherSim("npc_admiral", "Amiral Soren Vex", 52, "Homme", "Charismatique", "Commandant de Flotte", 0, currentLocationId = "spaceport", rank = "Amiral", faction = "Starfleet", hasPower = true, powerName = "Présence de Commandement",
            bio = "A mené 17 missions d'exploration. Respecté et craint.", secret = "Il a rencontré le Gardien une fois."),
        OtherSim("npc_helmsman", "Lieutenant Kael Orin", 31, "Homme", "Calme", "Pilote de Vaisseau", 6, currentLocationId = "starship", rank = "Lieutenant", faction = "Starfleet",
            bio = "Meilleur pilote de la flotte selon certains.", secret = "Il a désobéi à un ordre pour sauver un équipage."),
        OtherSim("npc_elara", "Commander Elara Quinn", 36, "Femme", "Brillante", "Officier Scientifique", 12, currentLocationId = "starship", rank = "Commander", faction = "Starfleet", hasPower = true, powerName = "Analyse Quantique",
            bio = "Spécialiste des anomalies et de la physique exotique.", secret = "Elle a des données sur l'Anomalie Temporelle qu'elle n'a pas partagées."),
        OtherSim("npc_torren", "Lt. Cmdr Torren Hale", 42, "Homme", "Protecteur", "Chef Sécurité", -3, currentLocationId = "starship", rank = "Lt. Commander", faction = "Starfleet",
            bio = "Ancien forces spéciales. Protège le vaisseau au péril de sa vie.", secret = "Il a un implant expérimental."),
        OtherSim("npc_ryn", "Ingénieur Ryn-7", 28, "Neutre", "Génie", "Ingénieur Warp", 9, currentLocationId = "starship", faction = "Starfleet",
            bio = "Comprend les moteurs comme personne. Parle peu.", secret = "Ryn-7 n'est peut-être pas entièrement humain."),
        OtherSim("npc_nova", "Captain Nova Sable", 35, "Femme", "Audacieuse", "Pilote de Chasseur", 16, currentLocationId = "spaceport", faction = "Starfleet", hasPower = true, powerName = "Réflexes Accélérés",
            bio = "As de l'aviation spatiale. Collectionne les cicatrices et les histoires.", secret = "Elle a volé un prototype une fois."),

        // Science & Nucléaire
        OtherSim("npc_hale", "Dr. Helena Hale", 45, "Femme", "Obsessionnelle", "Directrice Scientifique", 0, currentLocationId = "nuclear_plant", rank = "Directrice", faction = "Science", hasPower = true, powerName = "Contrôle des Radiations",
            bio = "Génie de la fusion. Prête à tout pour la découverte.", secret = "Elle a autorisé des tests illégaux sur des sujets."),
        OtherSim("npc_jaro", "Technicien Jaro Mesk", 26, "Homme", "Nerveux", "Technicien Réacteur", -8, currentLocationId = "nuclear_plant", faction = "Science",
            bio = "Surveille les niveaux jour et nuit. Dort très peu.", secret = "Il a vu quelque chose bouger dans le cœur du réacteur."),
        OtherSim("npc_omega", "Sujet Omega-7", 19, "Neutre", "Instable", "Expérience", -25, currentLocationId = "secret_lab", faction = "Underground", hasPower = true, powerName = "Rupture Dimensionnelle",
            bio = "Créé ou transformé dans le laboratoire. Souffre et possède un pouvoir terrifiant.", secret = "Omega-7 se souvient d'une autre vie."),

        // Underground & Criminal
        OtherSim("npc_shadow", "Shadow", 33, "Neutre", "Manipulateur", "Courtier", -12, currentLocationId = "market", faction = "Criminal", hasPower = true, powerName = "Effacement",
            bio = "Personne ne connaît son vrai visage. Vend informations et objets interdits.", secret = "Shadow a effacé son propre passé."),
        OtherSim("npc_kane", "Fixer Kane", 41, "Homme", "Pragmatique", "Contrebandier", -6, currentLocationId = "market", faction = "Criminal",
            bio = "Fait entrer et sortir n'importe quoi. Prix élevé, discrétion garantie.", secret = "Il travaille aussi pour la Police parfois."),
        OtherSim("npc_queen", "La Reine des Profondeurs", 48, "Femme", "Autoritaire", "Souveraine Souterraine", 0, currentLocationId = "underground_city", rank = "Reine", faction = "Underground", hasPower = true, powerName = "Domination Mentale",
            bio = "Règne sur la Cité Souterraine depuis 20 ans. Cruelle et respectée.", secret = "Elle était autrefois scientifique de surface."),

        // Dimensional & Mystérieux
        OtherSim("npc_guardian", "Le Gardien", 999, "Neutre", "Énigmatique", "Gardien de l'Anomalie", -20, currentLocationId = "anomaly", faction = "Dimensional", hasPower = true, powerName = "Maîtrise Temporelle",
            bio = "Existe depuis avant l'écriture. Observe. Intervient rarement.", secret = "Le Gardien a déjà rencontré plusieurs versions de toi."),
        OtherSim("npc_echo", "Écho", 0, "Neutre", "Fragmenté", "Résidu Temporel", -40, currentLocationId = "anomaly", faction = "Dimensional", hasPower = true, powerName = "Écho du Futur",
            bio = "Apparition instable. Parle parfois au passé ou au futur.", secret = "Écho est peut-être ce que tu deviendras."),

        // Civils intéressants
        OtherSim("npc_silas", "Dr. Silas Arden", 40, "Homme", "Empathique", "Médecin-Chef", 20, currentLocationId = "hospital", faction = "Civil",
            bio = "Soigne tout le monde, y compris ceux que la société rejette.", secret = "Il a soigné Omega-7 en secret."),
        OtherSim("npc_lyra", "Lyra Chen", 27, "Femme", "Curieuse", "Journaliste", 11, currentLocationId = "downtown", faction = "Civil",
            bio = "Cherche la vérité sur les pouvoirs et les laboratoires.", secret = "Elle a des preuves contre la Directrice Hale."),
        OtherSim("npc_rex", "Old Rex", 58, "Homme", "Sage", "Barman", 28, currentLocationId = "downtown", faction = "Civil",
            bio = "A tout vu. Donne des conseils cryptiques et d'excellents verres.", secret = "Rex a servi à bord d'un vaisseau il y a longtemps."),
        OtherSim("npc_zephyr", "Zephyr", 23, "Neutre", "Libre", "Messager", 5, currentLocationId = "spaceport", faction = "Civil", hasPower = true, powerName = "Vitesse du Vent",
            bio = "Livre des messages entre la surface, l'espace et les profondeurs.", secret = "Zephyr n'utilise jamais les portes."),
        OtherSim("npc_orin", "Orin Vale", 37, "Homme", "Mélancolique", "Archiviste", 3, currentLocationId = "underground_city", faction = "Underground",
            bio = "Garde la mémoire de la Cité Souterraine.", secret = "Il possède des documents sur l'origine de l'Anomalie."),

        // Nouvelle génération de Sims : vie quotidienne, romance et carrière
        OtherSim("npc_aria", "Aria Morel", 26, "Femme", "Romantique", "Architecte", 18, currentLocationId = "downtown", faction = "Civil",
            bio = "Imagine des maisons modulaires et des jardins suspendus.", secret = "Elle construit une maison secrète pour les Sims à pouvoirs."),
        OtherSim("npc_damien", "Damien Cross", 30, "Homme", "Ambitieux", "Avocat", -2, currentLocationId = "downtown", faction = "Civil",
            bio = "Négocie contrats et propriétés avec une efficacité redoutable.", secret = "Il protège discrètement un dossier sur Omega-7."),
        OtherSim("npc_sora", "Sora Vale", 21, "Femme", "Extravertie", "DJ Néon", 15, currentLocationId = "downtown", faction = "Civil", hasPower = true, powerName = "Onde Sonique",
            bio = "Fait vibrer les nuits de Zernex.", secret = "Sa musique déclenche parfois des phénomènes étranges."),
        OtherSim("npc_milo", "Milo Reyes", 34, "Homme", "Génie", "Inventeur", 7, currentLocationId = "nuclear_plant", faction = "Science", hasPower = true, powerName = "Technopathie",
            bio = "Fabrique des objets impossibles avec des pièces ordinaires.", secret = "Il a créé un prototype capable de copier un pouvoir."),
        OtherSim("npc_iris", "Iris Quill", 29, "Femme", "Juste", "Journaliste d'enquête", 9, currentLocationId = "downtown", faction = "Civil",
            bio = "Travaille avec Lyra pour révéler les secrets de la ville.", secret = "Elle sait qui finance le laboratoire secret."),
        OtherSim("npc_viktor", "Viktor Ash", 44, "Homme", "Froid", "Marchand d'artefacts", -10, currentLocationId = "market", faction = "Criminal", hasPower = true, powerName = "Détection des Artefacts",
            bio = "Connaît la valeur des objets provenant des anomalies.", secret = "Il possède une relique capable d'amplifier l'Écho Originel."),
        OtherSim("npc_nia", "Nia Star", 25, "Femme", "Aventurière", "Exploratrice Stellaire", 13, currentLocationId = "spaceport", faction = "Starfleet",
            bio = "Rêve de découvrir une planète habitée.", secret = "Elle a reçu un signal provenant d'une civilisation inconnue."),
        OtherSim("npc_cass", "Cass Orion", 38, "Neutre", "Mystérieux", "Chrono-chercheur", 1, currentLocationId = "anomaly", faction = "Dimensional", hasPower = true, powerName = "Boucle Temporelle",
            bio = "Étudie les répétitions du temps autour de l'Anomalie.", secret = "Il affirme venir de trois jours dans le futur."),
        OtherSim("npc_zoe", "Zoé Martin", 20, "Femme", "Créative", "Étudiante", 20, currentLocationId = "downtown", faction = "Civil",
            bio = "Étudie la physique quantique et rêve de devenir capitaine.", secret = "Elle possède une aptitude naturelle à détecter les portails."),
        OtherSim("npc_kael", "Kael Mercer", 33, "Homme", "Protecteur", "Secouriste", 16, currentLocationId = "hospital", faction = "Civil",
            bio = "Toujours présent lors des catastrophes et événements majeurs.", secret = "Il a sauvé un Sim pendant une rupture dimensionnelle."),
        OtherSim("npc_elio", "Elio Venn", 31, "Homme", "Sociable", "Chef cuisinier", 25, currentLocationId = "downtown", faction = "Civil",
            bio = "Son restaurant est le point de rencontre de nombreux Sims.", secret = "Il connaît une recette qui restaure énormément d'énergie."),
        OtherSim("npc_maeve", "Maeve Noctis", 27, "Femme", "Rebelle", "Hackeuse", -4, currentLocationId = "market", faction = "Underground", hasPower = true, powerName = "Vision Numérique",
            bio = "Entre dans les systèmes les mieux protégés de Zernex.", secret = "Elle peut voir les signatures numériques des pouvoirs.")
    )
}
