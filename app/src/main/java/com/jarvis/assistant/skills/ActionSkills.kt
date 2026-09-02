package com.jarvis.assistant.skills

import com.jarvis.assistant.data.AppDatabase

class WebSearchSkill : Skill {
    override val id = "web_search"
    private val triggers = listOf("suche nach", "google", "suche im internet nach", "suche")

    override fun matches(input: String) = triggers.any { input.contains(it) }

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        var query = input
        triggers.sortedByDescending { it.length }.forEach { query = query.replace(it, "") }
        query = query.replace("jarvis", "").trim().trim(',', '.', '?')
        return if (query.isBlank()) {
            SkillResult("Wonach soll ich suchen?")
        } else {
            SkillResult("Ich suche nach $query.", JarvisIntent.WebSearch(query))
        }
    }
}

class OpenAppSkill : Skill {
    override val id = "open_app"
    private val trigger = "öffne"

    override fun matches(input: String) = input.contains(trigger) && !input.contains("einstellung")

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val appName = input.substringAfter(trigger).replace("jarvis", "").trim().trim(',', '.', '?')
        return if (appName.isBlank()) {
            SkillResult("Welche App soll ich öffnen?")
        } else {
            SkillResult("Ich öffne $appName.", JarvisIntent.OpenApp(appName))
        }
    }
}

class SettingsSkill : Skill {
    override val id = "settings"
    override fun matches(input: String) = input.contains("öffne") && input.contains("einstellung")

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        return SkillResult("Ich öffne die Einstellungen.", JarvisIntent.OpenSettings())
    }
}
