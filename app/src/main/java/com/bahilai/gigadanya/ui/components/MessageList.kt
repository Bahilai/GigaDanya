package com.bahilai.gigadanya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bahilai.gigadanya.data.EconomicAgents
import com.bahilai.gigadanya.data.Message
import com.bahilai.gigadanya.data.TotalStatistics
import kotlinx.coroutines.launch

/**
 * Список сообщений чата
 */
@Composable
fun MessageList(
    messages: List<Message>,
    onImageClick: (String) -> Unit,
    statistics: TotalStatistics? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Автоматическая прокрутка к последнему элементу (сообщению или статистике)
    LaunchedEffect(messages.size, statistics) {
        val totalItems = messages.size + if (statistics != null) 1 else 0
        if (totalItems > 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageItem(
                message = message,
                onImageClick = onImageClick
            )
        }
        
        // Добавляем карточку статистики после всех сообщений
        if (statistics != null) {
            item(key = "statistics") {
                StatisticsCard(statistics = statistics)
            }
        }
    }
}

/**
 * Отдельное сообщение в чате
 */
@Composable
fun MessageItem(
    message: Message,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val agent = if (!message.isFromUser && message.agentName != null) {
        EconomicAgents.getAgentByName(message.agentName)
    } else null
    
    val agentColor = agent?.let { Color(it.colorHex) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) {
            Alignment.End
        } else {
            Alignment.Start
        }
    ) {
        // Бейдж с именем агента (только для сообщений от ботов)
        if (!message.isFromUser && message.agentName != null && agent != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = agentColor ?: MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .padding(start = 12.dp, bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = agent.emoji,
                        fontSize = 16.sp
                    )
                    Text(
                        text = message.agentName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.87f)
                    )
                }
            }
        }
        
        // Пузырь сообщения
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            color = if (message.isFromUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                agentColor?.copy(alpha = 0.15f) ?: MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier
                .widthIn(max = 300.dp)
                .then(
                    if (agentColor != null && !message.isFromUser) {
                        Modifier.border(
                            width = 2.dp,
                            color = agentColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 16.dp
                            )
                        )
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Текстовое сообщение
                if (message.text != null) {
                    Text(
                        text = message.text,
                        fontSize = 16.sp,
                        color = if (message.isFromUser) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                // Изображение
                if (message.imageUrl != null) {
                    Spacer(modifier = Modifier.height(if (message.text != null) 8.dp else 0.dp))
                    
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Image from bot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(message.imageUrl) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

