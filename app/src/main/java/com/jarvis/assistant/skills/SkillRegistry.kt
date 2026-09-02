package com.jarvis.assistant.skills

import android.content.Context
import com.jarvis.assistant.data.AppDatabase

/**
 * Central "brain": holds all skills in priority order and routes an
 * utterance to the first one whose matches() returns true. New
 * skills only need to be added to [skills] to become active —
 * fully modular, one skill = one responsibility.
 */
class SkillRegistry(context: Context) {

    private val appContext = context.applicationContext

    val skills: List<Skill> = listOf(
        MemorySkill(),          // "merke dir..." / "welchen ... habe ich"
        CalculatorSkill(),      // "rechne 12 mal 4"
        TimeSkill(),            // "wie spät ist es"
        DateSkill(),            // "welches datum haben wir"
        SystemInfoSkill(appContext), // "wie ist mein akkustand"
        NoteSkill(),            // "notiere ..."
        ReminderSkill(),        // "erinnere mich an ..."
        SettingsSkill(),        // "öffne die einstellungen"
        OpenAppSkill(),         // "öffne YouTube"
        WebSearchSkill(),       // "suche nach 3D-Druckern"
        SmallTalkSkill()        // fallback, always last
    )

    suspend fun route(rawInput: String, db: AppDatabase): SkillResult {
        val normalized = rawInput.lowercase().replace("jarvis,", "").replace("jarvis", "").trim()
        val skill = skills.first { it.matches(normalized) }
        return skill.execute(normalized, db)
    }
}
