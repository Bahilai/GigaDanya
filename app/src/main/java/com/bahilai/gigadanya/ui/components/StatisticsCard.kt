package com.bahilai.gigadanya.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahilai.gigadanya.data.AgentStatistics
import com.bahilai.gigadanya.data.TotalStatistics
import java.util.Locale

/**
 * Компонент для отображения статистики по всем агентам
 */
@Composable
fun StatisticsCard(
    statistics: TotalStatistics,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с кнопкой разворачивания
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Статистика запроса",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Итоговая статистика (всегда видима)
            TotalStatsSummary(statistics = statistics)
            
            // Детальная статистика по агентам (раскрывается)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Детально по агентам:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    statistics.agentStats.forEach { agentStat ->
                        AgentStatItem(agentStat = agentStat)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Компонент с итоговой статистикой
 */
@Composable
private fun TotalStatsSummary(statistics: TotalStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatChip(
            label = "Время",
            value = formatTime(statistics.totalResponseTime),
            icon = "⏱️"
        )
        
        StatChip(
            label = "Токены",
            value = statistics.totalTokens.toString(),
            icon = "🔢"
        )
        
        StatChip(
            label = "Стоимость",
            value = String.format(Locale.US, "%.2f₽", statistics.totalCost),
            icon = "💵"
        )
    }
}

/**
 * Компонент с статистикой отдельного агента
 */
@Composable
private fun AgentStatItem(agentStat: AgentStatistics) {
    val agentColor = Color(agentStat.agentInfo.colorHex)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(agentColor.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Информация об агенте
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = agentStat.agentInfo.emoji,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = agentStat.agentInfo.name.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${formatTime(agentStat.responseTime)} • ${agentStat.totalTokens} токенов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        
        // Стоимость
        Text(
            text = String.format(Locale.US, "%.2f₽", agentStat.cost),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = agentColor
        )
    }
}

/**
 * Компонент для отображения отдельной статистики
 */
@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Форматирование времени из миллисекунд
 */
private fun formatTime(milliseconds: Long): String {
    return when {
        milliseconds < 1000 -> "${milliseconds}мс"
        milliseconds < 60000 -> String.format(Locale.US, "%.1fс", milliseconds / 1000.0)
        else -> String.format(Locale.US, "%.1fм", milliseconds / 60000.0)
    }
}

