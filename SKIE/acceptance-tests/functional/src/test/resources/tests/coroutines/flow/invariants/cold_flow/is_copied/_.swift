var sum: Int32 = 0

for await i in AKt.flow() {
    sum += i.int32Value

    for await i in AKt.flow() {
        sum += i.int32Value
    }
}

exit(sum - 24)
