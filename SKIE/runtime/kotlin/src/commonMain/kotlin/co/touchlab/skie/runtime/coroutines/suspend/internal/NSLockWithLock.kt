package co.touchlab.skie.runtime.coroutines.suspend.internal

import platform.Foundation.NSLock

internal inline fun <T> NSLock.withLock(action: () -> T): T {
    this.lock()

    try {
        return action()
    } finally {
        this.unlock()
    }
}
