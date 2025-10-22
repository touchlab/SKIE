plugins {
    id("skie.runtime.swift")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Runtime - Swift"
    description = "Swift part of the SKIE runtime. It's used to facilitate certain features of SKIE."
    publishSources = true
}
