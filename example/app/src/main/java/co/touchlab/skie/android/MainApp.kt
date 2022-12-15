package co.touchlab.skie.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import co.touchlab.skie.AppInfo
import co.touchlab.skie.initKoin
import co.touchlab.skie.models.BreedViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(
            module {
                single<Context> { this@MainApp }
                viewModel { BreedViewModel(get(), get { parametersOf("BreedViewModel") }) }
                single<SharedPreferences> {
                    get<Context>().getSharedPreferences("SKIEDEMO_SETTINGS", Context.MODE_PRIVATE)
                }
                single<AppInfo> { AndroidAppInfo }
                single {
                    { Log.i("Startup", "Hello from Android/Kotlin!") }
                }
            }
        )
    }
}

object AndroidAppInfo : AppInfo {

    override val appId: String = BuildConfig.APPLICATION_ID
}
