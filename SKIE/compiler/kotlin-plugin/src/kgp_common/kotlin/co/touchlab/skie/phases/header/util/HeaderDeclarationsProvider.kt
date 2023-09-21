package co.touchlab.skie.phases.header.util

import java.io.File

class HeaderDeclarationsProvider(
    headerFile: File,
) {

    private val protocolDefinitionNextLinePrefix = "@required"

    private val classDefinitionRegex = "^@interface ([^ <,;]+)(<([^>]*)>)?.*".toRegex()
    private val protocolDefinitionRegex = "^@protocol ([^ ,;]+).*".toRegex()

    private val headerContent = headerFile.readLines()

    val definedClasses: Set<Declaration> =
        headerContent.mapNotNull { parseClassDefinition(it) }.toSet()

    private fun parseClassDefinition(line: String): Declaration? =
        classDefinitionRegex.matchEntire(line)?.groupValues?.let { groups ->
            val name = groups[1]
            val typeParameters = groups.getOrNull(3)?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim() } ?: emptyList()

            Declaration(name, typeParameters)
        }

    val definedProtocols: Set<Declaration> =
        headerContent.zipWithNext { line, nextLine -> parseProtocolDefinition(line, nextLine) }
            .filterNotNull()
            .toSet()

    private fun parseProtocolDefinition(line: String, nextLine: String): Declaration? =
        if (nextLine.startsWith(protocolDefinitionNextLinePrefix)) {
            protocolDefinitionRegex.matchEntire(line)?.groupValues?.get(1)?.let { Declaration(it, emptyList()) }
        } else {
            null
        }

    val forwardlyDeclaredClasses: Set<Declaration> =
        headerContent.flatMap { parseClassForwardDeclarations(it) }.toSet()

    val forwardlyDeclaredProtocols: Set<Declaration> =
        headerContent.zipWithNext { line, nextLine -> parseProtocolForwardDeclarations(line, nextLine) }
            .flatten()
            .toSet()

    private fun parseClassForwardDeclarations(line: String): List<Declaration> =
        if (line.startsWith("@class")) {
            val declarations = line.removePrefix("@class").removeSuffix(";").trim()

            val declarationsWithReplacedTypeParametersSeparators = declarations.replaceTypeParametersSeparators()

            declarationsWithReplacedTypeParametersSeparators
                .split(",")
                .map { it.trim() }
                .map { declaration ->
                    parseClassForwardDeclaration(declaration)
                }
        } else {
            emptyList()
        }

    private fun parseClassForwardDeclaration(declaration: String): Declaration {
        val name = declaration.substringBefore("<").trim()
        val typeParameters = declaration
            .takeIf { it.contains("<") }
            ?.substringAfter("<")
            ?.substringBefore(">")
            ?.split(";")
            ?.map { it.trim() }
            ?: emptyList()

        return Declaration(name, typeParameters)
    }

    private fun parseProtocolForwardDeclarations(line: String, nextLine: String?): List<Declaration> =
        if (line.startsWith("@protocol") && nextLine?.startsWith(protocolDefinitionNextLinePrefix) != true) {
            line.removePrefix("@protocol")
                .removeSuffix(";")
                .split(",")
                .map { it.trim() }
                .map { Declaration(it, emptyList()) }
        } else {
            emptyList()
        }

    private val externalDeclaredClasses: Set<Declaration> = run {
        val definedNames = definedClasses.map { it.name }.toSet()

        forwardlyDeclaredClasses.filter { it.name !in definedNames }.toSet()
    }

    private val externalDeclaredProtocol: Set<Declaration> = run {
        val definedNames = definedProtocols.map { it.name }.toSet()

        forwardlyDeclaredProtocols.filter { it.name !in definedNames }.toSet()
    }

    val externalTypes: Set<Declaration>
        get() = externalDeclaredClasses + externalDeclaredProtocol

    class Declaration(val name: String, val typeParameters: List<String>) {

        override fun toString(): String =
            if (typeParameters.isEmpty()) name else "$name<${typeParameters.joinToString(", ")}>"
    }
}

private fun String.replaceTypeParametersSeparators(): String {
    var isTypeParameter = false

    val builder = StringBuilder()

    forEach { char ->
        if (isTypeParameter && char == ',') {
            builder.append(';')
        } else {
            builder.append(char)
        }

        when (char) {
            '<' -> isTypeParameter = true
            '>' -> isTypeParameter = false
        }
    }

    return builder.toString()
}
