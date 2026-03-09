package ru.petr.songpacker.packer.songPart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    val arrowEndings by component.arrowEndings.subscribeAsState()

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
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        for ((index, string) in strings.withIndex()) {
                            Arrow(
                                modifier = Modifier.padding(vertical = 8.dp),
                                startPos = stringSelections[index].topLeft.x,
                                startArrow = arrowEndings[index].first,
                                endPos = stringSelections[index].topLeft.x + stringSelections[index].size.width,
                                endArrow = arrowEndings[index].second,
                                needTextField = true,
                                textValue = "3 p"
                            )
                            Box(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    string,
                                    modifier = Modifier
                                        .pointerHoverIcon(PointerIcon.Text)
                                        .pointerInput(string) {
                                            detectTapGestures(
                                                onTap = { offset -> component.onTextTap(index, offset) }
                                            )
                                        }
                                        .pointerInput(string) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    component.onTextDragStart(index, offset)
                                                },
                                                onDrag = { change, _ ->
                                                    component.onTextDrag(index, change)
                                                },
                                            )
                                        }
                                        .onGloballyPositioned {layoutCoordinates ->
                                            component.onTextPositioned(index, layoutCoordinates)
                                        },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
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

@Composable
fun Arrow(
    modifier: Modifier = Modifier,
    startPos: Float,
    startArrow: Boolean,
    endPos: Float,
    endArrow: Boolean,
    color: Color = Color.Blue,
    needTextField: Boolean = false,
    textValue: String = "",
    onValueChange: (String) -> Unit = {}
) {
    Box(modifier) {
        Canvas(Modifier.fillMaxWidth().wrapContentHeight()) {
            if (startArrow) {
                drawLine(
                    color = color,
                    start = Offset(startPos, 5f),
                    end = Offset(startPos + 10f, 0f)
                )
                drawLine(
                    color = color,
                    start = Offset(startPos, 5f),
                    end = Offset(startPos + 10f, 10f)
                )
            }
            drawLine(
                color = color,
                start = Offset(startPos, 5f),
                end = Offset(endPos, 5f)
            )
            if (endArrow) {
                drawLine(
                    color = color,
                    start = Offset(endPos, 5f),
                    end = Offset(endPos - 10f, 0f)
                )
                drawLine(
                    color = color,
                    start = Offset(endPos, 5f),
                    end = Offset(endPos - 10f, 10f)
                )
            }
        }
        if (needTextField) {
            val textMeasurer = rememberTextMeasurer()
            val arrowCenterX = (startPos + endPos) / 2
            val textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            val textLayout = textMeasurer.measure(textValue, style = textStyle)
            val textWidth = textLayout.size.width
            val textHeight = textLayout.size.height
            val horizontalPadding = 10.dp
            val density = LocalDensity.current
            val paddingPx = with(density) { horizontalPadding.roundToPx() }

            BasicTextField(
                value = textValue,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (arrowCenterX - textWidth / 2 - paddingPx).toInt(),
                            y = (6.dp.roundToPx() - textHeight / 2)
                        )
                    }
                    .width(with(density) { textWidth.toDp() } + horizontalPadding * 2)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    .padding(horizontal = horizontalPadding, vertical = 4.dp)
            )
        }
    }
}