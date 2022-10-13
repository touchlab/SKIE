let a: A = A1<KotlinInt>(value: KotlinInt(int: 0))

switch onEnum(of: a) {
    case .A1(let a1):
        exit((a1 as! A1<KotlinInt>).value!.int32Value)
}
