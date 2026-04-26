package ru.petr.songpacker.packer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.DefaultSongPartComponent
import ru.petr.songpacker.packer.songPart.SongPartComponent
import ru.petr.songpacker.packer.songPart.SongPartTypes
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerComponent

import kotlin.random.Random

class DefaultPackerComponent(
    componentContext: ComponentContext,
    ) : PackerComponent, ComponentContext by componentContext {

    private val _newText = MutableValue("")
    override val newText: Value<String> = _newText

    override fun onChangeNewText(text: String) {
        _newText.value = text
    }

    private val _songParts = MutableValue(listOf<SongPartComponent>())
    override val songParts: Value<List<SongPartComponent>> = _songParts

    private val _songMetadata = MutableValue(SongMetadata())
    override val songMetadata: Value<SongMetadata> = _songMetadata

    override fun onUpdateMetadata(metadata: SongMetadata) {
        _songMetadata.value = metadata
    }

    override fun onAddSongPart(type: SongPartTypes) {
        if (newText.value.isNotBlank()) {
            val mutableSongPartsList = _songParts.value.toMutableList()

            // Create unique id and key for child component
            val id = "SongPart_${Random.nextInt()}_${mutableSongPartsList.size}"

            // Create child context with the same key as id
            val childContext = childContext(key = id)

            // Create child component with the id
            val songPartComponent = DefaultSongPartComponent(
                componentContext = childContext,
                id = id,
                initialType = type,
                initialText = newText.value
            )

            mutableSongPartsList.add(songPartComponent)
            _songParts.value = mutableSongPartsList
            _newText.value = ""
        }
    }

    override fun onMoveSongPart(fromIndex: Int, toIndex: Int) {
        val list = _songParts.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _songParts.value = list
    }

    override fun clearAllSelections() {
        _songParts.value.forEach { it.clearSelection() }
    }

    override fun onNewSong() {
        _songParts.value = emptyList()
        _songMetadata.value = SongMetadata()
        _newText.value = ""
        SongLayerComponent.resetCurrentId()
    }

    override fun onImportSong(song: ParsedSong) {
        SongLayerComponent.resetCurrentId()

        val newParts = mutableListOf<SongPartComponent>()

        for (parsedPart in song.parts) {
            val id = "SongPart_${Random.nextInt()}_${newParts.size}"
            val childCtx = childContext(key = id)

            val (stringTexts, chordPlacements, repeatRanges) = computePartData(parsedPart)
            val initialText = stringTexts.joinToString("\n")

            val part = DefaultSongPartComponent(
                componentContext = childCtx,
                id = id,
                initialType = parsedPart.type,
                initialText = initialText
            )

            // Load repeat layers first (so they end up before the chord layer)
            for ((range, qty) in repeatRanges) {
                part.loadRepeatLayer(range, qty)
            }

            // Load chord layer (xPositions will be recalculated after first text layout)
            if (chordPlacements.isNotEmpty()) {
                part.loadChordLayer(chordPlacements)
            }

            newParts.add(part)
        }

        _songParts.value = newParts
        _songMetadata.value = song.metadata
        _newText.value = ""
    }
}
