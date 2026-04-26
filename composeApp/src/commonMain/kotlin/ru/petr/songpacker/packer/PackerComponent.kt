package ru.petr.songpacker.packer

import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.songPart.SongPartComponent
import ru.petr.songpacker.packer.songPart.SongPartTypes


interface PackerComponent {
    val newText: Value<String>

    fun onChangeNewText(text: String)


    val songParts: Value<List<SongPartComponent>>
    fun onAddSongPart(type: SongPartTypes)

    fun onMoveSongPart(fromIndex: Int, toIndex: Int)

    fun clearAllSelections()

    val songMetadata: Value<SongMetadata>

    fun onUpdateMetadata(metadata: SongMetadata)

    /** Reset all song parts and metadata to create a blank song. */
    fun onNewSong()

    /** Replace the current song with the imported [ParsedSong]. */
    fun onImportSong(song: ParsedSong)
}
