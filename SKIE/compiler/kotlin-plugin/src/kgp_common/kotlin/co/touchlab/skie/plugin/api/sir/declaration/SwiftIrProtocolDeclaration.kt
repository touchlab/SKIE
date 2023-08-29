package co.touchlab.skie.plugin.api.sir.declaration

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.applyVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

val ModuleDescriptor.swiftIdentifier: String
    get() = (this.stableName ?: this.name).asStringStripSpecialMarkers().toValidSwiftIdentifier()

sealed interface SwiftIrProtocolDeclaration : SwiftIrExtensibleDeclaration {

    override val swiftGenericExportScope: SwiftGenericExportScope
        get() = SwiftGenericExportScope.None

    sealed class Local : SwiftIrProtocolDeclaration, SwiftIrExtensibleDeclaration.Local {

        abstract override val publicName: SwiftFqName.Local.TopLevel

        override val internalName: SwiftFqName.Local.Nested
            get() = KotlinTypeSwiftModel.StableFqNameNamespace.nested(typealiasName)

        override fun toString(): String = "local protocol: $publicName"

        sealed class KotlinInterface : Local() {

            protected abstract val kotlinModule: String
            protected abstract val kotlinFqName: FqName

            override val typealiasName: String
                get() = "class__${kotlinModule}__${kotlinFqName.toValidSwiftIdentifier()}"

            class Modeled(
                private val model: KotlinClassSwiftModel,
                override val superTypes: List<SwiftIrExtensibleDeclaration>,
            ) : KotlinInterface() {

                override val kotlinModule: String
                    get() = model.classDescriptor.module.swiftIdentifier
                override val kotlinFqName: FqName
                    get() = model.classDescriptor.fqNameSafe

                override val publicName: SwiftFqName.Local.TopLevel
                    get() = SwiftFqName.Local.TopLevel(model.identifier.applyVisibility(model.visibility))
            }

            class Immutable(
                override val kotlinModule: String,
                override val kotlinFqName: FqName,
                swiftName: String,
                override val superTypes: List<SwiftIrExtensibleDeclaration>,
            ) : KotlinInterface() {

                override val publicName: SwiftFqName.Local.TopLevel = SwiftFqName.Local.TopLevel(swiftName)
            }
        }

        class SwiftProtocol(
            override val publicName: SwiftFqName.Local.TopLevel,
            override val superTypes: List<SwiftIrExtensibleDeclaration>,
        ) : Local() {

            override val typealiasName: String = "swift__${publicName.asString("_")}"
        }
    }

    class External(
        val module: SwiftIrModule,
        val name: String,
        override val superTypes: List<SwiftIrExtensibleDeclaration> = emptyList(),
    ) : SwiftIrProtocolDeclaration {

        override val publicName: SwiftFqName.External.TopLevel = SwiftFqName.External.TopLevel(module.name, name)

        override val internalName: SwiftFqName.External.TopLevel = publicName

        override fun toString(): String = "external protocol: $publicName"
    }
}
