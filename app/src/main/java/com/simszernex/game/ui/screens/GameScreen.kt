package com.simszernex.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simszernex.game.model.*
import com.simszernex.game.ui.GameState
import com.simszernex.game.world.WorldLocation
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: GameState,
    onCreateCharacter: (String, String, List<String>) -> Unit,
    onChangeRoom: (String) -> Unit,
    onPerformAction: (HouseObject) -> Unit,
    onUsePower: (PowerType) -> Unit,
    onLearnPower: (PowerType) -> Unit,
    onUnlockAllPowers: () -> Unit,
    onBuyFood: () -> Unit,
    onChangeTab: (Int) -> Unit,
    onTalkToNpc: (String) -> Unit,
    onProposeMarriage: (String) -> Unit,
    onTryForChild: () -> Unit,
    onClaimMission: (String) -> Unit,
    onWork: () -> Unit,
    onTravelTo: (String) -> Unit,
    onJoinCareer: (String) -> Unit,
    onSpecialAction: (String) -> Unit,
    onBuyHouse: (String) -> Unit,
    onBuyItem: (String) -> Unit,
    onAttendAcademy: () -> Unit
) {
    if (!state.isCreated) {
        CharacterCreationScreen(onCreate = onCreateCharacter)
        return
    }

    val currentLoc = state.locations.find { it.id == state.currentLocationId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SimAvatar(state.character.name, 42)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(state.character.name, fontWeight = FontWeight.Bold)
                            Text(
                                "Jour ${state.day} • %02d:%02d • ${currentLoc?.name ?: "Maison"}".format(state.timeHour, state.timeMinute),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                actions = {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                        Text("${state.character.money} §", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("${state.character.mana}/${state.character.maxMana} mana", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavItem(0, state.selectedTab, onChangeTab, Icons.Default.Home, "Vie")
                NavItem(1, state.selectedTab, onChangeTab, Icons.Default.Public, "Monde")
                NavItem(2, state.selectedTab, onChangeTab, Icons.Default.People, "Sims")
                NavItem(3, state.selectedTab, onChangeTab, Icons.Default.AutoAwesome, "Pouvoirs")
                NavItem(4, state.selectedTab, onChangeTab, Icons.Default.Inventory2, "Profil")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            StatusBanner(state)
            when (state.selectedTab) {
                0 -> LifeTab(state, onChangeRoom, onPerformAction, onUsePower, onBuyFood, onWork, onAttendAcademy)
                1 -> WorldTab(state, currentLoc, onTravelTo, onJoinCareer, onSpecialAction)
                2 -> SimsTab(state, onTalkToNpc, onProposeMarriage, onTryForChild)
                3 -> PowersTab(state, onUsePower, onLearnPower, onUnlockAllPowers, onClaimMission)
                else -> ProfileTab(state, onBuyItem, onBuyHouse)
            }
        }
    }

    if (state.showMagicCircle) MagicCircleEffect()
}

@Composable
private fun NavItem(index: Int, selected: Int, onChange: (Int) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    NavigationBarItem(selected = selected == index, onClick = { onChange(index) }, icon = { Icon(icon, null) }, label = { Text(label) })
}

@Composable
private fun StatusBanner(state: GameState) {
    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                Text(state.message, style = MaterialTheme.typography.bodySmall)
                if (state.lastEvent.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("✦ ${state.lastEvent}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
fun LifeTab(
    state: GameState,
    onChangeRoom: (String) -> Unit,
    onPerformAction: (HouseObject) -> Unit,
    onUsePower: (PowerType) -> Unit,
    onBuyFood: () -> Unit,
    onWork: () -> Unit,
    onAttendAcademy: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HeroCard(state) }
        item { NeedsCard(state.character.needs) }
        item {
            SectionTitle("Maison • ${state.currentRoom}")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HouseData.rooms) { room ->
                    FilterChip(selected = state.currentRoom == room, onClick = { onChangeRoom(room) }, label = { Text(room) })
                }
            }
        }
        items(state.availableActions) { obj -> ObjectCard(obj) { onPerformAction(obj) } }
        item {
            SectionTitle("Actions rapides")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBuyFood) { Icon(Icons.Default.Restaurant, null); Spacer(Modifier.width(6.dp)); Text("Manger") }
                OutlinedButton(onClick = onWork) { Icon(Icons.Default.Work, null); Spacer(Modifier.width(6.dp)); Text("Travail") }
                OutlinedButton(onClick = onAttendAcademy) { Icon(Icons.Default.School, null); Spacer(Modifier.width(6.dp)); Text("Académie") }
            }
        }
        item {
            SectionTitle("Pouvoirs actifs")
            val powers = state.character.unlockedPowers()
            if (powers.isEmpty()) Text("Écho Originel est éveillé. Apprends tes premiers pouvoirs dans l'onglet Pouvoirs.", style = MaterialTheme.typography.bodySmall)
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(powers.take(8)) { p -> AssistChip(onClick = { onUsePower(p.type) }, label = { Text("${p.name} • ${p.manaCost}") }, leadingIcon = { Icon(Icons.Default.AutoAwesome, null) }) }
            }
        }
    }
}

