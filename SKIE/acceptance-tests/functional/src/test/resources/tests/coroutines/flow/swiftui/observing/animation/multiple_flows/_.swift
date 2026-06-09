import SwiftUI

if #available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *) {
    let view = Observing(AKt.counter1, AKt.counter2, animation: .default) { v1, v2 in
        Text("\(v1) \(v2)")
    }
    _ = view
}

exit(0)
