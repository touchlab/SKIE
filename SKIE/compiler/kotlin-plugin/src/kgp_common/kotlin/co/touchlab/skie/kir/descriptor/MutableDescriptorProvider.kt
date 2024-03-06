package co.touchlab.skie.kir.descriptor

interface MutableDescriptorProvider : DescriptorProvider {

    fun mutate(block: DescriptorRegistrationScope.() -> Unit)

    fun recalculateExports()

    /**
     * Register a listener that will be called each time the provider is mutated.
     */
    fun onMutated(listener: () -> Unit)
}
