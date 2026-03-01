package ru.petr.songpacker.packer

import com.arkivanov.decompose.value.Value

interface PackerComponent {
    val newText: Value<String>

    fun onChangeNewText(text: String)


    val songParts: Value<List<SongPart>>
    fun onAddSongPart(type: SongPartTypes)
}

enum class SongPartTypes(val displayName: String) {
    VERSE("Куплет"),
    CHORUS("Припев"),
    BRIDGE("Мост")
}

data class SongPart (
    val type: SongPartTypes,
    val text: String,
    val layers: List<SongLayer>
) {
    val stringRanges: List<Pair<Int, Int>>

    init {
        val stringRangesMutable = mutableListOf<Pair<Int, Int>>()
        var currentPos = 0
        // Регулярное выражение для любого разделителя строк: \n, \r, \r\n, \n\r
        val lineBreakRegex = Regex("""\r\n|\n\r|\r|\n""")
        val strings = text.split(lineBreakRegex)
        val delimiters = lineBreakRegex.findAll(text).map { it.value }.toList()
        
        for ((index, string) in strings.withIndex()) {
            val start = currentPos
            val end = currentPos + string.length
            stringRangesMutable.add(start to end)
            // Добавляем длину разделителя (1 или 2 символа), кроме последней строки
            if (index < strings.size - 1) {
                val delimiterLength = delimiters.getOrNull(index)?.length ?: 1
                currentPos = end + delimiterLength
            }
        }
        stringRanges = stringRangesMutable
    }
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