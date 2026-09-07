// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/DriverProfileRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación concreta de DriverProfileRepository.
// Utiliza SharedPreferences para persistencia local inmediata (cero latencia en cabina)
// y emite reactivamente vía StateFlow, ejecutando sincronización oportunista hacia
// Google Cloud Firestore (/users/{userId}/profile/current) si hay sesión activa.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: REPOSITORIO DE PERFIL DE CONDUCTOR Y VEHÍCULO
// ------------------------------------------------------------------------------
@Singleton
class DriverProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val dispatchers: CoroutineDispatchers
) : DriverProfileRepository {

    companion object {
        private const val PREFS_NAME = "kilometa_driver_profile"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_VEHICLE_NAME = "vehicle_name"
        private const val KEY_VEHICLE_PLATE = "vehicle_plate"
        private const val KEY_PROPULSION_TYPE = "propulsion_type"
        private const val KEY_VEHICLE_TENURE = "vehicle_tenure"
        private const val KEY_TARGET_QUOTA = "target_quota_amount"
        private const val KEY_QUOTA_PERIODICITY = "quota_periodicity"
        private const val KEY_WORKING_DAYS = "working_days_per_week"
        private const val KEY_ACTIVE_PLATFORMS = "active_platforms"
        private const val KEY_UPDATED_AT = "updated_at"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _profileFlow = MutableStateFlow(loadProfileFromPrefs())

    override fun getProfile(): Flow<DriverProfile> = _profileFlow.asStateFlow()

    // --------------------------------------------------------------------------
    // BLOQUE 2: GUARDADO LOCAL Y SINCRONIZACIÓN OPORTUNISTA A CLOUD
    // --------------------------------------------------------------------------
    override suspend fun saveProfile(profile: DriverProfile) = withContext(dispatchers.io) {
        val updatedProfile = profile.copy(updatedAt = System.currentTimeMillis())

        // 1. Persistencia local inmediata (sin bloqueo de UI)
        prefs.edit().apply {
            putString(KEY_DRIVER_NAME, updatedProfile.driverName)
            putString(KEY_VEHICLE_NAME, updatedProfile.vehicleName)
            putString(KEY_VEHICLE_PLATE, updatedProfile.vehiclePlate)
            putString(KEY_PROPULSION_TYPE, updatedProfile.propulsionType.name)
            putString(KEY_VEHICLE_TENURE, updatedProfile.vehicleTenure.name)
            putFloat(KEY_TARGET_QUOTA, updatedProfile.targetQuotaAmount.toFloat())
            putString(KEY_QUOTA_PERIODICITY, updatedProfile.quotaPeriodicity.name)
            putInt(KEY_WORKING_DAYS, updatedProfile.workingDaysPerWeek)
            putStringSet(KEY_ACTIVE_PLATFORMS, updatedProfile.activePlatforms.map { it.name }.toSet())
            putLong(KEY_UPDATED_AT, updatedProfile.updatedAt)
            apply()
        }

        // 2. Emisión reactiva al StateFlow
        _profileFlow.value = updatedProfile

        // 3. Sincronización oportunista hacia Google Cloud Firestore si hay sesión
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            val profileMap = mapOf<String, Any>(
                "driverName" to updatedProfile.driverName,
                "vehicleName" to updatedProfile.vehicleName,
                "vehiclePlate" to updatedProfile.vehiclePlate,
                "propulsionType" to updatedProfile.propulsionType.name,
                "vehicleTenure" to updatedProfile.vehicleTenure.name,
                "targetQuotaAmount" to updatedProfile.targetQuotaAmount,
                "quotaPeriodicity" to updatedProfile.quotaPeriodicity.name,
                "workingDaysPerWeek" to updatedProfile.workingDaysPerWeek,
                "activePlatforms" to updatedProfile.activePlatforms.map { it.name },
                "updatedAt" to updatedProfile.updatedAt
            )
            firestoreDataSource.saveDriverProfile(currentUser.uid, profileMap)
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: LECTURA Y DESERIALIZACIÓN DESDE SHAREDPREFERENCES
    // --------------------------------------------------------------------------
    private fun loadProfileFromPrefs(): DriverProfile {
        val defaultProfile = DriverProfile()
        val driverName = prefs.getString(KEY_DRIVER_NAME, defaultProfile.driverName) ?: defaultProfile.driverName
        val vehicleName = prefs.getString(KEY_VEHICLE_NAME, defaultProfile.vehicleName) ?: defaultProfile.vehicleName
        val vehiclePlate = prefs.getString(KEY_VEHICLE_PLATE, defaultProfile.vehiclePlate) ?: defaultProfile.vehiclePlate

        val propulsionName = prefs.getString(KEY_PROPULSION_TYPE, defaultProfile.propulsionType.name)
        val propulsion = runCatching { PropulsionType.valueOf(propulsionName ?: "") }.getOrDefault(defaultProfile.propulsionType)

        val tenureName = prefs.getString(KEY_VEHICLE_TENURE, defaultProfile.vehicleTenure.name)
        val tenure = runCatching { VehicleTenure.valueOf(tenureName ?: "") }.getOrDefault(defaultProfile.vehicleTenure)

        val targetQuota = prefs.getFloat(KEY_TARGET_QUOTA, defaultProfile.targetQuotaAmount.toFloat()).toDouble()

        val periodicityName = prefs.getString(KEY_QUOTA_PERIODICITY, defaultProfile.quotaPeriodicity.name)
        val periodicity = runCatching { QuotaPeriodicity.valueOf(periodicityName ?: "") }.getOrDefault(defaultProfile.quotaPeriodicity)

        val workingDays = prefs.getInt(KEY_WORKING_DAYS, defaultProfile.workingDaysPerWeek)

        val platformStrings = prefs.getStringSet(KEY_ACTIVE_PLATFORMS, null)
        val platforms = if (platformStrings != null) {
            platformStrings.mapNotNull { name -> runCatching { PlatformType.valueOf(name) }.getOrNull() }.toSet()
        } else {
            defaultProfile.activePlatforms
        }

        val updatedAt = prefs.getLong(KEY_UPDATED_AT, defaultProfile.updatedAt)

        return DriverProfile(
            driverName = driverName,
            vehicleName = vehicleName,
            vehiclePlate = vehiclePlate,
            propulsionType = propulsion,
            vehicleTenure = tenure,
            targetQuotaAmount = targetQuota,
            quotaPeriodicity = periodicity,
            workingDaysPerWeek = workingDays,
            activePlatforms = platforms,
            updatedAt = updatedAt
        )
    }
}
