package ru.petr.songpacker.packer.songPart.songLayer.repeatSongLayer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class DefaultRepeatSongLayerComponent(
    componentContext: ComponentContext,
    override val id: Int
): RepeatSongLayerComponent, ComponentContext by componentContext {
    
    private val _arrowRanges = MutableValue(listOf<ArrowRange>())
    override val arrowRanges: Value<List<ArrowRange>> = _arrowRanges
    
    private val _repeatQtyStr = MutableValue("0")
    override val repeatQtyStr: Value<String> = _repeatQtyStr

    private var repeatRange = 0..0
    
    private val digitRegex = Regex("""\d{2}""")

    override fun onChangeRepeatQty(repeatQtyStr: String) {
        if (repeatQtyStr.isEmpty()) {
            _repeatQtyStr.value = "0"
        } else if (digitRegex.matches(repeatQtyStr)) {
            if (repeatQtyStr[1] == '0') {
                _repeatQtyStr.value = repeatQtyStr.substring(0, 1)
            } else {
                _repeatQtyStr.value = repeatQtyStr.substring(1)
            }
        }
    }

    override fun update(
        newRepeatRange: IntRange,
        newArrowRanges: List<ArrowRange>
    ) {
        repeatRange = newRepeatRange
        _arrowRanges.value = newArrowRanges
    }

    private val _visible = MutableValue(false)
    override val visible: Value<Boolean> = _visible

    override fun onShow() {
        _visible.value = true
    }

    override fun delete() {
        _visible.value = false
    }
}