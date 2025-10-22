package co.touchlab.skie.phases.runtime

import co.touchlab.skie.phases.SirPhase

object FlowCombineConversionGenerator {

    context(SirPhase.Context)
    fun generate() {
        generateSkieCombineSubscription()

        generateSkieFlowPublisher()

        generateToPublisherExtension()

        generatePublisherSinkExtension()
    }

    context(SirPhase.Context)
    private fun generateSkieCombineSubscription() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieCombineSubscription").content = """
                import Combine

                internal final actor SkieCombineSubscription<F: SkieSwiftFlowProtocol<S.Input>, S: Combine.Subscriber>: Combine.Subscription where S.Failure == _Concurrency.CancellationError {
                    private var currentDemand: Combine.Subscribers.Demand = .none
                    private var onDemandUpdated: _Concurrency.CheckedContinuation<Swift.Void, Swift.Never>? = nil
                    private var collectingTask: _Concurrency.Task<Swift.Void, Swift.Never>?

                    init(flow: F, subscriber: S) {
                        _Concurrency.Task {
                            await self.startCollectingTask(flow: flow, subscriber: subscriber)
                        }
                    }

                    nonisolated func request(_ demand: Combine.Subscribers.Demand) {
                        _Concurrency.Task {
                            await add(demand: demand)
                        }
                    }

                    nonisolated func cancel() {
                        _Concurrency.Task {
                            _ = await collectingTask?.cancel()
                        }
                    }

                    private func startCollectingTask(flow: F, subscriber: S) {
                        collectingTask = _Concurrency.Task {
                            await _Concurrency.withTaskCancellationHandler {
                                await waitForDemand()

                                // If we get cancelled, tell subscriber we failed with CancellationError
                                guard !_Concurrency.Task.isCancelled else {
                                    subscriber.receive(completion: .failure(_Concurrency.CancellationError()))
                                    return
                                }

                                do {
                                    for try await element in flow {
                                        let newDemand = subscriber.receive(element)
                                        add(demand: newDemand)
                                        await waitForDemand()
                                    }
                                } catch {
                                    Swift.fatalError("Collecting a SKIE flow threw an error. This isn't expected to happen and is a bug in SKIE. Error: \(error)")
                                }

                                // If we get cancelled, tell subscriber we failed with CancellationError
                                guard !_Concurrency.Task.isCancelled else {
                                    subscriber.receive(completion: .failure(_Concurrency.CancellationError()))
                                    return
                                }

                                // We should only get here if the underlying flow has completed
                                subscriber.receive(completion: .finished)
                            } onCancel: { [weak self] in
                                guard let self else { return }
                                _Concurrency.Task.detached {
                                    await self.notifyDemandUpdated()
                                }
                            }
                        }
                    }

                    private func waitForDemand() async {
                        // If we're cancelled, we'll just return.
                        while !_Concurrency.Task.isCancelled {
                            // If there's still demand, we decrement it and return right away.
                            if currentDemand > 0 {
                                currentDemand -= 1
                                return
                            }

                            // Otherwise we store continuation and wait for it to be invoked.
                            await _Concurrency.withCheckedContinuation { continuation in
                                onDemandUpdated = continuation
                            }

                            // Once  we get notified, we the next loop will decrement the demand, or keep waiting if there's no demand
                        }
                    }

                    private func add(demand: Combine.Subscribers.Demand) {
                        currentDemand += demand
                        notifyDemandUpdated()
                    }

                    private func notifyDemandUpdated() {
                        let onDemandUpdated = self.onDemandUpdated
                        self.onDemandUpdated = nil
                        onDemandUpdated?.resume()
                    }

                    deinit {
                        /*
                         * We shouldn't need this, as the Task strongly references this instance,
                         * so it either doesn't exist anymore (is canceled), or this deinit can't be called.
                         */
                        onDemandUpdated?.resume()
                    }
                }
            """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateSkieFlowPublisher() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieFlowPublisher").content = """
                import Combine

                internal struct SkieFlowPublisher<Output, F: SkieSwiftFlowProtocol<Output>>: Combine.Publisher {
                    typealias Failure = _Concurrency.CancellationError

                    let flow: F

                    func receive<S>(
                        subscriber: S
                    ) where S : Combine.Subscriber, _Concurrency.CancellationError == S.Failure, Output == S.Input {
                        let subscription = SkieCombineSubscription(flow: flow, subscriber: subscriber)
                        subscriber.receive(subscription: subscription)
                    }
                }
            """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateToPublisherExtension() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieSwiftFlowProtocol+toPublisher").content = """
                import Combine

                extension SkieSwiftFlowProtocol {
                    /**
                     Returns a Published from this Flow. This publisher can fail with a ``CancellationError`` when the underlying flow is cancelled from Kotlin.

                     - Returns: A publisher instance, which you can use Combine operators with. It's cold and won't start collecting the backing flow until a subscriber is attached.
                    */
                    public func toPublisher() -> some Combine.Publisher<Element, _Concurrency.CancellationError> {
                        SkieFlowPublisher(flow: self)
                    }
                }
            """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generatePublisherSinkExtension() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("Combine.Publisher+sink").content = """
            import Combine

            extension Combine.Publisher where Self.Failure == _Concurrency.CancellationError {

                /**
                 Attaches a subscriber with closure-based behavior to a publisher that fails with ``CancellationError``.

                 Use ``Publisher/sink(receiveValue:)`` to observe values received by the publisher and print them to the console.
                 This operator is meant to be used mainly with SKIE Flows, which can fail with ``CancellationError``.
                 That means the publisher’s ``Publisher/Failure`` type is ``CancellationError``.

                 This method creates the subscriber and immediately requests an unlimited number of values, prior to returning the subscriber.
                 The return value should be held, otherwise the stream will be canceled.

                 - parameter receiveValue: The closure to execute on receipt of a value.
                 - Returns: A cancellable instance, which you use when you end assignment of the received value. Deallocation of the result will tear down the subscription stream.
                */
                public func sink(receiveValue: @escaping ((Self.Output) -> Swift.Void)) -> Combine.AnyCancellable {
                    self.sink(receiveCompletion: { _ in }, receiveValue: receiveValue)
                }
            }
        """.trimIndent()
    }
}
