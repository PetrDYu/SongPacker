package ru.petr.songpacker.packer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SongMetadataDialog(
    initialMetadata: SongMetadata,
    onSave: (SongMetadata) -> Unit,
    onDismiss: () -> Unit
) {
    var number by remember(initialMetadata) { mutableStateOf(initialMetadata.number) }
    var name by remember(initialMetadata) { mutableStateOf(initialMetadata.name) }
    var isCanon by remember(initialMetadata) { mutableStateOf(initialMetadata.isCanon) }
    var textAuthor by remember(initialMetadata) { mutableStateOf(initialMetadata.textAuthor) }
    var textRusAuthor by remember(initialMetadata) { mutableStateOf(initialMetadata.textRusAuthor) }
    var musicAuthor by remember(initialMetadata) { mutableStateOf(initialMetadata.musicAuthor) }
    var additionalInfo by remember(initialMetadata) { mutableStateOf(initialMetadata.additionalInfo) }

    var numberError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Метаданные песни") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        numberError = false
                    },
                    label = { Text("Номер песни *") },
                    isError = numberError,
                    supportingText = if (numberError) {
                        { Text("Обязательное поле") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    placeholder = { Text("По умолчанию — первая строка первого куплета") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isCanon,
                        onCheckedChange = { isCanon = it }
                    )
                    Text("Канон")
                }

                OutlinedTextField(
                    value = textAuthor,
                    onValueChange = { textAuthor = it },
                    label = { Text("Автор текста") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = textRusAuthor,
                    onValueChange = { textRusAuthor = it },
                    label = { Text("Перевод") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = musicAuthor,
                    onValueChange = { musicAuthor = it },
                    label = { Text("Автор музыки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    label = { Text("Доп. информация") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (number.isBlank()) {
                        numberError = true
                    } else {
                        onSave(
                            SongMetadata(
                                number = number.trim(),
                                name = name.trim(),
                                isCanon = isCanon,
                                textAuthor = textAuthor.trim(),
                                textRusAuthor = textRusAuthor.trim(),
                                musicAuthor = musicAuthor.trim(),
                                additionalInfo = additionalInfo.trim()
                            )
                        )
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
