package com.fitnix.app.data

object FoodDb {
    val items = listOf(
        FoodItem(1, "Chicken Breast", "🍗", "100 g cooked", 165.0, 31.0, 0.0, 3.6),
        FoodItem(2, "Boiled Egg", "🥚", "1 large", 78.0, 6.3, 0.6, 5.3),
        FoodItem(3, "Paneer", "🧀", "100 g", 265.0, 18.0, 1.2, 20.8, indian = true),
        FoodItem(4, "Brown Rice (cooked)", "🍚", "1 cup", 216.0, 5.0, 45.0, 1.8),
        FoodItem(5, "Roti (Wheat)", "🫓", "1 piece", 104.0, 3.1, 18.0, 2.4, indian = true),
        FoodItem(6, "Dal (Toor)", "🍲", "1 cup", 200.0, 12.0, 30.0, 4.0, indian = true),
        FoodItem(7, "White Rice (cooked)", "🍚", "1 cup", 205.0, 4.3, 45.0, 0.4, indian = true),
        FoodItem(8, "Curd", "🥣", "150 g", 92.0, 5.3, 7.0, 4.8, indian = true),
        FoodItem(9, "Milk (Toned)", "🥛", "250 ml", 145.0, 8.0, 12.0, 7.5, indian = true),
        FoodItem(10, "Banana", "🍌", "1 medium", 105.0, 1.3, 27.0, 0.4),
        FoodItem(11, "Oats", "🥣", "50 g dry", 190.0, 6.5, 33.0, 3.5),
        FoodItem(12, "Peanut Butter", "🥜", "1 tbsp (16 g)", 94.0, 4.0, 3.2, 8.0),
        FoodItem(13, "Almonds", "🌰", "20 g", 116.0, 4.2, 4.3, 10.0),
        FoodItem(14, "Whey Protein", "🥤", "1 scoop (30 g)", 120.0, 24.0, 3.0, 1.5),
        FoodItem(15, "Soy Chunks", "🫘", "50 g dry", 172.0, 26.0, 16.0, 0.3, indian = true),
        FoodItem(16, "Rajma Curry", "🍛", "1 cup", 240.0, 13.0, 35.0, 6.0, indian = true),
        FoodItem(17, "Chole", "🍛", "1 cup", 270.0, 12.0, 40.0, 7.0, indian = true),
        FoodItem(18, "Poha", "🍽️", "1 plate", 250.0, 5.0, 45.0, 6.0, indian = true),
        FoodItem(19, "Idli", "⚪", "2 pieces", 116.0, 4.0, 24.0, 0.4, indian = true),
        FoodItem(20, "Plain Dosa", "🥞", "1 dosa", 168.0, 4.0, 29.0, 3.7, indian = true),
        FoodItem(21, "Upma", "🍲", "1 cup", 250.0, 6.0, 40.0, 7.0, indian = true),
        FoodItem(22, "Roasted Chana", "🫘", "40 g", 150.0, 8.0, 24.0, 2.5, indian = true),
        FoodItem(23, "Sprouts Salad", "🥗", "1 cup", 100.0, 7.0, 16.0, 0.5, indian = true),
        FoodItem(24, "Fish Curry", "🐟", "1 cup", 220.0, 22.0, 6.0, 12.0, indian = true),
        FoodItem(25, "Chicken Curry", "🍛", "1 cup", 300.0, 28.0, 8.0, 17.0, indian = true),
        FoodItem(26, "Egg Bhurji", "🍳", "2 eggs", 210.0, 13.0, 4.0, 16.0, indian = true),
        FoodItem(27, "Apple", "🍎", "1 medium", 95.0, 0.5, 25.0, 0.3),
        FoodItem(28, "Guava", "🍐", "1 medium", 68.0, 2.6, 14.0, 1.0, indian = true),
        FoodItem(29, "Sweet Potato", "🍠", "150 g", 130.0, 2.4, 30.0, 0.2),
        FoodItem(30, "Greek Yogurt", "🥣", "170 g", 100.0, 17.0, 6.0, 0.7),
        FoodItem(31, "Whole-wheat Bread", "🍞", "1 slice", 80.0, 3.0, 14.0, 1.0),
        FoodItem(32, "Vegetable Sabzi", "🥦", "1 cup", 150.0, 4.0, 15.0, 8.0, indian = true),
        FoodItem(33, "Mango", "🥭", "1 cup", 99.0, 1.4, 25.0, 0.6, indian = true),
        FoodItem(34, "Sweet Lassi", "🥛", "250 ml", 220.0, 8.0, 34.0, 6.0, indian = true),
        FoodItem(35, "Tofu", "🧈", "100 g", 145.0, 15.7, 3.5, 8.7),
        FoodItem(36, "Peanuts", "🥜", "30 g", 170.0, 7.7, 4.8, 14.6, indian = true),
        FoodItem(37, "Moong Dal Chilla", "🥞", "2 pieces", 220.0, 14.0, 30.0, 5.0, indian = true),
        FoodItem(38, "Chicken Biryani", "🍛", "1 plate", 490.0, 25.0, 60.0, 16.0, indian = true),
    )

