package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // e.g. "WEATHER", "TIMER", "FLASHLIGHT", "REMINDER", "DIAGNOSTIC", "NOTE"
    val actionPayload: String? = null
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dueTime: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: String = "NORMAL" // "LOW", "NORMAL", "HIGH", "CRITICAL"
)

@Entity(tableName = "notes")
data class JarvisNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "GENERAL",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_commands")
data class QuickCommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phrase: String,
    val actionType: String,
    val description: String
)
