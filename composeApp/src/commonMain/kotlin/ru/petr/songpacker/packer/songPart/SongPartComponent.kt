package ru.petr.songpacker.packer.songPart

import com.arkivanov.decompose.value.Value

interface SongPartComponent {
    val id: String
    val type: Value<SongPartTypes>
    val text: Value<String>
    val layers: Value<List<SongLayer>>
    val stringRanges: Value<List<Pair<Int, Int>>>

    fun updateType(newType: SongPartTypes)

    fun updateText(newText: String)

    fun appendLayer(newLayer: SongLayer)
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