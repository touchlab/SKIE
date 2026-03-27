func convert(_ flow: SkieSwiftStateFlow<String>) -> SkieSwiftFlow<String> {
    return SkieSwiftFlow(flow)
}

for await s in convert(AKt.flow) {
    if s == "A" {
        exit(0)
    }
}
