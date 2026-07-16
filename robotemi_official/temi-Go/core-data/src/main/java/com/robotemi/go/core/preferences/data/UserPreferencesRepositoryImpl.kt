package com.robotemi.go.core.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.robotemi.go.core.preferences.domain.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: DataStore<Preferences>
) : UserPreferencesRepository(){
    override suspend fun setPassword(password: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[PASSWORD] = password
            }
        }
    }

    override suspend fun getPassword(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[PASSWORD] ?: "0000"
                }
            val value = flow.firstOrNull() ?: "0000"
            value
        }
    }

    override fun getPasswordFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[PASSWORD] ?: "0000"
            }
    }

    override suspend fun setIdleLocation(idleLocation: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[IDLE_LOCATION] = idleLocation
            }
        }
    }

    override suspend fun getIdleLocation(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[IDLE_LOCATION] ?: "home base"
                }
            val value = flow.firstOrNull() ?: "home base"
            value
        }
    }

    override fun getIdleLocationFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[IDLE_LOCATION] ?: "home base"
            }
    }

    override suspend fun setArriveTimeout(arriveTimeout: Int) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[ARRIVE_TIMEOUT] = arriveTimeout
            }
        }
    }

    override suspend fun getArriveTimeout(): Result<Int> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[ARRIVE_TIMEOUT] ?: 30
                }
            val value = flow.firstOrNull() ?: 30
            value
        }
    }

    override fun getArriveTimeoutFlow(): Flow<Int> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[ARRIVE_TIMEOUT] ?: 30
            }
    }

    override suspend fun setPauseTimeout(pauseTimeout: Int) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[PAUSE_TIMEOUT] = pauseTimeout
            }
        }
    }

    override suspend fun getPauseTimeout(): Result<Int> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[PAUSE_TIMEOUT] ?: 30
                }
            val value = flow.firstOrNull() ?: 30
            value
        }
    }

    override fun getPauseTimeoutFlow(): Flow<Int> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[PAUSE_TIMEOUT] ?: 30
            }
    }

    override suspend fun setStartSpeech(startSpeech: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[START_SPEECH] = startSpeech
            }
        }
    }

    override suspend fun getStartSpeech(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[START_SPEECH] ?: "Start delivery"
                }
            val value = flow.firstOrNull() ?: "Start delivery"
            value
        }
    }

    override fun getStartSpeechFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[START_SPEECH] ?: "Start delivery"
            }
    }

    override suspend fun setObstacleSpeech(obstacleSpeech: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[OBSTACLE_SPEECH] = obstacleSpeech
            }
        }
    }

    override suspend fun getObstacleSpeech(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[OBSTACLE_SPEECH] ?: "Obstacle detected"
                }
            val value = flow.firstOrNull() ?: "Obstacle detected"
            value
        }
    }

    override fun getObstacleSpeechFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[OBSTACLE_SPEECH] ?: "Obstacle detected"
            }
    }

    override suspend fun setArriveSpeech(arriveSpeech: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[ARRIVE_SPEECH] = arriveSpeech
            }
        }
    }

    override suspend fun getArriveSpeech(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[ARRIVE_SPEECH] ?: "Arrived at location. Please take your order from the tray."
                }
            val value = flow.firstOrNull() ?: "Arrived at location. Please take your order from the tray."
            value
        }
    }

    override fun getArriveSpeechFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[ARRIVE_SPEECH] ?: "Arrived at location. Please take your order from the tray."
            }
    }

    override suspend fun setNextLocationSpeech(nextLocationSpeech: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[NEXT_LOCATION_SPEECH] = nextLocationSpeech
            }
        }
    }

    override suspend fun getNextLocationSpeech(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[NEXT_LOCATION_SPEECH] ?: "Going to next location"
                }
            val value = flow.firstOrNull() ?: "Going to next location"
            value
        }
    }

    override fun getNextLocationSpeechFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[NEXT_LOCATION_SPEECH] ?: "Going to next location"
            }
    }

    override suspend fun setWrongTraySpeech(wrongTraySpeech: String) {
        Result.runCatching {
            userPreferencesDataSource.edit { preferences ->
                preferences[WRONG_TRAY_SPEECH] = wrongTraySpeech
            }
        }
    }

    override suspend fun getWrongTraySpeech(): Result<String> {
        return Result.runCatching {
            val flow = userPreferencesDataSource.data
                .catch { exception ->
                    if (exception is Exception) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[WRONG_TRAY_SPEECH] ?: "Wrong tray, please put the tray back and take the correct tray."
                }
            val value = flow.firstOrNull() ?: "Wrong tray, please put the tray back and take the correct tray."
            value
        }
    }

    override fun getWrongTraySpeechFlow(): Flow<String> {
        return userPreferencesDataSource.data
            .map { preferences ->
                preferences[WRONG_TRAY_SPEECH] ?: "Wrong tray, please put the tray back and take the correct tray."
            }
    }

    companion object {
        val PASSWORD = stringPreferencesKey("password")
        val IDLE_LOCATION = stringPreferencesKey("idle_location")
        val ARRIVE_TIMEOUT = intPreferencesKey("arrive_timeout")
        val PAUSE_TIMEOUT = intPreferencesKey("pause_timeout")
        val START_SPEECH = stringPreferencesKey("start_speech")
        val OBSTACLE_SPEECH = stringPreferencesKey("obstacle_speech")
        val ARRIVE_SPEECH = stringPreferencesKey("arrive_speech")
        val NEXT_LOCATION_SPEECH = stringPreferencesKey("next_location_speech")
        val WRONG_TRAY_SPEECH = stringPreferencesKey("wrong_tray_speech")
    }
}