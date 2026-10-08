package com.fitnix.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class Store(context: Context) {
    private val prefs = context.getSharedPreferences("fitnix", Context.MODE_PRIVATE)

    var data by mutableStateOf(load())
        private set

    val today: String get() = LocalDate.now().toString()

    private fun load(): AppData =
        prefs.getString(KEY, null)?.let { runCatching { Json.decode(JSONObject(it)) }.getOrNull() } ?: AppData()

    private fun update(block: (AppData) -> AppData) {
        data = block(data)
        prefs.edit().putString(KEY, Json.encode(data).toString()).apply()
    }

    fun foodsOn(date: String = today) = data.foods[date].orEmpty()

    fun addFood(item: FoodItem, qty: Double, meal: Meal) = update { d ->
        val entry = LoggedFood(
            item.id, item.name, item.emoji, meal, qty,
            item.kcal * qty, item.protein * qty, item.carbs * qty, item.fat * qty,
        )
        d.copy(
            foods = d.foods + (today to (d.foods[today].orEmpty() + entry)),
            recent = (listOf(item.id) + d.recent.filter { it != item.id }).take(15),
        )
    }

    fun removeFood(date: String, index: Int) = update { d ->
        d.copy(foods = d.foods + (date to d.foods[date].orEmpty().filterIndexed { i, _ -> i != index }))
    }

    fun toggleFavourite(id: Int) = update { d ->
        d.copy(favourites = if (id in d.favourites) d.favourites - id else d.favourites + id)
    }

    fun waterOn(date: String = today) = data.water[date] ?: 0

    fun addWater(ml: Int) = update { d ->
        d.copy(water = d.water + (today to ((d.water[today] ?: 0) + ml).coerceIn(0, 20_000)))
    }

    fun setSteps(steps: Int) = update { it.copy(steps = it.steps + (today to steps)) }

    fun setSleep(hours: Double) = update { it.copy(sleep = it.sleep + (today to hours)) }

    fun addWeight(kg: Double) = update { d ->
        val list = (d.weights.filter { it.date != today } + WeightEntry(today, kg)).sortedBy { it.date }
        d.copy(weights = list, profile = d.profile.copy(weightKg = kg))
    }

    fun removeWeight(entry: WeightEntry) = update { d ->
        val list = d.weights - entry
        d.copy(weights = list, profile = d.profile.copy(weightKg = list.lastOrNull()?.kg ?: d.profile.weightKg))
    }

    fun saveWorkout(exercise: String, sets: List<SetEntry>) = update {
        it.copy(workouts = it.workouts + WorkoutSession(today, exercise, sets))
    }

    fun lastSession(exercise: String) = data.workouts.lastOrNull { it.exercise == exercise }

    fun setPlanDay(day: Int) = update { it.copy(planDay = day) }

    fun updateProfile(profile: Profile) = update { it.copy(profile = profile) }

    fun completeSetup(profile: Profile) = update { d ->
        val withTargets = Health.withTargets(profile).copy(onboarded = true, darkMode = d.profile.darkMode)
        val weights = (d.weights.filter { it.date != today } + WeightEntry(today, profile.weightKg)).sortedBy { it.date }
        d.copy(profile = withTargets, weights = weights)
    }

    fun exportJson(): String = Json.encode(data).toString(2)

    fun importJson(text: String): Boolean = runCatching {
        val restored = Json.decode(JSONObject(text.trim()))
        update { restored }
    }.isSuccess

    fun reset() = update { AppData(profile = Profile(darkMode = it.profile.darkMode)) }

    private companion object {
        const val KEY = "data"
    }
}

private object Json {
    fun encode(d: AppData): JSONObject = JSONObject().apply {
        put("version", 1)
        put("profile", JSONObject().apply {
            val p = d.profile
            put("name", p.name)
            put("male", p.male)
            put("age", p.age)
            put("height", p.heightCm)
            put("weight", p.weightKg)
            put("goal", p.goal.name)
            put("calories", p.calories)
            put("protein", p.protein)
            put("carbs", p.carbs)
            put("fat", p.fat)
            put("water", p.waterMl)
            put("dark", p.darkMode)
            put("onboarded", p.onboarded)
        })
        put("foods", JSONObject().apply {
            d.foods.forEach { (date, list) ->
                val arr = JSONArray()
                list.forEach { f ->
                    arr.put(
                        JSONObject()
                            .put("id", f.foodId).put("name", f.name).put("emoji", f.emoji)
                            .put("meal", f.meal.name).put("qty", f.qty).put("kcal", f.kcal)
                            .put("p", f.protein).put("c", f.carbs).put("f", f.fat)
                    )
                }
                put(date, arr)
            }
        })
        put("water", intMap(d.water))
        put("steps", intMap(d.steps))
        put("sleep", JSONObject().apply { d.sleep.forEach { (k, v) -> put(k, v) } })
        put("weights", JSONArray().apply {
            d.weights.forEach { put(JSONObject().put("date", it.date).put("kg", it.kg)) }
        })
        put("workouts", JSONArray().apply {
            d.workouts.forEach { w ->
                val sets = JSONArray()
                w.sets.forEach { s -> sets.put(JSONObject().put("kg", s.kg).put("reps", s.reps)) }
                put(JSONObject().put("date", w.date).put("exercise", w.exercise).put("sets", sets))
            }
        })
        put("favourites", JSONArray(d.favourites.toList()))
        put("recent", JSONArray(d.recent))
        put("planDay", d.planDay)
    }

