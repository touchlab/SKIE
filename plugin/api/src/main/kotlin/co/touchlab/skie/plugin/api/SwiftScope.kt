package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.function.SwiftFunctionScope
import co.touchlab.skie.plugin.api.property.SwiftPropertyScope
import co.touchlab.skie.plugin.api.type.SwiftClassScope
import co.touchlab.skie.plugin.api.type.SwiftTypeScope

interface SwiftScope : SwiftClassScope, SwiftTypeScope, SwiftPropertyScope, SwiftFunctionScope
