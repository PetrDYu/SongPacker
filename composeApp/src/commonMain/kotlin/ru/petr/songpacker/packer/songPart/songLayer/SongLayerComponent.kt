package ru.petr.songpacker.packer.songPart.songLayer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.ArrowRange
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.DefaultRepeatSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerComponent

interface SongLayerComponent {

    val id: Int

    companion object {
        private var currentId = 0

        @OptIn(ExperimentalUuidApi::class)
        fun buildRepeatSongLayer(parentComponentContext: ComponentContext): RepeatSongLayerComponent {
            val key = "DefaultRepeatSongLayerComponent-${Uuid.random()}"
            return DefaultRepeatSongLayerComponent(
                parentComponentContext.childContext(key),
                currentId
            )
        }

        fun updateCurrentRepeatSongLayer(layers: List<SongLayerComponent>,
                                         newRepeatRange: IntRange,
                                         newArrowRanges: List<ArrowRange>) {
            layers
                .filterIsInstance<RepeatSongLayerComponent>()
                .find { it.id == currentId }
                ?.update(newRepeatRange, newArrowRanges)
        }

        fun getCurrentRepeatSongLayerIdxAndDelete(layers: List<SongLayerComponent>): Int {
            val index = layers.filterIsInstance<RepeatSongLayerComponent>().indexOfFirst { it.id == currentId }
            if (index != -1) {
                currentId--
            }
            return index
        }

        fun freezeCurrentRepeatSongLayer() {
            currentId++
        }
    }
}