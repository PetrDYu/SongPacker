package ru.petr.songpacker.packer.songPart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun SongPartContent(component: SongPartComponent, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val type by component.type.subscribeAsState()
    val strings by component.strings.subscribeAsState()
    val stringSelections by component.stringSelections.subscribeAsState()

    AnimatedVisibility(
        visible = visible, // Можно управлять видимостью для анимации удаления
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut()
    ) {
        Card(
            modifier = modifier.wrapContentWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Column (Modifier.horizontalScroll(rememberScrollState())) {
                    for ((index, string) in strings.withIndex()) {
                        Box {
                            Text(
                                string,
                                modifier = Modifier
                                    .pointerHoverIcon(PointerIcon.Text)
                                    .pointerInput(string) {
                                        detectTapGestures(
                                            onTap = { offset ->  component.onTextTap(index, offset) }
                                        )
                                    }
                                    .pointerInput(string) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                component.onTextDragStart(index, offset)
                                            },
                                            onDragEnd = { component.onTextDragEndOrCancel(index) },
                                            onDrag = { change, _ ->
                                                component.onTextDrag(index, change)
                                            },
                                            onDragCancel = { component.onTextDragEndOrCancel(index) }
                                        )
                                    }
                                    .onGloballyPositioned {layoutCoordinates ->
                                        component.onTextPositioned(index, layoutCoordinates)
                                    },
                                style = MaterialTheme.typography.bodyMedium,
                                onTextLayout = { result -> component.onTextLayout(index, result) }
                            )
                            SelectionHighlight(stringSelection = stringSelections[index])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionHighlight(
    stringSelection: SelectionRect,
    color: Color = Color(0xFF3390FF).copy(alpha = 0.3f)
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = color,
            topLeft = stringSelection.topLeft,
            size = stringSelection.size
        )
    }
}