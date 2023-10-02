package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.PropertySpec

class SirProperty(
    var name: String,
    parent: SirDeclarationParent,
    var type: SirType,
    var visibility: SirVisibility = SirVisibility.Public,
    override var scope: SirScope = SirScope.Member,
    overriddenDeclarations: List<SirProperty> = emptyList(),
    attributes: List<String> = emptyList(),
) : SirDeclaration, SirDeclarationWithScope, SirOverridableDeclaration<SirProperty>,
    SirElementWithSwiftPoetBuilderModifications<PropertySpec.Builder> {

    override val parent: SirDeclarationParent by sirDeclarationParent(parent)

    var getter: SirGetter? = null
        private set

    var setter: SirSetter? = null
        private set

    override val memberOwner: SirClass?
        get() = when (val parent = parent) {
            is SirClass -> parent
            is SirExtension -> parent.classDeclaration
            else -> null
        }

    override val overriddenDeclarations: MutableList<SirProperty> = overriddenDeclarations.toMutableList()

    val attributes: MutableList<String> = attributes.toMutableList()

    override val swiftPoetBuilderModifications = mutableListOf<PropertySpec.Builder.() -> Unit>()

    fun setGetterInternal(getter: SirGetter) {
        this.getter = getter
    }

    fun setSetterInternal(setter: SirSetter) {
        this.setter = setter
    }

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            name: String,
            type: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            scope: SirScope = SirScope.Member,
            overriddenDeclarations: List<SirProperty> = emptyList(),
            attributes: List<String> = emptyList(),
        ): SirProperty =
            SirProperty(
                name = name,
                parent = this@SirDeclarationParent,
                type = type,
                visibility = visibility,
                scope = scope,
                overriddenDeclarations = overriddenDeclarations,
                attributes = attributes,
            )
    }
}
