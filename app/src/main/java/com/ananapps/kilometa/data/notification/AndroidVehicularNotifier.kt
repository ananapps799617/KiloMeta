// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/notification/AndroidVehicularNotifier.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación Android del emisor de notificaciones vehiculares de cuota.
// Implementa el protocolo de seguridad DataGuard: Visibilidad privada en pantalla
// bloqueada (setPublicVersion) para evitar atraco y 'shoulder surfing' en cabina.
// ==============================================================================

package com.ananapps.kilometa.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ananapps.kilometa.domain.repository.VehicularNotificationNotifier
import com.ananapps.kilometa.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: NOTIFICADOR VEHICULAR CON BLINDAJE DATAGUARD
// ------------------------------------------------------------------------------
@Singleton
class AndroidVehicularNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : VehicularNotificationNotifier {

    companion object {
        const val CHANNEL_ID = "kilometa_vehicular_quotas"
        const val CHANNEL_NAME = "Cuotas y Metas Vehiculares"
        private const val ID_WEEKLY_WARNING = 1001
        private const val ID_WEEKLY_FINAL = 1002
        private const val ID_MILESTONE = 1003
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de corte semanal y cumplimiento de metas de producción vehicular"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createBasePendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: AVISO PREVENTIVO DE CORTE SEMANAL (MARTES 18:00 HRS)
    // --------------------------------------------------------------------------
    override fun showWeeklyCutoffWarning(
        percentage: Int,
        remainingAmount: Double,
        deadlineHour: String
    ) {
        if (!hasNotificationPermission()) return

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("KiloMeta: Estado de Cuota")
            .setContentText("Tienes un reporte vehicular pendiente de revisión.")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val secureMessage = if (remainingAmount > 0.0) {
            "Avance del $percentage%. Quedan horas para el corte de las $deadlineHour."
        } else {
            "¡Meta al $percentage%! Estás en zona de superávit para el corte de las $deadlineHour."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Corte de Cuota Semanal Próximo ⏳")
            .setContentText(secureMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(secureMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setContentIntent(createBasePendingIntent())
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_WEEKLY_WARNING, notification)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: BALANCE DE CORTE FINAL SEMANAL (MARTES 23:59:59 HRS)
    // --------------------------------------------------------------------------
    override fun showWeeklyCutoffFinal(
        achieved: Boolean,
        achievedAmount: Double,
        targetAmount: Double
    ) {
        if (!hasNotificationPermission()) return

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("KiloMeta: Balance de Ciclo")
            .setContentText("El ciclo semanal de producción ha finalizado.")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val (title, text) = if (achieved) {
            Pair(
                "¡Cierre Semanal Exitoso! 🏆",
                "Completaste el objetivo de la bolsa semanal. La liquidación ha cerrado positivamente."
            )
        } else {
            Pair(
                "Cierre de Ciclo Semanal 📊",
                "El ciclo semanal cerró. Consulta el balance contable consolidado para iniciar nuevo ciclo."
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setContentIntent(createBasePendingIntent())
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_WEEKLY_FINAL, notification)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: CELEBRACIÓN DE HITO (100% CUOTA)
    // --------------------------------------------------------------------------
    override fun showMilestoneCelebration(title: String, message: String) {
        if (!hasNotificationPermission()) return

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("KiloMeta: Hito Alcanzado")
            .setContentText("Has completado un objetivo vehicular.")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setContentIntent(createBasePendingIntent())
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_MILESTONE, notification)
    }
}
