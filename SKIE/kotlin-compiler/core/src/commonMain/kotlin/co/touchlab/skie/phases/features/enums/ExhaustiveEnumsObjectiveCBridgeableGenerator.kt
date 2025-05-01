package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.bridging.ObjCBridgeableGenerator
import co.touchlab.skie.phases.bridging.ObjCBridgeableGenerator.fatalErrorFromObjectiveC
import co.touchlab.skie.sir.element.SirClass
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.joinToCode

object ExhaustiveEnumsObjectiveCBridgeableGenerator {

    context(SirPhase.Context)
    fun addObjcBridgeableImplementation(enumKirClass: KirClass, bridgedEnum: SirClass) {
        ObjCBridgeableGenerator.addObjcBridgeableImplementation(
            target = bridgedEnum,
            bridgedType = enumKirClass.enum.defaultType,
            bridgeToObjectiveC = {
                addBridgeToObjectiveCBody(enumKirClass)
            },
            bridgeFromObjectiveC = {
                addNonEmptyFromObjectiveCBody(enumKirClass)
            },
        )
    }

    private val KirClass.enum: SirClass
        get() = this.originalSirClass

    private fun CodeBlock.Builder.addBridgeToObjectiveCBody(enumKirClass: KirClass) {
        beginControlFlow("switch", "self")
        add(
            enumKirClass.enumEntries.map { it.getSwiftBridgeCase(enumKirClass.enum) }.joinToCode("\n", suffix = "\n"),
        )
        endControlFlow("switch")
    }

    private fun KirEnumEntry.getSwiftBridgeCase(enum: SirClass): CodeBlock {
        val typeName = enum.defaultType.evaluate().swiftPoetTypeName

        return CodeBlock.of(
            "case .%N: return %T.%N as %T",
            sirEnumEntry.name,
            typeName,
            sirEnumEntry.name,
            typeName,
        )
    }

    private fun CodeBlock.Builder.addNonEmptyFromObjectiveCBody(enumKirClass: KirClass) {
        val enum = enumKirClass.enum

        if (enumKirClass.enumEntries.isNotEmpty()) {
            enumKirClass.enumEntries.forEachIndexed { index, entry ->
                addFromObjectiveCBodyCase(entry, enum, index)
            }

            nextControlFlow("else")
            addStatement("%L", fatalErrorFromObjectiveC(enum))
            endControlFlow("if")
        } else {
            add(fatalErrorFromObjectiveC(enum))
        }
    }

    private fun CodeBlock.Builder.addFromObjectiveCBodyCase(entry: KirEnumEntry, enum: SirClass, index: Int) {
        val typeName = enum.defaultType.evaluate().swiftPoetTypeName

        val controlFlowCode = "source == %T.%N as %T"
        val controlFlowArguments = arrayOf(typeName, entry.sirEnumEntry.name, typeName)

        if (index == 0) {
            beginControlFlow("if", controlFlowCode, *controlFlowArguments)
        } else {
            nextControlFlow("else if", controlFlowCode, *controlFlowArguments)
        }

        addStatement("return .%N", entry.sirEnumEntry.name)
    }
}
