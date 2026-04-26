package ru.petr.songpacker.packer

import ru.petr.songpacker.packer.songPart.SongPartComponent
import ru.petr.songpacker.packer.songPart.SongPartTypes
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.Accidental
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordPlacement
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordQuality
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerComponent

fun generateSongXml(metadata: SongMetadata, parts: List<SongPartComponent>): String {
    val songName = metadata.name.ifBlank {
        parts.firstOrNull()?.strings?.value?.firstOrNull() ?: ""
    }

    val sb = StringBuilder()
    sb.appendLine("<?xml version='1.0' encoding='utf-8'?>")
    sb.append("<song")
    sb.append(" number=\"${escapeXml(metadata.number)}\"")
    sb.append(" name=\"${escapeXml(songName)}\"")
    sb.append(" canon=\"${metadata.isCanon}\"")
    sb.append(" text=\"${escapeXml(metadata.textAuthor)}\"")
    sb.append(" textRus=\"${escapeXml(metadata.textRusAuthor)}\"")
    sb.append(" music=\"${escapeXml(metadata.musicAuthor)}\"")
    sb.append(" additionalInfo=\"${escapeXml(metadata.additionalInfo)}\"")
    sb.appendLine(">")

    val verseTotal = parts.count { it.type.value == SongPartTypes.VERSE }
    var verseIdx = 0
    var chorusIdx = 0
    var bridgeIdx = 0
    var globalChordId = 0

    for (part in parts) {
        val type = part.type.value
        val tagName = type.name.lowercase()
        val partNumber = when (type) {
            SongPartTypes.VERSE -> if (verseTotal == 1) 0 else ++verseIdx
            SongPartTypes.CHORUS -> ++chorusIdx
            SongPartTypes.BRIDGE -> ++bridgeIdx
        }

        sb.appendLine("    <$tagName number=\"$partNumber\">")

        val strings = part.strings.value
        val stringStarts = computeStringStarts(strings)

        val chordLayer = part.layers.value
            .filterIsInstance<ChordSongLayerComponent>()
            .firstOrNull()
        val allChords = chordLayer?.chords?.value ?: emptyList()

        val repeatLayers = part.layers.value
            .filterIsInstance<RepeatSongLayerComponent>()
            .filter { it.visible.value }

        // Assign stable sequential IDs within this part (0, 1, 2...)
        val repeatIdMap = repeatLayers
            .mapIndexed { localIdx, layer -> layer.id to localIdx }
            .toMap()

        for ((strIdx, string) in strings.withIndex()) {
            sb.appendLine("        <string>")

            val stringStart = stringStarts[strIdx]
            // stringEnd is the last VALID char index; stringEndInclusive extends by 1
            // to capture range.last == stringStart + string.length (drag past end of line)
            val stringEnd = stringStart + string.length - 1
            val stringEndInclusive = stringStart + string.length

            // Each event: (charPosition, sortPriority, tieBreakSize, xmlFragment)
            // charPosition: position in string where event is placed (text before this pos, then event)
            // sortPriority: 0=repeat-open, 1=chord, 2=repeat-close
            // tieBreakSize: for opens at same pos: larger range first (-size); for closes: smaller first (+size)
            data class Event(val pos: Int, val priority: Int, val tieBreak: Int, val xml: String)
            val events = mutableListOf<Event>()

            // Chord events: placed at local char position
            for (cp in allChords.filter { it.stringIdx == strIdx }.sortedBy { it.charOffset }) {
                val localPos = cp.charOffset - stringStart
                if (localPos in 0..string.length) {
                    events.add(Event(localPos, 1, 0, buildChordXml(globalChordId++, cp)))
                }
            }

            // Repeat events
            for (repeatLayer in repeatLayers) {
                val range = repeatLayer.repeatRange.value
                val qty = repeatLayer.repeatQtyStr.value.toIntOrNull() ?: 0
                val localId = repeatIdMap[repeatLayer.id] ?: continue
                val rangeSize = range.last - range.first

                // Opening bracket: in the string containing range.first
                if (range.first in stringStart..stringEnd.coerceAtLeast(stringStart)) {
                    val localPos = range.first - stringStart
                    // Larger range (outer bracket) opens first → tieBreak = -rangeSize (ascending = larger first)
                    events.add(Event(localPos, 0, -rangeSize, buildRepeatOpenXml(localId)))
                }

                // Closing bracket: in the string containing range.last.
                // Use stringEndInclusive so that "drag past end of line" (range.last == stringStart + string.length)
                // is still treated as belonging to this string.
                if (range.last in stringStart..stringEndInclusive.coerceAtLeast(stringStart)) {
                    // Clamp to string.length so substring() never goes out of bounds
                    val localPos = (range.last - stringStart + 1).coerceAtMost(string.length)
                    // Smaller range (inner bracket) closes first → tieBreak = +rangeSize (ascending = smaller first)
                    events.add(Event(localPos, 2, rangeSize, buildRepeatCloseXml(localId, qty)))
                }
            }

            // Sort: position asc → priority asc → tieBreak asc
            events.sortWith(compareBy({ it.pos }, { it.priority }, { it.tieBreak }))

            // Emit string content
            var currentPos = 0
            for (event in events) {
                if (event.pos > currentPos) {
                    sb.appendLine("            <plain>${escapeXml(string.substring(currentPos, event.pos))}</plain>")
                    currentPos = event.pos
                }
                sb.appendLine("            ${event.xml}")
            }
            if (currentPos < string.length) {
                sb.appendLine("            <plain>${escapeXml(string.substring(currentPos))}</plain>")
            }

            sb.appendLine("        </string>")
        }

        sb.appendLine("    </$tagName>")
    }

    sb.append("</song>")
    return sb.toString()
}

fun buildSongFileName(metadata: SongMetadata, parts: List<SongPartComponent>): String {
    val name = metadata.name.ifBlank {
        parts.firstOrNull()?.strings?.value?.firstOrNull() ?: "song"
    }
    val sanitized = name.replace(Regex("[/\\\\:*?\"<>|]"), "_")
    return "${metadata.number} $sanitized.xml"
}

private fun computeStringStarts(strings: List<String>): List<Int> {
    val starts = mutableListOf<Int>()
    var pos = 0
    for (s in strings) {
        starts.add(pos)
        pos += s.length + 1  // +1 for the line separator
    }
    return starts
}

private fun buildChordXml(id: Int, cp: ChordPlacement): String {
    val chord = cp.chord
    val sign = when (chord.accidental) {
        Accidental.FLAT -> "b"
        Accidental.SHARP -> "#"
        Accidental.NONE -> ""
    }
    val isMinor = chord.quality == ChordQuality.MINOR
    val type = if (chord.isSeventh) "7" else ""
    return "<chord id=\"$id\" main_chord=\"${chord.baseNote.displayName}\" " +
            "chord_is_minor=\"$isMinor\" chord_sign=\"$sign\" chord_type=\"$type\"/>"
}

private fun buildRepeatOpenXml(id: Int): String =
    "<repeat id=\"$id\" rep_rate=\"-1\" is_opening=\"true\" />"

private fun buildRepeatCloseXml(id: Int, qty: Int): String =
    "<repeat id=\"$id\" rep_rate=\"$qty\" is_opening=\"false\" />"

private fun escapeXml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
