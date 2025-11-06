package com.bahilai.gigadanya.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bahilai.gigadanya.data.ResponseFormat

/**
 * Компонент выбора формата ответа AI-агента
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatSelector(
    currentFormat: ResponseFormat,
    onFormatChange: (ResponseFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Формат ответа:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Выпадающее меню
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFormat.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Выбрать формат"
                        )
                    }
                }
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ResponseFormat.entries.forEach { format ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = format.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = format.promptInstruction.take(60) + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onFormatChange(format)
                                expanded = false
                            },
                            leadingIcon = {
                                RadioButton(
                                    selected = format == currentFormat,
                                    onClick = null
                                )
                            }
                        )
                    }
                }
            }
            
            // Описание текущего формата
            if (currentFormat.promptInstruction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Text(
                        text = currentFormat.promptInstruction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Компактная версия селектора формата (для bottomBar)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactFormatSelector(
    currentFormat: ResponseFormat,
    onFormatChange: (ResponseFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = currentFormat.displayName,
                style = MaterialTheme.typography.bodySmall
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Выбрать формат",
                modifier = Modifier.size(18.dp)
            )
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ResponseFormat.entries.forEach { format ->
                DropdownMenuItem(
                    text = { Text(format.displayName) },
                    onClick = {
                        onFormatChange(format)
                        expanded = false
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = format == currentFormat,
                            onClick = null
                        )
                    }
                )
            }
        }
    }
}