    fun byId(id: Int) = items.firstOrNull { it.id == id }
}

data class Exercise(
    val name: String,
    val muscle: String,
    val equipment: String,
    val type: String,
    val sets: Int,
    val reps: String,
)

object Workouts {
    const val PLAN_NAME = "Push Pull Legs"

    val dayNames = listOf("Push", "Pull", "Legs")

    val days: List<List<Exercise>> = listOf(
        listOf(
            Exercise("Bench Press", "Chest", "Barbell", "Compound", 4, "8-10"),
            Exercise("Incline Dumbbell Press", "Chest", "Dumbbell", "Compound", 3, "8-12"),
            Exercise("Shoulder Press", "Shoulders", "Dumbbell", "Compound", 3, "8-10"),
            Exercise("Lateral Raise", "Shoulders", "Dumbbell", "Isolation", 4, "12-15"),
            Exercise("Triceps Pushdown", "Arms", "Cable", "Isolation", 3, "10-12"),
        ),
        listOf(
            Exercise("Deadlift", "Back", "Barbell", "Compound", 3, "5-6"),
            Exercise("Pull-ups", "Back", "Bodyweight", "Compound", 3, "6-10"),
            Exercise("Barbell Row", "Back", "Barbell", "Compound", 4, "8-10"),
            Exercise("Face Pull", "Shoulders", "Cable", "Isolation", 3, "12-15"),
            Exercise("Biceps Curl", "Arms", "Dumbbell", "Isolation", 3, "10-12"),
        ),
        listOf(
            Exercise("Back Squat", "Legs", "Barbell", "Compound", 4, "6-8"),
            Exercise("Romanian Deadlift", "Legs", "Barbell", "Compound", 3, "8-10"),
            Exercise("Leg Press", "Legs", "Machine", "Compound", 3, "10-12"),
            Exercise("Leg Curl", "Legs", "Machine", "Isolation", 3, "12-15"),
            Exercise("Calf Raise", "Legs", "Machine", "Isolation", 4, "12-15"),
        ),
    )

    val library: List<Exercise> = days.flatten() + listOf(
        Exercise("Push-ups", "Chest", "Bodyweight", "Compound", 3, "12-20"),
        Exercise("Cable Chest Fly", "Chest", "Cable", "Isolation", 3, "12-15"),
        Exercise("Lat Pulldown", "Back", "Cable", "Compound", 3, "10-12"),
        Exercise("Seated Cable Row", "Back", "Cable", "Compound", 3, "10-12"),
        Exercise("Walking Lunges", "Legs", "Dumbbell", "Compound", 3, "10-12"),
        Exercise("Hammer Curl", "Arms", "Dumbbell", "Isolation", 3, "10-12"),
        Exercise("Overhead Triceps Extension", "Arms", "Dumbbell", "Isolation", 3, "10-12"),
        Exercise("Plank", "Core", "Bodyweight", "Isolation", 3, "45-60"),
        Exercise("Hanging Leg Raise", "Core", "Bodyweight", "Isolation", 3, "10-15"),
    )

    val muscles = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core")
}
