package co.touchlab.skie.test.util

sealed interface KotlinTarget: KotlinTargetOrPreset {
    sealed interface Preset: KotlinTargetOrPreset {
        val children: List<KotlinTargetOrPreset>
        val targets: List<KotlinTarget>
            get() = children.flatMap {
                when (it) {
                    is Preset -> it.targets
                    is KotlinTarget -> listOf(it)
                    else -> error("WTF?")
                }
            }

        data object Root: Preset {
            override val id: String = "all"
            override val children = listOf(Native, Jvm, AndroidTarget)
        }

        sealed interface Native: Preset {
            override val targets: List<KotlinTarget.Native>

            sealed interface Darwin: Native {
                override val targets: List<KotlinTarget.Native.Darwin>
            }
        }
    }

    sealed interface Native: KotlinTarget {
        companion object: Preset {
            override val id: String = "native"
            override val children = listOf(Darwin) // + ...
        }
        sealed interface Darwin: Native {
            val triple: String
            val sdk: String
            val frameworkTarget: String

            companion object: Preset.Native {
                override val id: String = "darwin"
                override val children = listOf(
                    Ios,
                    Tvos,
                    MacOS,
                )
                override val targets: List<Darwin>
                    get() = children.flatMap {
                        it.targets
                    }
            }
        }

        enum class Ios(
            override val id: String,
            override val triple: String,
            override val sdk: String,
            override val frameworkTarget: String,
        ): Darwin {
            X64("iosX64", "x86_64-apple-ios17.0-simulator", "iphonesimulator", "ios_x64"),
            Arm64("iosArm64", "arm64-apple-ios17.0", "iphoneos", "ios_arm64"),
            SimulatorArm64("iosSimulatorArm64", "arm64-apple-ios17.0-simulator", "iphonesimulator", "ios_simulator_arm64");

            override fun toString(): String = id

            companion object: Preset.Native.Darwin {
                override val id: String = "ios"
                override val children: List<Ios> = entries
                override val targets: List<Ios> = entries

                override fun toString(): String = "iOS"
            }
        }

        enum class Tvos(
            override val id: String,
            override val triple: String,
            override val sdk: String,
            override val frameworkTarget: String,
        ): Darwin {
            X64("tvosX64", "x86_64-apple-tvos17.0-simulator", "appletvsimulator", "tvos_x64"),
            Arm64("tvosArm64", "arm64-apple-tvos17.0", "appletvos", "tvos_arm64"),
            SimulatorArm64("tvosSimulatorArm64", "arm64-apple-tvos17.0-simulator", "appletvsimulator", "tvos_simulator_arm64");

            override fun toString(): String = id

            companion object: Preset.Native.Darwin {
                override val id: String = "tvos"
                override val children: List<Tvos> = entries
                override val targets: List<Tvos> = entries

                override fun toString(): String = "tvOS"
            }
        }

        enum class MacOS(
            override val id: String,
            override val triple: String,
            override val sdk: String,
            override val frameworkTarget: String,
        ): Darwin {
            X64("macosX64", "x86_64-apple-macos10.15", "macosx", "macos_x64"),
            Arm64("macosArm64", "arm64-apple-macos10.15", "macosx", "macos_arm64");

            override fun toString(): String = id

            companion object: Preset.Native.Darwin {
                override val id: String = "macos"
                override val children: List<MacOS> = entries

                override val targets: List<MacOS> = entries
                override fun toString(): String = "macOS"
            }
        }
    }

    data object Jvm: KotlinTarget {
        override val id: String = "jvm"
    }

    data object AndroidTarget: KotlinTarget {
        override val id: String = "androidTarget"
    }
}
