for await s in SkieSwiftFlow(SkieKotlinFlow(AKt.flow)) {
    if s == "A" {
        exit(0)
    }
}
