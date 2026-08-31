package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatMessageEntity::class,
        ReminderEntity::class,
        JarvisNoteEntity::class,
        QuickCommandEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_database"
                )
                .addCallback(JarvisDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class JarvisDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.jarvisDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: JarvisDao) {
                // Initial greeting from JARVIS
                dao.insertMessage(
                    ChatMessageEntity(
                        sender = "JARVIS",
                        text = "Good day, sir. All core subsystems are initialized and operating at peak efficiency. How may I be of service today?",
                        actionType = "GREETING"
                    )
                )

                // Initial default quick commands
                dao.insertQuickCommand(
                    QuickCommandEntity(
                        phrase = "System Diagnostic",
                        actionType = "DIAGNOSTIC",
                        description = "Run full telemetry check on device resources and power"
                    )
                )
                dao.insertQuickCommand(
                    QuickCommandEntity(
                        phrase = "Flashlight on",
                        actionType = "FLASHLIGHT",
                        description = "Engage high-intensity beam emitter"
                    )
                )
                dao.insertQuickCommand(
                    QuickCommandEntity(
                        phrase = "Set 5 minute timer",
                        actionType = "TIMER",
                        description = "Start a 5 minute countdown timer"
                    )
                )
                dao.insertQuickCommand(
                    QuickCommandEntity(
                        phrase = "Weather report",
                        actionType = "WEATHER",
                        description = "Atmospheric conditions and environmental scan"
                    )
                )
                dao.insertQuickCommand(
                    QuickCommandEntity(
                        phrase = "Protocol Clean Slate",
                        actionType = "PROTOCOL",
                        description = "Clear current session and reset systems to standby"
                    )
                )

                // Initial reminders / notes
                dao.insertReminder(
                    ReminderEntity(
                        title = "Mark 85 Armor Calibration",
                        dueTime = "18:00 Today",
                        priority = "HIGH"
                    )
                )
                dao.insertNote(
                    JarvisNoteEntity(
                        title = "Arc Reactor Schematics",
                        content = "Palladium core replaced with synthetic vibranium element. Energy output stabilized at 100%.",
                        category = "SYSTEM"
                    )
                )
            }
        }
    }
}
