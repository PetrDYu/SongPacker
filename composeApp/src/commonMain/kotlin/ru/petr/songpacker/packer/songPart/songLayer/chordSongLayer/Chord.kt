package ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer

enum class BaseNote(val displayName: String) {
    C("C"),
    D("D"),
    E("E"),
    F("F"),
    G("G"),
    H("H"),
    A("A"),
    B("B") // = H♭ in German/Russian notation
}

enum class Accidental(val displayName: String) {
    FLAT("♭"),
    NONE(""),
    SHARP("♯")
}

enum class ChordQuality(val displayName: String) {
    MAJOR(""),
    MINOR("m")
}

data class Chord(
    val baseNote: BaseNote,
    val accidental: Accidental = Accidental.NONE,
    val quality: ChordQuality = ChordQuality.MAJOR,
    val isSeventh: Boolean = false
) {
    fun displayName(): String {
        val seventh = if (isSeventh) "7" else ""
        return "${baseNote.displayName}${accidental.displayName}${quality.displayName}${seventh}"
    }
}

data class ChordPlacement(
    val stringIdx: Int,
    val charOffset: Int,
    val xPosition: Float,
    val chord: Chord
)

data class PendingTap(
    val stringIdx: Int,
    val charOffset: Int,
    val xPosition: Float,
    val existingChord: Chord? = null
) {
    val isNone: Boolean get() = stringIdx == -1

    companion object {
        val NONE = PendingTap(stringIdx = -1, charOffset = -1, xPosition = 0f)
    }
}
