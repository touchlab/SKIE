import Foundation
import gradle_test

@main
struct Main {
    static func main() async throws {
        let enumValues: [BasicEnum] = [.a, .b, .c]
        for (index, value) in enumValues.enumerated() {
            switch value {
            case .a:
                assert(index == 0)
            case .b:
                assert(index == 1)
            case .c:
                assert(index == 2)
            }
        }

        // TODO: Add the rest

    }
}
