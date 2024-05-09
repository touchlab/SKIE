package co.touchlab.skie.plugin.util

data class DarwinTarget(
//     val konanTarget: KonanTarget,
    val konanTargetName: String,
    val targetTriple: TargetTriple,
    val sdk: String,
) {

    constructor(
//         konanTarget: KonanTarget,
        konanTargetName: String,
        targetTripleString: String,
        sdk: String,
    ) : this(
//         konanTarget,
        konanTargetName,
        TargetTriple.fromString(targetTripleString), sdk,
    )

    companion object {

        lateinit var allTargets: Map<String, DarwinTarget>
    }
}
