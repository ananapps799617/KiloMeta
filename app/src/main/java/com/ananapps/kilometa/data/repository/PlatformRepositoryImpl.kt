package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.data.local.dao.PlatformDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.mapper.toPlatformDomain
import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.domain.repository.PlatformRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de plataformas mediada por Room SQLite (Orden #054).
 */
@Singleton
class PlatformRepositoryImpl @Inject constructor(
    private val platformDao: PlatformDao
) : PlatformRepository {

    override fun observeActivePlatforms(): Flow<List<Platform>> {
        return platformDao.observeActivePlatforms().map { it.toPlatformDomain() }
    }

    override fun observeAllPlatforms(): Flow<List<Platform>> {
        return platformDao.observeAllPlatforms().map { it.toPlatformDomain() }
    }

    override suspend fun getPlatformById(id: String): Platform? {
        return platformDao.getPlatformById(id)?.toDomain()
    }

    override suspend fun savePlatform(platform: Platform): Result<Unit> = runCatching {
        platformDao.insertPlatform(platform.toEntity())
    }

    override suspend fun togglePlatformStatus(id: String, isActive: Boolean): Result<Unit> = runCatching {
        platformDao.updatePlatformActiveStatus(id, isActive)
    }

    override suspend fun deleteCustomPlatform(id: String): Result<Unit> = runCatching {
        val existing = platformDao.getPlatformById(id)
            ?: throw NoSuchElementException("No existe la plataforma con ID '$id'")
        if (!existing.isCustom) {
            throw SecurityException("Las plataformas nativas del sistema no pueden ser eliminadas.")
        }
        platformDao.deleteCustomPlatform(id)
    }
}
