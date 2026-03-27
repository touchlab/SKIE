package co.touchlab.skie.plugin.libraries.library

data class Component(
    val module: Module,
    val version: String,
) {

    constructor(
        coordinate: String,
    ) : this(
        Module(coordinate.substringBefore(':'), coordinate.substringAfter(':').substringBefore(':')),
        coordinate.substringAfterLast(':'),
    )

    constructor(
        group: String,
        name: String,
        version: String,
    ) : this(Module(group, name), version)

    val coordinate: String = "${module.fqName}:$version"

    override fun toString(): String = coordinate
}
