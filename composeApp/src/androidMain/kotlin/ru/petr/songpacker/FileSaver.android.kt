package ru.petr.songpacker

import android.content.Context
import java.io.File
import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the application context needed for file saving on Android.
 * Must be initialised in MainActivity.onCreate before any save call.
 * Using application context (not activity) so the weak reference is effectively permanent
 * for the lifetime of the process, but avoids the lint memory-leak warning.
 */
object AndroidFileSaverContext {
    private var contextRef: WeakReference<Context>? = null

    fun init(appContext: Context) {
        contextRef = WeakReference(appContext.applicationContext)
    }

    val context: Context? get() = contextRef?.get()
}

actual fun saveXmlFile(fileName: String, content: String): String {
    return try {
        val ctx = AndroidFileSaverContext.context
            ?: return "Error: application context not initialised"
        val dir = ctx.getExternalFilesDir(null)
            ?: return "Error: external storage unavailable"
        dir.mkdirs()
        val file = File(dir, fileName)
        file.writeText(content, Charsets.UTF_8)
        file.absolutePath
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
