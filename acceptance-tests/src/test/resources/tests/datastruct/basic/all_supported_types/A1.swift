let a: A = A(
    boolean: true,
    byte: 0,
    short: 0,
    int: 0,
    long: 0,
    float: 0,
    double: 0,
    string: "",
    booleanArray: KotlinBooleanArray(size: 10) { KotlinBoolean(value: $0.intValue % 2 == 0) },
    byteArray: KotlinByteArray(size: 10) { KotlinByte(value: $0.int8Value) },
    shortArray: KotlinShortArray(size: 10) { KotlinShort(value: $0.int16Value) },
    intArray: KotlinIntArray(size: 10) { KotlinInt(value: $0.int32Value) },
    longArray: KotlinLongArray(size: 10) { KotlinLong(value: $0.int64Value) },
    floatArray: KotlinFloatArray(size: 10) { KotlinFloat(value: $0.floatValue) },
    doubleArray: KotlinDoubleArray(size: 10) { KotlinDouble(value: $0.doubleValue) },
    stringArray: KotlinArray<NSString>(size: 10) { String(describing: $0) as NSString },
    booleanList: [KotlinBoolean(value: true), KotlinBoolean(value: false)],
    byteList: [KotlinByte(value: 8)],
    shortList: [KotlinShort(value: 16)],
    intList: [KotlinInt(value: 32)],
    longList: [KotlinLong(value: 64)],
    floatList: [KotlinFloat(value: 0.32)],
    doubleList: [KotlinDouble(value: 0.64)],
    stringList: ["Hello", "world", "!"]
)

assert(a.bridged.unbridged == a)

exit(0)
