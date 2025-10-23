package co.touchlab.skie.buildsetup.tests.extensions

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

open class LibraryTestsExtension @Inject constructor(private val objects: ObjectFactory) {

    val lockFile: RegularFileProperty = objects.fileProperty()

    val tests: NamedDomainObjectSet<Test> = objects.namedDomainObjectSet(Test::class.java)

    fun addTest(name: String, configuration: Test.() -> Unit) {
        val test = Test(name, objects)

        configuration(test)

        tests.add(test)
    }

    open class Test(
        private val nameProperty: String,
        objects: ObjectFactory,
    ) : Named {

        val description: Property<String> = objects.property(String::class.java)

        val systemFlags: SetProperty<String> = objects.setProperty(String::class.java)

        val systemProperties: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

        override fun getName(): String = nameProperty
    }
}
