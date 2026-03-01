package ru.petr.songpacker.packer

import com.arkivanov.decompose.value.Value

interface PackerComponent {
    val newText: Value<String>

    fun onChangeNewText(text: String)


    val songParts: Value<List<SongPart>>
    fun onAddSongPart(type: SongPartTypes)
}