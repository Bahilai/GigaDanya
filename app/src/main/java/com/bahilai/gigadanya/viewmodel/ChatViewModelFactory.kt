package com.bahilai.gigadanya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bahilai.gigadanya.data.database.ChatDatabase

/**
 * Factory для создания ChatViewModel с зависимостью от базы данных
 */
class ChatViewModelFactory(
    private val database: ChatDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

