package co.touchlab.skie.test.template

import java.io.File

object Templates {
    val basic = buildTemplate("basic") {
        kotlin("BasicSkieFeatures")
        swift("main")
    }
}
