let task = Task.detached {
    var sum: Int32 = 0

    for await i in AKt.flow() {
        sum += i.int32Value
    }

    return sum
}

let sum = await task.value

if task.isCancelled {
    exit(sum - 6)
} else {
    exit(7)
}
