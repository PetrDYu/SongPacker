package ru.petr.songpacker.root

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.petr.songpacker.root.RootComponent.Child.*
import ru.petr.songpacker.packer.PackerContent
import ru.petr.songpacker.root.RootComponent

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    val childStack by component.childStack.subscribeAsState()

    MaterialTheme() {
        Scaffold(
                modifier,
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                Children(
                        stack = childStack,
                        animation = stackAnimation(fade()),
                ) {
                    when (val child = it.instance) {
                        is RootComponent.Child.PackerChild -> PackerContent(component = child.component, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
