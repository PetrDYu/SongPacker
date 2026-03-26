package ru.petr.songpacker.packer.songPart.songLayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.chordSongLayer.ChordSongLayerContent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerComponent
import ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer.RepeatSongLayerContent

@Composable
fun SongLayerContent(
    component: SongLayerComponent,
    stringIdx: Int,
    onExitAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible by component.visible.subscribeAsState()
    val visibilityState = remember { MutableTransitionState(false) }
    var hasEverBeenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        component.onShow()
    }

    LaunchedEffect(visible) {
        if (visible) {
            hasEverBeenVisible = true
        }
        visibilityState.targetState = visible
    }

    LaunchedEffect(
        visibilityState.currentState,
        visibilityState.targetState,
        visibilityState.isIdle,
        hasEverBeenVisible
    ) {
        if (hasEverBeenVisible &&
            visibilityState.isIdle &&
            !visibilityState.currentState &&
            !visibilityState.targetState
        ) {
            onExitAnimationFinished()
        }
    }

    AnimatedVisibility(
        visibleState = visibilityState,
        enter = EnterTransition.None,
        exit = shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        when (component) {
            is RepeatSongLayerComponent -> RepeatSongLayerContent(component, stringIdx, modifier)
            is ChordSongLayerComponent -> ChordSongLayerContent(component, stringIdx, modifier)
        }
    }
}
