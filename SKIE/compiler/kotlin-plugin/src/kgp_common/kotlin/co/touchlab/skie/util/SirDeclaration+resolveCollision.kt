@file:Suppress("UNCHECKED_CAST")

package co.touchlab.skie.util

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirElement
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.classDescriptorOrNull
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeDeclaration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

context(SirPhase.Context)
fun <T : SirTypeDeclaration> T.resolveCollisionWithWarning(collisionReasonProvider: T.() -> String?): Boolean =
    resolveCollisionWithWarning(collisionReasonProvider) {
        baseName += "_"
    }

context(SirPhase.Context)
fun <T : SirCallableDeclaration> T.resolveCollisionWithWarning(collisionReasonProvider: T.() -> String?): Boolean =
    when (val declaration = this as SirCallableDeclaration) {
        is SirConstructor -> declaration.resolveCollisionWithWarning(collisionReasonProvider as SirConstructor.() -> String?)
        is SirSimpleFunction -> declaration.resolveCollisionWithWarning(collisionReasonProvider as SirSimpleFunction.() -> String?)
        is SirProperty -> declaration.resolveCollisionWithWarning(collisionReasonProvider as SirProperty.() -> String?)
    }

context(SirPhase.Context)
fun SirConstructor.resolveCollisionWithWarning(collisionReasonProvider: SirConstructor.() -> String?): Boolean =
    resolveCollisionWithWarning(collisionReasonProvider) {
        val lastValueParameter = valueParameters.lastOrNull()
            ?: error("Cannot resolve collision for $this because it does not have any value parameters.")

        lastValueParameter.label = lastValueParameter.labelOrName + "_"
    }

context(SirPhase.Context)
fun SirProperty.resolveCollisionWithWarning(collisionReasonProvider: SirProperty.() -> String?): Boolean =
    resolveCollisionWithWarning(collisionReasonProvider) {
        identifier += "_"
    }

context(SirPhase.Context)
fun SirSimpleFunction.resolveCollisionWithWarning(collisionReasonProvider: SirSimpleFunction.() -> String?): Boolean =
    resolveCollisionWithWarning(collisionReasonProvider) {
        identifier += "_"
    }

context(SirPhase.Context)
fun SirEnumCase.resolveCollisionWithWarning(collisionReasonProvider: SirEnumCase.() -> String?): Boolean =
    resolveCollisionWithWarning(
        collisionReasonProvider = collisionReasonProvider,
        rename = { simpleName += "_" },
        getName = { parent.fqName.toLocalString() + "." + simpleName },
        findKirElement = { kirProvider.findClass(parent)?.enumEntries?.get(index) },
        getDescriptor = { descriptor },
    )

context(SirPhase.Context)
private inline fun <T : SirCallableDeclaration> T.resolveCollisionWithWarning(
    collisionReasonProvider: T.() -> String?,
    rename: () -> Unit,
): Boolean =
    resolveCollisionWithWarning(
        collisionReasonProvider = collisionReasonProvider,
        rename = rename,
        getName = { name },
        findKirElement = {
            kirProvider.findCallableDeclaration<SirCallableDeclaration>(this)
                ?: if (this is SirProperty) kirProvider.findEnumEntry(this) else null
        },
        getDescriptor = {
            when (this) {
                is KirCallableDeclaration<*> -> descriptor
                is KirEnumEntry -> descriptor
                else -> null
            }
        },
    )

context(SirPhase.Context)
private inline fun <T : SirTypeDeclaration> T.resolveCollisionWithWarning(
    collisionReasonProvider: T.() -> String?,
    rename: () -> Unit,
): Boolean =
    resolveCollisionWithWarning(
        collisionReasonProvider = collisionReasonProvider,
        rename = rename,
        getName = { fqName.toLocalString() },
        findKirElement = { (this as? SirClass)?.let { kirProvider.findClass(it) } },
        getDescriptor = { classDescriptorOrNull },
    )

context(SirPhase.Context)
private inline fun <T, K : KirElement> T.resolveCollisionWithWarning(
    collisionReasonProvider: T.() -> String?,
    rename: () -> Unit,
    getName: T.() -> String,
    findKirElement: T.() -> K?,
    getDescriptor: K.() -> DeclarationDescriptor?,
): Boolean {
    val originalName = getName()

    val collisionReason = resolveCollision(collisionReasonProvider, rename) ?: return false

    val newName = getName()

    val kirElement = findKirElement()
    if (kirElement != null) {
        reportCollision(originalName, newName, collisionReason, kirElement.getDescriptor())
    }

    return true
}

private inline fun <T> T.resolveCollision(collisionReasonProvider: T.() -> String?, rename: () -> Unit): String? {
    val firstCollisionReason = collisionReasonProvider()

    var nextCollisionReason: String? = firstCollisionReason
    while (nextCollisionReason != null) {
        rename()

        nextCollisionReason = collisionReasonProvider()
    }

    return firstCollisionReason
}

private fun SirPhase.Context.reportCollision(
    originalName: String,
    newName: String,
    collisionReason: String,
    declarationDescriptor: DeclarationDescriptor?,
) {
    reporter.warning(
        message = "'$originalName' was renamed to '$newName' because of a name collision with $collisionReason. " +
            "Consider resolving the conflict either by changing the name in Kotlin, or via the @ObjCName annotation. " +
            "Using renamed declarations from Swift is not recommended because their name will change if the conflict is resolved.",
        declaration = declarationDescriptor,
    )
}
