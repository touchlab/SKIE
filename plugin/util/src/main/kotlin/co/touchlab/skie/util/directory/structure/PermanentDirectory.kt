package co.touchlab.skie.util.directory.structure

abstract class PermanentDirectory(
    parent: Directory,
    name: String,
) : SubDirectory(parent, name) {

        override val isTemporary: Boolean = false
}
