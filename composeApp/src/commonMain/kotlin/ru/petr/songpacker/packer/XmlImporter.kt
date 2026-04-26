package ru.petr.songpacker.packer

import ru.petr.songpacker.packer.songPart.SongPartTypes
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.Accidental
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.BaseNote
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.Chord
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordPlacement
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordQuality

// ---- Parsed data classes ----

data class ParsedSong(
    val metadata: SongMetadata,
    val parts: List<ParsedPart>
)

data class ParsedPart(
    val type: SongPartTypes,
    /** Each inner list represents one <string> block (ordered elements). */
    val strings: List<List<ParsedElement>>
)

sealed class ParsedElement {
    data class Text(val content: String) : ParsedElement()
    data class ChordEl(
        val note: String,
        val isMinor: Boolean,
        val sign: String,
        val chordType: String
    ) : ParsedElement() {
        fun toChord(): Chord? {
            val baseNote = BaseNote.entries.find { it.displayName == note } ?: return null
            val accidental = when (sign) {
                "b" -> Accidental.FLAT
                "#" -> Accidental.SHARP
                else -> Accidental.NONE
            }
            val quality = if (isMinor) ChordQuality.MINOR else ChordQuality.MAJOR
            val isSeventh = chordType == "7"
            return Chord(baseNote, accidental, quality, isSeventh)
        }
    }
    data class RepeatEl(
        val xmlId: Int,
        val repRate: Int,
        val isOpening: Boolean
    ) : ParsedElement()
}

// ---- Main entry point ----

/**
 * Parses a UTF-8 XML song file (produced by XmlGenerator) and returns a [ParsedSong],
 * or null if the file cannot be parsed.
 */
fun parseXmlSong(xmlContent: String): ParsedSong? {
    return try {
        // Extract song-level attributes
        val songTagMatch = Regex("""<song\s([^>]+)>""").find(xmlContent) ?: return null
        val songAttrs = parseXmlAttributes(songTagMatch.groupValues[1])

        val metadata = SongMetadata(
            number = songAttrs["number"] ?: "",
            name = unescapeXml(songAttrs["name"] ?: ""),
            isCanon = songAttrs["canon"] == "true",
            textAuthor = unescapeXml(songAttrs["text"] ?: ""),
            textRusAuthor = unescapeXml(songAttrs["textRus"] ?: ""),
            musicAuthor = unescapeXml(songAttrs["music"] ?: ""),
            additionalInfo = unescapeXml(songAttrs["additionalInfo"] ?: "")
        )

        // Extract parts: <verse …>, <chorus …>, <bridge …>
        // Use [\s\S]*? instead of .*? to match across newlines (DOT_MATCHES_ALL not available in KMP common)
        val partRegex = Regex(
            """<(verse|chorus|bridge)\s+number="\d+">([\s\S]*?)</(verse|chorus|bridge)>""",
            RegexOption.IGNORE_CASE
        )

        val parts = partRegex.findAll(xmlContent).map { partMatch ->
            val tagName = partMatch.groupValues[1].lowercase()
            val partContent = partMatch.groupValues[2]

            val type = when (tagName) {
                "chorus" -> SongPartTypes.CHORUS
                "bridge" -> SongPartTypes.BRIDGE
                else -> SongPartTypes.VERSE
            }

            // Extract <string> blocks
            val stringRegex = Regex("""<string>([\s\S]*?)</string>""")
            val strings = stringRegex.findAll(partContent).map { stringMatch ->
                parseStringElements(stringMatch.groupValues[1])
            }.toList()

            ParsedPart(type, strings)
        }.toList()

        ParsedSong(metadata, parts)
    } catch (e: Exception) {
        null
    }
}

// ---- Position computation ----

/**
 * Converts a [ParsedPart] into the data structures needed to build a song part component:
 * - list of string texts (join with "\n" to get initialText)
 * - list of chord placements (xPosition = -1f → recalculated after layout)
 * - list of (repeatRange, repeatQty) pairs
 */
