package ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer

import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent

interface RepeatSongLayerComponent: SongLayerComponent {

    val arrowRanges: Value<List<ArrowRange>>

    val repeatQtyStr: Value<String>

    fun onChangeRepeatQty(repeatQtyStr: String)

    fun update(newRepeatRange: IntRange,
               newArrowRanges: List<ArrowRange>)
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