// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/model/VehicleCatalogTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para validar la integridad del catálogo vehicular
// colombiano y la consistencia de modelos sugeridos.
// ==============================================================================

package com.ananapps.kilometa.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleCatalogTest {

    @Test
    fun `catalogo contiene marcas esenciales colombianas`() {
        val brandNames = VehicleCatalog.brands.map { it.name }
        assertTrue(brandNames.contains("Chevrolet"))
        assertTrue(brandNames.contains("Renault"))
        assertTrue(brandNames.contains("Kia"))
        assertTrue(brandNames.contains("Hyundai"))
        assertTrue(brandNames.contains("Nissan"))
        assertTrue(brandNames.contains("Toyota"))
        assertTrue(brandNames.contains("Suzuki"))
        assertTrue(brandNames.contains("BYD"))
        assertTrue(brandNames.contains("JAC"))
        assertTrue(brandNames.contains("Chery"))
        assertTrue(brandNames.contains("Changan"))
        assertTrue(brandNames.contains("DFSK"))
        assertTrue(brandNames.contains(VehicleCatalog.OTHER_BRAND_KEY))
    }

    @Test
    fun `getModelsForBrand retorna modelos correctos para Chevrolet y Renault`() {
        val chevroletModels = VehicleCatalog.getModelsForBrand("Chevrolet")
        assertTrue(chevroletModels.contains("Spark"))
        assertTrue(chevroletModels.contains("Spark GT"))
        assertTrue(chevroletModels.contains("Joy"))
        assertTrue(chevroletModels.contains("Sail"))

        val renaultModels = VehicleCatalog.getModelsForBrand("Renault")
        assertTrue(renaultModels.contains("Kwid"))
        assertTrue(renaultModels.contains("Logan"))
        assertTrue(renaultModels.contains("Sandero"))
    }

    @Test
    fun `getModelsForBrand retorna lista vacia para opcion personalizada o desconocida`() {
        val customModels = VehicleCatalog.getModelsForBrand(VehicleCatalog.OTHER_BRAND_KEY)
        assertTrue(customModels.isEmpty())

        val unknownModels = VehicleCatalog.getModelsForBrand("MarcaInexistente")
        assertTrue(unknownModels.isEmpty())
    }
}
