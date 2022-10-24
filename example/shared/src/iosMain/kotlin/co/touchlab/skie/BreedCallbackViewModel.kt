package co.touchlab.skie

import co.touchlab.skie.db.Breed
import co.touchlab.skie.models.BreedRepository
import co.touchlab.skie.models.BreedViewModel
import co.touchlab.skie.models.CallbackViewModel
import co.touchlab.kermit.Logger

@Suppress("Unused") // Members are called from Swift
class BreedCallbackViewModel(
    breedRepository: BreedRepository,
    log: Logger
) : CallbackViewModel() {

    override val viewModel = BreedViewModel(breedRepository, log)

    val breeds = viewModel.breedState.asCallbacks()

    fun refreshBreeds() {
        viewModel.refreshBreeds()
    }

    fun updateBreedFavorite(breed: Breed) {
        viewModel.updateBreedFavorite(breed)
    }
}
