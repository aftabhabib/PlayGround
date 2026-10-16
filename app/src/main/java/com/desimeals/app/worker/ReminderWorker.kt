package com.desimeals.app.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.desimeals.app.data.Repository
import kotlinx.coroutines.flow.first

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val repo = Repository.get(applicationContext)
            val balances = repo.allCustomerBalances()
            val overdue = balances.values.count { it > 0.005 }
            val prefs = com.desimeals.app.util.prefsFlow(applicationContext).first()
            if (overdue > 0 && (prefs.notifOverdue || prefs.notifDue)) {
                val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val n = NotificationCompat.Builder(applicationContext, "reminders")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Desi Meals: $overdue customers have dues")
                    .setContentText("Review overdue balances and send payment reminders.")
                    .setAutoCancel(true)
                    .build()
                try { nm.notify(1001, n) } catch (_: Exception) { }
            }
            Result.success()
        } catch (_: Exception) { Result.success() }
    }
}