    fun decode(o: JSONObject): AppData {
        val p = o.optJSONObject("profile") ?: JSONObject()
        val def = Profile()
        val profile = Profile(
            name = p.optString("name", def.name),
            male = p.optBoolean("male", def.male),
            age = p.optInt("age", def.age),
            heightCm = p.optInt("height", def.heightCm),
            weightKg = p.optDouble("weight", def.weightKg),
            goal = enumOr(p.optString("goal"), Goal.BuildMuscle),
            calories = p.optInt("calories", def.calories),
            protein = p.optInt("protein", def.protein),
            carbs = p.optInt("carbs", def.carbs),
            fat = p.optInt("fat", def.fat),
            waterMl = p.optInt("water", def.waterMl),
            darkMode = p.optBoolean("dark", true),
            onboarded = p.optBoolean("onboarded", false),
        )

        val foods = mutableMapOf<String, List<LoggedFood>>()
        o.optJSONObject("foods")?.let { fo ->
            fo.keys().forEach { date ->
                val arr = fo.optJSONArray(date) ?: JSONArray()
                foods[date] = (0 until arr.length()).mapNotNull { i ->
                    val f = arr.optJSONObject(i) ?: return@mapNotNull null
                    LoggedFood(
                        foodId = f.optInt("id"),
                        name = f.optString("name"),
                        emoji = f.optString("emoji"),
                        meal = enumOr(f.optString("meal"), Meal.Snacks),
                        qty = f.optDouble("qty", 1.0),
                        kcal = f.optDouble("kcal", 0.0),
                        protein = f.optDouble("p", 0.0),
                        carbs = f.optDouble("c", 0.0),
                        fat = f.optDouble("f", 0.0),
                    )
                }
            }
        }

        val sleep = mutableMapOf<String, Double>()
        o.optJSONObject("sleep")?.let { so -> so.keys().forEach { sleep[it] = so.optDouble(it, 0.0) } }

        val weights = o.optJSONArray("weights")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                arr.optJSONObject(i)?.let { WeightEntry(it.optString("date"), it.optDouble("kg", 0.0)) }
            }
        }.orEmpty()

        val workouts = o.optJSONArray("workouts")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val w = arr.optJSONObject(i) ?: return@mapNotNull null
                val setsArr = w.optJSONArray("sets") ?: JSONArray()
                val sets = (0 until setsArr.length()).mapNotNull { j ->
                    setsArr.optJSONObject(j)?.let { SetEntry(it.optDouble("kg", 0.0), it.optInt("reps", 0)) }
                }
                WorkoutSession(w.optString("date"), w.optString("exercise"), sets)
            }
        }.orEmpty()

        return AppData(
            profile = profile,
            foods = foods,
            water = readIntMap(o.optJSONObject("water")),
            steps = readIntMap(o.optJSONObject("steps")),
            sleep = sleep,
            weights = weights,
            workouts = workouts,
            favourites = readIntList(o.optJSONArray("favourites")).toSet(),
            recent = readIntList(o.optJSONArray("recent")),
            planDay = o.optInt("planDay", 0).coerceIn(0, Workouts.days.lastIndex),
        )
    }

    private fun intMap(map: Map<String, Int>) = JSONObject().apply { map.forEach { (k, v) -> put(k, v) } }

    private fun readIntMap(o: JSONObject?): Map<String, Int> {
        val m = mutableMapOf<String, Int>()
        o?.keys()?.forEach { m[it] = o.optInt(it) }
        return m
    }

    private fun readIntList(a: JSONArray?): List<Int> =
        if (a == null) emptyList() else (0 until a.length()).map { a.optInt(it) }

    private inline fun <reified T : Enum<T>> enumOr(name: String, fallback: T): T =
        runCatching { enumValueOf<T>(name) }.getOrDefault(fallback)
}
