package co.touchlab.swiftpack.spec

import co.touchlab.swiftpack.spec.NameMangling.demangledClassName
import co.touchlab.swiftpack.spec.NameMangling.mangledClassName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NameManglingTest {

    @Test
    fun `test mangled name`() {
        val names = listOf(
            "co.touchlab.P_P",
            "co.touchlab.PPP_PPP",
            "co.touchlab.PP0_P0P",
            "co.touchlab._p0_P0__P",
            "co.touchlab_p._p0_P0__P",
            "co.touchlab_p__._p0_P0__P",
        )

        names.forEach { name ->
            assertEquals(name, name.mangledClassName.demangledClassName)
        }
    }
}