@Composable
private fun HeroCard(state: GameState) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            SimAvatar(state.character.name, 76)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("${state.character.name} • ${state.character.age} ans", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${state.character.career} • ${state.character.rankTitle()}")
                Text(state.character.personality.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text("Humeur ${state.character.needs.mood.toInt()}% • Connaissance ${state.character.knowledge}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun NeedsCard(needs: Needs) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Besoins", fontWeight = FontWeight.Bold)
                Text("Humeur ${needs.mood.toInt()}%", color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(5.dp))
            NeedBar("Faim", needs.hunger)
            NeedBar("Énergie", needs.energy)
            NeedBar("Hygiène", needs.hygiene)
            NeedBar("Fun", needs.funNeed)
            NeedBar("Social", needs.social)
            NeedBar("Vessie", needs.bladder)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun WorldTab(state: GameState, currentLoc: WorldLocation?, onTravelTo: (String) -> Unit, onJoinCareer: (String) -> Unit, onSpecialAction: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(14.dp)) {
                    Text(currentLoc?.name ?: "Inconnu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(currentLoc?.description ?: "")
                    Spacer(Modifier.height(4.dp))
                    Text("${currentLoc?.universe ?: "Monde"} • Danger ${currentLoc?.dangerLevel ?: 0}/10", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            SectionTitle("Carrières")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CareerData.careers) { career ->
                    FilterChip(selected = state.character.career == career.name, onClick = { onJoinCareer(career.name) }, label = { Text(career.name) })
                }
            }
        }
        item { SectionTitle("Quartiers & destinations") }
        items(state.locations) { loc ->
            Card(onClick = { onTravelTo(loc.id) }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(locationIcon(loc), null, tint = if (loc.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(loc.name, fontWeight = FontWeight.SemiBold)
                        Text(loc.description, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                        Text(if (loc.isUnlocked) "Accessible" else "Verrouillé", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSpecialAction("board_starship") }) { Text("Vaisseau") }
                OutlinedButton(onClick = { onSpecialAction("enter_anomaly") }) { Text("Anomalie") }
            }
        }
    }
}

private fun locationIcon(loc: WorldLocation) = when (loc.type.name) {
    "STARSHIP", "SPACEPORT" -> Icons.Default.RocketLaunch
    "RESEARCH" -> Icons.Default.Science
    "CITY" -> Icons.Default.LocationCity
    "UNDERGROUND" -> Icons.Default.Stairs
    "DIMENSIONAL" -> Icons.Default.AutoAwesome
    else -> Icons.Default.Place
}

@Composable
fun SimsTab(state: GameState, onTalkToNpc: (String) -> Unit, onProposeMarriage: (String) -> Unit, onTryForChild: () -> Unit) {
    val nearby = state.npcs.filter { it.currentLocationId == state.currentLocationId }.sortedByDescending { it.relationship }
    val others = state.npcs.filter { it.currentLocationId != state.currentLocationId }.sortedByDescending { it.relationship }
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card {
                Column(Modifier.padding(14.dp)) {
                    Text("Vie sociale", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${state.npcs.size} Sims vivent et se déplacent dans Zernex.")
                    Text("${state.character.relationshipStatus}${if (state.character.hasChildren) " • ${state.character.children.size} enfant(s)" else ""}", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        item { SectionTitle("Dans ${state.locations.find { it.id == state.currentLocationId }?.name ?: "le quartier"}") }
        if (nearby.isEmpty()) item { EmptyCard("Personne ici pour le moment. Les Sims autonomes bougent pendant la journée.") }
        items(nearby) { npc -> NpcCard(npc, onTalkToNpc, onProposeMarriage, state.character.spouseId) }
        item { SectionTitle("Communauté") }
        items(others) { npc -> NpcCard(npc, onTalkToNpc, onProposeMarriage, state.character.spouseId, compact = true) }
        if (state.character.spouseId != null && !state.character.hasChildren) item {
            Button(onClick = onTryForChild, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Favorite, null); Spacer(Modifier.width(6.dp)); Text("Fonder une famille") }
        }
    }
}

@Composable
fun NpcCard(npc: OtherSim, onTalk: (String) -> Unit, onMarry: (String) -> Unit, spouseId: String?, compact: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SimAvatar(npc.name, 54)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(npc.name, fontWeight = FontWeight.Bold)
                    if (npc.hasPower) { Spacer(Modifier.width(5.dp)); Text("✦", color = MaterialTheme.colorScheme.tertiary) }
                }
                Text("${npc.job} • ${npc.faction}", style = MaterialTheme.typography.bodySmall)
                Text("${npc.relationshipLabel} (${npc.relationship}) • ${npc.mood}", style = MaterialTheme.typography.labelSmall)
                if (npc.hasPower) Text(npc.powerName ?: "Pouvoir inconnu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                if (!compact) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { onTalk(npc.id) }) { Text("Parler") }
                        if (npc.relationship >= 55 && !npc.isMarried && spouseId == null) OutlinedButton(onClick = { onMarry(npc.id) }) { Text("Romance") }
                    }
                }
            }
        }
    }
}

