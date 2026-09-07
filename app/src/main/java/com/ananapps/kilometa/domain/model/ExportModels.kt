// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/ExportModels.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelos inmutables de Dominio para la exportación contable y difusión de balances.
// Contiene las estructuras para reportes tabulares (CSV RFC 4180 con BOM) y
// resúmenes formateados en texto enriquecido para mensajería instantánea.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO PARA REPORTE TABULAR CSV
// ------------------------------------------------------------------------------
/**
 * Modela un reporte contable generado en formato CSV con delimitador ';' y codificación UTF-8 BOM.
 *
 * @property fileName Nombre sugerido para el archivo al persistir en Storage Access Framework (SAF).
 * @property content Contenido íntegro del reporte con encabezados contables y secciones multi-tabla.
 * @property totalRows Cantidad consolidada de registros de datos exportados en el documento.
 * @property generatedAtMillis Marca de tiempo Unix en milisegundos en que se compiló el reporte.
 */
data class CsvReport(
    val fileName: String,
    val content: String,
    val totalRows: Int,
    val generatedAtMillis: Long
)

// ------------------------------------------------------------------------------
// BLOQUE 2: MODELO PARA RESUMEN COMPARTIBLE EN MENSAJERÍA
// ------------------------------------------------------------------------------
/**
 * Modela una tarjeta de texto estructurada con emojis y métricas contables
 * lista para difusión inmediata a través de WhatsApp, Telegram o SMS.
 *
 * @property subject Título o asunto descriptivo del mensaje a compartir.
 * @property messageText Cuerpo formateado del balance financiero con formato monospace o viñetas.
 */
data class ShareableSummary(
    val subject: String,
    val messageText: String
)
