package ru.petr.songpacker.packer

data class SongMetadata(
    val number: String = "",
    val name: String = "",
    val isCanon: Boolean = false,
    val textAuthor: String = "",
    val textRusAuthor: String = "",
    val musicAuthor: String = "",
    val additionalInfo: String = ""
)