@Composable
fun PowersTab(state: GameState, onUsePower: (PowerType) -> Unit, onLearnPower: (PowerType) -> Unit, onUnlockAllPowers: () -> Unit, onClaimMission: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Laboratoire des pouvoirs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${state.character.unlockedPowers().size}/${state.character.powers.size} pouvoirs éveillés • ${state.character.unlockedFusions.size} fusions")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onUnlockAllPowers) { Text("Mode développeur : tout éveiller") }
                }
            }
        }
        item { SectionTitle("Pouvoirs") }
        items(state.character.powers) { power ->
            PowerCard(power, state.character.powerLevel(power.type), onUsePower, onLearnPower)
        }
        item { SectionTitle("Missions du jour") }
        items(state.missions) { mission ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(mission.title, fontWeight = FontWeight.Bold)
                    Text(mission.description, style = MaterialTheme.typography.bodySmall)
                    Text("${mission.progressText} • +${mission.rewardMoney} §", style = MaterialTheme.typography.labelSmall)
                    if (mission.isCompleted && !mission.isClaimed) Button(onClick = { onClaimMission(mission.id) }) { Text("Réclamer") }
                }
            }
        }
    }
}

@Composable
private fun PowerCard(power: Power, level: Int, onUse: (PowerType) -> Unit, onLearn: (PowerType) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(power.name, fontWeight = FontWeight.Bold)
                    Text("${power.category} • Niv.$level • ${power.manaCost} mana", style = MaterialTheme.typography.labelSmall)
                }
                if (power.unlocked) Button(onClick = { onUse(power.type) }) { Text("Utiliser") }
                else if (!power.isFusion) OutlinedButton(onClick = { onLearn(power.type) }) { Text("${power.learnCost} §") }
            }
            Text(power.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ProfileTab(state: GameState, onBuyItem: (String) -> Unit, onBuyHouse: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    SimAvatar(state.character.name, 82)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(state.character.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${state.character.gender} • ${state.character.age} ans")
                        Text("${state.character.career} niveau ${state.character.careerLevel}")
                        Text("${state.character.money} § • ${state.character.knowledge} connaissance")
                    }
                }
            }
        }
        item {
            SectionTitle("Inventaire")
            state.character.inventory.forEach { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, null)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${item.name} ×${item.quantity}", fontWeight = FontWeight.SemiBold)
                            if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        item {
            SectionTitle("Propriétés")
            LuxuryData.items.filter { it.category == "property" }.forEach { property ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, null)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(property.name, fontWeight = FontWeight.SemiBold); Text("${property.price} § • ${property.description}", style = MaterialTheme.typography.bodySmall) }
                        OutlinedButton(onClick = { onBuyHouse(property.id) }) { Text("Acheter") }
                    }
                }
            }
        }
        item {
            SectionTitle("Boutique")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.shopItems) { item ->
                    AssistChip(onClick = { onBuyItem(item.id) }, label = { Text("${item.name} • ${item.price} §") })
                }
            }
        }
        item {
            SectionTitle("Journal")
            state.character.journal.takeLast(8).reversed().forEach { entry ->
                ListItem(headlineContent = { Text("J${entry.day} • ${entry.title}") }, supportingContent = { Text(entry.text) }, leadingContent = { Icon(Icons.Default.Book, null) })
            }
        }
    }
}

