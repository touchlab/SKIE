package co.touchlab.skie.test.util

sealed interface KotlinTargetOrPreset {
    val id: String
}


fun KotlinTargetOrPreset(id: String): KotlinTargetOrPreset {
    fun KotlinTargetOrPreset.findById(id: String): KotlinTargetOrPreset? {
        return when {
            this.id == id -> this
            this is KotlinTarget.Preset -> this.children.firstNotNullOfOrNull { it.findById(id) }
            else -> null
        }
    }

    return checkNotNull(KotlinTarget.Preset.Root.findById(id)) {
        "Could not find target or preset with id '$id'"
    }
}

val List<KotlinTargetOrPreset>.targets: List<KotlinTarget>
    get() = flatMap {
        when (it) {
            is KotlinTarget.Preset -> it.targets
            is KotlinTarget -> listOf(it)
        }
    }

val List<KotlinTargetOrPreset>.presets: List<KotlinTarget.Preset>
    get() = flatMap {
        when (it) {
            is KotlinTarget.Preset -> listOf(it) +it.children.presets
            is KotlinTarget -> emptyList()
        }
    }
