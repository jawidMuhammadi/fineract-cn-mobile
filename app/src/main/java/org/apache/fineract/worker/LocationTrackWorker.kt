package org.apache.fineract.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.apache.fineract.R

/**
 * Created by Ahmad Jawid Muhammadi on 22/7/20.
 */

class LocationTrackWorker(context: Context, parameters: WorkerParameters) :
        CoroutineWorker(context, parameters) {

    private var broadcastReceiver: BroadcastReceiver? = null
    private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager

    override suspend fun doWork(): Result {
        val clientName = inputData.getString(KEY_CLIENT_NAME)
                ?: return Result.failure()

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == STOP_TRACKING) {
                    applicationContext.unregisterReceiver(broadcastReceiver)
                    notificationManager.cancel(ONGOING_NOTIFICATION_ID)
                }
            }

        }
        applicationContext.registerReceiver(broadcastReceiver, IntentFilter(STOP_TRACKING))
        setForeground(createForegroundInfo(clientName, applicationContext))
        return Result.success()
    }

    private fun download(inputUrl: String, outputFile: String) {
        // Downloads a file and updates bytes read
        // Calls setForegroundInfo() periodically when it needs to update
        // the ongoing Notification
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(clientName: String, context: Context): ForegroundInfo {
        val id = applicationContext.getString(R.string.notification_channel_id)
        val title = applicationContext.getString(R.string.notification_title)
        val cancel = applicationContext.getString(R.string.stop_tracking)
        // This PendingIntent can be used to cancel the worker
        val intent = Intent().setAction(STOP_TRACKING)
        val intentBroadcast = PendingIntent.getBroadcast(
                applicationContext, 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context)
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(clientName)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setOngoing(true)
                .setAutoCancel(false)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, cancel, intentBroadcast)
                .build()

        return ForegroundInfo(ONGOING_NOTIFICATION_ID, notification)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
                context.getString(R.string.notification_channel_id),
                context.getString(R.string.tracking_user_location),
                NotificationManager.IMPORTANCE_MAX
        )
        with(notificationManager) {
            createNotificationChannel(channel)
        }
    }


    companion object {
        const val KEY_CLIENT_NAME = "KEY_CLIENT_NAME"
        const val ONGOING_NOTIFICATION_ID = 10
        const val STOP_TRACKING = "stop_tracking"
    }


}