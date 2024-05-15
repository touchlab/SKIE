package co.touchlab.skie.configuration.provider

import co.touchlab.skie.configuration.ConfigurationScope
import co.touchlab.skie.configuration.ConfigurationTarget
import kotlin.reflect.KClass

interface IdentifiedConfigurationTarget : ConfigurationTarget {

    val fqName: String

    val belongsToSkieRuntime: Boolean

    val scopeType: KClass<out ConfigurationScope>

    object Root : IdentifiedConfigurationTarget {

        override val belongsToSkieRuntime: Boolean = false

        override val fqName: String = ""

        override val scopeType: KClass<out ConfigurationScope> = ConfigurationScope.Root::class

        override fun hasAnnotation(kClass: KClass<out Annotation>): Boolean = false

        override fun <T : Annotation> findAnnotation(kClass: KClass<T>): T? = null
    }

    interface Module : IdentifiedConfigurationTarget {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.Module::class
    }

    interface Package : IdentifiedConfigurationTarget {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.Package::class

        val parent: Module
    }

    sealed interface FileOrClass : IdentifiedConfigurationTarget

    interface File : FileOrClass {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.File::class

        val parent: Package
    }

    sealed interface ValueParameterParent : IdentifiedConfigurationTarget {

        val parent: FileOrClass
    }

    interface Class : ValueParameterParent, FileOrClass {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.Class::class
    }

    interface Constructor : ValueParameterParent {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.Constructor::class
    }

    interface SimpleFunction : ValueParameterParent {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.SimpleFunction::class
    }

    interface Property : ValueParameterParent {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.Property::class
    }

    interface ValueParameter : IdentifiedConfigurationTarget {

        override val scopeType: KClass<out ConfigurationScope>
            get() = ConfigurationScope.ValueParameter::class

        val parent: ValueParameterParent
    }
}
