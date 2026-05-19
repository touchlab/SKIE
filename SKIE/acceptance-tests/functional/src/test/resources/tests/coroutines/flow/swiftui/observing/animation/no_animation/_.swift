import SwiftUI

let view = Observing(AKt.counter) { value in
    Text("\(value)")
}

exit(0)
