package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.com.intellij.openapi.util.NotNullLazyValue

internal class LockBasedLazyValueReflector(
    override val instance: NotNullLazyValue<*>,
) : Reflector("org.jetbrains.kotlin.storage.LockBasedStorageManager\$LockBasedLazyValue") {

    val value by declaredProperty<Any>()
}
