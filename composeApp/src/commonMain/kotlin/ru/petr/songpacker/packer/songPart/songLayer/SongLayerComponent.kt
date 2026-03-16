package ru.petr.songpacker.packer.songPart.songLayer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.ArrowRange
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.DefaultRepeatSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerComponent

interface SongLayerComponent {

    companion object {
        private var repeatTagId = 0

        @OptIn(ExperimentalUuidApi::class)
        fun buildRepeatSongLayer(parentComponentContext: ComponentContext): RepeatSongLayerComponent {
            val key = "DefaultRepeatSongLayerComponent-${Uuid.random()}"
            return DefaultRepeatSongLayerComponent(
                parentComponentContext.childContext(key),
                repeatTagId
            )
        }

        fun updateCurrentRepeatSongLayer(layers: List<SongLayerComponent>,
                                         newRepeatRange: IntRange,
                                         newArrowRanges: List<ArrowRange>) {
            layers
                .filterIsInstance<RepeatSongLayerComponent>()
                .find { it.id == repeatTagId }
                ?.update(newRepeatRange, newArrowRanges)
        }

        fun getCurrentRepeatSongLayerIdxAndDelete(layers: List<SongLayerComponent>): Int {
            val index = layers.filterIsInstance<RepeatSongLayerComponent>().indexOfFirst { it.id == repeatTagId }
            if (index != -1) {
                repeatTagId--
            }
            return index
        }

        fun freezeCurrentRepeatSongLayer() {
            repeatTagId++
        }
    }
}