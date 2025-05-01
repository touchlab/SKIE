/*
 * Copyright 2018 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.swiftpoet

class AttributeSpec internal constructor(builder: Builder) : Taggable(builder.tags.toImmutableMap()) {

    internal val identifier = builder.identifier
    internal val arguments = builder.arguments

    internal fun emit(out: CodeWriter): CodeWriter {
        out.emit("@")
        out.emitCode(identifier)
        if (arguments.isNotEmpty()) {
            out.emit("(")
            out.emit(arguments.joinToString())
            out.emit(")")
        }
        return out
    }

    class Builder internal constructor(val identifier: CodeBlock) : Taggable.Builder<Builder>() {

        internal val arguments = mutableListOf<String>()

        fun addArgument(code: String): Builder = apply {
            arguments += code
        }

        fun addArguments(codes: List<String>): Builder = apply {
            arguments += codes
        }

        fun addArguments(vararg codes: String): Builder = apply {
            arguments += codes
        }

        fun build(): AttributeSpec = AttributeSpec(this)
    }

    companion object {

        fun builder(name: String): Builder = Builder(CodeBlock.of("%L", name))

        fun builder(typeName: DeclaredTypeName): Builder = Builder(CodeBlock.of("%T", typeName))

        fun available(vararg platforms: Pair<String, String>): AttributeSpec = builder("available").addArguments(
            platforms.map {
                "${it.first} ${it.second}"
            },
        ).build()

        fun rawBuilder(name: String): Builder = Builder(CodeBlock.of(name))

        val DISCARDABLE_RESULT = builder("discardableResult").build()
        val ESCAPING = builder("escaping").build()
        val CONVENTION_BLOCK = builder("convention").addArgument("block").build()
        val CONVENTION_C = builder("convention").addArgument("c").build()
        val IMPLEMENTATION_ONLY = builder("_implementationOnly").build()
    }
}
