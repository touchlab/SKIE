import SwiftUI

@available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *)
struct TestView: View {
    @State var value: KotlinInt = KotlinInt(0)

    var body: some View {
        Text("")
            .collect(flow: AKt.counter, into: $value, animation: nil)
    }
}

exit(0)
