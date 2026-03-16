package ru.petr.songpacker.packer.songPart.songLayer

import androidx.compose.runtime.Composable
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
    when(component) {
        is RepeatSongLayerComponent -> RepeatSongLayerContent(component, stringIdx, modifier)
        is ChordSongLayerComponent -> ChordSongLayerContent(component, stringIdx, modifier)
    }
}