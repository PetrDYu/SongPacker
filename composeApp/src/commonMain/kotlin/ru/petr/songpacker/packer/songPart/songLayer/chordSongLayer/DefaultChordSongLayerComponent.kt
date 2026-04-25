package ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class DefaultChordSongLayerComponent(
    componentContext: ComponentContext,
    override val id: Int
) : ChordSongLayerComponent, ComponentContext by componentContext {

    private val _chords = MutableValue(listOf<ChordPlacement>())
    override val chords: Value<List<ChordPlacement>> = _chords

    private val _pendingTap = MutableValue(PendingTap.NONE)
    override val pendingTap: Value<PendingTap> = _pendingTap

    private val _visible = MutableValue(false)
    override val visible: Value<Boolean> = _visible

    override fun onShow() {
        _visible.value = true
    }

    override fun delete() {
        _visible.value = false
    }

    override fun onTextTap(stringIdx: Int, charOffset: Int, xPosition: Float) {
        val existingChord = _chords.value
            .find { it.stringIdx == stringIdx && it.charOffset == charOffset }
            ?.chord
        _pendingTap.value = PendingTap(
            stringIdx = stringIdx,
            charOffset = charOffset,
            xPosition = xPosition,
            existingChord = existingChord
        )
    }

    override fun onChordSelected(chord: Chord) {
        val tap = _pendingTap.value.takeIf { !it.isNone } ?: return
        val updatedChords = _chords.value.toMutableList()
        updatedChords.removeAll { it.stringIdx == tap.stringIdx && it.charOffset == tap.charOffset }
        updatedChords.add(
            ChordPlacement(
                stringIdx = tap.stringIdx,
                charOffset = tap.charOffset,
                xPosition = tap.xPosition,
                chord = chord
            )
        )
        updatedChords.sortWith(compareBy({ it.stringIdx }, { it.charOffset }))
        _chords.value = updatedChords
        _pendingTap.value = PendingTap.NONE
    }

    override fun onDismissPopup() {
        _pendingTap.value = PendingTap.NONE
    }

    override fun removeChord(stringIdx: Int, charOffset: Int) {
        val updatedChords = _chords.value.toMutableList()
        updatedChords.removeAll { it.stringIdx == stringIdx && it.charOffset == charOffset }
        _chords.value = updatedChords
        _pendingTap.value = PendingTap.NONE
    }
}
