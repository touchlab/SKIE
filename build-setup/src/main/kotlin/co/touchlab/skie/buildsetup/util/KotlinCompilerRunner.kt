package co.touchlab.skie.buildsetup.util

import java.io.File
import java.io.PrintStream
import java.lang.reflect.Method
import java.net.URLClassLoader

class KotlinCompilerRunner(
    classpath: Collection<File>,
) {

    private val classLoader = createIsolatedClassLoaderWithCompilerEmbeddable(classpath)

    private val messageRendererClass = classLoader.loadClass("org.jetbrains.kotlin.cli.common.messages.MessageRenderer")
    private val messageRenderer = getGradleStyleMessageRenderer(messageRendererClass)

    private val exitCodeClass = classLoader.loadClass("org.jetbrains.kotlin.cli.common.ExitCode")
    private val codeMethod = getCodeMethod(exitCodeClass)

    fun compile(compilerClassFqName: String, arguments: Array<String>) {
        val compilerClass = classLoader.loadClass(compilerClassFqName)
        val compiler = getK2NativeCompilerInstance(compilerClass)
        val execMethod = getExecMethod(compilerClass, messageRendererClass)

        val exitCode = execMethod.invoke(compiler, System.err, messageRenderer, arguments)

        val exitCodeValue = codeMethod.invoke(exitCode) as Int

        if (exitCodeValue != 0) {
            error("Compilation failed. Exit code: $exitCodeValue")
        }
    }

    private fun createIsolatedClassLoaderWithCompilerEmbeddable(classpath: Collection<File>): ClassLoader {
        val embeddableClasspath = classpath.map { it.toURI().toURL() }.toTypedArray()

        return URLClassLoader(embeddableClasspath)
    }

    private fun getK2NativeCompilerInstance(compilerClass: Class<*>): Any =
        compilerClass.getDeclaredConstructor().newInstance()

    private fun getExecMethod(compilerClass: Class<*>, messageRendererClass: Class<*>): Method =
        compilerClass.getMethod(
            "exec",
            PrintStream::class.java,
            messageRendererClass,
            Array<String>::class.java,
        )

    private fun getGradleStyleMessageRenderer(messageRendererClass: Class<*>): Any =
        messageRendererClass.getField("GRADLE_STYLE").get(null)

    private fun getCodeMethod(exitCodeClass: Class<*>): Method =
        exitCodeClass.getMethod("getCode")
}
