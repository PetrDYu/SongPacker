package ru.petr.songpacker.packer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.petr.songpacker.saveXmlFile
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.petr.songpacker.packer.songPart.SongPartContent
import ru.petr.songpacker.packer.songPart.SongPartTypes

@Composable
fun PackerContent(component: PackerComponent, modifier: Modifier = Modifier) {
    val songParts by component.songParts.subscribeAsState()
    val songMetadata by component.songMetadata.subscribeAsState()

    // Metadata dialog state — open at startup
    var showMetadataDialog by remember { mutableStateOf(true) }

    // Save result dialog
    var saveResultMessage by remember { mutableStateOf<String?>(null) }

    // Drag and drop state
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var cumulativeDragY by remember { mutableFloatStateOf(0f) }
    val itemTops = remember { mutableStateMapOf<Int, Float>() }
    val itemHeights = remember { mutableStateMapOf<Int, Float>() }

    val targetIndex by remember {
        derivedStateOf {
            if (draggingIndex < 0) -1
            else {
                val fromTop = itemTops[draggingIndex] ?: 0f
                val fromHeight = itemHeights[draggingIndex] ?: 0f
                val draggedMidY = fromTop + fromHeight / 2f + cumulativeDragY
                var target = draggingIndex
                for (i in songParts.indices) {
                    val midY = (itemTops[i] ?: 0f) + (itemHeights[i] ?: 0f) / 2f
                    if (draggedMidY > midY) target = i
                }
                target.coerceIn(0, (songParts.size - 1).coerceAtLeast(0))
            }
        }
    }

    fun shiftYFor(idx: Int): Float {
        val from = draggingIndex
        val to = targetIndex
        if (from < 0 || to == from || idx == from) return 0f
        val draggedHeight = itemHeights[from] ?: 0f
        return when {
            from < to && idx in (from + 1)..to -> -draggedHeight
            from > to && idx in to until from -> draggedHeight
            else -> 0f
        }
    }

    // Verse numbering: verses get 0 if there's only one, or 1,2,3... if multiple
    val verseCount = remember(songParts) { songParts.count { it.type.value == SongPartTypes.VERSE } }
    val verseNumbers: List<Int?> = remember(songParts, verseCount) {
        var verseIdx = 0
        songParts.map { part ->
            if (part.type.value == SongPartTypes.VERSE) {
                if (verseCount == 1) 0 else ++verseIdx
            } else {
                null
            }
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    component.clearAllSelections()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header row: title + metadata button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Song Packer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { showMetadataDialog = true }) {
                Text("⚙ Метаданные")
            }
        }

        // Song number / name subtitle
        if (songMetadata.number.isNotBlank()) {
            val displayName = songMetadata.name.ifBlank {
                songParts.firstOrNull()
                    ?.strings?.value?.firstOrNull()?.take(40)
                    ?: ""
            }
            Text(
                text = buildString {
                    append("#${songMetadata.number}")
                    if (displayName.isNotBlank()) append(" — $displayName")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        } else {
            androidx.compose.foundation.layout.Spacer(
                Modifier.padding(bottom = 12.dp)
            )
        }

        // Parts list with drag-and-drop
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            songParts.forEachIndexed { idx, songPart ->
                key(songPart.id) {
                    val isDragging = idx == draggingIndex

                    val dragHandleModifier = Modifier.pointerInput(idx) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = idx
                                cumulativeDragY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                cumulativeDragY += dragAmount.y
                            },
                            onDragEnd = {
                                val target = targetIndex
                                if (target >= 0 && target != draggingIndex) {
                                    component.onMoveSongPart(draggingIndex, target)
                                }
                                draggingIndex = -1
                                cumulativeDragY = 0f
                            },
                            onDragCancel = {
                                draggingIndex = -1
                                cumulativeDragY = 0f
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .onGloballyPositioned { coords ->
                                itemTops[idx] = coords.positionInParent().y
                                itemHeights[idx] = coords.size.height.toFloat()
                            }
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) cumulativeDragY else shiftYFor(idx)
                                if (isDragging) {
                                    scaleX = 1.03f
                                    scaleY = 1.03f
                                    shadowElevation = 24f
                                }
                            }
                    ) {
                        SongPartContent(
                            component = songPart,
                            verseNumber = verseNumbers.getOrNull(idx),
                            dragHandleModifier = dragHandleModifier,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom control card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добавить часть песни",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { component.onAddSongPart(SongPartTypes.VERSE) }
                    ) { Text("Добавить куплет") }
                    Button(
                        onClick = { component.onAddSongPart(SongPartTypes.CHORUS) }
                    ) { Text("Добавить припев") }
                    Button(
                        onClick = { component.onAddSongPart(SongPartTypes.BRIDGE) }
                    ) { Text("Добавить мост") }
                }

                val newText by component.newText.subscribeAsState()
                TextField(
                    value = newText,
                    onValueChange = component::onChangeNewText,
                    placeholder = { Text("Введите текст куплета, припева или моста") },
                    singleLine = false,
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (songMetadata.number.isBlank()) {
                            showMetadataDialog = true
                        } else {
                            val xml = generateSongXml(songMetadata, songParts)
                            val fileName = buildSongFileName(songMetadata, songParts)
                            val result = saveXmlFile(fileName, xml)
                            saveResultMessage = if (result.startsWith("Error:") || result == "Cancelled") {
                                result
                            } else {
                                "Файл сохранён:\n$result"
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Save to file")
                }
            }
        }
    }

    // Save result dialog
    saveResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { saveResultMessage = null },
            title = { Text(if (message.startsWith("Файл")) "Готово" else "Ошибка") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { saveResultMessage = null }) { Text("OK") }
            }
        )
    }

    // Metadata dialog
    if (showMetadataDialog) {
        SongMetadataDialog(
            initialMetadata = songMetadata,
            onSave = { metadata ->
                component.onUpdateMetadata(metadata)
                showMetadataDialog = false
            },
            onDismiss = { showMetadataDialog = false }
        )
    }
}
