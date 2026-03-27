let i: I = AKt.getI()

switch onEnum(of: i) {
case .a(let a):
    switch a {
    case .a1:
        exit(0)
    case .a2:
        exit(1)
    }
}
