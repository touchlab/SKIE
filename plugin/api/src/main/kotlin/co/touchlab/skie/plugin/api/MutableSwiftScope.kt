package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.function.MutableSwiftFunctionScope
import co.touchlab.skie.plugin.api.property.MutableSwiftPropertyScope
import co.touchlab.skie.plugin.api.type.MutableSwiftClassScope

interface MutableSwiftScope : MutableSwiftClassScope, MutableSwiftPropertyScope, MutableSwiftFunctionScope
