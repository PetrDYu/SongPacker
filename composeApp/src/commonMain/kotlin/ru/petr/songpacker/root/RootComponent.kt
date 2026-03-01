package ru.petr.songpacker.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import ru.petr.songpacker.packer.DefaultPackerComponent

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class PackerChild(val component: DefaultPackerComponent) : Child()
    }
}
