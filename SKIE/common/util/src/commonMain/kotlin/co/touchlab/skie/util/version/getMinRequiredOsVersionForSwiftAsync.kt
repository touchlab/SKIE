package co.touchlab.skie.util.version

fun getMinRequiredOsVersionForSwiftAsync(konanTargetName: String): String =
    when (konanTargetName) {
        "ios_arm32",
        "ios_arm64",
        "ios_simulator_arm64",
        "ios_x64",
        -> "13.0"
        "macos_arm64",
        "macos_x64",
        -> "10.15"
        "tvos_arm64",
        "tvos_simulator_arm64",
        "tvos_x64",
        -> "13.0"
        "watchos_arm32",
        "watchos_arm64",
        "watchos_device_arm64",
        "watchos_simulator_arm64",
        "watchos_x64",
        "watchos_x86",
        -> "6.0"
        else -> error("Unsupported target: $konanTargetName")
    }
