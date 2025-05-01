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

@file:Suppress("UNCHECKED_CAST")

package io.outfoxx.swiftpoet

import kotlin.reflect.KClass

open class AttributedSpec(val attributes: List<AttributeSpec>, tags: Map<KClass<*>, Any>) : Taggable(tags.toImmutableMap()) {

    /** The builder analogue to [AttributedSpec] types. */
    abstract class Builder<out B : Builder<B>> : Taggable.Builder<B>() {

        /** Mutable list of the current attributes this builder contains. */
        val attributes = mutableListOf<AttributeSpec>()

        fun addAttribute(attribute: AttributeSpec): B = apply {
            this.attributes += attribute
        } as B

        fun addAttribute(name: String, vararg arguments: String): B = apply {
            this.attributes += AttributeSpec.builder(name).addArguments(arguments.toList()).build()
        } as B
    }
}
