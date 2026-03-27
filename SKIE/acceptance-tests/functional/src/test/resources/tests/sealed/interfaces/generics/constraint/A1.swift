let a: A = A1<IntWrapper>(wrapper: IntWrapper(value: 0))

switch onEnum(of: a) {
    case .a1(let a1):
        exit((a1 as! A1<IntWrapper>).wrapper.value.int32Value)
}
