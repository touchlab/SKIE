package co.touchlab.skie.phases.runtime

import co.touchlab.skie.phases.SirPhase

object FutureCombineExtensionGenerator {
    context(SirPhase.Context)
    fun generate() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("Combine.Future+asyncInit").content = """
            import Combine

            extension Combine.Future where Failure == Swift.Error {
                /**
                 A convenience initializer that can be used to convert Swift async throwing functions into Combine Futures.

                 - parameter async: An async throwing closure, or an async throwing function reference to back this Future.
                */
                public convenience init(async function: @escaping () async throws -> Output) {
                    self.init { promise in
                        _Concurrency.Task {
                            do {
                                let result = try await function()
                                promise(.success(result))
                            } catch {
                                promise(.failure(error))
                            }
                        }
                    }
                }
            }

            extension Combine.Future where Failure == Swift.Never {
                /**
                 A convenience initializer that can be used to convert Swift async functions into Combine Futures.

                 - parameter async: An async closure, or an async function reference to back this Future.
                */
                public convenience init(async function: @escaping () async -> Output) {
                    self.init { promise in
                        _Concurrency.Task {
                            let result = await function()
                            promise(.success(result))
                        }
                    }
                }
            }
        """.trimIndent()
    }
}
