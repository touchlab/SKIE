package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.KobjcTransforms
import co.touchlab.swiftpack.spec.KotlinFileReference
import co.touchlab.swiftpack.spec.KotlinFunctionReference
import co.touchlab.swiftpack.spec.KotlinPropertyReference
import co.touchlab.swiftpack.spec.KotlinTypeReference

fun List<KobjcTransforms>.merge(): KobjcTransforms {
    val types = this.flatMap { it.types.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val files = this.flatMap { it.files.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val properties = this.flatMap { it.properties.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val functions = this.flatMap { it.functions.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }

    return KobjcTransforms(types, files, properties, functions)
}

fun List<KobjcTransforms.TypeTransform>.merge(reference: KotlinTypeReference): KobjcTransforms.TypeTransform {
    val properties = this.flatMap { it.properties.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val functions = this.flatMap { it.methods.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }

    return KobjcTransforms.TypeTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
        bridge = mapNotNull { it.bridge }.singleOrNull(),
        properties = properties,
        methods = functions,
    )
}

fun List<KobjcTransforms.FileTransform>.merge(reference: KotlinFileReference): KobjcTransforms.FileTransform {
    return KobjcTransforms.FileTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
        bridge = mapNotNull { it.bridge }.singleOrNull(),
    )
}

fun List<KobjcTransforms.PropertyTransform>.merge(reference: KotlinPropertyReference): KobjcTransforms.PropertyTransform {
    return KobjcTransforms.PropertyTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
    )
}

fun List<KobjcTransforms.FunctionTransform>.merge(reference: KotlinFunctionReference): KobjcTransforms.FunctionTransform {
    return KobjcTransforms.FunctionTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
    )
}
