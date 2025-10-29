plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "co.touchlab.skie"

dependencies {
}

gradlePlugin {
    plugins.register("base") {
        id = "base"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.base.BasePlugin"
    }

    plugins.register("base.root") {
        id = "base.root"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.base.BaseRootPlugin"
    }
}
