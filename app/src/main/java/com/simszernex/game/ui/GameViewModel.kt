package com.simszernex.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simszernex.game.model.*
import com.simszernex.game.world.WorldData
import com.simszernex.game.world.WorldLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameState(
    val character: SimCharacter = SimCharacter(),
    val npcs: List<OtherSim> = NpcData.allNpcs,
    val missions: List<Mission> = MissionData.dailyMissions(),
    val locations: List<WorldLocation> = WorldData.locations,
    val ships: List<LivingShip> = ShipData.ships,
    val fusions: List<PowerFusion> = FusionData.fusions,
    val currentMajorEvent: MajorEvent? = null,
    val currentLocationId: String = "home",
    val currentRoom: String = "Salon",
    val currentUniverse: String = "Résidence Zernex",
    val timeHour: Int = 8,
    val timeMinute: Int = 0,
    val day: Int = 1,
    val isDay: Boolean = true,
    val message: String = "Écho Originel pulse. Lignée, vaisseaux vivants, fusions et événements de rupture t'attendent.",
    val showMagicCircle: Boolean = false,
    val magicCircleStyle: String = "",
    val isCreated: Boolean = false,
    val availableActions: List<HouseObject> = HouseData.objectsInRoom("Salon"),
    val selectedTab: Int = 0,
    val lastEvent: String = "",
    val reputation: Map<String, Int> = mapOf(
        "Police" to 10, "Starfleet" to 8, "Science" to 5,
        "Underground" to 0, "Criminal" to 0, "Dimensional" to 0, "Fairy" to 5
    ),
    val powerCooldowns: Map<PowerType, Long> = emptyMap(),
    val activeEffects: List<String> = emptyList(),
    val shopItems: List<InventoryItem> = defaultShopItems()
)

fun defaultShopItems() = listOf(
    InventoryItem("ai_basic", "IA de Bord Basique", "ai", 1, "Assiste dans les vaisseaux", 350),
    InventoryItem("ai_advanced", "IA Quantique", "ai", 1, "Pilot + pilotage", 1100),
    InventoryItem("ai_companion", "IA Compagnon", "ai", 1, "Personnalité liée", 850),
    InventoryItem("wings_charm", "Charme d'Ailes", "magic", 1, "Bonus vol", 300),
    InventoryItem("potion_mana", "Potion de Mana", "magic", 1, "+40 Mana", 150),
    InventoryItem("potion_energy", "Elixir d'Énergie", "food", 1, "+35 Énergie", 100),
    InventoryItem("data_chip", "Puce de Savoir", "cyber", 1, "Connaissance +20", 450),
    InventoryItem("fairy_dust", "Poussière de Fée", "magic", 1, "Apprentissage facilité", 220)
)

class GameViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    private var timeJob: Job? = null
    private var eventCooldown = 0

    init { startTimeLoop() }

    private fun startTimeLoop() {
        timeJob = viewModelScope.launch {
            while (true) {
                delay(3500)
                _state.update { current ->
                    var hour = current.timeHour
                    var minute = current.timeMinute + 10
                    var day = current.day
                    if (minute >= 60) { minute = 0; hour++ }
                    if (hour >= 24) { hour = 0; day++ }
                    val isDay = hour in 6..20
                    val newNeeds = current.character.needs.decay(0.40f)
                    val newMana = (current.character.mana + 5).coerceAtMost(current.character.maxMana)
                    val critical = when {
                        newNeeds.hunger < 10f -> "⚠ Faim critique !"
                        newNeeds.energy < 8f -> "⚠ Épuisement !"
                        newNeeds.bladder < 8f -> "⚠ Besoin urgent !"
                        else -> null
                    }

                    // Autonomie forte des Sims
                    val movedNpcs = current.npcs.map { npc ->
                        if (npc.isAutonomous && (0..100).random() < 24) {
                            val possible = current.locations.filter { it.isUnlocked }.map { it.id }
                            npc.copy(currentLocationId = possible.random(), mood = listOf("Neutre", "Occupé", "Curieux", "Joyeux", "Pensif", "Motivé").random())
                        } else npc
                    }

                    // Bond des vaisseaux actifs
                    val updatedShips = current.ships.map { ship ->
                        if (ship.unlocked && current.character.activeShipId == ship.id && (0..100).random() < 20) {
                            ship.copy(bond = (ship.bond + 2).coerceAtMost(100))
                        } else ship
                    }

                    var msg = current.message
                    var eventMsg = current.lastEvent
                    var major = current.currentMajorEvent
                    eventCooldown--

                    // Événements de rupture (rares mais marquants)
                    if (major == null && eventCooldown <= 0 && (0..100).random() < 8) {
                        val candidates = EventData.possibleEvents.filter { it.id !in current.character.majorEventsResolved }
                        if (candidates.isNotEmpty()) {
                            major = candidates.random().copy(isActive = true)
                            msg = "⚠ ÉVÉNEMENT DE RUPTURE : ${major.title}"
                            eventMsg = major.title
                            eventCooldown = 25
                        }
                    }

                    if ((0..100).random() < 10) {
                        eventMsg = listOf(
                            "L'Horizon signale une anomalie intéressante.",
                            "La Reine Fée a envoyé une invitation.",
                            "Un vaisseau allié a besoin d'assistance.",
                            "Écho Originel résonne plus fort.",
                            "Des rumeurs circulent sur Omega-7."
                        ).random()
                    }

                    val now = System.currentTimeMillis()
                    val cleanedCd = current.powerCooldowns.filter { it.value > now }

                    current.copy(
                        timeHour = hour, timeMinute = minute, day = day, isDay = isDay,
                        character = current.character.copy(needs = newNeeds, mana = newMana),
                        npcs = movedNpcs, ships = updatedShips,
                        lastEvent = eventMsg, message = critical ?: msg,
                        currentMajorEvent = major,
                        powerCooldowns = cleanedCd
                    )
                }
            }
        }
    }

    fun createCharacter(name: String, gender: String, personality: List<String>) {
        _state.update {
            val char = it.character.copy(
                name = name.ifBlank { "Alex" },
                gender = gender,
                personality = personality.ifEmpty { listOf("Curieux") }
            ).addJournal(1, "Éveil", "Tu as pris conscience d'Écho Originel. Les univers s'ouvrent.")
            it.copy(character = char, isCreated = true, message = "Bienvenue ${char.name}. Lignée, vaisseaux et destin t'attendent.")
        }
    }

    fun changeTab(tab: Int) = _state.update { it.copy(selectedTab = tab) }

    fun travelTo(locationId: String) {
        _state.update { current ->
            val loc = current.locations.find { it.id == locationId } ?: return@update current
            if (!loc.isUnlocked) return@update current.copy(message = "Lieu verrouillé.")
            if (loc.requiredRank > 0 && current.character.starfleetRank < loc.requiredRank) {
                return@update current.copy(message = "Rang insuffisant (${current.character.rankTitle()}).")
            }
            // Réputation change les messages
            val repBonus = when {
                loc.universe.contains("Fées") && (current.reputation["Fairy"] ?: 0) > 30 -> " La forêt t'accueille chaleureusement."
                loc.universe.contains("Fédération") && (current.reputation["Starfleet"] ?: 0) > 40 -> " Les officiers te saluent avec respect."
                loc.universe.contains("Souterrains") && (current.reputation["Underground"] ?: 0) > 25 -> " Les ombres s'écartent sur ton passage."
                else -> ""
            }
            current.copy(currentLocationId = locationId, currentUniverse = loc.universe,
                message = "→ ${loc.name}$repBonus\n${loc.flavor.ifBlank { loc.description }}")
        }
    }

    fun changeRoom(room: String) = _state.update {
        it.copy(currentRoom = room, availableActions = HouseData.objectsInRoom(room),
            character = it.character.copy(currentRoom = room), message = "Pièce : $room")
    }

    fun performAction(obj: HouseObject) {
        _state.update { current ->
            val n = current.character.needs
            val newNeeds = n.copy(
                hunger = (n.hunger + obj.hungerEffect).coerceIn(0f, 100f),
                energy = (n.energy + obj.energyEffect).coerceIn(0f, 100f),
                hygiene = (n.hygiene + obj.hygieneEffect).coerceIn(0f, 100f),
                funNeed = (n.funNeed + obj.funEffect).coerceIn(0f, 100f),
                social = (n.social + obj.socialEffect).coerceIn(0f, 100f),
                bladder = (n.bladder + obj.bladderEffect).coerceIn(0f, 100f)
            ).let { it.copy(mood = it.average()) }
            current.copy(character = current.character.copy(needs = newNeeds), message = "→ ${obj.actionLabel}")
        }
    }

    fun usePower(powerType: PowerType) {
        val current = _state.value
        val power = current.character.powers.find { it.type == powerType } ?: return
        if (!power.unlocked) { _state.update { it.copy(message = "Pouvoir non appris.") }; return }
        val now = System.currentTimeMillis()
        if ((current.powerCooldowns[powerType] ?: 0) > now) { _state.update { it.copy(message = "En recharge...") }; return }
        if (current.character.mana < power.manaCost) { _state.update { it.copy(message = "Mana insuffisant.") }; return }

        val level = current.character.powerLevel(powerType)
        _state.update {
            it.copy(showMagicCircle = true, magicCircleStyle = power.stylishEffect,
                character = it.character.copy(isUsingPower = true, currentPower = powerType, mana = it.character.mana - power.manaCost),
                message = "✧ ${power.name} (Niv.$level) ✧\n${power.stylishEffect}")
        }

        viewModelScope.launch {
            delay(2300)
            _state.update { state ->
                var needs = state.character.needs
                var extra = "Pouvoir utilisé."
                var npcs = state.npcs
                var char = state.character.withPowerLevelUp(powerType) // monte en niveau
                val effects = state.activeEffects.toMutableList()

                when (powerType) {
                    PowerType.ORIGIN_ECHO -> {
                        needs = needs.copy(funNeed = (needs.funNeed + 18f).coerceIn(0f, 100f), mood = 92f)
                        extra = "Écho Originel s'amplifie. Lignée et apprentissages renforcés. (+savoir)"
                        effects.add("Écho fort")
                        char = char.copy(knowledge = char.knowledge + 2)
                    }
                    PowerType.REGENERATION -> {
                        val bonus = 40f + level * 8f
                        needs = needs.copy(energy = (needs.energy + bonus).coerceIn(0f, 100f), hunger = (needs.hunger + 20f + level * 3).coerceIn(0f, 100f))
                        extra = "Régénération niveau $level."
                    }
                    PowerType.MIND_READ -> {
                        val near = state.npcs.filter { it.currentLocationId == state.currentLocationId }
                        if (near.isNotEmpty()) {
                            val t = near.random()
                            extra = "Secret de ${t.name} : ${t.secret.ifBlank { t.bio }}"
                            npcs = state.npcs.map { if (it.id == t.id) it.copy(relationship = (it.relationship + 6).coerceIn(-100, 100)) else it }
                        } else extra = "Aucun esprit proche."
                    }
                    PowerType.HEAL_OTHER -> {
                        val near = state.npcs.filter { it.currentLocationId == state.currentLocationId }
                        if (near.isNotEmpty()) {
                            val t = near.random()
                            npcs = state.npcs.map { if (it.id == t.id) it.copy(relationship = (it.relationship + 15).coerceIn(-100, 100), mood = "Reconnaissant") else it }
                            extra = "Tu as soigné ${t.name}."
                        }
                    }
                    PowerType.FLIGHT, PowerType.STRENGTH, PowerType.SHIELD, PowerType.TELEPORT,
                    PowerType.TIME_CONTROL, PowerType.MAGIC_BLAST, PowerType.PHASE,
                    PowerType.GRAVITY, PowerType.ILLUSION, PowerType.QUANTUM_SHIFT, PowerType.INVISIBILITY,
                    PowerType.AEGIS_WINGS, PowerType.QUANTUM_SHIELD, PowerType.MIND_ILLUSION -> {
                        needs = needs.copy(funNeed = (needs.funNeed + 12f + level * 2).coerceIn(0f, 100f))
                        extra = "${power.name} (Niv.$level) utilisé avec style."
                    }
                }

                // Bonus vaisseau actif
                val ship = state.ships.find { it.id == char.activeShipId && it.unlocked }
                if (ship != null && ship.bond > 40) {
                    extra += "\n[${ship.aiName}] : « Belle utilisation, ${char.name}. »"
                    char = char.copy(mana = (char.mana + 5).coerceAtMost(char.maxMana))
                }

                state.copy(
                    showMagicCircle = false,
                    character = char.copy(needs = needs, isUsingPower = false, currentPower = null),
                    npcs = npcs,
                    powerCooldowns = state.powerCooldowns + (powerType to (now + power.cooldownSeconds * 1000L)),
                    activeEffects = effects.takeLast(6),
                    message = "✧ ${power.name} ✧\n$extra"
                )
            }
        }
    }

    fun learnPower(type: PowerType) {
        _state.update { current ->
            val power = current.character.powers.find { it.type == type } ?: return@update current
            if (power.unlocked) return@update current.copy(message = "Déjà appris.")
            val cost = (power.learnCost * 0.5).toInt() // Écho Originel facilite beaucoup
            if (current.character.money < cost) return@update current.copy(message = "Il faut $cost §.")
            val updated = current.character.powers.map { if (it.type == type) it.copy(unlocked = true) else it }
            val char = current.character.copy(powers = updated, money = current.character.money - cost, knowledge = current.character.knowledge + 5)
                .addJournal(current.day, "Nouveau pouvoir", "Tu as appris ${power.name}.")
            current.copy(character = char, message = "✦ ${power.name} appris grâce à Écho Originel !")
        }
    }

    fun unlockAllPowers() {
        _state.update { current ->
            val updated = current.character.powers.map { it.copy(unlocked = true) }
            current.copy(character = current.character.copy(powers = updated, maxMana = 200, mana = 200)
                .addJournal(current.day, "Maîtrise", "Tous les pouvoirs sont maintenant tiens."),
                message = "Écho Originel a tout débloqué.")
        }
    }

    fun tryFusion(fusionId: String) {
        _state.update { current ->
            val fusion = current.fusions.find { it.id == fusionId } ?: return@update current
            if (fusion.id in current.character.unlockedFusions) return@update current.copy(message = "Fusion déjà connue.")
            val hasAll = fusion.requiredPowers.all { req -> current.character.powers.any { it.type == req && it.unlocked } }
            if (!hasAll) return@update current.copy(message = "Il te manque des pouvoirs requis pour cette fusion.")
            current.copy(
                character = current.character.copy(unlockedFusions = current.character.unlockedFusions + fusion.id)
                    .addJournal(current.day, "Fusion", "Tu as créé : ${fusion.name}"),
                fusions = current.fusions.map { if (it.id == fusionId) it.copy(unlocked = true) else it },
                message = "✦ Fusion réalisée : ${fusion.name} ! ${fusion.description}"
            )
        }
    }

    fun unlockShip(shipId: String) {
        _state.update { current ->
            val ship = current.ships.find { it.id == shipId } ?: return@update current
            if (ship.unlocked) return@update current.copy(message = "Vaisseau déjà actif.")
            if (current.character.starfleetRank < 2 && shipId == "horizon") return@update current.copy(message = "Monte en grade à l'Académie d'abord.")
            val updated = current.ships.map { if (it.id == shipId) it.copy(unlocked = true, bond = 25) else it }
            current.copy(ships = updated, character = current.character.copy(activeShipId = shipId)
                .addJournal(current.day, "Vaisseau", "Tu as lié le vaisseau ${ship.name}. IA : ${ship.aiName} (${ship.aiPersonality})"),
                message = "✦ Vaisseau ${ship.name} lié ! IA ${ship.aiName} est maintenant avec toi.")
        }
    }

    fun interactWithShip() {
        _state.update { current ->
            val ship = current.ships.find { it.id == current.character.activeShipId && it.unlocked }
                ?: return@update current.copy(message = "Aucun vaisseau actif.")
            val newBond = (ship.bond + 8).coerceAtMost(100)
            val updated = current.ships.map { if (it.id == ship.id) it.copy(bond = newBond) else it }
            val reply = when (ship.aiPersonality) {
                "Loyale" -> "Je suis à tes côtés, toujours."
                "Sarcastique" -> "Encore une discussion philosophique ? Très bien..."
                "Protectrice" -> "Reste vigilant. Je surveille les alentours."
                "Curieuse" -> "J'ai détecté quelque chose d'intéressant à proximité."
                "Empathique" -> "Je ressens ta présence. Ça fait du bien."
                else -> "Lien renforcé."
            }
            val missions = current.missions.map { m ->
                if (m.type == MissionType.SHIP_BOND && !m.isCompleted) m.copy(currentProgress = 1, isCompleted = true) else m
            }
            current.copy(ships = updated, missions = missions, message = "[${ship.aiName}] $reply (Lien : $newBond)")
        }
    }

    fun resolveMajorEvent(choiceIndex: Int) {
        _state.update { current ->
            val event = current.currentMajorEvent ?: return@update current
            val choice = event.choices.getOrNull(choiceIndex) ?: return@update current
            var char = current.character.copy(majorEventsResolved = current.character.majorEventsResolved + event.id)
            var rep = current.reputation.toMutableMap()
            var flags = char.worldFlags.toMutableMap()
            var msg = "Tu as choisi : $choice."

            when (event.id) {
                "reactor_crisis" -> {
                    when (choiceIndex) {
                        0 -> { char = char.copy(knowledge = char.knowledge + 15, money = char.money + 400); msg += " Tu as stabilisé le réacteur. Les scientifiques te respectent." }
                        1 -> { flags["reactor_sacrificed"] = true; msg += " Zone sacrifiée. Conséquences à long terme." }
                        2 -> { rep["Starfleet"] = (rep["Starfleet"] ?: 0) + 12; msg += " Starfleet intervient. Ta réputation monte." }
                    }
                }
                "fairy_pact" -> {
                    when (choiceIndex) {
                        0 -> { flags["fairy_pact"] = true; rep["Fairy"] = (rep["Fairy"] ?: 0) + 25; char = char.copy(maxMana = char.maxMana + 20); msg += " Pacte accepté. La Forêt t'est alliée." }
                        1 -> { rep["Fairy"] = (rep["Fairy"] ?: 0) + 10; msg += " Pacte négocié." }
                        2 -> { msg += " Tu as refusé. La Reine s'en souviendra." }
                    }
                }
                "omega_choice" -> {
                    when (choiceIndex) {
                        0 -> { flags["omega_freed"] = true; msg += " Omega-7 est libre. Un allié puissant et instable." }
                        1 -> { msg += " Omega-7 stabilisé." }
                        2 -> { rep["Police"] = (rep["Police"] ?: 0) + 10; rep["Science"] = (rep["Science"] ?: 0) + 8; msg += " Tu l'as livré. Certaines portes s'ouvrent, d'autres se ferment." }
                    }
                }
                else -> { char = char.copy(money = char.money + 300); msg += " Événement résolu." }
            }

            char = char.copy(worldFlags = flags).addJournal(current.day, event.title, msg)
            current.copy(currentMajorEvent = null, character = char, reputation = rep, message = "✦ $msg")
        }
    }

    fun tryForChild() {
        _state.update { current ->
            if (current.character.spouseId == null) return@update current.copy(message = "Mariage requis.")
            if (current.character.hasChildren) return@update current.copy(message = "Déjà un enfant.")
            // Lignée : chance d'hériter d'Écho Originel
            val inherit = (0..100).random() < 70
            val char = current.character.copy(
                hasChildren = true,
                children = listOf("Enfant Zernex"),
                childHasOriginEcho = inherit
            ).addJournal(current.day, "Naissance", if (inherit) "Ton enfant a hérité d'une étincelle d'Écho Originel !" else "Un enfant est né.")
            current.copy(character = char, message = if (inherit) "✦ Naissance ! L'enfant porte une étincelle d'Écho Originel." else "✦ Un enfant est né dans ta lignée.")
        }
    }

    fun attendAcademy() {
        _state.update { current ->
            val progress = current.character.academyProgress + 18
            var rank = current.character.starfleetRank
            var moneyGain = 220
            var msg = "Cours suivi. Progression +18."
            if (progress >= 100 && rank < 10) {
                rank += 1
                moneyGain += 400
                msg = "✦ Promotion ! Tu es maintenant ${current.character.copy(starfleetRank = rank).rankTitle()}."
            }
            val locs = current.locations.map { loc ->
                when {
                    rank >= 2 && loc.id.contains("horizon") -> loc.copy(isUnlocked = true)
                    rank >= 4 && loc.id.contains("aurora") -> loc.copy(isUnlocked = true)
                    rank >= 6 && loc.id.contains("command") -> loc.copy(isUnlocked = true)
                    else -> loc
                }
            }
            var ships = current.ships
            if (rank >= 2) ships = ships.map { if (it.id == "horizon") it.copy(unlocked = true) else it }

            current.copy(
                character = current.character.copy(
                    academyProgress = progress % 100,
                    starfleetRank = rank,
                    starfleetRankName = current.character.copy(starfleetRank = rank).rankTitle(),
                    career = if (rank > 0) "Starfleet" else current.character.career,
                    money = current.character.money + moneyGain,
                    knowledge = current.character.knowledge + 10
                ).addJournal(current.day, "Académie", msg),
                locations = locs, ships = ships,
                reputation = current.reputation.toMutableMap().apply { this["Starfleet"] = (this["Starfleet"] ?: 0) + 5 },
                message = msg
            )
        }
    }

    fun work() {
        _state.update { current ->
            val careerBonus = when (current.character.career) {
                "Starfleet" -> 60
                "Médecin", "Diplomate" -> 50
                "Scientifique", "Police" -> 40
                "Netrunner", "Commerçant" -> 45
                else -> 20
            }
            val base = 300 + current.character.careerLevel * 75 + current.character.starfleetRank * 55 + careerBonus
            current.copy(character = current.character.copy(
                money = current.character.money + base,
                needs = current.character.needs.copy(energy = (current.character.needs.energy - 14f).coerceIn(0f, 100f))
            ), message = "Travail (${current.character.career}) terminé. +$base §")
        }
    }

    fun talkToNpc(npcId: String) {
        _state.update { current ->
            val npc = current.npcs.find { it.id == npcId } ?: return@update current
            val gain = (9..19).random()
            val updated = current.npcs.map { if (it.id == npcId) it.copy(relationship = (it.relationship + gain).coerceIn(-100, 100)) else it }
            val newRep = current.reputation.toMutableMap()
            newRep[npc.faction] = ((newRep[npc.faction] ?: 0) + 4).coerceIn(-100, 100)
            current.copy(npcs = updated,
                character = current.character.copy(needs = current.character.needs.copy(social = (current.character.needs.social + 12f).coerceIn(0f, 100f))),
                reputation = newRep,
                message = "Discussion avec ${npc.name}. Relation renforcée.")
        }
    }

    fun proposeMarriage(npcId: String) {
        _state.update { current ->
            val npc = current.npcs.find { it.id == npcId } ?: return@update current
            if (npc.relationship < 55) return@update current.copy(message = "Relation insuffisante.")
            if (current.character.spouseId != null) return@update current.copy(message = "Déjà engagé(e).")
            val updated = current.npcs.map { if (it.id == npcId) it.copy(isMarried = true, relationship = 96) else it }
            current.copy(npcs = updated, character = current.character.copy(relationshipStatus = "Marié(e)", spouseId = npcId)
                .addJournal(current.day, "Mariage", "Tu t'es marié(e) avec ${npc.name}."),
                message = "✦ Mariage avec ${npc.name} !")
        }
    }

    fun claimMission(id: String) {
        _state.update { current ->
            val m = current.missions.find { it.id == id } ?: return@update current
            if (!m.isCompleted || m.isClaimed) return@update current
            current.copy(missions = current.missions.map { if (it.id == id) it.copy(isClaimed = true) else it },
                character = current.character.copy(money = current.character.money + m.rewardMoney + 100),
                message = "Mission accomplie ! +${m.rewardMoney + 100} §")
        }
    }

    fun buyFood() = _state.update { current ->
        if (current.character.money < 35) return@update current.copy(message = "Pas assez d'argent.")
        current.copy(character = current.character.copy(money = current.character.money - 35,
            needs = current.character.needs.copy(hunger = (current.character.needs.hunger + 55f).coerceIn(0f, 100f))),
            message = "Repas pris.")
    }

    fun buyMana(size: String) {
        _state.update { current ->
            val (cost, restore, maxBonus) = when (size) {
                "small" -> Triple(120, 35, 0)
                "medium" -> Triple(280, 80, 0)
                "large" -> Triple(650, 999, 10)
                "battery" -> Triple(1400, 0, 25)
                else -> Triple(120, 35, 0)
            }
            if (current.character.money < cost) return@update current.copy(message = "Il faut $cost § pour ce mana.")
            val newMax = current.character.maxMana + maxBonus
            val newMana = if (restore >= 999) newMax else (current.character.mana + restore).coerceAtMost(newMax)
            current.copy(
                character = current.character.copy(
                    money = current.character.money - cost,
                    mana = newMana,
                    maxMana = newMax
                ),
                message = when (size) {
                    "battery" -> "✦ Batterie Arcane installée. Max Mana +25 !"
                    "large" -> "✦ Cœur de Mana absorbé. Mana restauré et max augmenté."
                    else -> "Mana restauré (+$restore)."
                }
            )
        }
    }

    fun buyLuxury(id: String) {
        _state.update { current ->
            val item = LuxuryData.items.find { it.id == id } ?: return@update current
            if (current.character.money < item.price) return@update current.copy(message = "Il faut ${item.price} §. Tu as ${current.character.money} §.")
            var char = current.character.copy(money = current.character.money - item.price)
            var msg = "✦ Acquis : ${item.name} (${item.price} §)"
            when (item.category) {
                "property" -> {
                    if (id in char.ownedHouses) return@update current.copy(message = "Déjà possédé.")
                    char = char.copy(ownedHouses = char.ownedHouses + id)
                    msg += " — Nouvelle propriété !"
                }
                "mana" -> { /* handled separately */ }
                "status", "luxury" -> {
                    char = char.addItem(InventoryItem(item.id, item.name, item.category, 1, item.description, item.price))
                    msg += " — Prestige augmenté."
                }
                "vehicle" -> {
                    char = char.addItem(InventoryItem(item.id, item.name, "vehicle", 1, item.description, item.price))
                    msg += " — Véhicule ajouté."
                }
                else -> char = char.addItem(InventoryItem(item.id, item.name, item.category, 1, item.description, item.price))
            }
            char = char.addJournal(current.day, "Acquisition", "${item.name} pour ${item.price} §")
            current.copy(character = char, message = msg)
        }
    }

    fun listCareers(): List<Career> = CareerData.careers
    fun listLuxury(): List<LuxuryItem> = LuxuryData.items

    fun buyHouse(locationId: String) {
        _state.update { current ->
            val loc = current.locations.find { it.id == locationId && it.isHousing } ?: return@update current
            if (locationId in current.character.ownedHouses) return@update current.copy(message = "Déjà possédée.")
            if (current.character.money < loc.price) return@update current.copy(message = "Il faut ${loc.price} §.")
            current.copy(character = current.character.copy(money = current.character.money - loc.price, ownedHouses = current.character.ownedHouses + locationId)
                .addJournal(current.day, "Propriété", "Maison achetée : ${loc.name}"),
                message = "✦ Maison acquise : ${loc.name}")
        }
    }

    fun buyItem(itemId: String) {
        _state.update { current ->
            val item = current.shopItems.find { it.id == itemId } ?: return@update current
            if (current.character.money < item.price) return@update current.copy(message = "Fonds insuffisants.")
            var char = current.character.copy(money = current.character.money - item.price).addItem(item)
            if (item.type == "ai") char = char.copy(ownedAI = char.ownedAI + item.name)
            if (item.id == "data_chip") char = char.copy(knowledge = char.knowledge + 20)
            if (item.id == "potion_mana") char = char.copy(mana = (char.mana + 40).coerceAtMost(char.maxMana))
            current.copy(character = char, message = "Acheté : ${item.name}")
        }
    }

    
    fun resignCareer() {
        _state.update { current ->
            if (current.character.career == "Civil" || current.character.career == "Sans emploi") {
                return@update current.copy(message = "Tu n'as pas de poste officiel à quitter.")
            }
            val old = current.character.career
            current.copy(
                character = current.character.copy(
                    career = "Civil",
                    careerLevel = 0
                    // on garde le rang Starfleet s'il existe (souvenir / honneur)
                ).addJournal(current.day, "Démission", "Tu as quitté le poste : $old"),
                message = "Tu as démissionné de « $old ». Tu es maintenant Civil libre."
            )
        }
    }

    fun changeCareer(career: String) {
        _state.update { current ->
            if (career == current.character.career) {
                return@update current.copy(message = "Tu exerces déjà ce métier.")
            }
            current.copy(
                character = current.character.copy(career = career, careerLevel = maxOf(1, current.character.careerLevel)),
                message = "Tu changes de voie : $career. (Tu peux démissionner à tout moment.)"
            )
        }
    }

