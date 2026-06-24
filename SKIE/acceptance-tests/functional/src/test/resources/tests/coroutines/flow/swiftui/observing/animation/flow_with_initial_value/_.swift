import SwiftUI

if #available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *) {
    let view = Observing(AKt.ticking().withInitialValue(KotlinInt(0)), animation: .default) { tick in
        Text("\(tick)")
    }
    _ = view
}

exit(0)
