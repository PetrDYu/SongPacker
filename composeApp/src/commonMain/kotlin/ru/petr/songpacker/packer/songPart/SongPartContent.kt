package ru.petr.songpacker.packer.songPart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.petr.songpacker.packer.songPart.songLayer.SongLayerContent

private const val SPACING_TEXT = 4

@Composable
fun SongPartContent(component: SongPartComponent, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val type by component.type.subscribeAsState()
    val strings by component.strings.subscribeAsState()
    val stringSelections by component.stringSelections.subscribeAsState()
    val layers by component.layers.subscribeAsState()
    val selectionIsActive by component.selectionIsActive.subscribeAsState()

    val density = LocalDensity.current

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
    val height = with(density) {textMeasurer.measure("0p.", textStyle).size.height.toDp() }

    val textYCoords = remember { mutableStateMapOf<Int, SnapshotStateList<Dp>>() }
    LaunchedEffect(strings.size, layers.size) {
        textYCoords.clear()
    }

    var boxYCoord by remember {  mutableStateOf(0.dp)}

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .wrapContentWidth()
        ) {
            Card(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .wrapContentWidth()
                                .onGloballyPositioned({ coordinates ->  boxYCoord = with(density) { coordinates.positionInRoot().y.toDp() } })
                        ) {
                            for ((_, coordinateList) in textYCoords) {
                                for (yCoord in coordinateList) {
                                    BasicButton(
                                        onClick = { println("delete") },
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .offset(y = yCoord - boxYCoord)
                                    ) {
                                        Text("✕")
                                    }
                                }
                            }
                        }

                        Column(Modifier.horizontalScroll(rememberScrollState())) {
                            for ((strIdx, string) in strings.withIndex()) {
                                for ((layerIdx, layer) in layers.withIndex()) {
                                    SongLayerContent(
                                        layer,
                                        strIdx,
                                        Modifier
                                            .padding(horizontal = SPACING_TEXT.dp)
                                            .onGloballyPositioned { coordinates ->
                                                val y = with(density) { coordinates.positionInRoot().y.toDp() }
                                                val list = textYCoords.getOrPut(layerIdx) {
                                                    MutableList(strings.size) { 0.dp }.toMutableStateList()
                                                }
                                                while (list.size <= strIdx) list.add(0.dp)
                                                list[strIdx] = y
                                            }
                                    )
                                }
                                AnimatedVisibility(
                                    visible = !selectionIsActive,
                                    enter = expandVertically(expandFrom = Alignment.Top),
                                    exit = ExitTransition.None
                                ) {
                                    Spacer(
                                        Modifier.fillMaxWidth().height((height.value + 4 * 4).dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        string,
                                        modifier = Modifier
                                            .pointerHoverIcon(PointerIcon.Text)
                                            .pointerInput(string) {
                                                detectTapGestures(
                                                    onTap = { offset ->
                                                        component.onTextTap(
                                                            strIdx,
                                                            offset
                                                        )
                                                    }
                                                )
                                            }
                                            .pointerInput(string) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        component.onTextDragStart(strIdx, offset)
                                                    },
                                                    onDrag = { change, _ ->
                                                        component.onTextDrag(strIdx, change)
                                                    },
                                                    onDragEnd = component::onTextDragEndOrCancel,
                                                    onDragCancel = component::onTextDragEndOrCancel
                                                )
                                            }
                                            .onGloballyPositioned { layoutCoordinates ->
                                                component.onTextPositioned(
                                                    strIdx,
                                                    layoutCoordinates
                                                )
                                            }
                                            .padding(horizontal = SPACING_TEXT.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        onTextLayout = { result ->
                                            component.onTextLayout(
                                                strIdx,
                                                result
                                            )
                                        }
                                    )
                                    Row {
                                        Spacer(Modifier.width(SPACING_TEXT.dp))
                                        SelectionHighlight(stringSelection = stringSelections[strIdx])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
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