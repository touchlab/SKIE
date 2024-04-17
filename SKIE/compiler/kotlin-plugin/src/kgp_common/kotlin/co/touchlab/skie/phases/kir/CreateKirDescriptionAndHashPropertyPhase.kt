@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.configuration.PropertyConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.translation.withTypeParameterScope
import co.touchlab.skie.kir.util.addOverrides
import co.touchlab.skie.oir.type.PrimitiveOirType
import co.touchlab.skie.phases.KirCompilerPhase
import co.touchlab.skie.phases.KirPhase
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class CreateKirDescriptionAndHashPropertyPhase(
    context: KirCompilerPhase.Context,
) : BaseCreateKirMembersPhase(context) {

    context(KirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Migration_AnyMethodsAsFunctions.isDisabled

    private val cache = mutableMapOf<FunctionDescriptor, KirProperty>()

    context(KirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach {
            createDescriptionProperty(it)
        }
    }

    context(KirPhase.Context)
    private fun createDescriptionProperty(kirClass: KirClass) {
        if (kirClass in kirProvider.kirBuiltins.builtinClasses) {
            // TODO Implement accurate way to generate builtin members once needed
            return
        }

        val classDescriptor = descriptorKirProvider.findClassDescriptor(kirClass) ?: return

        classDescriptor.findSpecialFunction(OperatorNameConventions.TO_STRING)?.let {
            getOrCreateProperty(it, kirClass, kirBuiltins.NSString.defaultType)
        }

        classDescriptor.findSpecialFunction(OperatorNameConventions.HASH_CODE)?.let {
            getOrCreateProperty(it, kirClass, OirBasedKirType(PrimitiveOirType.NSConvertedUInteger))
        }
    }

    private fun getOrCreateProperty(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        type: KirType,
    ): KirProperty =
        cache.getOrPut(descriptor.original) {
            createProperty(descriptor, kirClass, type)
        }

    private fun createProperty(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        type: KirType,
    ): KirProperty {
        val baseDescriptor = descriptor.baseFunction

        kirClass.withTypeParameterScope {
            val property = KirProperty(
                kotlinName = descriptor.name.asString(),
                kotlinSignature = descriptor.signature,
                objCName = namer.getSelector(baseDescriptor),
                swiftName = namer.getSwiftName(baseDescriptor).removeSuffix("()"),
                owner = kirClass,
                origin = KirCallableDeclaration.Origin.Member,
                scope = kirClass.callableDeclarationScope,
                isFakeOverride = if (kirClass == kirProvider.kirBuiltins.Base) {
                    // TODO Solves issue with methods from Any which are technically a fake override - remove once the builtins have correct members
                    true
                } else {
                    !descriptor.kind.isReal
                },
                type = type,
                isVar = false,
                deprecationLevel = descriptor.kirDeprecationLevel,
                isRefinedInSwift = baseDescriptor.isRefinedInSwift,
                configuration = getFunctionConfiguration(descriptor),
            )

            descriptorKirProvider.registerCallableDeclaration(property, descriptor)

            getDirectParents(descriptor)
                .map { getOrCreateOverriddenProperty(it, type) }
                .let { property.addOverrides(it) }

            return property
        }
    }

    private fun getFunctionConfiguration(descriptor: FunctionDescriptor): PropertyConfiguration {
        check(descriptor is SimpleFunctionDescriptor) {
            "$descriptor is expected to be a SimpleFunctionDescriptor."
        }

        val functionConfiguration = descriptorConfigurationProvider.getConfiguration(descriptor)

        val propertyConfiguration = PropertyConfiguration(functionConfiguration.parent)

        propertyConfiguration.overwriteBy(functionConfiguration)

        return propertyConfiguration
    }

    private fun getOrCreateOverriddenProperty(descriptor: FunctionDescriptor, type: KirType): KirProperty {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported property $descriptor")

        val kirClass = descriptorKirProvider.getClass(classDescriptor)

        return getOrCreateProperty(descriptor, kirClass, type)
    }

    private fun ClassDescriptor.findSpecialFunction(name: Name): FunctionDescriptor? =
        unsubstitutedMemberScope
            .getContributedFunctions(name, NoLookupLocation.FROM_BACKEND)
            .firstOrNull { isToStringOrEquals(it) }

    companion object {

        fun isToStringOrEquals(descriptor: FunctionDescriptor): Boolean =
            descriptor is SimpleFunctionDescriptor &&
                (descriptor.name == OperatorNameConventions.TO_STRING || descriptor.name == OperatorNameConventions.HASH_CODE) &&
                descriptor.valueParameters.isEmpty() &&
                descriptor.isInheritedFromAny

        private val FunctionDescriptor.isInheritedFromAny: Boolean
            get() {
                val containingClass = containingDeclaration as? ClassDescriptor ?: return false

                if (containingClass.fqNameSafe.asString() == "kotlin.Any") {
                    return true
                }

                return overriddenDescriptors.any { it.isInheritedFromAny }
            }
    }
}
