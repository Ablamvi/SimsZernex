package com.simszernex.game.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Sauvegarde simple et fiable (SharedPreferences).
 * Nécessaire pour ne pas perdre la partie.
 */
class SaveSystem(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("simszernex_save", Context.MODE_PRIVATE)

    fun save(
        name: String,
        money: Int,
        day: Int,
        career: String,
        rank: Int,
        mana: Int,
        maxMana: Int,
        locationId: String,
        hasChildren: Boolean,
        relationshipStatus: String
    ) {
        prefs.edit()
            .putString("name", name)
            .putInt("money", money)
            .putInt("day", day)
            .putString("career", career)
            .putInt("rank", rank)
            .putInt("mana", mana)
            .putInt("maxMana", maxMana)
            .putString("location", locationId)
            .putBoolean("hasChildren", hasChildren)
            .putString("relation", relationshipStatus)
            .putBoolean("hasSave", true)
            .apply()
    }

    fun hasSave(): Boolean = prefs.getBoolean("hasSave", false)

    fun load(): Map<String, Any?> = mapOf(
        "name" to prefs.getString("name", "Alex"),
        "money" to prefs.getInt("money", 8000),
        "day" to prefs.getInt("day", 1),
        "career" to prefs.getString("career", "Civil"),
        "rank" to prefs.getInt("rank", 0),
        "mana" to prefs.getInt("mana", 130),
        "maxMana" to prefs.getInt("maxMana", 130),
        "location" to prefs.getString("location", "home"),
        "hasChildren" to prefs.getBoolean("hasChildren", false),
        "relation" to prefs.getString("relation", "Célibataire")
    )

    fun clear() = prefs.edit().clear().apply()
}
