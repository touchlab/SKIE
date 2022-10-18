# SwiftCompilationError(switch must be exhaustive)

let a = AKt.a1()

switch a {
    case .a1:
        exit(1)
    case .a2:
        exit(1)
}
