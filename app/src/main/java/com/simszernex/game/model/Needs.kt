package com.simszernex.game.model

data class Needs(
    val hunger: Float = 80f,      // 0 = starving, 100 = full
    val energy: Float = 80f,      // 0 = exhausted, 100 = rested
    val hygiene: Float = 80f,     // 0 = filthy, 100 = clean
    val funNeed: Float = 70f,     // 0 = bored, 100 = entertained
    val social: Float = 70f,      // 0 = lonely, 100 = socialized
    val bladder: Float = 80f,     // 0 = urgent, 100 = empty
    val mood: Float = 75f         // overall mood
) {
    fun isCritical(): Boolean {
        return hunger < 15f || energy < 10f || bladder < 10f || hygiene < 10f
    }

    fun average(): Float {
        return (hunger + energy + hygiene + funNeed + social + bladder) / 6f
    }

    fun decay(amount: Float = 0.4f): Needs {
        return copy(
            hunger = (hunger - amount * 1.2f).coerceIn(0f, 100f),
            energy = (energy - amount * 0.8f).coerceIn(0f, 100f),
            hygiene = (hygiene - amount * 0.5f).coerceIn(0f, 100f),
            funNeed = (funNeed - amount * 0.7f).coerceIn(0f, 100f),
            social = (social - amount * 0.4f).coerceIn(0f, 100f),
            bladder = (bladder - amount * 1.0f).coerceIn(0f, 100f),
            mood = average().coerceIn(0f, 100f)
        )
    }
}
