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
    val layers: Value<List<SongLayer>>
    val strings: Value<List<String>>

    val stringSelections: Value<List<SelectionRect>>

    val arrowEndings: Value<List<Pair<Boolean, Boolean>>>

    fun updateType(newType: SongPartTypes)

    fun updateText(newText: String)

    fun appendLayer(newLayer: SongLayer)

    fun onTextLayout(stringIdx: Int, textLayoutResult: TextLayoutResult)

    fun onTextPositioned(stringIdx: Int, layoutCoordinates: LayoutCoordinates)

    fun onTextTap(stringIdx: Int, offset: Offset)
    fun onTextDragStart(stringIdx: Int, offset: Offset)
    fun onTextDrag(stringIdx: Int, change: PointerInputChange)

    fun clearSelection()
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
    val end: Int,
    val isPrelim: Boolean
) {
    fun intersectWith(layer: SongLayer): Boolean {
        return intersectWith(layer.type, layer.start, layer.end)
    }

    fun intersectWith(type: SongLayerTypes, start: Int, end: Int): Boolean {
        return (type == this.type) && (start <= this.end) && (end >= this.start)
    }
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