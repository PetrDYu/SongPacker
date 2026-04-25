package ru.petr.songpacker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun saveXmlFile(fileName: String, content: String): String {
    return try {
        val docsUrls = NSFileManager.defaultManager
            .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val docsUrl = docsUrls.firstOrNull() as? NSURL
            ?: return "Error: documents directory not found"
        val fileUrl = docsUrl.URLByAppendingPathComponent(fileName)
            ?: return "Error: could not build file URL"
        val filePath = fileUrl.path
            ?: return "Error: could not get file path"
        val nsStr = NSString.create(string = content)
        nsStr.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        filePath
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
