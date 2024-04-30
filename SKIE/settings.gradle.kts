import java.util.Properties

rootProject.name = "SKIE"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    includeBuild("../build-setup-settings")
    includeBuild("../build-setup")
}

plugins {
    id("dev.settings")
}

val skieProperties = Properties()
file("skie.gradle.properties").inputStream().use {
    skieProperties.load(it)
}
skieProperties.stringPropertyNames().forEach { propertyName ->
    val propertyValue = skieProperties.getProperty(propertyName)
    if (propertyValue != null) {
        extra[propertyName] = propertyValue
    }
}

@Suppress("UNUSED_VARIABLE", "LocalVariableName")
buildSetup {
    val common by module {
        val analytics by module
        val configuration by group {
            val api by module
            val annotations by module
            val declaration by module
        }
        val util by module
    }

    val runtime by group {
        val kotlin by module
        val swift by module
    }

    val `kotlin-compiler` by group {
        val core by module
        val `linker-plugin` by module
    }

    val gradle by group("skie-gradle") {
        val plugin by module
        val `plugin-shim-api` by module
        val `plugin-shim-impl` by module
    }

    val `acceptance-tests` by module {
        val `acceptance-tests-framework` by module("framework")

        val `test-dependencies` by module {
            val `regular-dependency` by module("regular")
            val `exported-dependency` by module("exported")
        }
    }
}
