tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("cleanAll") {
    dependsOn(gradle.includedBuilds.map { it.task(":cleanAll") })
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
