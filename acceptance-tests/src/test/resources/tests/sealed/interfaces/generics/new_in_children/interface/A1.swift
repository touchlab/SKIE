let a: A = A1A<KotlinInt>(value: KotlinInt(int: 0))

switch exhaustively(a) {
    case .A1(let a1):
        exit((a1.value as! KotlinInt).int32Value)
}
