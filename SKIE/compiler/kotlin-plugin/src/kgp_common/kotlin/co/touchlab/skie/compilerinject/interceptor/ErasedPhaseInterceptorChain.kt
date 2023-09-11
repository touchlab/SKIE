package co.touchlab.skie.compilerinject.interceptor

typealias OriginalPhaseBody<Context, Input, Output> = (Context, Input) -> Output
typealias ErasedPhaseInterceptor<Context, Input, Output> = (Context, Input, OriginalPhaseBody<Context, Input, Output>) -> Output

internal class ErasedPhaseInterceptorChain<Context, Input, Output>(
    interceptors: List<PhaseInterceptor<Context, Input, Output>>,
) : ErasedPhaseInterceptor<Context, Input, Output> {

    // We need to get rid of the `PhaseInterceptor` type as it's not available between different class loaders
    private val chainedInterceptors: ErasedPhaseInterceptor<Context, Input, Output> by lazy {
        val erasedInterceptors: Sequence<ErasedPhaseInterceptor<Context, Input, Output>> = interceptors.asSequence().map { it::intercept }
        erasedInterceptors.reduce { acc, next ->
            acc then next
        }
    }

    override fun invoke(context: Context, input: Input, original: OriginalPhaseBody<Context, Input, Output>): Output {
        return chainedInterceptors(context, input, original)
    }
}

internal infix fun <Context, Input, Output> ErasedPhaseInterceptor<Context, Input, Output>.then(
    next: ErasedPhaseInterceptor<Context, Input, Output>,
): ErasedPhaseInterceptor<Context, Input, Output> {
    return { outerContext, outerInput, original ->
        this.invoke(outerContext, outerInput) { innerContext, innerInput ->
            next.invoke(innerContext, innerInput, original)
        }
    }
}
