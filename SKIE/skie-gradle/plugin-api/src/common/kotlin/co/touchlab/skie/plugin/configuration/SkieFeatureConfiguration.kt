@file:Suppress("MemberVisibilityCanBePrivate")

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieFeatureConfiguration @Inject constructor(objects: ObjectFactory) {

    val coroutinesInterop: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * For performance reasons SKIE does not generate default arguments for functions in external libraries even if enabled via the group configuration.
     * This behavior can be overridden by setting this property to true.
     *
     * Warning: Depending on the project this action can have a significant impact the compilation time because it turns off the Kotlin compiler caching.
     *
     * Note that even with this property turned on, it is still required to enable default arguments for individual functions from those libraries.
     * This can be done only via the group configuration from Gradle.
     * To opt in for some functions, use `group("$declarationFqNamePrefix") { DefaultArgumentInterop.Enabled(true) }`.
     * To opt in for all functions globally, use `group { DefaultArgumentInterop.Enabled(true) }`.
     */
    val defaultArgumentsInExternalLibraries: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * SKIE has a **preview** support for converting `suspend` functions into `Combine.Future` types.
     * When enabled,
     * SKIE will extend `Combine.Future` with an initializer accepting any `async` lambda.
     *
     * With it, you can easily convert any `suspend` function into `Combine.Future`.
     * Let's take a look at an example usage. We'll declare a suspending global function `helloWorld`.
     *
     * ```kotlin
     * suspend fun helloWorld(): String {
     *     return "Hello World!"
     * }
     * ```
     *
     * Then in Swift you can use the new `Future` initializer like so:
     *
     * ```swift
     * let future = Future(async: helloWorld)
     *
     * future.sink { error in
     *     // Handle an error throw by the function.
     *     // Note that this could also be `CancellationError`.
     * } receiveValue: { value in
     *     // Value is a `String` "Hello World!"
     *     print(value)
     * }
     * ```
     *
     * NOTE: It's important to understand how Combine and its operators work.
     *       The example above doesn't store the cancellable returned by `sink`,
     *       so it would immediately cancel and `receiveValue` wouldn't get called.
     *       Additionally, Futures are hot and will invoke the provided `async` immediately.
     *       This means Futures don't wait for calling `.sink` on them.
     *       Futures also don't support cancellation.
     */
    val enableFutureCombineExtension: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * SKIE has a **preview** support for converting Flows into `Combine.Publisher` types.
     * When enabled,
     * each Flow bridged to Swift by SKIE will contain a `toPublisher()` method.
     *
     * Use this if you need to interface with *Combine* code and can't use Swift's native `AsyncSequence`.
     * Let's take a look at an example usage. We'll declare a global function `helloWorld` returning a `Flow`.
     *
     * ```kotlin
     * fun helloWorld(): Flow<String> {
     *     return flow {
     *         emit("Hello")
     *         delay(1.seconds)
     *         emit("World")
     *         delay(1.seconds)
     *         emit("!")
     *     }
     * }
     * ```
     *
     * Then in Swift you can use the `toPublisher()` function.
     *
     * ```swift
     * let publisher = helloWorld().toPublisher()
     *
     * publisher.sink { value in
     *     // Value is a `String` type
     *     // Receives with a 1-second delay between them:
     *     // - "Hello"
     *     // - "World"
     *     // - "!"
     * }
     * ```
     *
     * NOTE: It's important to understand how Combine and its operators work.
     *       The example above doesn't store the cancellable returned by `sink`,
     *       so it would immediately cancel and `receiveValue` wouldn't get called.
     */
    val enableFlowCombineConvertor: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal val groupConfigurations = mutableListOf<GroupConfiguration>()

    fun group(targetFqNamePrefix: String = "", overridesAnnotations: Boolean = false, action: GroupConfiguration.() -> Unit) {
        val groupConfiguration = GroupConfiguration(targetFqNamePrefix, overridesAnnotations)

        groupConfigurations.add(groupConfiguration)

        groupConfiguration.action()
    }

    class GroupConfiguration(
        internal val targetFqNamePrefix: String,
        internal val overridesAnnotations: Boolean,
    ) {

        internal val items = mutableMapOf<String, String?>()

        operator fun <T> ConfigurationKey<T>.invoke(value: T) {
            items[this.name] = this.serialize(value)
        }
    }

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> =
        setOfNotNull(
            SkieConfigurationFlag.Feature_CoroutinesInterop takeIf coroutinesInterop,
            SkieConfigurationFlag.Feature_DefaultArgumentsInExternalLibraries takeIf defaultArgumentsInExternalLibraries,
            SkieConfigurationFlag.Feature_FlowCombineConvertor takeIf enableFlowCombineConvertor,
            SkieConfigurationFlag.Feature_FutureCombineExtension takeIf enableFutureCombineExtension,
        )
}
