# KotlinCompilationError(There are multiple sealed class/interface children with the same name `A1` for the enum case.)

let a: A = A1()

switch onEnum(of: a) {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}
