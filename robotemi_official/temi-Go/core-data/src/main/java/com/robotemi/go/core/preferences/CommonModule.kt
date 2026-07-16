package com.robotemi.go.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.robotemi.go.core.preferences.data.UserPreferencesRepositoryImpl
import com.robotemi.go.core.preferences.domain.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    companion object {
        @Provides
        fun provideGson(): Gson = Gson()

        @Provides
        @Singleton
        fun provideUserPreferences(@ApplicationContext context: Context): DataStore<Preferences> =
            context.userDataStore
    }

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}