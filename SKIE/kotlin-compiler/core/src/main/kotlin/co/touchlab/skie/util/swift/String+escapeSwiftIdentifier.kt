package co.touchlab.skie.util.swift

import io.outfoxx.swiftpoet.CodeBlock

fun String.escapeSwiftIdentifier(): String =
    CodeBlock.toString("%N", this)
