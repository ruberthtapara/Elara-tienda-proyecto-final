package com.example.proyect_final.util

object GenderUtils {
    /**
     * Intelligently determines the gender of a product based on its title, category, and description.
     */
    fun determineGender(title: String, category: String, description: String, existingGender: String? = null): String {
        // If there's an existing valid gender in the DB that isn't empty or default "Unisex", use it
        if (!existingGender.isNullOrBlank() && 
            !existingGender.equals("Unisex", ignoreCase = true) && 
            (existingGender.equals("Hombre", ignoreCase = true) || existingGender.equals("Mujer", ignoreCase = true))
        ) {
            return existingGender
        }

        val titleLower = title.lowercase()
        val descLower = description.lowercase()
        val catLower = category.lowercase()
        
        // 1. Explicit indicators in title or description
        if (titleLower.contains("mujer") || titleLower.contains("dama") || titleLower.contains("women") || titleLower.contains("lady") ||
            descLower.contains("mujer") || descLower.contains("dama") || descLower.contains("women") || descLower.contains("lady") ||
            titleLower.contains("aros") || titleLower.contains("aretes") || titleLower.contains("vestido") || titleLower.contains("falda")
        ) {
            return "Mujer"
        }
        
        if (titleLower.contains("hombre") || titleLower.contains("caballero") || titleLower.contains("varon") || titleLower.contains("varón") ||
            titleLower.contains("men") || titleLower.contains("gentleman") ||
            descLower.contains("hombre") || descLower.contains("caballero") || descLower.contains("varon") || descLower.contains("varón") ||
            descLower.contains("men")
        ) {
            return "Hombre"
        }
        
        // 2. Specific item type checking in Title
        if (titleLower.contains("dress") || titleLower.contains("skirt") || titleLower.contains("blouse") || 
            titleLower.contains("blusa") || titleLower.contains("crop top") || titleLower.contains("heels") || 
            titleLower.contains("tacones") || titleLower.contains("tacos") || titleLower.contains("cartera") || 
            titleLower.contains("purse") || titleLower.contains("collar") || titleLower.contains("necklace") || 
            titleLower.contains("pendant") || titleLower.contains("joya") || titleLower.contains("ring") || 
            titleLower.contains("anillo") || titleLower.contains("aros ") || titleLower.contains("pendientes")
        ) {
            return "Mujer"
        }
        
        if (titleLower.contains("camisa") || titleLower.contains("saco") || titleLower.contains("blazer") ||
            titleLower.contains("corbata") || titleLower.contains("billetera") || titleLower.contains("jeans 501") ||
            titleLower.contains("polo tommy") || titleLower.contains("polo nike") || titleLower.contains("saco de vestir") ||
            titleLower.contains("boxer") || titleLower.contains("bóxer") || titleLower.contains("suit") || 
            titleLower.contains("terno")
        ) {
            return "Hombre"
        }

        // 3. Fallbacks based on category/subcategory names
        if (catLower.contains("vestido") || catLower.contains("falda") || catLower.contains("bags") || catLower.contains("bolso")) {
            return "Mujer"
        }

        // Default
        return "Unisex"
    }
}
