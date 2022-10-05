package tests.enums.with_members.properties

import co.touchlab.swiftgen.api.EnumInterop

enum class A {
    A1 {
        override val abstractImmutableProperty: Int
            get() = 1

        override var abstractMutableProperty: Int = 0
    },
    A2 {
        override val abstractImmutableProperty: Int
            get() = 2

        override var abstractMutableProperty: Int = 1
            get() = field
            set(value) {
                field = value * 2
            }

        override val overridableImmutableProperty: Int = 1

        override var overridableMutableProperty: Int = 1
            get() = field
            set(value) {
                field = value * 2
            }
    };

    val immutableProperty: Int = 0

    var mutableProperty: Int = 0

    abstract val abstractImmutableProperty: Int

    abstract var abstractMutableProperty: Int

    open val overridableImmutableProperty: Int = 0

    open var overridableMutableProperty: Int = 0

    // TODO: See if throwing properties are supported in Kotlin/Native
    // val throwingGetterProperty: Int
    //     @Throws(Exception::class)
    //     get() = throw Exception()
    //
    // var throwingSetterProperty: Int = 0
    //     @Throws(Exception::class)
    //     set(value) {
    //         throw Exception()
    //     }
}

fun a1(): A {
    return A.A1
}

fun a2(): A {
    return A.A2
}
