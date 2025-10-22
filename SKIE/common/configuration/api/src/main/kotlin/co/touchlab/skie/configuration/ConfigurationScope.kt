package co.touchlab.skie.configuration

interface ConfigurationScope {

    interface Global : ConfigurationScope

    interface Module : ConfigurationScope

    interface Package : ConfigurationScope

    interface File : ConfigurationScope

    interface Class : ConfigurationScope

    interface Constructor : ConfigurationScope

    interface SimpleFunction : ConfigurationScope

    interface Property : ConfigurationScope

    interface ValueParameter : ConfigurationScope

    interface Function : SimpleFunction, Constructor

    interface CallableDeclaration : Function, Property

    interface AllExceptCallableDeclarations : Global, Module, Package, File, Class

    interface AllExceptConstructorsAndProperties : AllExceptCallableDeclarations, SimpleFunction

    interface All : AllExceptConstructorsAndProperties, CallableDeclaration, ValueParameter
}
