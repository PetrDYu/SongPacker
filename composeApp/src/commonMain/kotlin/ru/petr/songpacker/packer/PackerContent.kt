package ru.petr.songpacker.packer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun PackerContent(component: PackerComponent, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val songParts by component.songParts.subscribeAsState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (songPart in songParts) {
                key(songPart) {
                    SongPartCard(
                        songPart = songPart,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        FlowRow {
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
            minLines = 4
        )
        Button(onClick = { TODO() }) {
            Text("Save to file")
        }
    }
}

@Composable
fun SongPartCard(songPart: SongPart, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible, // Можно управлять видимостью для анимации удаления
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut()
    ) {
        Card(
            modifier = modifier
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = songPart.type.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(songPart.text)
            }
        }
    }
}