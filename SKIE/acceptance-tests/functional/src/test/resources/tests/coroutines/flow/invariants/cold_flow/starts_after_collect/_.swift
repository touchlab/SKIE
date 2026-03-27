var sum: Int32 = 0
var index = 0

let flow = AKt.flow()

if AKt.emittedElements != 0 {
    exit(-1)
}

for await i in flow {
    index += 1

    if AKt.emittedElements != i.intValue {
        exit(-AKt.emittedElements)
    }

    sum += i.int32Value
}

exit(sum - 6)
