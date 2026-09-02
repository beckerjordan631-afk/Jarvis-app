package com.jarvis.assistant.skills

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import com.jarvis.assistant.data.AppDatabase
import com.jarvis.assistant.data.NoteEntity
import com.jarvis.assistant.data.ReminderEntity

/**
 * Reads live Android system vitals — battery level and free RAM —
 * for the "Systeminformationen" skill. Requires an application
 * Context, injected once at skill-registry construction time.
 */
class SystemInfoSkill(private val appContext: Context) : Skill {
    override val id = "system_info"
    override fun matches(input: String) =
        input.contains("akku") || input.contains("batterie") || input.contains("systeminfo") || input.contains("speicher")

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val freeGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)

        return SkillResult("Akkustand: $battery Prozent. Freier Arbeitsspeicher: ${"%.1f".format(freeGb)} Gigabyte.")
    }
}

class NoteSkill : Skill {
    override val id = "notes"
    private val trigger = "notiere"

    override fun matches(input: String) = input.contains(trigger)

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val text = input.substringAfter(trigger).trim().trim(',', '.', ':')
        return if (text.isBlank()) {
            SkillResult("Was soll ich notieren?")
        } else {
            db.noteDao().insert(NoteEntity(text = text))
            SkillResult("Notiert: $text")
        }
    }
}

class ReminderSkill : Skill {
    override val id = "reminder"
    private val trigger = "erinnere mich"

    override fun matches(input: String) = input.contains(trigger)

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val text = input.substringAfter(trigger).replace("daran, dass", "").replace("an", "").trim().trim(',', '.', ':')
        return if (text.isBlank()) {
            SkillResult("Woran soll ich dich erinnern?")
        } else {
            // Simplified: due in 1 hour by default; a real scheduler (AlarmManager/WorkManager)
            // would be wired here to fire a system notification at dueAt.
            val dueAt = System.currentTimeMillis() + 60 * 60 * 1000
            db.reminderDao().insert(ReminderEntity(text = text, dueAt = dueAt))
            SkillResult("Ich erinnere dich in einer Stunde: $text")
        }
    }
}

/** Fallback skill: always matches last, so the router never returns "nothing found". */
class SmallTalkSkill : Skill {
    override val id = "smalltalk"
    override fun matches(input: String) = true

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val responses = listOf(
            "Das habe ich nicht ganz verstanden. Kannst du es anders formulieren?",
            "Damit kenne ich mich noch nicht aus, aber ich lerne dazu.",
            "Sag mir gern, was ich für dich tun soll."
        )
        return SkillResult(responses.random())
    }
}
