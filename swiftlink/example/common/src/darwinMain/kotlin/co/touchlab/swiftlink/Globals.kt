package co.touchlab.swiftlink

@Test
val globalProperty = "Hello"

@Test(rename = "renamedGlobalProperty")
val toBeRenamedGlobalProperty = "Hello Renamed"

@Test
fun globalFunction() = "Global"

@Test(rename = "renamedGlobalFunction()")
fun toBeRenamedGlobalFunction() = "Global Renamed"
