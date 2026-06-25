package com.example.proyect_final.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.proyect_final.MainActivity
import com.example.proyect_final.R

/**
 * MODIFICADO PARA EL EJERCICIO FINAL:
 * Implementación del AppWidgetProvider para actualizar en tiempo real el estado del carrito.
 * Vincula los botones de RemoteViews con PendingIntents para atajos rápidos a las secciones
 * de compras y catálogo en la aplicación principal.
 */
class CartAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == action) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = android.content.ComponentName(context, CartAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("stylegen_prefs", Context.MODE_PRIVATE)
            val cartCount = prefs.getInt("cart_item_count", 0)

            val views = RemoteViews(context.packageName, R.layout.app_widget_cart)

            // Dynamic Personalized Welcome
            val profilePrefs = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
            val profileJson = profilePrefs.getString("user_profile", null)
            var userName = ""
            if (profileJson != null) {
                try {
                    val nameRegex = """"name"\s*:\s*"([^"]+)"""".toRegex()
                    val match = nameRegex.find(profileJson)
                    if (match != null) {
                        userName = match.groupValues[1].trim().substringBefore(" ")
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
            val titleText = if (userName.isNotEmpty()) "¡Hola, $userName! 👋" else "Elara AI"
            views.setTextViewText(R.id.widget_title_text, titleText)

            val cartText = if (cartCount == 1) {
                "Tienes 1 prenda en tu carrito"
            } else {
                "Tienes $cartCount prendas en tu carrito"
            }
            views.setTextViewText(R.id.widget_cart_count_text, cartText)

            val subText = if (cartCount > 0) {
                "¡Completa tu compra antes de que se agoten!"
            } else {
                "¡Explora las novedades de hoy en Elara!"
            }
            views.setTextViewText(R.id.widget_subtext, subText)

            // 1. PendingIntent for Cart
            val intentCart = Intent(context, MainActivity::class.java).apply {
                putExtra("EXTRA_NAV_DESTINATION", "cart")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentCart = PendingIntent.getActivity(
                context,
                1,
                intentCart,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_cart, pendingIntentCart)

            // 2. PendingIntent for AI Advisor
            val intentAdvisor = Intent(context, MainActivity::class.java).apply {
                putExtra("EXTRA_NAV_DESTINATION", "advisor")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentAdvisor = PendingIntent.getActivity(
                context,
                3,
                intentAdvisor,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_advisor, pendingIntentAdvisor)

            // 3. PendingIntent for Catalog
            val intentCatalog = Intent(context, MainActivity::class.java).apply {
                putExtra("EXTRA_NAV_DESTINATION", "catalog")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentCatalog = PendingIntent.getActivity(
                context,
                2,
                intentCatalog,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_catalog, pendingIntentCatalog)

            // 4. Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
