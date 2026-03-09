package ru.petr.songpacker.packer

import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.SongPartComponent
import ru.petr.songpacker.packer.songPart.SongPartTypes

interface PackerComponent {
    val newText: Value<String>

    fun onChangeNewText(text: String)


    val songParts: Value<List<SongPartComponent>>
    fun onAddSongPart(type: SongPartTypes)

    fun clearAllSelections()
}