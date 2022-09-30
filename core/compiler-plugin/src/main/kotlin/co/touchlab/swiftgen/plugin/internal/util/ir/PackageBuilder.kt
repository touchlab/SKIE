package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.SimpleMemberScope
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.scopes.MemberScope

internal class PackageBuilder(
    packageName: String,
    fileName: String,
    moduleDescriptor: ModuleDescriptor,
) {

    init {
        require("." !in fileName) { "File should be specified without an extension (and cannot have . in its name). Was: $fileName" }
    }

    private val descriptorsWithDeclarationBuilder = mutableMapOf<DeclarationDescriptor, DeclarationBuilder<*, *, *>>()

    val descriptors: Set<DeclarationDescriptor>
        get() = descriptorsWithDeclarationBuilder.keys

    private val packageContent = mutableListOf<DeclarationDescriptor>()

    private val packageFqName = FqName(packageName)
    private val fileName = "$fileName.kt"

    private val packageDescriptor: PackageFragmentDescriptor = SyntheticPackageFragmentDescriptor(
        module = moduleDescriptor,
        fqName = packageFqName,
        memberScope = SimpleMemberScope(packageContent),
    )

    fun <D : DeclarationDescriptor> add(declarationBuilder: DeclarationBuilder<D, *, *>): D {
        val sourceElement = SourceElement { SourceFile { fileName } }

        val descriptor = declarationBuilder.createDescriptor(packageDescriptor, sourceElement)

        descriptorsWithDeclarationBuilder[descriptor] = declarationBuilder
        packageContent.add(descriptor)

        return descriptor
    }

    fun buildPackageDescriptor(): PackageFragmentDescriptor = packageDescriptor

    fun buildFile(moduleFragment: IrModuleFragment): IrFileImpl {
        val fileEntry = DummyIrFileEntry(fileName)
        val fileSymbol = IrFileSymbolImpl(packageDescriptor)

        val file = IrFileImpl(fileEntry, fileSymbol, packageFqName, moduleFragment)

        moduleFragment.files.add(file)

        return file
    }

    fun getBuilder(
        descriptor: DeclarationDescriptor,
    ): DeclarationBuilder<DeclarationDescriptor, IrDeclaration, IrBindableSymbol<DeclarationDescriptor, IrDeclaration>> {
        val declarationBuilder = descriptorsWithDeclarationBuilder[descriptor]

        requireNotNull(declarationBuilder) { "Descriptor $descriptor is not part of package $packageFqName." }

        @Suppress("UNCHECKED_CAST")
        return declarationBuilder as DeclarationBuilder<
                DeclarationDescriptor, IrDeclaration, IrBindableSymbol<DeclarationDescriptor, IrDeclaration>
                >
    }

    private class SyntheticPackageFragmentDescriptor(
        module: ModuleDescriptor,
        fqName: FqName,
        private val memberScope: MemberScope,
    ) : PackageFragmentDescriptorImpl(module, fqName) {

        override fun getMemberScope(): MemberScope = memberScope
    }

    private class DummyIrFileEntry(override val name: String) : IrFileEntry {

        override val maxOffset: Int = 0

        override fun getColumnNumber(offset: Int): Int = 0

        override fun getLineNumber(offset: Int): Int = 0

        override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo = SourceRangeInfo(
            name, 0, 0, 0, -1, 0, 0,
        )
    }
}


