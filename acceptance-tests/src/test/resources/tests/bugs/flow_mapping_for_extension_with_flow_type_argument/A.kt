package `tests`.`bugs`.`flow_mapping_for_extension_with_flow_type_argument`

import kotlinx.coroutines.flow.Flow

class A<T>

suspend fun A<Flow<Int>>.foo() {
}
