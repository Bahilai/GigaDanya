package com.bahilai.gigadanya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bahilai.gigadanya.ui.components.ChatHeader
import com.bahilai.gigadanya.ui.components.ImageViewerDialog
import com.bahilai.gigadanya.ui.components.MessageInput
import com.bahilai.gigadanya.ui.components.MessageList
import com.bahilai.gigadanya.ui.theme.GigaDanyaTheme
import com.bahilai.gigadanya.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GigaDanyaTheme {
                // Surface с правильными отступами для системных элементов
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            ChatHeader()
        },
        bottomBar = {
            MessageInput(
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                isLoading = viewModel.isLoading.value
            )
        }
        // Убираем настройку contentWindowInsets, чтобы Scaffold автоматически учитывал системные элементы
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MessageList(
                messages = viewModel.messages,
                onImageClick = { imageUrl ->
                    selectedImageUrl = imageUrl
                }
            )
            
            // Показываем ошибку, если есть
            viewModel.errorMessage.value?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.errorMessage.value = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
    
    // Диалог просмотра изображения
    selectedImageUrl?.let { imageUrl ->
        ImageViewerDialog(
            imageUrl = imageUrl,
            onDismiss = { selectedImageUrl = null }
        )
    }
}