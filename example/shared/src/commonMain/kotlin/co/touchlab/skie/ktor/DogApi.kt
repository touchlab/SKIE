package co.touchlab.skie.ktor

import co.touchlab.skie.response.BreedResult

interface DogApi {
    suspend fun getJsonFromApi(): BreedResult
}
