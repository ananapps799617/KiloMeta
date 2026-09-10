// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/DriverProfileRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación concreta con persistencia SharedPreferences y sincronización Cloud.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleProfile
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

@Singleton
class DriverProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val dispatchers: CoroutineDispatchers
) : DriverProfileRepository {

    companion object {
        private const val PREFS_NAME = "kilometa_driver_profile"
        private const val KEY_DRIVER_ID = "driver_id"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_EMAIL = "driver_email"
        private const val KEY_PHONE = "driver_phone"
        private const val KEY_GENDER = "driver_gender"
        private const val KEY_ACTIVE_VEHICLE_ID = "active_vehicle_id"

        private const val KEY_VEHICLE_PLATE = "vehicle_plate"
        private const val KEY_BRAND = "vehicle_brand"
        private const val KEY_MODEL = "vehicle_model"
        private const val KEY_PROPULSION_TYPE = "propulsion_type"
        private const val KEY_TENURE_TYPE = "tenure_type"
        private const val KEY_QUOTA_PERIODICITY = "quota_periodicity"
        private const val KEY_QUOTA_AMOUNT = "quota_amount"
        private const val KEY_WORK_DAYS_WEEK = "work_days_week"
        private const val KEY_CUTOFF_DAY = "quota_cutoff_day"
        private const val KEY_CUTOFF_HOUR = "quota_cutoff_hour"

        private const val KEY_ACTIVE_PLATFORMS = "active_platforms"
        private const val KEY_IS_PROFILE_COMPLETED = "is_profile_completed"
        private const val KEY_UPDATED_AT = "updated_at"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _driverFlow = MutableStateFlow(loadDriverFromPrefs())
    private val _vehicleFlow = MutableStateFlow(loadVehicleFromPrefs())

    override fun getProfile(): Flow<DriverProfile> = _driverFlow.asStateFlow()
    override fun getVehicleProfile(): Flow<VehicleProfile> = _vehicleFlow.asStateFlow()

    override suspend fun saveProfile(profile: DriverProfile) = withContext(dispatchers.io) {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        prefs.edit().apply {
            putString(KEY_DRIVER_ID, updated.id)
            putString(KEY_DRIVER_NAME, updated.driverName)
            putString(KEY_EMAIL, updated.email)
            putString(KEY_PHONE, updated.phone)
            putString(KEY_GENDER, updated.gender.name)
            putString(KEY_ACTIVE_VEHICLE_ID, updated.activeVehicleId)
            putString(KEY_VEHICLE_PLATE, updated.vehiclePlate)
            putString(KEY_PROPULSION_TYPE, updated.propulsionType.name)
            putStringSet(KEY_ACTIVE_PLATFORMS, updated.activePlatforms.map { it.name }.toSet())
            putBoolean(KEY_IS_PROFILE_COMPLETED, updated.isProfileCompleted)
            putLong(KEY_UPDATED_AT, updated.updatedAt)
            apply()
        }
        _driverFlow.value = updated
        syncToCloudIfAuthenticated(updated, _vehicleFlow.value)
    }

    override suspend fun saveVehicleProfile(vehicle: VehicleProfile) = withContext(dispatchers.io) {
        val updated = vehicle.copy(updatedAt = System.currentTimeMillis())
        prefs.edit().apply {
            putString(KEY_VEHICLE_PLATE, updated.vehiclePlate)
            putString(KEY_BRAND, updated.brand)
            putString(KEY_MODEL, updated.model)
            putString(KEY_PROPULSION_TYPE, updated.propulsionType.name)
            putString(KEY_TENURE_TYPE, updated.tenureType.name)
            putString(KEY_QUOTA_PERIODICITY, updated.quotaPeriodicity.name)
            putFloat(KEY_QUOTA_AMOUNT, updated.quotaAmount.toFloat())
            putInt(KEY_WORK_DAYS_WEEK, updated.workDaysWeek)
            putInt(KEY_CUTOFF_DAY, updated.quotaCutoffDay)
            putInt(KEY_CUTOFF_HOUR, updated.quotaCutoffHour)
            putLong(KEY_UPDATED_AT, updated.updatedAt)
            apply()
        }
        _vehicleFlow.value = updated
        val currentDriver = _driverFlow.value
        _driverFlow.value = currentDriver.copy(
            vehiclePlate = updated.vehiclePlate,
            propulsionType = updated.propulsionType
        )
        syncToCloudIfAuthenticated(_driverFlow.value, updated)
    }

    override suspend fun saveFullProfile(driver: DriverProfile, vehicle: VehicleProfile) = withContext(dispatchers.io) {
        saveVehicleProfile(vehicle)
        saveProfile(driver.copy(vehiclePlate = vehicle.vehiclePlate, propulsionType = vehicle.propulsionType))
    }

    override suspend fun resetProfileForTesting() = withContext(dispatchers.io) {
        prefs.edit().apply {
            putBoolean(KEY_IS_PROFILE_COMPLETED, false)
            putString(KEY_DRIVER_NAME, "")
            putString(KEY_VEHICLE_PLATE, "")
            apply()
        }
        _driverFlow.value = loadDriverFromPrefs()
        _vehicleFlow.value = loadVehicleFromPrefs()
    }

    override suspend fun fetchAndHydrateRemoteProfile(userId: String): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            val remoteResult = firestoreDataSource.getDriverProfile(userId)
            val map = remoteResult.getOrNull()
            if (map != null && map["isProfileCompleted"] == true) {
                val driverName = map["driverName"] as? String ?: ""
                val plate = map["vehiclePlate"] as? String ?: ""
                val brand = map["brand"] as? String ?: ""
                val model = map["model"] as? String ?: ""
                val prop = (map["propulsionType"] as? String)?.let {
                    runCatching { PropulsionType.valueOf(it) }.getOrNull()
                } ?: PropulsionType.BI_FUEL
                val tenure = (map["tenureType"] as? String)?.let {
                    runCatching { VehicleTenure.valueOf(it) }.getOrNull()
                } ?: VehicleTenure.RENTED
                val periodicity = (map["quotaPeriodicity"] as? String)?.let {
                    runCatching { QuotaPeriodicity.valueOf(it) }.getOrNull()
                } ?: QuotaPeriodicity.WEEKLY
                val quota = (map["quotaAmount"] as? Number)?.toDouble() ?: 500_000.0
                val workDays = (map["workDaysWeek"] as? Number)?.toInt() ?: 6
                val cutoffDay = (map["quotaCutoffDay"] as? Number)?.toInt() ?: 2
                val cutoffHour = (map["quotaCutoffHour"] as? Number)?.toInt() ?: 23

                prefs.edit().apply {
                    putBoolean(KEY_IS_PROFILE_COMPLETED, true)
                    putString(KEY_DRIVER_NAME, driverName)
                    putString(KEY_VEHICLE_PLATE, plate)
                    putString(KEY_BRAND, brand)
                    putString(KEY_MODEL, model)
                    putString(KEY_PROPULSION_TYPE, prop.name)
                    putString(KEY_TENURE_TYPE, tenure.name)
                    putString(KEY_QUOTA_PERIODICITY, periodicity.name)
                    putFloat(KEY_QUOTA_AMOUNT, quota.toFloat())
                    putInt(KEY_WORK_DAYS_WEEK, workDays)
                    putInt(KEY_CUTOFF_DAY, cutoffDay)
                    putInt(KEY_CUTOFF_HOUR, cutoffHour)
                    apply()
                }
                _driverFlow.value = loadDriverFromPrefs()
                _vehicleFlow.value = loadVehicleFromPrefs()
                true
            } else {
                false
            }
        }
    }

    private fun loadDriverFromPrefs(): DriverProfile {
        val def = DriverProfile()
        val genderName = prefs.getString(KEY_GENDER, def.gender.name)
        val gender = runCatching { Gender.valueOf(genderName ?: "") }.getOrDefault(def.gender)
        val propName = prefs.getString(KEY_PROPULSION_TYPE, def.propulsionType.name)
        val prop = runCatching { PropulsionType.valueOf(propName ?: "") }.getOrDefault(def.propulsionType)
        val platforms = prefs.getStringSet(KEY_ACTIVE_PLATFORMS, null)?.mapNotNull {
            runCatching { PlatformType.valueOf(it) }.getOrNull()
        }?.toSet() ?: def.activePlatforms

        return DriverProfile(
            id = prefs.getString(KEY_DRIVER_ID, def.id) ?: def.id,
            driverName = prefs.getString(KEY_DRIVER_NAME, def.driverName) ?: def.driverName,
            email = prefs.getString(KEY_EMAIL, def.email) ?: def.email,
            phone = prefs.getString(KEY_PHONE, def.phone) ?: def.phone,
            gender = gender,
            activeVehicleId = prefs.getString(KEY_ACTIVE_VEHICLE_ID, def.activeVehicleId) ?: def.activeVehicleId,
            vehiclePlate = prefs.getString(KEY_VEHICLE_PLATE, def.vehiclePlate) ?: def.vehiclePlate,
            propulsionType = prop,
            activePlatforms = platforms,
            isProfileCompleted = prefs.getBoolean(KEY_IS_PROFILE_COMPLETED, false),
            updatedAt = prefs.getLong(KEY_UPDATED_AT, def.updatedAt)
        )
    }

    private fun loadVehicleFromPrefs(): VehicleProfile {
        val def = VehicleProfile()
        val propName = prefs.getString(KEY_PROPULSION_TYPE, def.propulsionType.name)
        val prop = runCatching { PropulsionType.valueOf(propName ?: "") }.getOrDefault(def.propulsionType)
        val tenureName = prefs.getString(KEY_TENURE_TYPE, def.tenureType.name)
        val tenure = runCatching { VehicleTenure.valueOf(tenureName ?: "") }.getOrDefault(def.tenureType)
        val periodName = prefs.getString(KEY_QUOTA_PERIODICITY, def.quotaPeriodicity.name)
        val period = runCatching { QuotaPeriodicity.valueOf(periodName ?: "") }.getOrDefault(def.quotaPeriodicity)

        return VehicleProfile(
            id = prefs.getString(KEY_ACTIVE_VEHICLE_ID, def.id) ?: def.id,
            vehiclePlate = prefs.getString(KEY_VEHICLE_PLATE, def.vehiclePlate) ?: def.vehiclePlate,
            brand = prefs.getString(KEY_BRAND, def.brand) ?: def.brand,
            model = prefs.getString(KEY_MODEL, def.model) ?: def.model,
            propulsionType = prop,
            tenureType = tenure,
            quotaPeriodicity = period,
            quotaAmount = prefs.getFloat(KEY_QUOTA_AMOUNT, def.quotaAmount.toFloat()).toDouble(),
            workDaysWeek = prefs.getInt(KEY_WORK_DAYS_WEEK, def.workDaysWeek),
            quotaCutoffDay = prefs.getInt(KEY_CUTOFF_DAY, def.quotaCutoffDay),
            quotaCutoffHour = prefs.getInt(KEY_CUTOFF_HOUR, def.quotaCutoffHour),
            updatedAt = prefs.getLong(KEY_UPDATED_AT, def.updatedAt)
        )
    }

    private suspend fun syncToCloudIfAuthenticated(driver: DriverProfile, vehicle: VehicleProfile) {
        val currentUser = authRepository.getCurrentUser() ?: return
        val map = mapOf<String, Any>(
            "driverName" to driver.driverName,
            "email" to driver.email,
            "phone" to driver.phone,
            "gender" to driver.gender.name,
            "vehiclePlate" to vehicle.vehiclePlate,
            "brand" to vehicle.brand,
            "model" to vehicle.model,
            "propulsionType" to vehicle.propulsionType.name,
            "tenureType" to vehicle.tenureType.name,
            "quotaPeriodicity" to vehicle.quotaPeriodicity.name,
            "quotaAmount" to vehicle.quotaAmount,
            "workDaysWeek" to vehicle.workDaysWeek,
            "quotaCutoffDay" to vehicle.quotaCutoffDay,
            "quotaCutoffHour" to vehicle.quotaCutoffHour,
            "isProfileCompleted" to driver.isProfileCompleted,
            "updatedAt" to System.currentTimeMillis()
        )
        firestoreDataSource.saveDriverProfile(currentUser.uid, map)
    }
}
