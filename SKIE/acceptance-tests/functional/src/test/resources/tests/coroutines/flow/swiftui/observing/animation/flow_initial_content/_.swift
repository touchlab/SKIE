import SwiftUI

if #available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *) {
    let view = Observing(AKt.items(), animation: .default) {
        ProgressView()
    } content: { item in
        Text("\(item)")
    }
    _ = view
}

exit(0)
