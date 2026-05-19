import SwiftUI

let view = Observing(AKt.counter, animation: nil) { value in
    Text("\(value)")
}

exit(0)
