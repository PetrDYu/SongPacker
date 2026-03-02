package ru.petr.songpacker.packer.songPart

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class DefaultSongPartComponent(
    componentContext: ComponentContext,
    override val id: String,
    initialType: SongPartTypes = SongPartTypes.VERSE,
    initialText: String = ""
) : SongPartComponent, ComponentContext by componentContext {

    private val _type = MutableValue(initialType)
    override val type: Value<SongPartTypes> = _type

    private val _text = MutableValue(initialText)
    override val text: Value<String> = _text

    private val _layers = MutableValue(emptyList<SongLayer>())
    override val layers: Value<List<SongLayer>> = _layers

    private val _stringRanges = MutableValue(emptyList<Pair<Int, Int>>())
    override val stringRanges: Value<List<Pair<Int, Int>>> = _stringRanges

    init {
        // Initialize string ranges for the initial text
        updateText(initialText)
    }

    override fun updateType(newType: SongPartTypes) {
        _type.value = newType
    }

    override fun updateText(newText: String) {
        if (newText.isNotBlank()) {
            val stringRangesMutable = mutableListOf<Pair<Int, Int>>()
            var currentPos = 0
            // Регулярное выражение для любого разделителя строк: \n, \r, \r\n, \n\r
            val lineBreakRegex = Regex("""\r\n|\n\r|\r|\n""")
            val strings = newText.split(lineBreakRegex)
            val delimiters = lineBreakRegex.findAll(newText).map { it.value }.toList()

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
            _stringRanges.value = stringRangesMutable
        }
    }

    override fun appendLayer(newLayer: SongLayer) {
        TODO("Not yet implemented")
    }

}