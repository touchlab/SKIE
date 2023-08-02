package co.touchlab.skie.analytics.declaration

import kotlinx.serialization.Serializable

// Project size
// Declarations (separately classes and functions) x exported/internal x origin (library, project)
//
// val librariesNames: List<Library>
// val modules: List<Module>

@Serializable
data class Library(
    val uniqueNames: List<String>,
    val version: String,
    val isExported: Boolean,
)

@Serializable
data class Module(
    val id: String,
    val isExported: Boolean,
    val numberOfIRElements: Int,
)
