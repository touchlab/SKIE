import SwiftUI

let view = Observing(AKt.ticking().withInitialValue(KotlinInt(0)), animation: .default) { tick in
    Text("\(tick)")
}

exit(0)
