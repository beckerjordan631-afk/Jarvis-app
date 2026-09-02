package com.jarvis.assistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert
    suspend fun insert(entity: MemoryEntity)

    @Query("SELECT * FROM memory WHERE key = :key ORDER BY timestamp DESC LIMIT 1")
    suspend fun findByKey(key: String): MemoryEntity?

    @Query("SELECT * FROM memory WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun search(query: String): List<MemoryEntity>

    @Query("SELECT * FROM memory ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query("DELETE FROM memory WHERE key = :key")
    suspend fun deleteByKey(key: String)
}

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(entity: ChatEntity)

    @Query("SELECT * FROM chat_log ORDER BY id DESC LIMIT 100")
    fun observeRecent(): Flow<List<ChatEntity>>

    @Query("DELETE FROM chat_log")
    suspend fun clear()
}

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(entity: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun observeAll(): Flow<List<NoteEntity>>
}

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(entity: ReminderEntity)

    @Update
    suspend fun update(entity: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE done = 0 ORDER BY dueAt ASC")
    fun observeOpen(): Flow<List<ReminderEntity>>
}
