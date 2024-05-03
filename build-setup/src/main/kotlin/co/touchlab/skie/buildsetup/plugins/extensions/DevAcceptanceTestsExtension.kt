package co.touchlab.skie.buildsetup.plugins.extensions

import co.touchlab.skie.gradle.version.target.Target
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import javax.inject.Inject

open class DevAcceptanceTestsExtension @Inject constructor(private val objects: ObjectFactory) {

    private val testsMap = mutableMapOf<Target, DomainObjectSet<Test>>()

    fun getTests(target: Target): DomainObjectSet<Test> =
        testsMap.getOrPut(target) {
            objects.domainObjectSet(Test::class.java)
        }
}

val Project.devAcceptanceTests: DevAcceptanceTestsExtension
    get() = extensions.getByType()
