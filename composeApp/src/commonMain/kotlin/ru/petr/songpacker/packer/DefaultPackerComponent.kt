package ru.petr.songpacker.packer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class DefaultPackerComponent(
    componentContext: ComponentContext,
    ) : PackerComponent, ComponentContext by componentContext {

    private val _newText = MutableValue("")
    override val newText: Value<String> = _newText

    override fun onChangeNewText(text: String) {
        _newText.value = text
    }

    private val _songParts = MutableValue(listOf<SongPart>())
    override val songParts: Value<List<SongPart>> = _songParts

    override fun onAddSongPart(type: SongPartTypes) {
        if (newText.value.isNotEmpty()) {
            val mutableSongPartsList = _songParts.value.toMutableList()
            mutableSongPartsList.add(SongPart(type, newText.value, emptyList()))
            _songParts.value = mutableSongPartsList
            _newText.value = ""
        }
    }
}