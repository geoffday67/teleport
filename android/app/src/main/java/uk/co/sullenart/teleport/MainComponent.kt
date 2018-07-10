package uk.co.sullenart.teleport

import dagger.Component
import javax.inject.Singleton

@Component(modules = [MainModule::class])
@Singleton
interface MainComponent {
    fun inject(activity: BaseActivity)
}