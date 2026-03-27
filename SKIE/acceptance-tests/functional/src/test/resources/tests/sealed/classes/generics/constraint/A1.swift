let a: A<IntWrapper> = A1<IntWrapper>(wrapper: IntWrapper(value: 0))

switch onEnum(of: a) {
    case .a1(let a1):
        exit(a1.wrapper.value.int32Value)
}
