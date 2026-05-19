import SwiftUI

let view = Observing(AKt.counter, animation: .default) { value in
    Text("\(value)")
}

exit(0)
