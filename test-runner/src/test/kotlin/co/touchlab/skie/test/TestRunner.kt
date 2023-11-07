package co.touchlab.skie.test

import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.util.AnnotationUtils.isAnnotated
import org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations
import java.util.stream.Stream
import javax.print.attribute.standard.MediaSize.Other
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@TestTemplate
@ExtendWith(SkieTestRunner::class)
@ExtendWith(SmokeTestCondition::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SkieTest {

}

enum class TestLevel {
    Smoke,
    Thorough,
}
//
// val linkMode: LinkMode = EnvDefaults.linkMode ?: LinkMode.Static,
// val buildConfiguration: BuildConfiguration = EnvDefaults.buildConfiguration ?: BuildConfiguration.Debug,
// val target: Target = EnvDefaults.target ?: Target.current,

enum class BuildConfiguration {
    Debug,
    Release,
}

enum class BinaryTarget(val kotlinName: String) {
    IOS_ARM64("ios_arm64"),
    IOS_X64("ios_x64"),
    IOS_SIMULATOR_ARM64("ios_simulator_arm64"),
    MACOS_ARM64("macos_arm64"),
    MACOS_X64("macos_x64"),
    ;

    val sdk: String
        get() = when (this) {
            IOS_ARM64 -> "iphoneos"
            IOS_X64 -> "iphonesimulator"
            IOS_SIMULATOR_ARM64 -> "iphonesimulator"
            MACOS_ARM64 -> "macosx"
            MACOS_X64 -> "macosx"
        }

//     val targetTriple: TargetTriple
//         get() = when (this) {
//             IOS_ARM64 -> TargetTriple("arm64", "apple", "ios13.0", null)
//             IOS_X64 -> TargetTriple("x86_64", "apple", "ios13.0", null)
//             IOS_SIMULATOR_ARM64 -> TargetTriple("arm64", "apple", "ios13.0", "simulator")
//             MACOS_ARM64 -> TargetTriple("arm64", "apple", "macos10.15", null)
//             MACOS_X64 -> TargetTriple("x86_64", "apple", "macos10.15", null)
//         }
//
//     companion object {
//
//         val current: BinaryTarget by lazy {
//             val possibleTargets = mapOf(
//                 "arm64" to MACOS_ARM64,
//                 "x86_64" to MACOS_X64,
//             )
//             val systemName = "uname -m".execute().stdOut.trim()
//
//             possibleTargets[systemName] ?: error("Unsupported architecture: $systemName")
//         }
//     }
}

enum class LinkMode {
    Dynamic,
    Static,
}

data class SkieTestMatrix(
    val axes: List<Axis<*>>,
) {
    val values = axes.associateBy { it.type }

    fun <T> mapCells(transformCell: (List<AxisValue>) -> T): List<T> {
        val cartesianProduct = axes.fold(listOf(emptyList<AxisValue>())) { acc, axis ->
            acc.flatMap { list ->
                axis.values.map { element ->
                    list + AxisValue(axis.type, axis.name, element)
                }
            }
        }

        return cartesianProduct.map(transformCell)
    }

    data class Axis<T: Any>(
        val type: Class<T>,
        val name: String,
        val values: List<T>,
    ) {
        companion object {
            inline operator fun <reified T: Any> invoke(name: String, values: List<T>): Axis<T> {
                return Axis(
                    type = T::class.java,
                    name = name,
                    values = values,
                )
            }
        }
    }

    data class AxisValue(
        val type: Class<*>,
        val name: String,
        val value: Any,
    )
}

object SkieTestMatrixConfiguration {

    val testLevel: TestLevel by singleValue { TestLevel.Thorough }
    val targets: List<BinaryTarget> by multipleValues()
    val configurations: List<BuildConfiguration> by multipleValues()
    val linkModes: List<LinkMode> by multipleValues()

    fun filteredAxes(filter: OnlyFor) = listOf(
        SkieTestMatrix.Axis<BinaryTarget>("Target", targets.intersectOrKeepIfEmpty(filter.targets)),
        SkieTestMatrix.Axis<BuildConfiguration>("Configuration", configurations.intersectOrKeepIfEmpty(filter.configurations)),
        SkieTestMatrix.Axis<LinkMode>("Linkage", linkModes.intersectOrKeepIfEmpty(filter.linkModes)),
    ).associateBy { it.type }



//     private operator fun <E: Enum<E>> getValue(skieTestMatrix: SkieTestMatrix, property: KProperty<*>): E {
// //         return System.getProperty(property.name, "").toBoolean()
//         TODO()
//     }

    private inline fun <reified E: Enum<E>> singleValue(crossinline default: () -> E) = PropertyDelegateProvider { _: SkieTestMatrixConfiguration, property ->
        val properties = System.getProperties()
        val value = if (properties.containsKey(property.name)) {
            val rawValue = properties.getProperty(property.name)
            enumValueOf<E>(rawValue)
        } else {
            default()
        }
        PropertyStorage(value)
    }

    private inline fun <reified E: Enum<E>> multipleValues(crossinline default: () -> List<E> = { enumValues<E>().toList() }) = PropertyDelegateProvider { _: SkieTestMatrixConfiguration, property ->
        val properties = System.getProperties()
        val values = if (properties.containsKey(property.name)) {
            val rawValue = properties.getProperty(property.name)
            rawValue.split(',').map {
                enumValueOf<E>(it)
            }

        } else {
            default()
        }
        PropertyStorage(values)
    }

    private fun <T> List<T>.intersectOrKeepIfEmpty(other: Array<T>): List<T> {
        return if (other.isNotEmpty()) {
            val otherAsSet = other.toSet()
            filter(otherAsSet::contains)
        } else {
            this
        }
    }

    private data class PropertyStorage<T>(val value: T): ReadOnlyProperty<SkieTestMatrixConfiguration, T> {
        override operator fun getValue(thisRef: SkieTestMatrixConfiguration, property: KProperty<*>): T = value
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Smoke

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Smoke
annotation class SmokeOnly

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class OnlyFor(
    val targets: Array<BinaryTarget> = [],
    val configurations: Array<BuildConfiguration> = [],
    val linkModes: Array<LinkMode> = [],
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(targets = [BinaryTarget.IOS_SIMULATOR_ARM64, BinaryTarget.IOS_ARM64, BinaryTarget.IOS_X64])
annotation class OnlyIos

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(targets = [BinaryTarget.MACOS_ARM64, BinaryTarget.MACOS_X64])
annotation class OnlyMacos

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(configurations = [BuildConfiguration.Debug])
annotation class OnlyDebug

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(configurations = [BuildConfiguration.Release])
annotation class OnlyRelease

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(linkModes = [LinkMode.Static])
annotation class OnlyStatic

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(linkModes = [LinkMode.Dynamic])
annotation class OnlyDynamic



class SmokeTestCondition: ExecutionCondition {
    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return when (SkieTestMatrixConfiguration.testLevel) {
            TestLevel.Smoke -> if (isAnnotated(context.element, Smoke::class.java)) {
                ConditionEvaluationResult.enabled("${context.element} is marked as @Smoke test")
            } else {
                ConditionEvaluationResult.disabled("${context.element} is not marked as @Smoke test")
            }
            TestLevel.Thorough -> if (isAnnotated(context.element, SmokeOnly::class.java)) {
                ConditionEvaluationResult.disabled("${context.element} is marked as @SmokeOnly test")
            } else {
                ConditionEvaluationResult.enabled("${context.element} is not marked as @SmokeOnly test")
            }
        }
    }
}

class SkieMatrixExtension(
    private val runValues: Map<Class<*>, Any>,
): ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return runValues.containsKey(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return checkNotNull(runValues[parameterContext.parameter.type]) {
            "Value for type ${parameterContext.parameter.type} not available! Check if `true` was returned for it in `supportsParameter`."
        }
    }
}

class SkieTestRunner: TestTemplateInvocationContextProvider {
    override fun supportsTestTemplate(context: ExtensionContext): Boolean {
        return isAnnotated(context.testMethod, SkieTest::class.java)
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> {
        val testMethod = context.requiredTestMethod
        val matrixFilter = findRepeatableAnnotations(testMethod, OnlyFor::class.java).fold(OnlyFor()) { acc, onlyFor ->
            OnlyFor(
                targets = acc.targets.intersectOrChoose(onlyFor.targets),
                configurations = acc.configurations.intersectOrChoose(onlyFor.configurations),
                linkModes = acc.linkModes.intersectOrChoose(onlyFor.linkModes),
            )
        }
        val filteredAxes = SkieTestMatrixConfiguration.filteredAxes(matrixFilter)

        val matrixAxes = testMethod.parameterTypes.map { requestedAxisType ->
            checkNotNull(filteredAxes[requestedAxisType]) {
                "Parameter of type $requestedAxisType not supported!"
            }
        }

        val matrix = SkieTestMatrix(axes = matrixAxes)

        return matrix.mapCells {
            SkieTestMatrixContext(it) as TestTemplateInvocationContext
        }.stream()
    }

    private companion object {

        inline fun <reified T> Array<T>.intersectOrChoose(other: Array<T>): Array<T> {
            return when {
                this.isEmpty() -> other
                other.isEmpty() -> this
                else -> {
                    val otherAsSet = other.toSet()
                    filter(otherAsSet::contains).toTypedArray()
                }
            }
        }
    }
}

class SkieTestMatrixContext(
    private val axisValues: List<SkieTestMatrix.AxisValue>,
): TestTemplateInvocationContext {
    private val runValues = axisValues.associate { it.type to it.value }

    override fun getDisplayName(invocationIndex: Int): String {
        val nameWithoutIndex = if (axisValues.isNotEmpty()) {
            axisValues.joinToString(", ") { it.value.toString() }
        } else {
            "No arguments"
        }
        return "[$invocationIndex]: $nameWithoutIndex"
    }

    override fun getAdditionalExtensions(): List<Extension> {
        return listOf(
            SkieMatrixExtension(runValues),
        )
    }
}
