package co.touchlab.skie.plugin.api.kotlin

interface MutableDescriptorProvider: DescriptorProvider {
    fun mutate(block: DescriptorRegistrationScope.() -> Unit)

    /*
     * Disables registration of new descriptors. This has to be called before any descriptors are
     * used for further computations that may be cached.
     */
    fun preventFurtherMutations(): DescriptorProvider

    /**
     * Register a listener that will be called each time the provider is mutated.
     */
    fun onMutated(listener: () -> Unit)
}
