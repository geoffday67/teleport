package uk.co.sullenart.teleport

import android.content.Context
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class MainModule(val context: Context) {
    @Singleton
    @Provides
    fun provideRxSharedPreferences(): RxSharedPreferences {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return RxSharedPreferences.create(preferences)
    }

    @Singleton
    @Provides
    @Named("namePreference")
    fun provideNamePreference(preferences: RxSharedPreferences): Preference<String> = preferences.getString("name")
}