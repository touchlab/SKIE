package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.util.hasArgumentValue
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.sir.element.isExported
import org.jetbrains.kotlin.backend.konan.KonanFqNames

object EnumEntryRenamingPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allEnums
            .filter { it.isSupported }
            .forEach {
                it.renameEnumEntries()
            }
    }

    context(SkiePhase.Context)
    private val KirClass.isSupported: Boolean
        get() = this.originalSirClass.isExported && !this.getConfiguration(EnumInterop.LegacyCaseName)

    context(SkiePhase.Context)
    private val KirEnumEntry.isSupported: Boolean
        get() = this.descriptor.annotations.findAnnotation(KonanFqNames.objCName)
            ?.let { !it.hasArgumentValue("name") && !it.hasArgumentValue("swiftName") }
            ?: true

    context(SirPhase.Context)
    private fun KirClass.renameEnumEntries() {
        this.enumEntries
            .filter { it.isSupported }
            .forEach {
                it.rename()
            }
    }

    private fun KirEnumEntry.rename() {
        this.sirEnumEntry.identifier = getNewEnumEntryName(this)
    }

    private fun getNewEnumEntryName(enumEntry: KirEnumEntry): String {
        val kotlinName = enumEntry.descriptor.name.asString()

        val words = NameParser(kotlinName).parse()

        val lowerCaseWords = words.map { it.lowercase() }

        return lowerCaseWords.first() + lowerCaseWords.drop(1).joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
    }

    private class NameParser(
        private val kotlinName: String,
    ) {

        private val words = mutableListOf<String>()

        private var wordStart = 0
        private var previousCharIsLowerCase = false

        fun parse(): List<String> {
            kotlinName.forEachIndexed { index, char ->
                parseCharacter(char, index)
            }

            nextWord(kotlinName.length)

            return words
        }

        private fun parseCharacter(char: Char, index: Int) {
            when {
                char == '_' -> {
                    nextWord(index)
                    wordStart++

                    previousCharIsLowerCase = false
                }
                char.isUpperCase() -> {
                    if (previousCharIsLowerCase) {
                        nextWord(index)
                    }

                    previousCharIsLowerCase = false
                }
                else -> {
                    previousCharIsLowerCase = true
                }
            }
        }

        private fun nextWord(endIndex: Int) {
            if (wordStart >= endIndex) {
                return
            }

            val word = kotlinName.substring(wordStart, endIndex)

            words.add(word)

            wordStart = endIndex
        }
    }
}
