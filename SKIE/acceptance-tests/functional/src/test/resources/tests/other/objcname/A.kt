@file:Suppress("UNRESOLVED_REFERENCE")
@file:OptIn(ExperimentalObjCName::class)

package `tests`.`other`.`objcname`

import kotlin.experimental.ExperimentalObjCName

@ObjCName("SomeClass")
class A

@ObjCName("SomeEnum")
enum class E {

    @ObjCName("someCase")
    q;

    @ObjCName("someProperty")
    val property: A = A()

    @ObjCName("someFunction")
    fun foo(@ObjCName("someParameter") a: A) {
    }
}
