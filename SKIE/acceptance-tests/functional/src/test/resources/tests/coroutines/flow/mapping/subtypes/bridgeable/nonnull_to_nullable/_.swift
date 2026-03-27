func convert(_ flow: SkieSwiftStateFlow<String>) -> SkieSwiftOptionalFlow<String> {
    return SkieSwiftOptionalFlow(flow)
}

for await s in convert(AKt.flow) {
    if s == "A" {
        exit(0)
    }
}
