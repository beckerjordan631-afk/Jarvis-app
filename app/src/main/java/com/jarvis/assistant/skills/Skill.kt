package com.jarvis.assistant.skills

import com.jarvis.assistant.data.AppDatabase

/**
 * Result of a skill execution: text shown in chat + spoken by TTS,
 * plus an optional Android Intent-action the ViewModel should fire
 * (e.g. opening an app or a web search), decoupled from Android
 * classes so skills stay easy to unit test.
 */
data class SkillResult(
    val spokenText: String,
    val intentAction: JarvisIntent? = null
)

sealed class JarvisIntent {
    data class OpenApp(val appName: String) : JarvisIntent()
    data class WebSearch(val query: String) : JarvisIntent()
    data class OpenSettings(val section: String? = null) : JarvisIntent()
}

/**
 * One single-purpose skill. Each skill decides for itself whether it
 * can handle a given (already lower-cased) utterance via [matches],
 * and produces a [SkillResult] via [execute]. Small, single-purpose
 * skills beat one giant prompt.
 */
interface Skill {
    val id: String
    fun matches(input: String): Boolean
    suspend fun execute(input: String, db: AppDatabase): SkillResult
}
