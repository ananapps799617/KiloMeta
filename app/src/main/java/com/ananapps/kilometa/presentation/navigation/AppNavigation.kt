// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/AppNavigation.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Definición canónica de los destinos de cabina en KiloMeta.
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation

// ------------------------------------------------------------------------------
// BLOQUE 1: DESTINOS CANÓNICOS DE LA APLICACIÓN
// ------------------------------------------------------------------------------
/**
 * Pantallas principales accesibles desde el menú lateral de cabina.
 */
enum class AppDestination(
    val title: String,
    val subtitle: String
) {
    COCKPIT("Cabina en Vivo", "En Turno"),
    HISTORY("Historial & Analíticas", "Auditoría Contable"),
    MAINTENANCE("Mantenimientos", "Alertas Predictivas & Odómetro"),
    EXPENSES("Gastos en Ruta", "Lavadas, Despinchadas & Comida"),
    PROFILE("Mi Perfil & Vehículo", "Configuración Técnica"),
    PLACES("Mis Bombas & Talleres", "Ruta directa a Waze & Maps"),
    BACKUP("Respaldo Nube", "Cloud Sync & Firestore")
}
