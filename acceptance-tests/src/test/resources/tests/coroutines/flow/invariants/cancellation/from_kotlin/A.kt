package `tests`.`coroutines`.`flow`.`invariants`.`cancellation`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

fun flow(): Flow<Int> = channelFlow {
    launch {
        delay(1)
        send(1)

        delay(1)
        send(2)

        delay(1)
        send(3)

        cancel()

        delay(1)
        send(4)
    }
}
