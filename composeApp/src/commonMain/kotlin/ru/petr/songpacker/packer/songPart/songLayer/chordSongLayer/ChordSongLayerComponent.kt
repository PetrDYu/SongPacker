package ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer

import androidx.compose.ui.text.TextLayoutResult
import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent

interface ChordSongLayerComponent : SongLayerComponent {

    val chords: Value<List<ChordPlacement>>

    val pendingTap: Value<PendingTap>

    fun onTextTap(stringIdx: Int, charOffset: Int, xPosition: Float)

    fun onChordSelected(chord: Chord)

    fun onDismissPopup()

    fun removeChord(stringIdx: Int, charOffset: Int)

    /** Bulk-load chords (used when importing from XML). xPosition = -1f means "recalculate after layout". */
    fun loadChords(chords: List<ChordPlacement>)

    /**
     * Called after a string's text has been laid out.
     * Recalculates the xPosition for any chord in [stringIdx] that has xPosition < 0
     * (sentinel set during XML import).
     */
    fun onStringLayout(stringIdx: Int, layoutResult: TextLayoutResult, stringAbsoluteStart: Int)
}
