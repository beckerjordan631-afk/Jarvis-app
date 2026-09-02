package com.jarvis.assistant.skills

import com.jarvis.assistant.data.AppDatabase
import com.jarvis.assistant.data.MemoryEntity

/**
 * Handles two patterns:
 *  - "merke dir, dass <key> <verb> <value>"  -> stores a fact
 *  - "welche/r/s <key> habe ich" / "was ist mein <key>" -> recalls it
 *
 * Facts are stored locally only (Room/SQLite on-device), never sent
 * anywhere else.
 */
class MemorySkill : Skill {
    override val id = "memory"

    private val saveTriggers = listOf("merke dir", "speichere dir", "merk dir")
    private val recallTriggers = listOf("welchen", "welche", "welches", "was ist mein", "was ist meine", "woran erinnerst du dich")

    override fun matches(input: String): Boolean {
        return saveTriggers.any { input.contains(it) } || recallTriggers.any { input.contains(it) }
    }

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val dao = db.memoryDao()
        val isSave = saveTriggers.any { input.contains(it) }

        return if (isSave) {
            val fact = extractFact(input)
            if (fact == null) {
                SkillResult("Was genau soll ich mir merken?")
            } else {
                dao.insert(MemoryEntity(key = fact.first, value = fact.second))
                SkillResult("Verstanden. Ich merke mir: ${fact.first} = ${fact.second}.")
            }
        } else {
            val subject = extractSubject(input)
            val hits = if (subject.isNotBlank()) dao.search(subject) else emptyList()
            if (hits.isEmpty()) {
                SkillResult("Dazu habe ich noch nichts gespeichert.")
            } else {
                val best = hits.first()
                SkillResult("Du hast mir gesagt: ${best.key} ist ${best.value}.")
            }
        }
    }

    /** Extracts a rough "key" / "value" pair from a free-form save sentence. */
    private fun extractFact(input: String): Pair<String, String>? {
        var s = input
        saveTriggers.forEach { s = s.replace(it, "") }
        s = s.replace("dass", "").trim().trim(',', '.', ' ')
        if (s.isBlank()) return null

        // Try to split on common linking verbs: "ist", "hat", "heißt"
        val linkers = listOf(" ist ein ", " ist eine ", " ist ", " hat ", " heißt ")
        for (linker in linkers) {
            val idx = s.indexOf(linker)
            if (idx > 0) {
                val key = s.substring(0, idx).trim()
                val value = s.substring(idx + linker.length).trim()
                if (key.isNotBlank() && value.isNotBlank()) return key to value
            }
        }
        // Fallback: store whole sentence under a generic key
        return "Notiz" to s
    }

    private fun extractSubject(input: String): String {
        var s = input
        recallTriggers.forEach { s = s.replace(it, "") }
        listOf("habe ich", "jarvis", "?").forEach { s = s.replace(it, "") }
        return s.trim()
    }
}
