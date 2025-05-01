package co.touchlab.skie.oir.element

// Instantiate only in OirProvider
sealed interface OirModule : OirElement {

    class Kotlin(val name: String) : OirModule {

        val files: MutableList<OirFile> = mutableListOf()

        override fun toString(): String = "OirModule.${this::class.simpleName}: $name"
    }

    class External :
        OirModule,
        OirTopLevelDeclarationParent {

        override val module: OirModule
            get() = this

        override var declarations: MutableList<OirTopLevelDeclaration> = mutableListOf()

        override fun toString(): String = "OirModule.${this::class.simpleName}"
    }
}
