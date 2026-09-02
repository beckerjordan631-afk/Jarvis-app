package com.jarvis.assistant.skills

import com.jarvis.assistant.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeSkill : Skill {
    override val id = "time"
    override fun matches(input: String) =
        input.contains("wie spät") || input.contains("uhrzeit") || input.contains("welche zeit")

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val fmt = SimpleDateFormat("HH:mm", Locale.GERMANY)
        return SkillResult("Es ist ${fmt.format(Date())} Uhr.")
    }
}

class DateSkill : Skill {
    override val id = "date"
    override fun matches(input: String) =
        input.contains("welcher tag") || input.contains("welches datum") || input.contains("heutiges datum")

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val fmt = SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMANY)
        return SkillResult("Heute ist ${fmt.format(Date())}.")
    }
}
