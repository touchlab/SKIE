package co.touchlab.skie.acceptancetests.framework.util

val isCI: Boolean
    get() = System.getenv("CI") != null
