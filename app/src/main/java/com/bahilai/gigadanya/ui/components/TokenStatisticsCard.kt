package com.bahilai.gigadanya.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahilai.gigadanya.viewmodel.ChatViewModel

/**
 * Компонент для отображения статистики по токенам
 */
@Composable
fun TokenStatisticsCard(
    stats: ChatViewModel.TokenStats?,
    modifier: Modifier = Modifier
) {
    if (stats == null || (stats.totalInputTokens == 0 && stats.totalOutputTokens == 0)) {
        return
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "📊 Статистика токенов",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Входные",
                    value = "${stats.totalInputTokens}",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    label = "Выходные",
                    value = "${stats.totalOutputTokens}",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    label = "Всего",
                    value = "${stats.totalInputTokens + stats.totalOutputTokens}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (stats.compressionCount > 0 || stats.savedTokens > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Сжатий",
                        value = "${stats.compressionCount}",
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatItem(
                        label = "Сэкономлено",
                        value = "~${stats.savedTokens}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

