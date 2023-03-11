@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.api.util

import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.name.ClassId

fun nsNumberKindClassIds(): List<ClassId> = NSNumberKind.values().mapNotNull {
    it.mappedKotlinClassId
}
