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

import kotlin.reflect.KClass

/** A type that can be tagged with extra metadata of the user's choice. */
abstract class Taggable(
  /** all tags. */
  val tags: Map<KClass<*>, Any>,
) {

  /** Returns the tag attached with [type] as a key, or null if no tag is attached with that key. */
  inline fun <reified T : Any> tag(type: KClass<T>): T? = tags[type] as T?

  /** Returns the tag attached with [type] as a key, or null if no tag is attached with that key. */
  inline fun <reified T : Any> tag(type: Class<T>): T? = tag(type.kotlin)

  /** The builder analogue to [Taggable] types. */
  abstract class Builder<out B : Builder<B>> {

    /** Mutable map of the current tags this builder contains. */
    val tags: MutableMap<KClass<*>, Any> = mutableMapOf()

    /**
     * Attaches [tag] to the request using [type] as a key. Tags can be read from a
     * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
     * [type].
     *
     * Use this API to attach originating elements, debugging, or other application data to a spec
     * so that you may read it in other APIs or callbacks.
     */
    fun <T : Any> tag(type: Class<T>, tag: T?): B = tag(type.kotlin, tag)

    /**
     * Attaches [tag] to the request using [type] as a key. Tags can be read from a
     * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
     * [type].
     *
     * Use this API to attach originating elements, debugging, or other application data to a spec
     * so that you may read it in other APIs or callbacks.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> tag(type: KClass<T>, tag: T?): B = apply {
      if (tag == null) {
        this.tags.remove(type)
      } else {
        this.tags[type] = tag
      }
    } as B
  }
}

/** Returns the tag attached with [T] as a key, or null if no tag is attached with that key. */
inline fun <reified T : Any> Taggable.tag(): T? = tag(T::class)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */

inline fun <reified T : Any> AttributeSpec.Builder.tag(tag: T?): AttributeSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */

inline fun <reified T : Any> EnumerationCaseSpec.Builder.tag(tag: T?): EnumerationCaseSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */

inline fun <reified T : Any> ExtensionSpec.Builder.tag(tag: T?): ExtensionSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */

inline fun <reified T : Any> FileMemberSpec.Builder.tag(tag: T?): FileMemberSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
inline fun <reified T : Any> FileSpec.Builder.tag(tag: T?): FileSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
inline fun <reified T : Any> FunctionSpec.Builder.tag(tag: T?): FunctionSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */

inline fun <reified T : Any> ImportSpec.Builder.tag(tag: T?): ImportSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
inline fun <reified T : Any> ParameterSpec.Builder.tag(tag: T?): ParameterSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
inline fun <reified T : Any> PropertySpec.Builder.tag(tag: T?): PropertySpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
inline fun <reified T : Any> TypeAliasSpec.Builder.tag(tag: T?): TypeAliasSpec.Builder =
  tag(T::class, tag)

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a
 * request using [Taggable.tag]. Use `null` to remove any existing tag assigned for
 * [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
inline fun <reified T : Any> TypeSpec.Builder.tag(tag: T?): TypeSpec.Builder =
  tag(T::class, tag)
