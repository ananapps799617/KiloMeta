// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: build.gradle.kts (Raíz)
// DESCRIPCIÓN PEDAGÓGICA: Script raíz de construcción. Declara plugins comunes
// sin aplicarlos directamente, delegando su aplicación a los submódulos.
// ==============================================================================

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}
