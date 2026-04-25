package ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer

import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent

interface ChordSongLayerComponent : SongLayerComponent {

    val chords: Value<List<ChordPlacement>>

    val pendingTap: Value<PendingTap>

    fun onTextTap(stringIdx: Int, charOffset: Int, xPosition: Float)

    fun onChordSelected(chord: Chord)

    fun onDismissPopup()

    fun removeChord(stringIdx: Int, charOffset: Int)
}
