package co.touchlab.skie.plugin.license.util

import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

internal fun Path.getLicensePaths(): List<Path> =
    if (isDirectory()) {
        listDirectoryEntries().filter { JwtParser.isAnyJwt(it) }
    } else {
        emptyList()
    }
