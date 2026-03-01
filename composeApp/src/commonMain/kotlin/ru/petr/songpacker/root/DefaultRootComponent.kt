package ru.petr.songpacker.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

import ru.petr.songpacker.packer.DefaultPackerComponent
import ru.petr.songpacker.root.RootComponent

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {
    
    private val navigation = StackNavigation<Config>()

    private val stack =
        childStack(
                source = navigation,
                serializer = serializer<Config>(),
                initialConfiguration = Config.Packer,
                handleBackButton = true,
                childFactory = ::child,
        )

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = stack

    @OptIn(DelicateDecomposeApi::class)
    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.Packer -> RootComponent.Child.PackerChild(
                DefaultPackerComponent(
                    componentContext,
                )
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Packer : Config
    }
}
