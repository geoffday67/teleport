package uk.co.sullenart.teleport

import android.app.Application
import timber.log.Timber

class MainApplication : Application() {
    lateinit var component: MainComponent

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        component = DaggerMainComponent.builder()
                .mainModule(MainModule(this))
                .build()
    }
}