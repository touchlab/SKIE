package co.touchlab.skie.util.directory.structure

abstract class TemporaryDirectory(parent: Directory, name: String) : SubDirectory(parent, name) {

    override val isTemporary: Boolean = true
}
