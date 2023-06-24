@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.gradle.version.target

import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.applyUserDefinedAttributes

fun KotlinTarget.applyUserDefinedAttributes() {
    applyUserDefinedAttributes(this as AbstractKotlinTarget)
}
