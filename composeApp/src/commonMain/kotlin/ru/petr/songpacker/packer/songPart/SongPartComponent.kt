package ru.petr.songpacker.packer.songPart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult
import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordPlacement

interface SongPartComponent {
    val id: String
    val type: Value<SongPartTypes>
    val layers: Value<List<SongLayerComponent>>
    val strings: Value<List<String>>

    val stringSelections: Value<List<SelectionRect>>

    val layersAreFrozen: Value<Boolean>

    fun updateType(newType: SongPartTypes)

    fun updateText(newText: String)

    fun onTextLayout(stringIdx: Int, textLayoutResult: TextLayoutResult)

    fun onTextPositioned(stringIdx: Int, layoutCoordinates: LayoutCoordinates)

    fun onTextTap(stringIdx: Int, offset: Offset)
    fun onTextDragStart(stringIdx: Int, offset: Offset)
    fun onTextDrag(stringIdx: Int, change: PointerInputChange)

    fun onTextDragEndOrCancel()

    fun clearSelection()

    fun onDeleteLayerClick(layerIdx: Int)

    fun onLayerHidden(layerId: Int)

    /** Load a chord layer from imported data (positions recalculated after first layout). */
    fun loadChordLayer(chords: List<ChordPlacement>)

    /** Add a repeat layer from imported data (visual arrows recalculated lazily). */
    fun loadRepeatLayer(range: IntRange, qty: Int)
}

enum class SongPartTypes(val displayName: String) {
    VERSE("Куплет"),
    CHORUS("Припев"),
    BRIDGE("Мост")
}

data class SelectionRect(
    val topLeft: Offset,
    val size: Size
) {
    companion object {
        fun empty(): SelectionRect {
            return SelectionRect(Offset(0f, 0f), Size(0f, 0f))
        }
    }
}
