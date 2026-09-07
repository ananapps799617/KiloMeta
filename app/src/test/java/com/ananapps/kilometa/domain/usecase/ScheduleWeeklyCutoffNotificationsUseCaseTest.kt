// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/ScheduleWeeklyCutoffNotificationsUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para ScheduleWeeklyCutoffNotificationsUseCase
// y NotifyQuotaMilestoneUseCase con dobles de prueba puros (FakeScheduler y FakeNotifier).
// Regla de Oro: Tipado estricto al 100%, sin dependencias de Android framework.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.model.WeeklyQuotaProgress
import com.ananapps.kilometa.domain.repository.VehicularNotificationNotifier
import com.ananapps.kilometa.domain.repository.VehicularNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIONES FAKE PARA PRUEBAS
// ------------------------------------------------------------------------------
private class FakeVehicularNotificationScheduler : VehicularNotificationScheduler {
    var scheduleCount: Int = 0
    var cancelCount: Int = 0

    override fun scheduleWeeklyCutoffAlarms() {
        scheduleCount++
    }

    override fun cancelScheduledAlarms() {
        cancelCount++
    }
}

private class FakeVehicularNotificationNotifier : VehicularNotificationNotifier {
    val celebrations = mutableListOf<Pair<String, String>>()
    val warnings = mutableListOf<String>()
    val finals = mutableListOf<Boolean>()

    override fun showWeeklyCutoffWarning(percentage: Int, remainingAmount: Double, deadlineHour: String) {
        warnings.add("Warning $percentage% $deadlineHour")
    }

    override fun showWeeklyCutoffFinal(achieved: Boolean, achievedAmount: Double, targetAmount: Double) {
        finals.add(achieved)
    }

    override fun showMilestoneCelebration(title: String, message: String) {
        celebrations.add(title to message)
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS UNITARIAS
// ------------------------------------------------------------------------------
class ScheduleWeeklyCutoffNotificationsUseCaseTest {

    private lateinit var scheduler: FakeVehicularNotificationScheduler
    private lateinit var notifier: FakeVehicularNotificationNotifier
    private lateinit var scheduleUseCase: ScheduleWeeklyCutoffNotificationsUseCase
    private lateinit var notifyMilestoneUseCase: NotifyQuotaMilestoneUseCase

    @Before
    fun setUp() {
        scheduler = FakeVehicularNotificationScheduler()
        notifier = FakeVehicularNotificationNotifier()
        scheduleUseCase = ScheduleWeeklyCutoffNotificationsUseCase(scheduler)
        notifyMilestoneUseCase = NotifyQuotaMilestoneUseCase(notifier)
    }

    @Test
    fun `invoke en scheduleUseCase delega la programacion de alarmas al scheduler`() {
        // GIVEN: Estado inicial sin programaciones
        assertEquals(0, scheduler.scheduleCount)

        // WHEN: Se ejecuta el caso de uso
        scheduleUseCase()

        // THEN: Se programan las alarmas en el scheduler
        assertEquals(1, scheduler.scheduleCount)
        assertEquals(0, scheduler.cancelCount)
    }

    @Test
    fun `cancel en scheduleUseCase cancela todas las alarmas en el scheduler`() {
        // GIVEN: Scheduler con alarmas activas
        scheduleUseCase()

        // WHEN: Se cancelan las alarmas
        scheduleUseCase.cancel()

        // THEN: Se registra la cancelación
        assertEquals(1, scheduler.cancelCount)
    }

    @Test
    fun `checkAndNotifyWeeklyMilestone emite celebracion segura si se alcanzo el 100 por ciento de la bolsa`() {
        // GIVEN: Progreso semanal que iguala o supera la meta ($500.000 COP)
        val progress = WeeklyQuotaProgress(
            weeklyTarget = 500_000.0,
            accumulatedSoFar = 520_000.0,
            remainingTarget = 0.0,
            daysRemaining = 2,
            suggestedDailyQuota = 0.0
        )
        assertTrue(progress.isWeeklyTargetAchieved)

        // WHEN: Se evalúa el hito
        notifyMilestoneUseCase.checkAndNotifyWeeklyMilestone(progress)

        // THEN: Se emite una celebración motivacional sin cifras expuestas
        assertEquals(1, notifier.celebrations.size)
        assertTrue(notifier.celebrations.first().first.contains("Meta Semanal Cumplida"))
        // Validación DataGuard: Cero números de dinero en texto de notificación
        assertFalse(notifier.celebrations.first().second.contains("520.000"))
        assertFalse(notifier.celebrations.first().second.contains("$"))
    }

    @Test
    fun `checkAndNotifyWeeklyMilestone no emite nada si la bolsa semanal no ha sido completada`() {
        // GIVEN: Progreso semanal pendiente ($350.000 / $500.000 COP)
        val progress = WeeklyQuotaProgress(
            weeklyTarget = 500_000.0,
            accumulatedSoFar = 350_000.0,
            remainingTarget = 150_000.0,
            daysRemaining = 3,
            suggestedDailyQuota = 50_000.0
        )
        assertFalse(progress.isWeeklyTargetAchieved)

        // WHEN: Se evalúa el hito
        notifyMilestoneUseCase.checkAndNotifyWeeklyMilestone(progress)

        // THEN: No se emite ninguna notificación
        assertTrue(notifier.celebrations.isEmpty())
    }

    @Test
    fun `checkAndNotifyShiftMilestone emite celebracion cuando la jornada cubre la cuota diaria`() {
        // GIVEN: Turno que superó la cuota diaria
        val shiftSummary = ShiftFinancialSummary(
            shiftId = "shift-001",
            status = ShiftStatus.COMPLETED,
            startTime = 1700000000000L,
            startOdometer = 1000,
            endOdometer = 1120,
            totalKilometers = 120,
            dailyQuotaTarget = 83_333.0,
            totalGrossIncome = 150_000.0,
            totalCommissions = 30_000.0,
            totalNetIncome = 120_000.0,
            totalGasExpense = 20_000.0,
            totalGasolineExpense = 0.0,
            totalFuelExpense = 20_000.0,
            totalOperationalExpenses = 5_000.0,
            totalExpenses = 25_000.0,
            netProfit = 95_000.0, // 95.000 >= 83.333
            quotaProgressPercentage = 114.0,
            costPerKilometer = 208.3,
            grossIncomePerKilometer = 1250.0,
            netIncomePerKilometer = 1000.0,
            tripsCount = 6,
            fuelingsCount = 1,
            expensesCount = 1
        )
        assertTrue(shiftSummary.isQuotaAchieved)

        // WHEN: Se evalúa el hito del turno
        notifyMilestoneUseCase.checkAndNotifyShiftMilestone(shiftSummary)

        // THEN: Se emite celebración
        assertEquals(1, notifier.celebrations.size)
        assertTrue(notifier.celebrations.first().first.contains("Meta de Jornada Alcanzada"))
    }
}
