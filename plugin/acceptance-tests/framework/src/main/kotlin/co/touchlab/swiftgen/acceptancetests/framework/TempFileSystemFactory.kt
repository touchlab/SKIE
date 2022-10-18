package co.touchlab.swiftgen.acceptancetests.framework

class TempFileSystemFactory {

    fun create(test: TestNode.Test): TempFileSystem =
        TempFileSystem(test.outputPath)
}