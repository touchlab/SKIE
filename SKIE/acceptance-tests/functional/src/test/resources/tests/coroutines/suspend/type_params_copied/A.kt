package `tests`.`coroutines`.`suspend`.`type_params_copied`

class SingleUnboundParamClass<T> {

    suspend fun `member__0_type_params__0_value_params`() = Unit

    suspend fun `member__0_type_params__1_value_param`(i: T): T = i

    suspend fun <U> `member__1_unbound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j

    suspend fun <U : Int> `member__1_Int_bound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j

    suspend fun <U : T> `member__1_T_bound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j
}

class SingleIntBoundParamClass<T : Int> {

    suspend fun `member__0_type_params__0_value_params`() = Unit

    suspend fun `member__0_type_params__1_value_param`(i: T): T = i

    suspend fun <U> `member__1_unbound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j

    suspend fun <U : Int> `member__1_Int_bound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j

    suspend fun <U : T> `member__1_T_bound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j
}

class SingleRecursiveParamClass<T : SingleRecursiveParamClass<T>> {

    suspend fun `member__0_type_params__0_value_params`() = Unit

    suspend fun `member__0_type_params__1_value_param`(i: T): T = i

    suspend fun <U> `member__1_unbound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j

    suspend fun <U : Int> `member__1_Int_bound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j

    suspend fun <U : T> `member__1_T_bound_type_param__2_value_params`(i: T, j: U): Pair<T, U> = i to j
}

class TwoRecursiveParamsClass<T : TwoRecursiveParamsClass<T, U>, U : T> {

    suspend fun member__0_type_params__2_value_params(i: T, j: U): Pair<T, U> = i to j
}

class TwoUnboundParamsClass<T, U>

class TwoParamsSecondBoundToFirstClass<T, U : T>

class TwoParamsFirstBoundToSecondClass<T, U> where T : U

// suspend fun <T> singleUnboundParameter

suspend fun <T> SingleUnboundParamClass<T>.singleUnboundParameterInUnboundClass(i: T): T = i

suspend fun <T : Int> SingleUnboundParamClass<T>.singleIntBoundParameterInUnboundClass(i: Int): T = i as T

suspend fun <T : Int> SingleIntBoundParamClass<T>.singleIntBoundParameterInIntBoundClass(i: T): T = i
