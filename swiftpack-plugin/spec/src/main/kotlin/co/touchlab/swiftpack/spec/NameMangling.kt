package co.touchlab.swiftpack.spec

object NameMangling {
    val String.mangledClassName: String
        get() {
            return this
                .replace("_", "__")
                .replace(".", "_p")
        }

    val String.demangledClassName: String
        get() {
            return this
                .replace("__|_p".toRegex()) {
                    if (it.value == "_p") {
                        "."
                    } else {
                        "_"
                    }
                }
                // .replace("P_P_", "P_")
        }
}
