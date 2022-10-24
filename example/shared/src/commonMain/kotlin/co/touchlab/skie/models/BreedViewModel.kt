package co.touchlab.skie.models

import co.touchlab.skie.db.Breed
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreedViewModel(
    private val breedRepository: BreedRepository,
    log: Logger
) : ViewModel() {
    private val log = log.withTag("BreedCommonViewModel")

    private val mutableBreedState: MutableStateFlow<BreedViewState> =
        MutableStateFlow(BreedViewState.Loading)

    val breedState: StateFlow<BreedViewState> = mutableBreedState

    init {
        observeBreeds()
    }

    override fun onCleared() {
        log.v("Clearing BreedViewModel")
    }

    private fun observeBreeds() {
        // Refresh breeds, and emit any exception that was thrown so we can handle it downstream
        val refreshFlow = flow<Throwable?> {
            try {
                breedRepository.refreshBreedsIfStale()
                emit(null)
            } catch (exception: Exception) {
                emit(exception)
            }
        }

        viewModelScope.launch {
            combine(refreshFlow, breedRepository.getBreeds()) { throwable, breeds -> throwable to breeds }
                .collect { (error, breeds) ->
                    mutableBreedState.update { previousState ->
                        val errorMessage = if (error != null) {
                            "Unable to download breed list"
                        } else {
                            if (previousState is BreedViewState.Error) {
                                previousState.type.message
                            } else {
                                null
                            }
                        }

                        if (errorMessage != null) {
                            BreedViewState.Error(BreedErrorType.Other(errorMessage))
                        } else {
                            BreedViewState.Data(breeds)
                        }
                    }
                }
        }
    }

    fun refreshBreeds(): Job {
        // Set loading state, which will be cleared when the repository re-emits
        mutableBreedState.update { BreedViewState.Loading }
        return viewModelScope.launch {
            log.v { "refreshBreeds" }
            try {
                breedRepository.refreshBreeds()
            } catch (exception: Exception) {
                handleBreedError(exception)
            }
        }
    }

    fun updateBreedFavorite(breed: Breed): Job {
        return viewModelScope.launch {
            breedRepository.updateBreedFavorite(breed)
        }
    }

    private fun handleBreedError(throwable: Throwable) {
        log.e(throwable) { "Error downloading breed list" }
        mutableBreedState.update {
            BreedViewState.Error(BreedErrorType.RefreshFailure)
        }
    }
}

sealed interface BreedViewState {
    class Data(val breeds: List<Breed>) : BreedViewState
    object Loading : BreedViewState
    class Error(val type: BreedErrorType): BreedViewState
}

sealed interface BreedErrorType {
    val message: String
        get() = when (this) {
            is Network -> "A connection error occurred. Please try again."
            is Invalid -> "Invalid response."
            is Other -> "An error occurred."
            is RefreshFailure -> "Unable to refresh breed list"
        }

    class Other(override val message: String) : BreedErrorType
    object Network : BreedErrorType
    object Invalid : BreedErrorType
    object RefreshFailure : BreedErrorType
}

/*
data class BreedViewState(
    val breeds: List<Breed>? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)*/
