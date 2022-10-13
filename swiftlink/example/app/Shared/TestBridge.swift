import ExampleKit

public func instantiateSwiftTest() -> SwiftTest {
    return SwiftTest()
}

public func accessKotlinTest(in swiftTest: SwiftTest) -> KotlinTest {
    return swiftTest.kotlin
}
