import SwiftUI

let view = Observing(AKt.counter1, AKt.counter2, animation: .default) { v1, v2 in
    Text("\(v1) \(v2)")
}

exit(0)
