package co.touchlab.skie.plugin.api.kotlin

interface MutableDescriptorProvider : DescriptorProvider {

    fun mutate(block: DescriptorRegistrationScope.() -> Unit)

    /**
     * Register a listener that will be called each time the provider is mutated.
     */
    fun onMutated(listener: () -> Unit)
}
