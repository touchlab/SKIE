package co.touchlab.skie.util.cache

import java.io.File

fun File.readTextOrNull(): String? = if (isFile) readText() else null