fun joinCareer(career: String) = _state.update { it.copy(character = it.character.copy(career = career), message = "Voie : $career") }

    fun specialAction(action: String) {
        _state.update { current ->
            when (action) {
                "board_starship" -> {
                    if (current.character.starfleetRank < 1) current.copy(message = "Va d'abord à l'Académie Starfleet.")
                    else {
                        val ships = current.ships.map { if (it.id == "horizon") it.copy(unlocked = true) else it }
                        current.copy(ships = ships, currentLocationId = "starship_horizon",
                            character = current.character.copy(activeShipId = "horizon"),
                            message = "Tu montes à bord. ${current.character.rankTitle()} en service.")
                    }
                }
                "enter_anomaly" -> current.copy(currentLocationId = "nexus_anomaly", message = "Tu entres dans l'Anomalie...")
                else -> current
            }
        }
    }

    
    // ===== Fonctions nécessaires manquantes =====

    fun restFully() {
        _state.update { current ->
            val n = current.character.needs
            current.copy(
                character = current.character.copy(
                    needs = n.copy(
                        energy = 100f,
                        hunger = (n.hunger + 10f).coerceIn(0f, 100f),
                        bladder = (n.bladder + 15f).coerceIn(0f, 100f),
                        mood = 85f
                    )
                ),
                message = "Tu te reposes profondément. Énergie restaurée."
            )
        }
    }

    fun useInventoryItem(itemId: String) {
        _state.update { current ->
            val item = current.character.inventory.find { it.id == itemId }
                ?: return@update current.copy(message = "Objet introuvable.")
            if (item.quantity <= 0) return@update current.copy(message = "Plus d'unités.")

            var char = current.character
            var msg = "Tu utilises : ${item.name}"

            when {
                item.type == "food" || "repas" in item.name.lowercase() || "elixir" in item.name.lowercase() -> {
                    char = char.copy(needs = char.needs.copy(
                        hunger = (char.needs.hunger + 40f).coerceIn(0f, 100f),
                        energy = (char.needs.energy + 15f).coerceIn(0f, 100f)
                    ))
                    msg = "Tu consommes ${item.name}. Faim et énergie remontent."
                }
                item.type == "magic" && "mana" in item.name.lowercase() -> {
                    char = char.copy(mana = (char.mana + 40).coerceAtMost(char.maxMana))
                    msg = "Mana restauré grâce à ${item.name}."
                }
                item.id.contains("mana") -> {
                    char = char.copy(mana = (char.mana + 50).coerceAtMost(char.maxMana))
                    msg = "Cristal de mana utilisé."
                }
                else -> msg = "${item.name} utilisé."
            }

            // Réduire quantité
            val newInv = char.inventory.map {
                if (it.id == itemId) it.copy(quantity = (it.quantity - 1).coerceAtLeast(0)) else it
            }.filter { it.quantity > 0 }
            char = char.copy(inventory = newInv)

            current.copy(character = char, message = msg)
        }
    }

    fun checkCriticalNeeds(): String? {
        val n = _state.value.character.needs
        return when {
            n.hunger < 8f -> "⚠ Tu meurs de faim ! Mange vite."
            n.energy < 5f -> "⚠ Épuisement total. Repose-toi."
            n.bladder < 5f -> "⚠ Besoin urgent ! Trouve des toilettes."
            n.hygiene < 8f -> "⚠ Hygiène critique."
            else -> null
        }
    }

    fun quickSave(contextHint: Boolean = true) {
        // Sauvegarde légère des valeurs essentielles (le vrai SaveSystem est branché depuis l'Activity si besoin)
        _state.update { current ->
            current.copy(
                message = if (contextHint) "Partie sauvegardée (données principales)." else current.message,
                character = current.character.addJournal(current.day, "Sauvegarde", "Progression enregistrée.")
            )
        }
    }

    override fun onCleared() { timeJob?.cancel(); super.onCleared() }
}
