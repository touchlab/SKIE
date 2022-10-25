let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
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

    switch value {
    case .a1:
        exit(index == 0 ? 0 : 1)
    case .a2:
        exit(index == 1 ? 0 : 1)
    }
}
