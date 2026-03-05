package ru.petr.songpacker.packer.songPart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.TextLayoutResult
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

    private val _strings = MutableValue(emptyList<String>())
    override val strings: Value<List<String>> = _strings

    private var textLayoutResults: MutableList<TextLayoutResult?> = mutableListOf()

    private var selectionRange = 0 .. 0

    private val _stringSelections = MutableValue(emptyList<SelectionRect>())
    override val stringSelections: Value<List<SelectionRect>> = _stringSelections

    private var textLayoutCoordinates: MutableList<LayoutCoordinates?> = mutableListOf()

    private val rightSelectionRange: IntRange
        get() = if (selectionRange.first < selectionRange.last) {
                selectionRange
            } else {
                selectionRange.last .. selectionRange.first
            }


    init {
        // Initialize string ranges for the initial text
        updateText(initialText)
    }

    override fun updateType(newType: SongPartTypes) {
        _type.value = newType
    }

    override fun updateText(newText: String) {
        if (newText.isNotBlank()) {
            textLayoutResults = mutableListOf()
            val stringRangesMutable = mutableListOf<Pair<Int, Int>>()
            val stringsMutable = mutableListOf<String>()
            val stringSelectionsMutable = mutableListOf<SelectionRect>()
            textLayoutCoordinates = mutableListOf()
            var currentPos = 0
            // Регулярное выражение для любого разделителя строк: \n, \r, \r\n, \n\r
            val lineBreakRegex = Regex("""\r\n|\n\r|\r|\n""")
            val strings = newText.split(lineBreakRegex)
            val delimiters = lineBreakRegex.findAll(newText).map { it.value }.toList()

            for ((index, string) in strings.withIndex()) {
                stringsMutable.add(string)
                textLayoutResults.add(null)
                stringSelectionsMutable.add(SelectionRect.empty())
                textLayoutCoordinates.add(null)

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
            _strings.value = stringsMutable
            _stringSelections.value = stringSelectionsMutable
        }
    }

    override fun appendLayer(newLayer: SongLayer) {
        TODO("Not yet implemented")
    }

    override fun onTextLayout(stringIdx: Int, textLayoutResult: TextLayoutResult) {
        textLayoutResults[stringIdx] = textLayoutResult
    }

    override fun onTextPositioned(
        stringIdx: Int,
        layoutCoordinates: LayoutCoordinates
    ) {
        textLayoutCoordinates[stringIdx] = layoutCoordinates
    }

    override fun onTextTap(stringIdx: Int, offset: Offset) {
        resetSelection()
    }

    override fun onTextDragStart(stringIdx: Int, offset: Offset) {
        println("start drag")
        resetSelection()
//        val stringIdx = getStringIdxByYCoord(offset.y)
        if (stringIdx < _strings.value.size) {
            textLayoutResults[stringIdx]?.let { layoutResult ->
                val charOffset = _stringRanges.value[stringIdx].first + layoutResult.getOffsetForPosition(offset)
                selectionRange = charOffset .. charOffset
                println(selectionRange)
            }
        }
    }

    override fun onTextDrag(
        stringIdx: Int,
        change: PointerInputChange
    ) {
        change.consume()
        val endStringIdx = getStringIdxByYCoord(change.position.y, stringIdx)
        if (endStringIdx < _strings.value.size) {
            textLayoutResults[endStringIdx]?.let { layoutResult ->
                val dragEnd = _stringRanges.value[endStringIdx].first +
                        layoutResult.getOffsetForPosition(change.position)
                selectionRange.let { range ->
                    selectionRange = range.start .. dragEnd
                }
            }
            recalcSelection()
        }
    }

    override fun onTextDragEndOrCancel(stringIdx: Int) {

    }

    private fun getStringIdxByYCoord(y: Float, baseStringIdx: Int): Int {
        var retStringIdx = _strings.value.size
        for (stringIdx in (_strings.value.size - 1) downTo 0) {
            textLayoutResults[stringIdx]?.let { layoutResult ->
                val correction = textLayoutCoordinates[stringIdx]!!.positionInWindow().y -
                            textLayoutCoordinates[baseStringIdx]!!.positionInWindow().y
                val lineTop = layoutResult.getLineTop(0)
                val lineBottom = layoutResult.getLineBottom(0)
                if ((y - correction) in lineTop..lineBottom) {
                    retStringIdx = stringIdx
                    break
                }
            }
        }
        return retStringIdx
    }

    private fun resetSelection() {
        val stringSelectionsMutable = mutableListOf<SelectionRect>()
        for (strIdx in 0..<_strings.value.size) {
            stringSelectionsMutable.add(SelectionRect.empty())
        }
        _stringSelections.value = stringSelectionsMutable
    }

    private fun recalcSelection() {
        val stringSelectionsMutable = mutableListOf<SelectionRect>()
        for (stringIdx in 0..<_strings.value.size) {
            if ((rightSelectionRange.first < stringRanges.value[stringIdx].second) &&
                (stringRanges.value[stringIdx].first < rightSelectionRange.last)) {
                // String stringIdx contain selected part
                val firstSelectedCharIdx = maxOf(rightSelectionRange.first, stringRanges.value[stringIdx].first) -
                        stringRanges.value[stringIdx].first
                val lastSelectedCharIdx = minOf(rightSelectionRange.last, stringRanges.value[stringIdx].second) -
                        stringRanges.value[stringIdx].first
                val leftSelectionSide = textLayoutResults[stringIdx]!!.getHorizontalPosition(firstSelectedCharIdx, true)
                val rightSelectionSide = textLayoutResults[stringIdx]!!.getHorizontalPosition(lastSelectedCharIdx, false)
                val topSelectionSide = textLayoutResults[stringIdx]!!.getLineTop(0)
                val bottomSelectionSide = textLayoutResults[stringIdx]!!.getLineBottom(0)
                stringSelectionsMutable.add(SelectionRect(
                    Offset(leftSelectionSide, topSelectionSide),
                    Size(rightSelectionSide - leftSelectionSide, bottomSelectionSide - topSelectionSide)))
            } else {
                stringSelectionsMutable.add(SelectionRect.empty())
            }
        }
        _stringSelections.value = stringSelectionsMutable
    }

}