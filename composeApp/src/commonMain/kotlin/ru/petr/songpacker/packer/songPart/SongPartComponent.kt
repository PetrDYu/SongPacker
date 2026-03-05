package ru.petr.songpacker.packer.songPart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult
import com.arkivanov.decompose.value.Value

interface SongPartComponent {
    val id: String
    val type: Value<SongPartTypes>
    val text: Value<String>
    val layers: Value<List<SongLayer>>
    val stringRanges: Value<List<Pair<Int, Int>>>
    val strings: Value<List<String>>

    val stringSelections: Value<List<SelectionRect>>

    fun updateType(newType: SongPartTypes)

    fun updateText(newText: String)

    fun appendLayer(newLayer: SongLayer)

    fun onTextLayout(stringIdx: Int, textLayoutResult: TextLayoutResult)

    fun onTextPositioned(stringIdx: Int, layoutCoordinates: LayoutCoordinates)

    fun onTextTap(stringIdx: Int, offset: Offset)
    fun onTextDragStart(stringIdx: Int, offset: Offset)
    fun onTextDrag(stringIdx: Int, change: PointerInputChange)
    fun onTextDragEndOrCancel(stringIdx: Int)
}

enum class SongPartTypes(val displayName: String) {
    VERSE("Куплет"),
    CHORUS("Припев"),
    BRIDGE("Мост")
}

enum class SongLayerTypes {
    REPEAT, CHORD
}

data class SongLayer(
    val type: SongLayerTypes,
    val start: Int,
    val end: Int
) {

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