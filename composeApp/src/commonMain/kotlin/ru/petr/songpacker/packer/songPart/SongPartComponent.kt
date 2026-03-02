package ru.petr.songpacker.packer.songPart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.text.TextLayoutResult
import com.arkivanov.decompose.value.Value

interface SongPartComponent {
    val id: String
    val type: Value<SongPartTypes>
    val text: Value<String>
    val layers: Value<List<SongLayer>>
    val stringRanges: Value<List<Pair<Int, Int>>>
    val strings: Value<List<String>>

    fun updateType(newType: SongPartTypes)

    fun updateText(newText: String)

    fun appendLayer(newLayer: SongLayer)

    fun onTextLayout(stringIdx: Int, textLayoutResult: TextLayoutResult)

    fun onTextTap(offset: Offset)
    fun onTextDragStart(offset: Offset)
    fun onTextDrag(change: PointerInputChange, offset: Offset)
    fun onTextDragEndOrCancel()
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