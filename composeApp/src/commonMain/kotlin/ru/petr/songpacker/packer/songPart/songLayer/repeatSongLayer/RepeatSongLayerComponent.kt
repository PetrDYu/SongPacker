package ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer

import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent

interface RepeatSongLayerComponent: SongLayerComponent {

    val arrowRanges: Value<List<ArrowRange>>

    val repeatQtyStr: Value<String>

    val repeatRange: Value<IntRange>

    fun onChangeRepeatQty(repeatQtyStr: String)

    fun update(newRepeatRange: IntRange,
               newArrowRanges: List<ArrowRange>)

    /** Load repeat data from XML without visual arrow computation. */
    fun loadRepeat(range: IntRange, qty: Int)
}

data class ArrowRange(
    val start: Float,
    val startEnding: Boolean,
    val end: Float,
    val endEnding: Boolean
) {
    fun isEmpty(): Boolean {
        return this == emptyArrowRange
    }

    companion object {
        val emptyArrowRange = ArrowRange(0f, false, 0f, false)
    }
}