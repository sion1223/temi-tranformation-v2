package com.robotemi.go.core.preferences.domain

import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.Flow

abstract class UserPreferencesRepository: PreferenceDataStore() {
    abstract suspend fun setPassword(password: String)
    abstract suspend fun getPassword(): Result<String>
    abstract fun getPasswordFlow(): Flow<String>

    abstract suspend fun setIdleLocation(idleLocation: String)
    abstract suspend fun getIdleLocation(): Result<String>
    abstract fun getIdleLocationFlow(): Flow<String>

    abstract suspend fun setArriveTimeout(arriveTimeout: Int)
    abstract suspend fun getArriveTimeout(): Result<Int>
    abstract fun getArriveTimeoutFlow(): Flow<Int>

    abstract suspend fun setPauseTimeout(pauseTimeout: Int)
    abstract suspend fun getPauseTimeout(): Result<Int>
    abstract fun getPauseTimeoutFlow(): Flow<Int>

    abstract suspend fun setStartSpeech(startSpeech: String)
    abstract suspend fun getStartSpeech(): Result<String>
    abstract fun getStartSpeechFlow(): Flow<String>

    abstract suspend fun setObstacleSpeech(obstacleSpeech: String)
    abstract suspend fun getObstacleSpeech(): Result<String>
    abstract fun getObstacleSpeechFlow(): Flow<String>

    abstract suspend fun setArriveSpeech(arriveSpeech: String)
    abstract suspend fun getArriveSpeech(): Result<String>
    abstract fun getArriveSpeechFlow(): Flow<String>

    abstract suspend fun setNextLocationSpeech(nextLocationSpeech: String)
    abstract suspend fun getNextLocationSpeech(): Result<String>
    abstract fun getNextLocationSpeechFlow(): Flow<String>

    abstract suspend fun setWrongTraySpeech(wrongTraySpeech: String)
    abstract suspend fun getWrongTraySpeech(): Result<String>
    abstract fun getWrongTraySpeechFlow(): Flow<String>
}