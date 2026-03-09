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

    private val _layers = MutableValue(emptyList<SongLayer>())
    override val layers: Value<List<SongLayer>> = _layers

    private var stringRanges = emptyList<Pair<Int, Int>>()

    private val _strings = MutableValue(emptyList<String>())
    override val strings: Value<List<String>> = _strings

    private var textLayoutResults: MutableList<TextLayoutResult?> = mutableListOf()

    private var selectionRange = 0 .. 0

    private val _stringSelections = MutableValue(emptyList<SelectionRect>())
    override val stringSelections: Value<List<SelectionRect>> = _stringSelections

    private val _arrowEndings = MutableValue(emptyList<Pair<Boolean, Boolean>>())
    override val arrowEndings: Value<List<Pair<Boolean, Boolean>>> = _arrowEndings

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
            textLayoutCoordinates = mutableListOf()
            var currentPos = 0
            // Регулярное выражение для любого разделителя строк: \n, \r, \r\n, \n\r
            val lineBreakRegex = Regex("""\r\n|\n\r|\r|\n""")
            val strings = newText.split(lineBreakRegex)
            val delimiters = lineBreakRegex.findAll(newText).map { it.value }.toList()

            for ((index, string) in strings.withIndex()) {
                stringsMutable.add(string)
                textLayoutResults.add(null)
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

            stringRanges = stringRangesMutable
            _strings.value = stringsMutable
            clearSelection()
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
        clearSelection()
    }

    override fun onTextDragStart(stringIdx: Int, offset: Offset) {
        println("start drag")
        clearSelection()
        if (stringIdx < _strings.value.size) {
            textLayoutResults[stringIdx]?.let { layoutResult ->
                val charOffset = stringRanges[stringIdx].first + layoutResult.getOffsetForPosition(offset)
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
                val dragEnd = stringRanges[endStringIdx].first +
                        layoutResult.getOffsetForPosition(change.position)
                selectionRange.let { range ->
                    selectionRange = range.start .. dragEnd
                }
            }
            recalcSelection()
        }
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

    override fun clearSelection() {
        val stringSelectionsMutable = mutableListOf<SelectionRect>()
        val arrowEndingsMutable = mutableListOf<Pair<Boolean, Boolean>>()
        for (strIdx in 0..<_strings.value.size) {
            stringSelectionsMutable.add(SelectionRect.empty())
            arrowEndingsMutable.add(false to false)
        }
        _stringSelections.value = stringSelectionsMutable
        _arrowEndings.value = arrowEndingsMutable
    }

    private fun recalcSelection() {
        val stringSelectionsMutable = mutableListOf<SelectionRect>()
        val arrowEndingsMutable = mutableListOf<Pair<Boolean, Boolean>>()
        var firstSelectedString = true
        for (stringIdx in 0..<_strings.value.size) {
            if ((rightSelectionRange.first <= stringRanges[stringIdx].second) &&
                (stringRanges[stringIdx].first <= rightSelectionRange.last)) {
                // String stringIdx contains selected part
                val firstSelectedCharIdx = maxOf(rightSelectionRange.first, stringRanges[stringIdx].first) -
                        stringRanges[stringIdx].first
                val lastSelectedCharIdx = minOf(rightSelectionRange.last, stringRanges[stringIdx].second) -
                        stringRanges[stringIdx].first
                val arrowStartEnding = firstSelectedString
                val arrowEndEnding = (rightSelectionRange.last <= stringRanges[stringIdx].second)
                val leftSelectionSide = textLayoutResults[stringIdx]!!.getHorizontalPosition(firstSelectedCharIdx, true)
                val rightSelectionSide = textLayoutResults[stringIdx]!!.getHorizontalPosition(lastSelectedCharIdx, false)
                val topSelectionSide = textLayoutResults[stringIdx]!!.getLineTop(0)
                val bottomSelectionSide = textLayoutResults[stringIdx]!!.getLineBottom(0)
                stringSelectionsMutable.add(SelectionRect(
                    Offset(leftSelectionSide, topSelectionSide),
                    Size(rightSelectionSide - leftSelectionSide, bottomSelectionSide - topSelectionSide)))
                arrowEndingsMutable.add(arrowStartEnding to arrowEndEnding)
                firstSelectedString = false
            } else {
                stringSelectionsMutable.add(SelectionRect.empty())
                arrowEndingsMutable.add(false to false)
            }
        }
        _stringSelections.value = stringSelectionsMutable
        _arrowEndings.value = arrowEndingsMutable
    }

}