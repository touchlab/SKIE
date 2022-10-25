let a: A<KotlinInt> = A1(value: 0)

switch onEnum(of: a) {
    case .A1(let a1):
        exit(a1.value!.int32Value)
}