@Composable
fun CharacterCreationScreen(onCreate: (String, String, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Neutre") }
    val personalities = listOf("Ambitieux", "Créatif", "Sociable", "Mystérieux", "Aventurier", "Juste", "Rebelle", "Génie", "Romantique", "Extraverti")
    var selected by remember { mutableStateOf(setOf<String>()) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF16142B), Color(0xFF090A12))))) {
        Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            SimAvatar(name.ifBlank { "Z" }, 96)
            Spacer(Modifier.height(14.dp))
            Text("SIMS ZERNEX", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
            Text("Une vie ouverte sur la ville, l'espace et les pouvoirs.", color = Color.LightGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom du Sim") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Homme", "Femme", "Neutre").forEach { FilterChip(selected = gender == it, onClick = { gender = it }, label = { Text(it) }) } }
            Spacer(Modifier.height(10.dp))
            Text("Choisis jusqu'à 3 traits", color = Color.White, style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(personalities) { p -> FilterChip(selected = p in selected, onClick = { selected = if (p in selected) selected - p else if (selected.size < 3) selected + p else selected }, label = { Text(p) }) }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onCreate(name, gender, selected.toList()) }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Créer mon Sim") }
        }
    }
}

@Composable
private fun SimAvatar(name: String, size: Int) {
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "S" }
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(size.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = (size / 3).sp) }
    }
}

@Composable
private fun NeedBar(label: String, value: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, Modifier.width(62.dp), style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(progress = { value.coerceIn(0f, 100f) / 100f }, modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)))
        Text("${value.toInt()}", Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ObjectCard(obj: HouseObject, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (obj.type == ObjectType.SPECIAL) Icons.Default.AutoAwesome else Icons.Default.Home, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(obj.name, fontWeight = FontWeight.SemiBold); Text(obj.actionLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }
        }
    }
}

@Composable
private fun EmptyCard(text: String) { Card { Text(text, Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall) } }

@Composable
fun MagicCircleEffect() {
    val inf = rememberInfiniteTransition(label = "magic")
    val angle by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "angle")
    val scale by inf.animateFloat(0.82f, 1.15f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "scale")
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(230.dp)) {
            val r = size.minDimension / 2 * scale
            drawCircle(Brush.radialGradient(listOf(Color(0xCC7C4DFF), Color.Transparent)), r, style = Stroke(11f))
            drawCircle(Color(0xFF00E5FF), r * 0.65f, style = Stroke(3f))
            for (i in 0 until 8) { val a = Math.toRadians((angle + i * 45).toDouble()); drawCircle(Color(0xFFFF6B9D), 6f, Offset(center.x + r * 0.85f * cos(a).toFloat(), center.y + r * 0.85f * sin(a).toFloat())) }
        }
        Text("✧ ÉCHO ORIGINEL ✧", color = Color.White, fontWeight = FontWeight.Bold)
    }
}
