package co.touchlab.skie.phases.apinotes.parser

import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.builder.ApiNotesType
import java.io.File

object ExternalApiNotesParser {

    fun parse(file: File): ApiNotes = Engine(file).parse()

    private class Engine(private val file: File) {

        private val lines = file.readLines()

        private var currentLineIndex = -1

        private var currentParserLevel = 0

        private var moduleName: String? = null

        private val classes = mutableListOf<ApiNotesType>()
        private val protocols = mutableListOf<ApiNotesType>()

        private var keyStartLevel: Int? = null
        private var arrayStartLevel: Int? = null

        private val currentLine: String
            get() = lines[currentLineIndex]

        private val hasLine: Boolean
            get() = currentLineIndex <= lines.lastIndex

        private val isArrayStart: Boolean
            get() = arrayStartLevel != null

        fun parse(): ApiNotes {
            nextLine()

            parseModule()

            return ApiNotes(
                moduleName = moduleName ?: parserError("Module name not found"),
                classes = classes,
                protocols = protocols,
            )
        }

        private fun parseModule() {
            parseMap { key ->
                when (key) {
                    "Name" -> moduleName = parseStringValue()
                    "Classes" -> parseClasses()
                    "Protocols" -> parseProtocols()
                }
            }
        }

        private fun parseClasses() {
            parseTypes(classes)
        }

        private fun parseProtocols() {
            parseTypes(protocols)
        }

        private fun parseTypes(output: MutableList<ApiNotesType>) {
            nextLine()

            parseArray {
                parseType(output)
            }
        }

        private fun parseType(output: MutableList<ApiNotesType>) {
            var objCFqName: String? = null
            var bridgeFqName: String? = null
            var swiftFqName: String? = null
            var importAsNonGeneric = false

            parseMap { key ->
                when (key) {
                    "Name" -> objCFqName = parseStringValue()
                    "SwiftBridge" -> bridgeFqName = parseStringValue()
                    "SwiftName" -> swiftFqName = parseStringValue()
                    "SwiftImportAsNonGeneric" -> importAsNonGeneric = parseStringValue().toBoolean()
                }
            }

            val type = ApiNotesType(
                objCFqName = objCFqName ?: parserError("objCFqName not found"),
                bridgeFqName = bridgeFqName,
                swiftFqName = swiftFqName,
                importAsNonGeneric = importAsNonGeneric,
            )

            output.add(type)
        }

        private fun parseStringValue(): String {
            val baseValue = currentLine.substringAfter(":").trim()

            return if (baseValue.startsWith('"')) {
                baseValue.substringAfter("\"").substringBefore("\"")
            } else {
                baseValue.substringBefore(" ")
            }
        }

        private inline fun parseMap(parseKey: (key: String) -> Unit) {
            while (hasLine) {
                val parsedLineIndex = currentLineIndex
                val parsedLineLevel = keyStartLevel

                when {
                    parsedLineLevel == null -> {}
                    parsedLineLevel == currentParserLevel && !isArrayStart -> {
                        val keyEnd = currentLine.indexOf(':').takeIf { it != -1 } ?: parserError("Key not found")

                        val key = currentLine.substring(parsedLineLevel, keyEnd).trim()

                        parseKey(key)
                    }
                    parsedLineLevel <= currentParserLevel -> return
                }

                if (parsedLineIndex == currentLineIndex) {
                    nextLine()
                }
            }
        }

        private inline fun parseArray(parseLine: () -> Unit) {
            while (hasLine) {
                val parsedLineIndex = currentLineIndex
                val parsedLineLevel = keyStartLevel
                val parsedArrayStartLevel = arrayStartLevel

                when {
                    parsedLineLevel == null -> {}
                    parsedLineLevel <= currentParserLevel -> return
                    parsedArrayStartLevel == currentParserLevel -> {
                        currentParserLevel = parsedLineLevel
                        arrayStartLevel = null

                        parseLine()

                        currentParserLevel = parsedArrayStartLevel
                    }
                }

                if (parsedLineIndex == currentLineIndex) {
                    nextLine()
                }
            }
        }

        private fun nextLine() {
            currentLineIndex++

            keyStartLevel = null
            arrayStartLevel = null

            if (!hasLine) {
                return
            }

            currentLine.forEachIndexed { index, char ->
                when {
                    char.isLetterOrDigit() -> {
                        keyStartLevel = index
                        return
                    }
                    char == '-' -> {
                        arrayStartLevel = index
                    }
                    char == '#' -> return
                }
            }
        }

        private fun parserError(description: String): Nothing {
            error("Parser error: $description. Line: $currentLineIndex, File: $file")
        }
    }
}
