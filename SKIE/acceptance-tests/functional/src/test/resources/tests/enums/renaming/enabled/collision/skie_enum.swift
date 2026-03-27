switch a {
case .theAlloc:
    exit(0)
case .theMutableCopy:
    exit(1)
case .theInit:
    exit(1)
case .retain:
    exit(1)
case .theRelease:
    exit(1)
case .autorelease:
    exit(1)
case .theInitialize:
    exit(1)
case .theLoad:
    exit(1)
case .theCopy:
    exit(1)
case .theNew:
    exit(1)
case .theClass:
    exit(1)
case .theSuperclass:
    exit(1)
case .theClassFallbacksForKeyedArchiver:
    exit(1)
case .theClassForKeyedUnarchiver:
    exit(1)
case .theDescription:
    exit(1)
case .theDebugDescription:
    exit(1)
case .theVersion:
    exit(1)
case .theHash:
    exit(1)
case .theUseStoredAccessor:
    exit(1)
case .theIsProxy:
    exit(1)
case .theRetainCount:
    exit(1)
case .theZone:
    exit(1)
}
