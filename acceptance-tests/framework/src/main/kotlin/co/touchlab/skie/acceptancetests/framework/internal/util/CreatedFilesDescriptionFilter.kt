package co.touchlab.skie.acceptancetests.framework.internal.util

import java.io.File

internal val CreatedFilesDescriptionFilter = fun(file: File): Boolean {
    if (file.absolutePath.contains(".framework/Versions")) return false
    if (file.absolutePath.contains(".framework/Modules/")) return false
    if (file.extension in listOf("o", "bc", "klib", "")) return false

    return true
}
