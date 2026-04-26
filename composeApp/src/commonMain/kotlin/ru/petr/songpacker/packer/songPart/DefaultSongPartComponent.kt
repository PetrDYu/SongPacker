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
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordPlacement
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.DefaultChordSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.ArrowRange
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerComponent

class DefaultSongPartComponent(
    componentContext: ComponentContext,
    override val id: String,
    initialType: SongPartTypes = SongPartTypes.VERSE,
    initialText: String = ""
) : SongPartComponent, ComponentContext by componentContext {

    private val _type = MutableValue(initialType)
    override val type: Value<SongPartTypes> = _type

    private val _layers = MutableValue(emptyList<SongLayerComponent>())
    override val layers: Value<List<SongLayerComponent>> = _layers

    private var stringRanges = emptyList<Pair<Int, Int>>()

    private val _strings = MutableValue(emptyList<String>())
    override val strings: Value<List<String>> = _strings

    private var textLayoutResults: MutableList<TextLayoutResult?> = mutableListOf()

    private var selectionRange = 0 .. 0

    private val _stringSelections = MutableValue(emptyList<SelectionRect>())
    override val stringSelections: Value<List<SelectionRect>> = _stringSelections

    private var textLayoutCoordinates: MutableList<LayoutCoordinates?> = mutableListOf()

    private var selectionIsActive = false

    private val _layersAreFrozen = MutableValue(true)
    override val layersAreFrozen: Value<Boolean> = _layersAreFrozen

    // Single chord layer shared across all taps; created lazily on first tap
    private var chordSongLayer: ChordSongLayerComponent? = null

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
            val strings = newText.split(lineBreakRegex).map { it.trim() }

            for (string in strings) {
                stringsMutable.add(string)
                textLayoutResults.add(null)
                textLayoutCoordinates.add(null)

                val start = currentPos
                val end = currentPos + string.length
                stringRangesMutable.add(start to end)
                currentPos = end + 1
            }

            stringRanges = stringRangesMutable
            _strings.value = stringsMutable
            selectionIsActive = true
            clearSelection()
        }
    }

    override fun onTextLayout(stringIdx: Int, textLayoutResult: TextLayoutResult) {
        textLayoutResults[stringIdx] = textLayoutResult
        // Recalculate imported chord x-positions after the text is measured
        (chordSongLayer as? DefaultChordSongLayerComponent)?.onStringLayout(
            stringIdx,
            textLayoutResult,
            stringRanges.getOrNull(stringIdx)?.first ?: 0
        )
        // Recalculate repeat arrow ranges once all strings are laid out
        recalcLoadedRepeatArrows()
    }

    override fun onTextPositioned(
        stringIdx: Int,
        layoutCoordinates: LayoutCoordinates
    ) {
        textLayoutCoordinates[stringIdx] = layoutCoordinates
    }

    override fun onTextTap(stringIdx: Int, offset: Offset) {
        clearSelection()
        // Ensure chord layer exists (re-create if deleted)
        var layer = chordSongLayer
        if (layer == null || !_layers.value.any { it.id == layer.id }) {
            layer = SongLayerComponent.buildChordSongLayer(this)
            chordSongLayer = layer
            // Chord layer always goes last so it renders directly above the text line
            _layers.value = _layers.value.filter { it !is ChordSongLayerComponent } + layer
        }
        textLayoutResults[stringIdx]?.let { layoutResult ->
            // getOffsetForPosition snaps to the nearest boundary; when tapping the right
            // half of a character it returns the boundary AFTER that character.
            // We correct this so the chord always appears BEFORE the tapped character.
            val rawIdx = layoutResult.getOffsetForPosition(offset)
            val localCharIdx = if (rawIdx > 0 &&
                offset.x < layoutResult.getHorizontalPosition(rawIdx, true)
            ) rawIdx - 1 else rawIdx
            val absoluteCharOffset = stringRanges[stringIdx].first + localCharIdx
            val xPosition = layoutResult.getHorizontalPosition(localCharIdx, true)
            layer.onTextTap(stringIdx, absoluteCharOffset, xPosition)
        }
    }

    override fun onTextDragStart(stringIdx: Int, offset: Offset) {
        println("start drag")
        clearSelection()
        selectionIsActive = true
        _layersAreFrozen.value = false
        if (stringIdx < _strings.value.size) {
            textLayoutResults[stringIdx]?.let { layoutResult ->
                val charOffset = stringRanges[stringIdx].first + layoutResult.getOffsetForPosition(offset)
                selectionRange = charOffset .. charOffset
                println(selectionRange)
            }
        }
        val updatedLayers = _layers.value.toMutableList()
        val newRepeatLayer = SongLayerComponent.buildRepeatSongLayer(parentComponentContext = this)
        // Insert before the chord layer so chords always stay directly above the text
        val chordIdx = updatedLayers.indexOfFirst { it is ChordSongLayerComponent }
        if (chordIdx >= 0) updatedLayers.add(chordIdx, newRepeatLayer)
        else updatedLayers.add(newRepeatLayer)
        _layers.value = updatedLayers
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

    override fun onTextDragEndOrCancel() {
        SongLayerComponent.freezeCurrentRepeatSongLayer()
        _layersAreFrozen.value = true
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
        if (selectionIsActive) {
            val stringSelectionsMutable = mutableListOf<SelectionRect>()
            val arrowRanges = mutableListOf<ArrowRange>()
            for (strIdx in 0..<_strings.value.size) {
                stringSelectionsMutable.add(SelectionRect.empty())
                arrowRanges.add(ArrowRange.emptyArrowRange)
            }
            _stringSelections.value = stringSelectionsMutable
            selectionIsActive = false
        }
    }

    override fun onDeleteLayerClick(layerIdx: Int) {
        _layers.value.getOrNull(layerIdx)?.delete()
    }

    override fun onLayerHidden(layerId: Int) {
        deleteLayer(layerId)
    }

    override fun loadChordLayer(chords: List<ChordPlacement>) {
        if (chords.isEmpty()) return
        val layer = SongLayerComponent.buildChordSongLayer(this)
        (layer as DefaultChordSongLayerComponent).loadChords(chords)
        chordSongLayer = layer
        // Chord layer stays last (renders directly above text)
        _layers.value = _layers.value.filter { it !is ChordSongLayerComponent } + layer
    }

    override fun loadRepeatLayer(range: IntRange, qty: Int) {
        val layer = SongLayerComponent.buildRepeatSongLayer(this)
        SongLayerComponent.freezeCurrentRepeatSongLayer()
        layer.loadRepeat(range, qty)
        // Insert before the chord layer
        val updatedLayers = _layers.value.toMutableList()
        val chordIdx = updatedLayers.indexOfFirst { it is ChordSongLayerComponent }
        if (chordIdx >= 0) updatedLayers.add(chordIdx, layer)
        else updatedLayers.add(layer)
        _layers.value = updatedLayers
    }

    /**
     * For each repeat layer loaded from XML (arrowRanges is empty), computes and sets
     * the visual arrow positions once all string text layouts are available.
     */
    private fun recalcLoadedRepeatArrows() {
        // Only proceed when every string has been measured
        if (textLayoutResults.any { it == null }) return

        val loadedRepeatLayers = _layers.value
            .filterIsInstance<RepeatSongLayerComponent>()
            .filter { it.arrowRanges.value.isEmpty() }

        if (loadedRepeatLayers.isEmpty()) return

        for (repeatLayer in loadedRepeatLayers) {
            val range = repeatLayer.repeatRange.value
            val arrowRanges = computeArrowRangesForRange(range)
            if (arrowRanges.isNotEmpty()) {
                repeatLayer.update(range, arrowRanges)
            }
        }
    }

    /**
     * Computes pixel-level [ArrowRange] entries for each string covered by [range],
     * matching the same logic used in [recalcSelection].
     * Returns an empty list if any required [TextLayoutResult] is not yet available.
     */
    private fun computeArrowRangesForRange(range: IntRange): List<ArrowRange> {
        val result = mutableListOf<ArrowRange>()
        var firstSelectedString = true
        for (stringIdx in 0..<_strings.value.size) {
            val stringStart = stringRanges[stringIdx].first
            val stringEnd = stringRanges[stringIdx].second  // stringStart + string.length
            if (range.first <= stringEnd && stringStart <= range.last) {
                val firstCharIdx = maxOf(range.first, stringStart) - stringStart
                val lastCharIdx = minOf(range.last, stringEnd) - stringStart
                val arrowStartEnding = firstSelectedString
                val arrowEndEnding = range.last <= stringEnd
                val layout = textLayoutResults[stringIdx] ?: return emptyList()
                result.add(
                    ArrowRange(
                        start = layout.getHorizontalPosition(firstCharIdx, true),
                        startEnding = arrowStartEnding,
                        end = layout.getHorizontalPosition(lastCharIdx, false),
                        endEnding = arrowEndEnding
                    )
                )
                firstSelectedString = false
            } else {
                result.add(ArrowRange.emptyArrowRange)
            }
        }
        return result
    }

    private fun deleteLayer(layerId: Int) {
        val updatedLayers = _layers.value.toMutableList()
        val idx = updatedLayers.indexOfFirst { it.id == layerId }
        if (idx != -1) updatedLayers.removeAt(idx)
        _layers.value = updatedLayers
    }

    private fun recalcSelection() {
        val stringSelectionsMutable = mutableListOf<SelectionRect>()
        val arrowRanges = mutableListOf<ArrowRange>()
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
                arrowRanges.add(ArrowRange(
                    start = leftSelectionSide,
                    startEnding = arrowStartEnding,
                    end = rightSelectionSide,
                    endEnding = arrowEndEnding
                ))
                firstSelectedString = false
            } else {
                stringSelectionsMutable.add(SelectionRect.empty())
                arrowRanges.add(ArrowRange.emptyArrowRange)
            }
        }
        _stringSelections.value = stringSelectionsMutable
        SongLayerComponent.updateCurrentRepeatSongLayer(layers.value, rightSelectionRange, arrowRanges)
    }

}
