package com.fitnix.app.data

data class NutrientRef(val name: String, val amount: String, val source: String)

object Nutrients {
    fun vitamins(male: Boolean) = listOf(
        NutrientRef("Vitamin A", if (male) "900 mcg" else "700 mcg", "Carrots, spinach, sweet potato"),
        NutrientRef("Vitamin B1", if (male) "1.2 mg" else "1.1 mg", "Whole grains, dal"),
        NutrientRef("Vitamin B2", if (male) "1.3 mg" else "1.1 mg", "Milk, curd, eggs"),
        NutrientRef("Vitamin B3", if (male) "16 mg" else "14 mg", "Chicken, peanuts"),
        NutrientRef("Vitamin B5", "5 mg", "Eggs, mushrooms"),
        NutrientRef("Vitamin B6", "1.3 mg", "Chickpeas, banana"),
        NutrientRef("Biotin", "30 mcg", "Eggs, nuts"),
        NutrientRef("Folate", "400 mcg", "Leafy greens, dal"),
        NutrientRef("Vitamin B12", "2.4 mcg", "Eggs, milk, curd"),
        NutrientRef("Vitamin C", if (male) "90 mg" else "75 mg", "Amla, guava, lemon"),
        NutrientRef("Vitamin D", "15 mcg (600 IU)", "Sunlight, fortified milk, fish"),
        NutrientRef("Vitamin E", "15 mg", "Almonds, seeds"),
        NutrientRef("Vitamin K", if (male) "120 mcg" else "90 mcg", "Green vegetables"),
    )

    fun minerals(male: Boolean) = listOf(
        NutrientRef("Calcium", "1,000 mg", "Milk, curd, paneer"),
        NutrientRef("Iron", if (male) "8 mg" else "18 mg", "Dal, beans, spinach"),
        NutrientRef("Magnesium", if (male) "400 mg" else "310 mg", "Nuts, seeds, whole grains"),
        NutrientRef("Zinc", if (male) "11 mg" else "8 mg", "Seeds, meat, dal"),
        NutrientRef("Potassium", if (male) "3,400 mg" else "2,600 mg", "Potato, banana, beans"),
        NutrientRef("Phosphorus", "700 mg", "Dairy, dal, eggs"),
        NutrientRef("Iodine", "150 mcg", "Iodized salt"),
        NutrientRef("Selenium", "55 mcg", "Eggs, fish"),
        NutrientRef("Copper", "900 mcg", "Nuts, legumes"),
        NutrientRef("Manganese", if (male) "2.3 mg" else "1.8 mg", "Whole grains"),
        NutrientRef("Chromium", if (male) "35 mcg" else "25 mcg", "Whole grains"),
        NutrientRef("Molybdenum", "45 mcg", "Beans, dal"),
    )

    /** Highest-protein foods in the diary, per one serving. */
    fun proteinFoods(): List<NutrientRef> = FoodDb.items
        .sortedByDescending { it.protein }
        .take(12)
        .map {
            val grams = if (it.protein % 1.0 == 0.0) it.protein.toInt().toString() else String.format("%.1f", it.protein)
            NutrientRef(it.name, "$grams g", "${it.unit} • ${it.kcal.toInt()} kcal")
        }
}
