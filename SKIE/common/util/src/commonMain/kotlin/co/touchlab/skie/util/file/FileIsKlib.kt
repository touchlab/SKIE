package co.touchlab.skie.util.file

import java.io.File

val File.isKlib: Boolean
    get() = extension == "klib"
