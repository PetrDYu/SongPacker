package ru.petr.songpacker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform