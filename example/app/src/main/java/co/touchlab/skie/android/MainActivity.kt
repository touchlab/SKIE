package co.touchlab.skie.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import co.touchlab.skie.android.ui.MainScreen
import co.touchlab.skie.android.ui.theme.SkieTheme
import co.touchlab.skie.injectLogger
import co.touchlab.skie.models.BreedViewModel
import co.touchlab.kermit.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    private val log: Logger by injectLogger("MainActivity")
    private val viewModel: BreedViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkieTheme {
                MainScreen(viewModel, log)
            }
        }
    }
}
