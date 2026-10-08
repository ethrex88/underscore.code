package com.fitnix.app.data

import kotlin.math.pow
import kotlin.math.roundToInt

enum class Goal(val label: String) {
    BuildMuscle("Build Muscle"),
    LoseWeight("Lose Weight"),
    StayFit("Stay Fit"),
}

enum class Meal(val label: String, val emoji: String) {
    Breakfast("Breakfast", "🍳"),
    Lunch("Lunch", "🍛"),
    Snacks("Snacks", "🍎"),
    Dinner("Dinner", "🍲"),
}

data class Profile(
    val name: String = "Ranjan",
    val male: Boolean = true,
    val age: Int = 25,
    val heightCm: Int = 175,
    val weightKg: Double = 72.0,
    val goal: Goal = Goal.BuildMuscle,
    val calories: Int = 2900,
    val protein: Int = 145,
    val carbs: Int = 375,
    val fat: Int = 90,
    val waterMl: Int = 3000,
    val darkMode: Boolean = true,
    val onboarded: Boolean = false,
)

data class FoodItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val unit: String,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val indian: Boolean = false,
) {
    val highProtein: Boolean get() = protein >= 10
}

data class LoggedFood(
    val foodId: Int,
    val name: String,
    val emoji: String,
    val meal: Meal,
    val qty: Double,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)

data class Macros(val kcal: Double = 0.0, val protein: Double = 0.0, val carbs: Double = 0.0, val fat: Double = 0.0)

fun List<LoggedFood>.totals() = Macros(sumOf { it.kcal }, sumOf { it.protein }, sumOf { it.carbs }, sumOf { it.fat })

data class WeightEntry(val date: String, val kg: Double)

data class SetEntry(val kg: Double, val reps: Int)

data class WorkoutSession(val date: String, val exercise: String, val sets: List<SetEntry>) {
    val volume: Double get() = sets.sumOf { it.kg * it.reps }
}

data class AppData(
    val profile: Profile = Profile(),
    val foods: Map<String, List<LoggedFood>> = emptyMap(),
    val water: Map<String, Int> = emptyMap(),
    val steps: Map<String, Int> = emptyMap(),
    val sleep: Map<String, Double> = emptyMap(),
    val weights: List<WeightEntry> = emptyList(),
    val workouts: List<WorkoutSession> = emptyList(),
    val favourites: Set<Int> = emptySet(),
    val recent: List<Int> = emptyList(),
    val planDay: Int = 0,
)

object Health {
    fun bmr(p: Profile): Double = 10 * p.weightKg + 6.25 * p.heightCm - 5 * p.age + if (p.male) 5 else -161

    fun tdee(p: Profile): Double = bmr(p) * 1.55

    fun bmi(p: Profile): Double = p.weightKg / (p.heightCm / 100.0).pow(2)

    fun bmiCategory(bmi: Double): String = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25 -> "Healthy"
        bmi < 30 -> "Overweight"
        else -> "Obese"
    }

    fun healthyWeightRange(p: Profile): Pair<Double, Double> {
        val m2 = (p.heightCm / 100.0).pow(2)
        return 18.5 * m2 to 24.9 * m2
    }

    /** Mifflin-St Jeor maintenance at a moderate activity level, adjusted for the goal. */
    fun withTargets(p: Profile): Profile {
        val adjust = when (p.goal) {
            Goal.BuildMuscle -> 300
            Goal.LoseWeight -> -450
            Goal.StayFit -> 0
        }
        val kcal = (((tdee(p) + adjust) / 50).roundToInt() * 50).coerceAtLeast(1200)
        val protein = (p.weightKg * if (p.goal == Goal.StayFit) 1.6 else 2.0).roundToInt()
        val fat = (kcal * 0.25 / 9).roundToInt()
        val carbs = ((kcal - protein * 4 - fat * 9) / 4.0).roundToInt().coerceAtLeast(50)
        val water = ((p.weightKg * 40 / 250).roundToInt() * 250).coerceIn(2000, 5000)
        return p.copy(calories = kcal, protein = protein, carbs = carbs, fat = fat, waterMl = water)
    }
}
