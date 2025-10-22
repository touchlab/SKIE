package co.touchlab.skie.util.file

import java.io.File

val File.isSwift: Boolean
    get() = extension == "swift"
