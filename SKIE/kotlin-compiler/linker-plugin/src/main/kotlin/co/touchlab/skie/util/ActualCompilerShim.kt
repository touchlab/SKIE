@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.util

class ActualCompilerShim : CompilerShim {

    override val cKeywords: Set<String> = org.jetbrains.kotlin.backend.konan.cKeywords
}
