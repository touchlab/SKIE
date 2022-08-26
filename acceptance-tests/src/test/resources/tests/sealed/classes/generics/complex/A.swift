let a: A<NSString, KotlinInt> = A1<NSString, KotlinInt, KotlinInt, NSString>(value: KotlinInt(int: 1), value2: KotlinInt(int: -1), valueI: "I")

switch exhaustively(a) {
    case .A1(let a1):
        exit(a1.value!.int32Value + (a1.value2 as! KotlinInt).int32Value)
}
