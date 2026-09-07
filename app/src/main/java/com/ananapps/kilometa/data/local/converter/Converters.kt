// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/converter/Converters.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Conversores de tipos para Room SQLite. Convierte enumeraciones de dominio a String
// para almacenamiento relacional seguro, y viceversa, con fallbacks deterministas.
// ==============================================================================

package com.ananapps.kilometa.data.local.converter

import androidx.room.TypeConverter
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ShiftStatus

// ------------------------------------------------------------------------------
// BLOQUE 1: CONVERSORES DE ENUMERACIONES
// ------------------------------------------------------------------------------
class Converters {

    // --------------------------------------------------------------------------
    // PLATFORM TYPE
    // --------------------------------------------------------------------------
    @TypeConverter
    fun fromPlatformType(platform: PlatformType): String = platform.name

    @TypeConverter
    fun toPlatformType(value: String): PlatformType = try {
        PlatformType.valueOf(value)
    } catch (_: IllegalArgumentException) {
        PlatformType.PRIVATE
    }

    // --------------------------------------------------------------------------
    // FUEL TYPE
    // --------------------------------------------------------------------------
    @TypeConverter
    fun fromFuelType(fuelType: FuelType): String = fuelType.name

    @TypeConverter
    fun toFuelType(value: String): FuelType = try {
        FuelType.valueOf(value)
    } catch (_: IllegalArgumentException) {
        FuelType.GAS
    }

    // --------------------------------------------------------------------------
    // PAYMENT METHOD
    // --------------------------------------------------------------------------
    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value)
    } catch (_: IllegalArgumentException) {
        PaymentMethod.CASH
    }

    // --------------------------------------------------------------------------
    // EXPENSE CONCEPT
    // --------------------------------------------------------------------------
    @TypeConverter
    fun fromExpenseConcept(concept: ExpenseConcept): String = concept.name

    @TypeConverter
    fun toExpenseConcept(value: String): ExpenseConcept = try {
        ExpenseConcept.valueOf(value)
    } catch (_: IllegalArgumentException) {
        ExpenseConcept.OTHER
    }

    // --------------------------------------------------------------------------
    // SHIFT STATUS
    // --------------------------------------------------------------------------
    @TypeConverter
    fun fromShiftStatus(status: ShiftStatus): String = status.name

    @TypeConverter
    fun toShiftStatus(value: String): ShiftStatus = try {
        ShiftStatus.valueOf(value)
    } catch (_: IllegalArgumentException) {
        ShiftStatus.ACTIVE
    }
}
