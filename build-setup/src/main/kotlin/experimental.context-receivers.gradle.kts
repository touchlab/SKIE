// import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

afterEvaluate {
    tasks.withType<KotlinCompile<*>>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
        }
    }
}
