import SwiftUI

let view = Observing(AKt.items(), animation: .default) {
    ProgressView()
} content: { item in
    Text("\(item)")
}

exit(0)
