package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFileProvider
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

// Instantiate only in SirFileProvider
class SirIrFile(
    override val module: SirModule.Skie,
    // Relative to the SKIE Swift generated directory
    val relativePath: Path,
) : SirFile,
    SirTopLevelDeclarationParent {

    val fileNameWithoutSuffix: String
        get() = relativePath.nameWithoutExtension

    constructor(
        namespace: String,
        baseName: String,
        module: SirModule.Skie,
    ) : this(
        module = module,
        relativePath = SirFileProvider.relativePath(namespace, baseName),
    )

    val imports: MutableList<String> = mutableListOf()

    override val parent: SirDeclarationParent?
        get() = null

    init {
        module.files.add(this)
    }

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: $relativePath"
}
