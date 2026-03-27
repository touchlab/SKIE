package `tests`.`enums`.`renaming`.`enabled`.`collision`

enum class A {

    copy,
    mutableCopy,
    `init`,
    retain,
    release,
    autorelease,
    initialize,
    load,
    alloc,
    new,
    `class`,
    superclass,
    classFallbacksForKeyedArchiver,
    classForKeyedUnarchiver,
    description,
    debugDescription,
    version,
    hash,
    useStoredAccessor,
    isProxy,
    retainCount,
    zone,
}

val a: A = A.alloc

val index: Int = 0
