let a: A<KotlinInt> = A1<KotlinInt>(value: KotlinInt(int: 0))

switch exhaustively(a) {
    case .A1(let a1):
        exit(a1.value!.int32Value)
}
