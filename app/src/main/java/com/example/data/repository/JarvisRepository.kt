package com.example.data.repository

import com.example.data.db.ChatMessageEntity
import com.example.data.db.JarvisDao
import com.example.data.db.JarvisNoteEntity
import com.example.data.db.QuickCommandEntity
import com.example.data.db.ReminderEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val dao: JarvisDao) {
    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()
    val allNotes: Flow<List<JarvisNoteEntity>> = dao.getAllNotes()
    val allQuickCommands: Flow<List<QuickCommandEntity>> = dao.getAllQuickCommands()

    suspend fun saveMessage(sender: String, text: String, actionType: String? = null, actionPayload: String? = null): Long {
        return dao.insertMessage(
            ChatMessageEntity(
                sender = sender,
                text = text,
                actionType = actionType,
                actionPayload = actionPayload
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearChatHistory()
    }

    suspend fun addReminder(title: String, dueTime: String, priority: String = "NORMAL"): Long {
        return dao.insertReminder(
            ReminderEntity(
                title = title,
                dueTime = dueTime,
                priority = priority
            )
        )
    }

    suspend fun toggleReminder(reminder: ReminderEntity) {
        dao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
    }

    suspend fun deleteReminder(id: Long) {
        dao.deleteReminderById(id)
    }

    suspend fun addNote(title: String, content: String, category: String = "GENERAL"): Long {
        return dao.insertNote(
            JarvisNoteEntity(
                title = title,
                content = content,
                category = category
            )
        )
    }

    suspend fun deleteNote(id: Long) {
        dao.deleteNoteById(id)
    }

    suspend fun addQuickCommand(phrase: String, actionType: String, description: String) {
        dao.insertQuickCommand(
            QuickCommandEntity(
                phrase = phrase,
                actionType = actionType,
                description = description
            )
        )
    }
}