fun computePartData(
    parsedPart: ParsedPart
): Triple<List<String>, List<ChordPlacement>, List<Pair<IntRange, Int>>> {

    // 1. Build string texts and compute start positions
    val stringTexts = mutableListOf<String>()
    val stringStarts = mutableListOf<Int>()
    var pos = 0
    for (parsedString in parsedPart.strings) {
        val text = parsedString.filterIsInstance<ParsedElement.Text>()
            .joinToString("") { it.content }
        stringStarts.add(pos)
        stringTexts.add(text)
        pos += text.length + 1  // +1 for the "\n" separator
    }

    // 2. Collect chord placements and repeat events
    val chordPlacements = mutableListOf<ChordPlacement>()
    val repeatRanges = mutableListOf<Pair<IntRange, Int>>()
    val openings = mutableMapOf<Int, Int>()  // xmlId -> absolute opening position

    for ((strIdx, parsedString) in parsedPart.strings.withIndex()) {
        val stringStart = stringStarts[strIdx]
        var charCount = 0

        for (element in parsedString) {
            when (element) {
                is ParsedElement.Text -> charCount += element.content.length
                is ParsedElement.ChordEl -> {
                    val chord = element.toChord() ?: continue
                    // xPosition = -1f signals "needs recalculation after text layout"
                    chordPlacements.add(
                        ChordPlacement(
                            stringIdx = strIdx,
                            charOffset = stringStart + charCount,
                            xPosition = -1f,
                            chord = chord
                        )
                    )
                }
                is ParsedElement.RepeatEl -> {
                    val absPos = stringStart + charCount
                    if (element.isOpening) {
                        openings[element.xmlId] = absPos
                    } else {
                        val openAbsPos = openings[element.xmlId] ?: continue
                        // rangeEnd = absPos - 1 (last included char position)
                        val rangeEnd = (absPos - 1).coerceAtLeast(openAbsPos)
                        val qty = element.repRate.coerceAtLeast(0)
                        repeatRanges.add(Pair(openAbsPos..rangeEnd, qty))
                    }
                }
            }
        }
    }

    return Triple(stringTexts, chordPlacements, repeatRanges)
}

// ---- Private helpers ----

private fun parseStringElements(stringContent: String): List<ParsedElement> {
    val elements = mutableListOf<ParsedElement>()

    // Match <plain>…</plain>, <chord …/>, <repeat …/>  in document order
    // Each element is on its own line so single-line matching is sufficient here.
    val elementRegex = Regex(
        """<plain>(.*?)</plain>|<chord ([^/]+?)/>|<repeat ([^/]+?)/>"""
    )

    for (match in elementRegex.findAll(stringContent)) {
        when {
            match.value.startsWith("<plain>") -> {
                elements.add(ParsedElement.Text(unescapeXml(match.groupValues[1])))
            }
            match.value.startsWith("<chord") -> {
                val attrs = parseXmlAttributes(match.groupValues[2])
                elements.add(
                    ParsedElement.ChordEl(
                        note = attrs["main_chord"] ?: "",
                        isMinor = attrs["chord_is_minor"] == "true",
                        sign = attrs["chord_sign"] ?: "",
                        chordType = attrs["chord_type"] ?: ""
                    )
                )
            }
            match.value.startsWith("<repeat") -> {
                val attrs = parseXmlAttributes(match.groupValues[3])
                elements.add(
                    ParsedElement.RepeatEl(
                        xmlId = attrs["id"]?.toIntOrNull() ?: 0,
                        repRate = attrs["rep_rate"]?.toIntOrNull() ?: -1,
                        isOpening = attrs["is_opening"] == "true"
                    )
                )
            }
        }
    }

    return elements
}

private fun parseXmlAttributes(attrsStr: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val attrRegex = Regex("""(\w+)="([^"]*?)"""")
    for (match in attrRegex.findAll(attrsStr)) {
        result[match.groupValues[1]] = match.groupValues[2]
    }
    return result
}

private fun unescapeXml(text: String): String = text
    .replace("&quot;", "\"")
    .replace("&gt;", ">")
    .replace("&lt;", "<")
    .replace("&amp;", "&")
