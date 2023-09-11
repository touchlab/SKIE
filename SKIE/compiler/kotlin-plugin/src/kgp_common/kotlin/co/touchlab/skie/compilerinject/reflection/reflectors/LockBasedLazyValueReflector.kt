package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.com.intellij.openapi.util.NotNullLazyValue

class LockBasedLazyValueReflector(
    override val instance: NotNullLazyValue<*>,
) : Reflector("org.jetbrains.kotlin.storage.LockBasedStorageManager\$LockBasedLazyValue") {

    val value by declaredProperty<Any>()
}
