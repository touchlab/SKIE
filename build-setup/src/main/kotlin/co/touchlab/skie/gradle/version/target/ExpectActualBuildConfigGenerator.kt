package co.touchlab.skie.gradle.version.target

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.internal.impldep.org.apache.commons.lang.ClassUtils

class ExpectActualBuildConfigGenerator(
    @get:Input var topLevelConstants: Boolean = false,
    @get:Input var internalVisibility: Boolean = true,
    @get:Input var isActualImplementation: Boolean,
) : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map {
        val typeName = when (it.type.get()) {
            "String" -> String::class.asClassName()
            else -> runCatching { ClassName.bestGuess(it.type.get()) }
                .getOrElse { _ -> ClassUtils.getClass(it.type.get(), false).asTypeName() }
        }.copy(nullable = it.optional.get())

        return@map PropertySpec.builder(it.name, typeName, kModifiers)
            .apply {
                if (isActualImplementation) {
                    initializer("%L", it.value.get())
                }
            }
            .build()
    }

    override fun execute(spec: BuildConfigGeneratorSpec) {
        logger.debug("Generating {} for fields {}", spec.className, spec.fields)

        val fields = spec.fields.asPropertiesSpec()

        FileSpec.builder(spec.packageName, spec.className)
            .addFields(fields)
            .build()
            .writeTo(spec.outputDir)
    }

    private fun FileSpec.Builder.addFields(fields: List<PropertySpec>): FileSpec.Builder = when {
        topLevelConstants -> fields.fold(this, FileSpec.Builder::addProperty)
        else -> addType(
            TypeSpec.objectBuilder(name)
                .addModifiers(kModifiers)
                .addProperties(fields)
                .build(),
        )
    }

    private val kModifiers
        get() = listOf(
            if (internalVisibility) KModifier.INTERNAL else KModifier.PUBLIC,
            if (isActualImplementation) KModifier.ACTUAL else KModifier.EXPECT,
        )
}
