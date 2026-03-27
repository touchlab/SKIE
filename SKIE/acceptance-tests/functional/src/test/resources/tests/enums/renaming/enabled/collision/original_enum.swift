let values = [
__A.theAlloc,
__A.theCopy,
__A.theMutableCopy,
__A.theInit,
__A.retain,
__A.theRelease,
__A.autorelease,
__A.theInitialize,
__A.theLoad,
__A.theNew,
__A.theClass,
__A.theSuperclass,
__A.theClassFallbacksForKeyedArchiver,
__A.theClassForKeyedUnarchiver,
__A.theDescription,
__A.theDebugDescription,
__A.theVersion,
__A.theHash,
__A.theUseStoredAccessor,
__A.theIsProxy,
__A.theRetainCount,
__A.theZone
]

switch values[Int(index)] as A {
case .theAlloc:
    exit(0)
default:
    exit(1)
}
