package co.touchlab.skie.compilerinject.reflection

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Reflector {

    private val reflectedClass: Class<*>

    protected abstract val instance: Any

    constructor(reflectedClass: Class<*>) {
        this.reflectedClass = reflectedClass
    }

    constructor(reflectedClass: KClass<*>) : this(reflectedClass.java)

    constructor(fqName: String) {
        reflectedClass = this::class.java.classLoader.loadClass(fqName)
    }

    protected inline fun <reified R> declaredMethod0() =
        Provider { DeclaredMethod0(it, R::class.java) }

    protected inline fun <reified P1, reified R> declaredMethod1() =
        Provider {
            DeclaredMethod1(it, P1::class.java, R::class.java)
        }

    protected inline fun <reified P1, reified P2, reified R> declaredMethod2() =
        Provider {
            DeclaredMethod2(it, P1::class.java, P2::class.java, R::class.java)
        }

    protected inline fun <reified P1, reified P2, reified P3, reified R> declaredMethod3() =
        Provider {
            DeclaredMethod3(it, P1::class.java, P2::class.java, P3::class.java, R::class.java)
        }

    protected inline fun <reified P1, reified R : Reflector> reflectedDeclaredMethod1() =
        Provider {
            ReflectedMethod<(P1) -> Any, (P1) -> R>(DeclaredMethod1(it, P1::class.java, Any::class.java)) { invoke ->
                { param ->
                    invoke(param).reflectedBy()
                }
            }
        }

    protected inline fun <reified T> declaredProperty() =
        Provider { DeclaredProperty(it, T::class.java) }

    protected inline fun <reified T> declaredField(nameOverride: String? = null) =
        Provider { DeclaredField(nameOverride ?: it, T::class.java) }

    protected inline fun <reified R> extensionFunction0(extensionClassFqName: String) =
        Provider { ExtensionFunction0(it, extensionClassFqName, R::class.java) }

    protected inline fun <reified P1, reified R> extensionFunction1(extensionClassFqName: String) =
        Provider { ExtensionFunction1(it, extensionClassFqName, P1::class.java, R::class.java) }

    protected inline fun <reified P1, reified R> extensionFunction1(extensionClassFqName: String, functionName: String) =
        Provider { ExtensionFunction1(functionName, extensionClassFqName, P1::class.java, R::class.java) }

    protected inline fun <reified T> extensionProperty(extensionClassFqName: String) =
        Provider { ExtensionProperty(it, extensionClassFqName, T::class.java) }

    protected class Provider<T>(private val factory: (String) -> T) : PropertyDelegateProvider<Reflector, T> {

        override fun provideDelegate(thisRef: Reflector, property: KProperty<*>): T = factory(property.name)
    }

    protected abstract inner class DeclaredMethod<T, R>(
        name: String,
        private val returnType: Class<R>,
        parameterTypes: Array<Class<*>>,
    ) : ReadOnlyProperty<Reflector, T> {

        private val method by lazy {
            reflectedClass.getDeclaredMethod(name, *parameterTypes).also { it.isAccessible = true }
        }

        protected fun invoke(arguments: Array<Any?>): R =
            method.invoke(instance, *arguments).let { returnType.cast(it) }
    }

    protected inner class DeclaredMethod0<R>(
        name: String,
        returnType: Class<R>,
    ) : DeclaredMethod<() -> R, R>(name, returnType, emptyArray()) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): () -> R = {
            invoke(emptyArray())
        }
    }

    protected inner class DeclaredMethod1<P1, R>(
        name: String,
        param1: Class<P1>,
        returnType: Class<R>,
    ) : DeclaredMethod<(P1) -> R, R>(name, returnType, arrayOf(param1)) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): (P1) -> R = {
            invoke(arrayOf(it))
        }
    }

    protected inner class DeclaredMethod2<P1, P2, R>(
        name: String,
        param1: Class<P1>,
        param2: Class<P2>,
        returnType: Class<R>,
    ) : DeclaredMethod<(P1, P2) -> R, R>(name, returnType, arrayOf(param1, param2)) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): (P1, P2) -> R = { p1, p2 ->
            invoke(arrayOf(p1, p2))
        }
    }

    protected inner class DeclaredMethod3<P1, P2, P3, R>(
        name: String,
        param1: Class<P1>,
        param2: Class<P2>,
        param3: Class<P3>,
        returnType: Class<R>,
    ) : DeclaredMethod<(P1, P2, P3) -> R, R>(name, returnType, arrayOf(param1, param2, param3)) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): (P1, P2, P3) -> R = { p1, p2, p3 ->
            invoke(arrayOf(p1, p2, p3))
        }
    }

    protected inner class ReflectedMethod<WRAPPED, T>(
        private val method: DeclaredMethod<WRAPPED, Any>,
        private val reflectorFactory: (invoke: WRAPPED) -> T,
    ) : ReadOnlyProperty<Reflector, T> {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T {
            return method.getValue(thisRef, property).let(reflectorFactory)
        }
    }

    protected inner class DeclaredProperty<T>(name: String, type: Class<T>) : ReadWriteProperty<Reflector, T> {

        private val getter by lazy {
            DeclaredMethod0("get" + name.replaceFirstChar { it.uppercase() }, type)
        }
        private val setter by lazy {
            DeclaredMethod1("set" + name.replaceFirstChar { it.uppercase() }, type, Unit::class.java)
        }

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T =
            getter.getValue(thisRef, property).invoke()

        override fun setValue(thisRef: Reflector, property: KProperty<*>, value: T) {
            setter.getValue(thisRef, property).invoke(value)
        }
    }

    protected inner class DeclaredField<T>(name: String, private val type: Class<T>) : ReadWriteProperty<Reflector, T> {

        private val field by lazy {
            reflectedClass.getDeclaredField(name).also { it.isAccessible = true }
        }

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T =
            field.get(instance).let { type.cast(it) }

        override fun setValue(thisRef: Reflector, property: KProperty<*>, value: T) {
            field.set(instance, value)
        }
    }

    protected abstract inner class ExtensionFunction<T, R>(
        name: String,
        extensionClassFqName: String,
        private val returnType: Class<R>,
        parameterTypes: Array<Class<*>>,
    ) : ReadOnlyProperty<Reflector, T> {

        private val method by lazy {
            val extensionClass = this::class.java.classLoader.loadClass(extensionClassFqName)

            extensionClass.getDeclaredMethod(name, reflectedClass, *parameterTypes).also { it.isAccessible = true }
        }

        protected operator fun invoke(arguments: Array<Any?>): R =
            method.invoke(null, *arguments).let { returnType.cast(it) }
    }

    protected inner class ExtensionFunction0<R>(
        name: String,
        extensionClassFqName: String,
        returnType: Class<R>,
    ) : ExtensionFunction<() -> R, R>(name, extensionClassFqName, returnType, emptyArray()) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): () -> R = {
            invoke(arrayOf(instance))
        }
    }

    protected inner class ExtensionFunction1<P1, R>(
        name: String,
        extensionClassFqName: String,
        param1: Class<P1>,
        returnType: Class<R>,
    ) : ExtensionFunction<(P1) -> R, R>(name, extensionClassFqName, returnType, arrayOf(param1)) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): (P1) -> R = {
            invoke(arrayOf(instance, it))
        }
    }

    protected inner class ExtensionProperty<T>(
        name: String,
        extensionClassFqName: String,
        type: Class<T>,
    ) : ReadWriteProperty<Reflector, T> {

        private val getter by lazy {
            ExtensionFunction0("get" + name.replaceFirstChar { it.uppercase() }, extensionClassFqName, type)
        }
        private val setter by lazy {
            ExtensionFunction1("set" + name.replaceFirstChar { it.uppercase() }, extensionClassFqName, type, Unit::class.java)
        }

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T =
            getter.getValue(thisRef, property).invoke()

        override fun setValue(thisRef: Reflector, property: KProperty<*>, value: T) {
            setter.getValue(thisRef, property).invoke(value)
        }
    }
}
