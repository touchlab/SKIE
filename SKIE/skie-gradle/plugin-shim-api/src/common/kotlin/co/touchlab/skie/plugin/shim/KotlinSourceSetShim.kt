package co.touchlab.skie.plugin.shim

interface KotlinSourceSetShim {

    val name: String

    val dependsOn: Set<KotlinSourceSetShim>
}
