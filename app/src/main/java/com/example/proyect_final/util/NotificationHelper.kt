package com.example.proyect_final.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyect_final.R
import com.example.proyect_final.StyleGenApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object NotificationHelper {
    private const val CHANNEL_ID = "elara_notifications"
    private const val CHANNEL_NAME = "Notificaciones Elara"
    private const val CHANNEL_DESC = "Alertas y confirmaciones de pedidos de Elara AI"

    fun showOrderConfirmationNotification(context: Context, total: Double) {
        val application = context.applicationContext as StyleGenApplication
        val userProfileRepository = application.container.userProfileRepository

        CoroutineScope(Dispatchers.IO).launch {
            val preferences = userProfileRepository.getUserPreferences().firstOrNull()
            // Check if stock alerts / notifications are enabled (default is true)
            val notificationsEnabled = preferences?.stockAlerts ?: true

            if (notificationsEnabled) {
                showNotification(
                    context,
                    "¡Compra Exitosa! 🛍️",
                    "Tu pedido por un total de $${String.format("%.2f", total)} ha sido procesado con éxito. ¡Gracias por elegir Elara!"
                )
            }
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESC
            }
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            manager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
