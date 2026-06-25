package com.example.proyect_final.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.proyect_final.ui.widget.CartAppWidgetProvider

/**
 * MODIFICADO PARA EL EJERCICIO FINAL:
 * Clase utilitaria para actualizar el widget de carrito desde la lógica de la app principal.
 * Guarda de manera persistente la cantidad de prendas en SharedPreferences y dispara
 * un broadcast de actualización del WidgetManager.
 */
object WidgetUtils {

    fun updateCartWidget(context: Context, cartItemCount: Int) {
        try {
            // 1. Guardar de forma persistente la cantidad en SharedPreferences
            val prefs = context.getSharedPreferences("stylegen_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("cart_item_count", cartItemCount).apply()

            // 2. Disparar intent para forzar al AppWidgetProvider a actualizarse
            val intent = Intent(context, CartAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CartAppWidgetProvider::class.java)
            )
            
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("WidgetUtils", "Error actualizando widget de carrito: ${e.message}", e)
        }
    }
}
