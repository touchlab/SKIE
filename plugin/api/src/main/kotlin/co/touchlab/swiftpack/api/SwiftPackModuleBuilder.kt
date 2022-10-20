package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.api.internal.InternalReferenceContext
import co.touchlab.swiftpack.api.internal.InternalTemplateVariableContext
import co.touchlab.swiftpack.api.internal.InternalTransformContext
import co.touchlab.swiftpack.api.internal.impl.DefaultReferenceContext
import co.touchlab.swiftpack.api.internal.impl.DefaultTemplateVariableContext
import co.touchlab.swiftpack.api.internal.impl.DefaultTransformContext
import co.touchlab.swiftpack.spec.module.SwiftPackModule
import io.outfoxx.swiftpoet.FileSpec
import java.io.File

class SwiftPackModuleBuilder internal constructor(
    private val moduleName: String,
    private val referenceContext: InternalReferenceContext = DefaultReferenceContext(),
    private val templateVariableContext: InternalTemplateVariableContext = DefaultTemplateVariableContext(),
    private val transformContext: InternalTransformContext = DefaultTransformContext(),
): ReferenceContext by referenceContext, TemplateVariableContext by templateVariableContext, TransformContext by transformContext {

    private val mutableFiles = mutableSetOf<FileSpec>()
    val files: Set<FileSpec> get() = mutableFiles

    fun file(name: String, contents: FileSpec.Builder.() -> Unit): FileSpec {
        val builder = FileSpec.builder(name)
        builder.contents()
        val file = builder.build()
        mutableFiles.add(file)
        return file
    }

    fun addFile(file: FileSpec): SwiftPackModuleBuilder {
        mutableFiles.add(file)
        return this
    }

    fun build(): SwiftPackModule {
        return SwiftPackModule(
            name = SwiftPackModule.Name.Simple(moduleName),
            templateVariables = templateVariableContext.variables.toList(),
            references = referenceContext.references,
            files = files.map {
                SwiftPackModule.TemplateFile(
                    name = it.name,
                    contents = it.toString(),
                )
            }.sortedBy { it.name },
            transforms = emptyList(),
        )
    }

    object Config {
        private val storage = ThreadLocal<File?>()

        var outputDir: File?
            get() = storage.get()
            set(value) {
                storage.set(value)
            }
    }
}
