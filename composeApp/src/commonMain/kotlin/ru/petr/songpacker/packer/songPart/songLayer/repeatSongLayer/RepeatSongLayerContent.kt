package ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun RepeatSongLayerContent(
    component: RepeatSongLayerComponent,
    stringIdx: Int,
    modifier: Modifier = Modifier,
) {
    val arrowRanges by component.arrowRanges.subscribeAsState()
    val repeatQtyStr by component.repeatQtyStr.subscribeAsState()
    Arrow(
        modifier = modifier.padding(vertical = 4.dp),
        arrowRange = arrowRanges[stringIdx],
        textValue = repeatQtyStr,
        onValueChange = component::onChangeRepeatQty,
    )
}

@Composable
fun Arrow(
    modifier: Modifier = Modifier,
    arrowRange: ArrowRange,
    color: Color = Color.Blue,
    textValue: String = "",
    onValueChange: (String) -> Unit = {}
) {
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Canvas(Modifier.fillMaxWidth().wrapContentHeight()) {
            if (arrowRange.startEnding) {
                drawLine(
                    color = color,
                    start = Offset(arrowRange.start, 5f),
                    end = Offset(arrowRange.start + 10f, 0f)
                )
                drawLine(
                    color = color,
                    start = Offset(arrowRange.start, 5f),
                    end = Offset(arrowRange.start + 10f, 10f)
                )
            }
            drawLine(
                color = color,
                start = Offset(arrowRange.start, 5f),
                end = Offset(arrowRange.end, 5f)
            )
            if (arrowRange.endEnding) {
                drawLine(
                    color = color,
                    start = Offset(arrowRange.end, 5f),
                    end = Offset(arrowRange.end - 10f, 0f)
                )
                drawLine(
                    color = color,
                    start = Offset(arrowRange.end, 5f),
                    end = Offset(arrowRange.end - 10f, 10f)
                )
            }
        }

        // Text field
        val textMeasurer = rememberTextMeasurer()
        val arrowCenterX = (arrowRange.start + arrowRange.end) / 2
        val textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        val textWidth = remember(textValue) { textMeasurer.measure(textValue, style = textStyle).size.width }
        val suffixWidth = remember(Unit) { textMeasurer.measure("р.", style = textStyle).size.width }
        val horizontalPadding = 10.dp
        val density = LocalDensity.current
        val paddingPx = with(density) { horizontalPadding.roundToPx() }
        val fieldWidthPx = textWidth + paddingPx * 2 + suffixWidth
        val xOffsetPrelim = (arrowCenterX - fieldWidthPx / 2).toInt()
        val minX = 0
        val maxX = (arrowRange.end - fieldWidthPx).toInt().coerceAtLeast(minX)
        val xOffset = xOffsetPrelim.coerceIn(minX, maxX)

        Row(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = xOffset,
                        y = 0
                    )
                }
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                modifier = Modifier
                    .width(with(density) { (textWidth * 2).toDp() })
                    .padding(vertical = 4.dp)
            )
            Text(
                text = "р.",
                fontStyle = textStyle.fontStyle,
                fontWeight = textStyle.fontWeight,
                fontSize = textStyle.fontSize
            )
        }
    }
}