// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/PlaceDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DAO para la tabla 'places'. Provee consultas reactivas y mutaciones atómicas
// para la libreta de direcciones vehiculares del conductor.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY created_at DESC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Query("DELETE FROM places WHERE id = :placeId")
    suspend fun deletePlace(placeId: String)

    @Query("UPDATE places SET gnv_price_per_m3 = :price WHERE id = :placeId")
    suspend fun updateGnvPrice(placeId: String, price: Double)

    @Query("UPDATE places SET gasoline_price_per_gal = :price WHERE id = :placeId")
    suspend fun updateGasolinePrice(placeId: String, price: Double)
}
