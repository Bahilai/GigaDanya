package com.bahilai.gigadanya.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * База данных Room для хранения данных чата
 */
@Database(
    entities = [
        MessageEntity::class,
        YandexMessageEntity::class,
        TokenStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun yandexMessageDao(): YandexMessageDao
    abstract fun tokenStatsDao(): TokenStatsDao
    
    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null
        
        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                )
                    .fallbackToDestructiveMigration() // Для упрощения, в продакшене нужны миграции
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

