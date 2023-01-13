let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    assert(value.name == "A\(index + 1)")
    assert(value.immutableProperty == 0, "immutableProperty should be 0")
    assert(value.mutableProperty == 0, "mutableProperty should be 0")
    value.mutableProperty = Int32(index)
    assert(value.mutableProperty == index, "mutableProperty should be \(index)")
    assert(value.abstractImmutableProperty == index + 1, "abstractImmutableProperty should be \(index + 1)")
    assert(value.abstractMutableProperty == index, "abstractMutableProperty should be \(index)")
    assert(value.overridableImmutableProperty == index, "overridableImmutableProperty should be \(index)")
    assert(value.overridableMutableProperty == index, "overridableMutableProperty should be \(index)")
    value.abstractMutableProperty = Int32(index + 2)
    value.overridableMutableProperty = Int32(index + 3)
    if (index == 0) {
        assert(value.abstractMutableProperty == index + 2, "abstractMutableProperty should be \(index + 2), is \(value.abstractMutableProperty)")
        assert(value.overridableMutableProperty == index + 3, "overridableMutableProperty should be \(index + 3)")
    } else {
        assert(value.abstractMutableProperty == 2 * (index + 2), "abstractMutableProperty should be \(2 * (index + 2)), is \(value.abstractMutableProperty)")
        assert(value.overridableMutableProperty == 2 * (index + 3), "overridableMutableProperty should be \(2 * (index + 3))")
    }

    assert(value.extensionProperty == 0, "extensionProperty should be 0")
    assert(value.extensionMutableProperty == 0, "extensionMutableProperty should be 0")
    value.extensionMutableProperty = Int32(index)
    assert(value.extensionPropertyWithSelf == value, "value should be \(value)")
    assert(value.extensionMutablePropertyWithSelf == value, "value should be \(value)")
    value.extensionMutablePropertyWithSelf = value

    assert(value.immutableWrapperInterfaceProperty is WrapperInterface, "immutableWrapperInterfaceProperty should be WrapperInterface")
    assert(value.mutableWrapperInterfaceProperty is WrapperInterface, "mutableWrapperInterfaceProperty should be WrapperInterface")

    assert(value.immutableWrapperClassProperty is WrapperClass, "immutableWrapperClassProperty should be WrapperClass")
    assert(value.mutableWrapperClassProperty is WrapperClass, "mutableWrapperClassProperty should be WrapperClass")

    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

// Static properties are no longer supported, defer to `companion``
assert(A.companion.staticProperty == 0, "staticProperty should be 0")
assert(A.companion.staticMutableProperty == 0, "staticProperty should be 0")
A.companion.staticMutableProperty = Int32(1)
assert(__A.Companion.shared.staticMutableProperty == 1, "staticMutableProperty in Companion should be 1")
__A.Companion.shared.staticMutableProperty = Int32(2)
assert(A.companion.staticMutableProperty == 2, "staticMutableProperty should be 2")

exit(0)
