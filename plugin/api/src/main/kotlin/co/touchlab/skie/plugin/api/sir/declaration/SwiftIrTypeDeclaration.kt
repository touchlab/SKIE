package co.touchlab.skie.plugin.api.sir.declaration

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.applyVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinFileSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

sealed interface SwiftIrTypeDeclaration: SwiftIrExtensibleDeclaration {
    val typeParameters: List<SwiftIrTypeParameterDeclaration>

    val containingDeclaration: SwiftIrTypeDeclaration?

    // val defaultType: SirType

    override val swiftGenericExportScope: SwiftGenericExportScope
        get() = SwiftGenericExportScope.FromTypeDeclaration(this)

    sealed class Local(): SwiftIrTypeDeclaration, SwiftIrExtensibleDeclaration.Local {
        abstract override val publicName: SwiftFqName

        override val internalName: SwiftFqName.Local
            get() = KotlinTypeSwiftModel.StableFqNameNamespace.nested(typealiasName)

        override fun toString(): String = "local type: $publicName"

        sealed class KotlinClass: Local() {
            protected abstract val kotlinModule: String
            protected abstract val kotlinFqName: FqName

            override val typealiasName: String
                get() = "class__${kotlinModule}__${kotlinFqName.toValidSwiftIdentifier()}"

            class Modeled(
                private val model: KotlinClassSwiftModel,
            ): KotlinClass() {
                override val kotlinModule: String
                    get() = model.classDescriptor.module.swiftIdentifier
                override val kotlinFqName: FqName
                    get() = model.classDescriptor.fqNameSafe

                override val publicName: SwiftFqName
                    get() = model.identifier.applyVisibility(model.visibility).let { name ->
                        model.containingType?.nonBridgedDeclaration?.publicName?.nested(name) ?: SwiftFqName.Local.TopLevel(name)
                    }

                override val typeParameters: List<SwiftIrTypeParameterDeclaration> = model.classDescriptor.typeConstructor.parameters.map {
                    SwiftIrTypeParameterDeclaration.KotlinTypeParameter(
                        descriptor = it,
                        name = it.name.asString(),
                        bounds = listOf(BuiltinDeclarations.Swift.AnyObject),
                    )
                }

                override val containingDeclaration: SwiftIrTypeDeclaration?
                    // TODO: Instead of runtime crash, it'd be nice if the compiler could catch this.
                    get() = model.containingType?.nonBridgedDeclaration?.let { it as SwiftIrTypeDeclaration }

                override val superTypes: List<SwiftIrExtensibleDeclaration>
                    get() = TODO("Not yet implemented")

                fun toImmutable(): Immutable {
                    return Immutable(
                        kotlinModule = kotlinModule,
                        kotlinFqName = kotlinFqName,
                        swiftName = model.identifier.applyVisibility(model.visibility),
                        containingDeclaration = containingDeclaration,
                    )
                }
            }

            class Immutable(
                override val kotlinModule: String,
                override val kotlinFqName: FqName,
                override val typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList(),
                override val superTypes: List<SwiftIrExtensibleDeclaration> = emptyList(),
                swiftName: String,
                override val containingDeclaration: SwiftIrTypeDeclaration? = null,
            ): KotlinClass() {
                override val publicName: SwiftFqName = containingDeclaration?.publicName?.nested(swiftName) ?: SwiftFqName.Local.TopLevel(swiftName)
            }
        }

        sealed class KotlinFile: Local() {
            protected abstract val kotlinModule: String
            protected abstract val kotlinFileName: String

            override val typealiasName: String
                get() = "file__${kotlinModule}__${kotlinFileName}"

            override val typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList()

            override val superTypes: List<SwiftIrExtensibleDeclaration> = listOf(
                BuiltinDeclarations.Foundation.NSObject,
            )

            class Modeled(
                val model: KotlinFileSwiftModel,
                override val kotlinModule: String,
                override val kotlinFileName: String,
            ): KotlinFile() {

                override val publicName: SwiftFqName
                    get() = model.identifier.applyVisibility(model.visibility).let { name ->
                        model.containingType?.nonBridgedDeclaration?.publicName?.nested(name) ?: SwiftFqName.Local.TopLevel(name)
                    }

                override val containingDeclaration: SwiftIrTypeDeclaration?
                    // TODO: Instead of runtime crash, it'd be nice if the compiler could catch this.
                    get() = model.containingType?.nonBridgedDeclaration?.let { it as SwiftIrTypeDeclaration }

                fun toImmutable(): Immutable {
                    return Immutable(
                        kotlinModule = kotlinModule,
                        kotlinFileName = kotlinFileName,
                        swiftName = model.identifier.applyVisibility(model.visibility),
                        containingDeclaration = containingDeclaration,
                    )
                }
            }

            class Immutable(
                override val kotlinModule: String,
                override val kotlinFileName: String,
                val swiftName: String,
                override val containingDeclaration: SwiftIrTypeDeclaration? = null,
            ): KotlinFile() {
                override val publicName: SwiftFqName
                    get() = containingDeclaration?.publicName?.nested(swiftName) ?: SwiftFqName.Local.TopLevel(swiftName)
            }
        }

        class ObjcType(
            private val swiftName: String,
            override val typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList(),
            override val superTypes: List<SwiftIrExtensibleDeclaration> = emptyList(),
        ): Local() {
            override val typealiasName: String
                get() = error("Not supported!")

            override val internalName: SwiftFqName.Local = SwiftFqName.Local.TopLevel(swiftName)
            override val publicName: SwiftFqName = internalName

            override val containingDeclaration: SwiftIrTypeDeclaration? = null
        }

        class SwiftType(
            private val swiftName: String,
            override val typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList(),
            override val superTypes: List<SwiftIrExtensibleDeclaration> = emptyList(),
            override val containingDeclaration: Local? = null,
        ): Local() {
            override val publicName: SwiftFqName
                get() = containingDeclaration?.publicName?.nested(swiftName) ?: SwiftFqName.Local.TopLevel(swiftName)

            override val typealiasName: String
                get() = "swift__${publicName.asString("_")}"
        }
    }

    class External(
        val module: SwiftIrModule,
        val name: String,
        override val typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList(),
        override val superTypes: List<SwiftIrExtensibleDeclaration> = emptyList(),
        override val containingDeclaration: External? = null,
    ): SwiftIrTypeDeclaration {
        override val publicName: SwiftFqName.External = containingDeclaration?.let {
            SwiftFqName.External.Nested(it.publicName, name)
        } ?: SwiftFqName.External.TopLevel(module.name, name)

        override val internalName: SwiftFqName.External = publicName

        override fun toString(): String = "external type: $publicName"
    }
}
