package com.bahilai.gigadanya.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
<<<<<<< HEAD
import androidx.compose.material.icons.filled.Email
=======
>>>>>>> 069cb25bb2159bafe8ca18362048d11512205ac1
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
import com.bahilai.gigadanya.data.ResponseFormat
=======
>>>>>>> 069cb25bb2159bafe8ca18362048d11512205ac1

/**
 * Компонент ввода сообщений
 */
@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    isLoading: Boolean,
<<<<<<< HEAD
    responseFormat: ResponseFormat,
    onFormatToggle: () -> Unit,
=======
>>>>>>> 069cb25bb2159bafe8ca18362048d11512205ac1
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
<<<<<<< HEAD
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Переключатель формата
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Формат ответа:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Переключатель
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (responseFormat == ResponseFormat.TEXT) "Текст" else "JSON",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = responseFormat == ResponseFormat.JSON,
                        onCheckedChange = { onFormatToggle() },
                        enabled = !isLoading
                    )
                }
            }
            
            Divider()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Поле ввода
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp, max = 120.dp),
                    placeholder = {
                        Text(
                            text = if (isLoading) "Ожидание ответа..." else "Введите сообщение..."
                        )
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Кнопка отправки
                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isLoading) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (messageText.isBlank() || isLoading) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = if (messageText.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    }
=======
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Поле ввода
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 120.dp),
                placeholder = {
                    Text(
                        text = if (isLoading) "Ожидание ответа..." else "Введите сообщение..."
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Кнопка отправки
            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank() && !isLoading) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier.size(56.dp),
                containerColor = if (messageText.isBlank() || isLoading) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (messageText.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
>>>>>>> 069cb25bb2159bafe8ca18362048d11512205ac1
                }
            }
        }
    }
}

