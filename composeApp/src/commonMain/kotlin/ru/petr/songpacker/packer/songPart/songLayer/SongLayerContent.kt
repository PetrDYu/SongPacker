package ru.petr.songpacker.packer.songPart.songLayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordSongLayerContent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerContent

@Composable
fun SongLayerContent(
    component: SongLayerComponent,
    stringIdx: Int,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        when (component) {
            is RepeatSongLayerComponent -> RepeatSongLayerContent(component, stringIdx, modifier)
            is ChordSongLayerComponent -> ChordSongLayerContent(component, stringIdx, modifier)
        }
    }
}