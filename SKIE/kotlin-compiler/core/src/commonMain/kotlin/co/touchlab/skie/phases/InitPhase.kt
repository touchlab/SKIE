package co.touchlab.skie.phases

import co.touchlab.skie.context.CommonSkieContext

object InitPhase {

    interface Context : CommonSkieContext {

        override val context: Context
    }
}
