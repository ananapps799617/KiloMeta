package com.ananapps.kilometa.core.util

/**
 * Catálogo vehicular cerrado de glifos/emojis ergonómicos para plataformas y bolsillos
 * del Ecosistema AnAnApps v2.0 (Orden #055).
 * Proporciona sanitización defensiva estricta para mitigar inyecciones o caracteres anómalos.
 */
object IconCatalog {

    const val DEFAULT_PLATFORM_ICON = "🚘"
    const val DEFAULT_WALLET_ICON = "💵"
    const val MAX_ICON_LENGTH = 8

    /**
     * Lista blanca inmutable de 13 glifos temáticos de alta visibilidad en cabina vehicular.
     */
    val VEHICULAR_ICONS: List<String> = listOf(
        "🚘", // Automóvil / Particular / Uber
        "🚕", // Taxi / Servicio Público
        "🛵", // Motocicleta / Reparto Express
        "📦", // Encomienda / Carga Pequeña
        "💵", // Efectivo Físico
        "💳", // Tarjeta / Datáfono
        "🏦", // Cuenta Bancaria
        "🟣", // Billetera Digital / Nequi / Dale
        "🪙", // Monedas / Menudo / Peaje
        "⛽", // Combustible / Gasolina / GNV
        "🔧", // Taller / Mantenimiento
        "⭐", // Meta de Ahorro / Especial
        "👤"  // Personal / Cuota Auto
    )

    /**
     * Sanitiza un glifo/emoji asegurando que pertenezca a la lista blanca oficial [VEHICULAR_ICONS]
     * y respete los límites de longitud de cabina. Ante cualquier entrada nula, en blanco o no
     * autorizada, retorna el valor seguro por defecto [default].
     */
    fun sanitizeIcon(icon: String?, default: String = DEFAULT_WALLET_ICON): String {
        if (icon.isNullOrBlank()) return default
        val trimmed = icon.trim()
        if (trimmed.length > MAX_ICON_LENGTH) return default
        return if (trimmed in VEHICULAR_ICONS) trimmed else default
    }
}
