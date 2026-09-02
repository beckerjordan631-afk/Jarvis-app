package com.jarvis.assistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_log")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "jarvis"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val dueAt: Long,
    val done: Boolean = false
)
