package co.touchlab.skie.util.directory.structure

abstract class SubDirectory(
    parent: Directory,
    name: String,
) : Directory(parent, parent.directory.resolve(name)) {

    override fun addChild(child: Directory) {
        parent?.addChild(child)
    }
}
