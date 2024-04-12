package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.phases.ScheduledPhase
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.isExported
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

object EnumEntryRenamingPhase : SirPhase {

    private val forbiddenNames = setOf(
        "alloc",
        "copy",
        "mutableCopy",
        "new",
        "init",
        "isProxy",
        "retainCount",
        "zone",
        "release",
        "initialize",
        "load",
        "class",
        "superclass",
        "classFallbacksForKeyedArchiver",
        "classForKeyedUnarchiver",
        "description",
        "debugDescription",
        "version",
        "hash",
        "useStoredAccessor",
    )

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinEnums
            .filter { it.isSupported }
            .forEach {
                it.renameEnumEntries()
            }
    }

    context(ScheduledPhase.Context)
    private val KirClass.isSupported: Boolean
        get() = this.originalSirClass.isExported && !this.configuration[EnumInterop.LegacyCaseName]

    context(ScheduledPhase.Context)
    private val KirEnumEntry.isSupported: Boolean
        get() = !this.hasUserDefinedName

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
        val words = NameParser(enumEntry.kotlinName).parse()

        val lowerCaseWords = words.map { it.lowercase() }

        val rawNameCandidate = lowerCaseWords.first() + lowerCaseWords.drop(1).joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

        val nameCandidate = rawNameCandidate.toValidSwiftIdentifier()

        return if (nameCandidate in forbiddenNames) {
            "the" + nameCandidate.replaceFirstChar(Char::uppercaseChar)
        } else {
            nameCandidate
        }
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
