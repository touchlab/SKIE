let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    value.noParam()
    value.singleParam(p: 0)
    value.twoParams(p1: 0, p2: "")
    value.threeParams(p1: 0, p2: "", p3: 0.0)

    assert(value.abstractFun() == index + 1)
    assert(value.overridableFun() == index)

    do {
        try value.throwingNoParam()
        exit(1)
    } catch {
    }

    do {
        try value.throwingSingleParam(p: 0)
        exit(1)
    } catch {
    }

    do {
        try value.throwingTwoParams(p1: 0, p2: "")
        exit(1)
    } catch {
    }

    do {
        try value.throwingThreeParams(p1: 0, p2: "", p3: 0.0)
        exit(1)
    } catch {
    }

    value.inlineNoParam()
    value.inlineSingleParam(p: 0)
    value.inlineSingleParamClosure { }
    value.inlineSingleParamCrossinlineClosure { }
    value.inlineSingleParamNoinlineClosure { }

    value.singleVarargParam(p: KotlinIntArray(size: 3) { $0 })
    value.singleParamSingleVarargParamSameType(p1: 0, p: KotlinIntArray(size: 3) { $0 })
    value.singleParamSingleVarargParamDifferentType(p1: 0, p: KotlinArray<NSString>(size: 1) { _ in "Hello World" })

    value.closureSingleParam { p1 in }
    value.closureTwoParams { p1, p2 in }
    value.closureSingleParamReturnInt { p1 in p1 }
    value.closureTwoParamsReturnString { p1, p2 in p2 }

    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

exit(0)
