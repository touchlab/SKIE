let i: I = A1<NSString, KotlinInt, KotlinInt, NSString>(value: KotlinInt(int: 1), value2: KotlinInt(int: -1), valueI: "I")

switch onEnum(of: i) {
    case .A1(let a1):
        exit((a1.value as! KotlinInt).int32Value + (a1.value2 as! KotlinInt).int32Value)
}
