package co.touchlab.swiftpack.api

interface SwiftTypeName {
    val parent: SwiftTypeName?
    val separator: String
    val simpleName: String
    val originalSimpleName: String

    val originalQualifiedName: String
    val qualifiedName: String
}
