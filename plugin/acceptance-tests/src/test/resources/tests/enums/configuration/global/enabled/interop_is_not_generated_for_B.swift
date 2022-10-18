# SwiftCompilationError(switch must be exhaustive)

let b = DisabledBKt.b1()

switch b {
    case .b1:
        exit(0)
    case .b2:
        exit(1)
}
