package com.fitnix.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.fitnix.app.data.Exercise
import com.fitnix.app.data.Meal
import com.fitnix.app.data.Store
import com.fitnix.app.ui.screens.*
import com.fitnix.app.ui.theme.LocalPalette

sealed interface Route {
    data object Splash : Route
    data object Onboarding : Route
    data class Setup(val edit: Boolean) : Route
    data object Main : Route
    data object Water : Route
    data class Log(val exercise: Exercise) : Route
}

@Composable
fun FitnixApp(store: Store) {
    val c = LocalPalette.current
    val stack = remember { mutableStateListOf<Route>(Route.Splash) }
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var foodMeal by remember { mutableStateOf<Meal?>(null) }
    var foodSearch by remember { mutableStateOf(false) }
    val route = stack.last()

    fun push(r: Route) = stack.add(r)
    fun pop() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun replace(r: Route) { stack.clear(); stack.add(r) }

    BackHandler(enabled = stack.size > 1 || (route == Route.Main && tab != Tab.Home)) {
        if (stack.size > 1) pop() else tab = Tab.Home
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime)),
    ) {
        AnimatedContent(
            targetState = route,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            label = "route",
        ) { r ->
            when (r) {
                Route.Splash -> SplashScreen {
                    replace(if (store.data.profile.onboarded) Route.Main else Route.Onboarding)
                }
                Route.Onboarding -> OnboardingScreen { push(Route.Setup(edit = false)) }
                is Route.Setup -> SetupScreen(
                    store = store,
                    edit = r.edit,
                    onBack = if (stack.size > 1) ({ pop() }) else null,
                    onDone = { if (r.edit) pop() else { tab = Tab.Home; replace(Route.Main) } },
                )
                Route.Main -> Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            Tab.Home -> HomeScreen(
                                store = store,
                                openWater = { push(Route.Water) },
                                openFood = { meal -> foodMeal = meal; foodSearch = meal != null; tab = Tab.Food },
                                openTab = { tab = it },
                            )
                            Tab.Food -> FoodScreen(store, foodMeal, foodSearch)
                            Tab.Train -> WorkoutScreen(store) { push(Route.Log(it)) }
                            Tab.Progress -> ProgressScreen(store)
                            Tab.Profile -> ProfileScreen(
                                store = store,
                                onEdit = { push(Route.Setup(edit = true)) },
                                openTab = { tab = it },
                                onReset = { tab = Tab.Home; replace(Route.Onboarding) },
                            )
                        }
                    }
                    BottomBar(tab) { t ->
                        if (t == Tab.Food) { foodMeal = null; foodSearch = false }
                        tab = t
                    }
                }
                Route.Water -> WaterScreen(store) { pop() }
                is Route.Log -> LogExerciseScreen(store, r.exercise) { pop() }
            }
        }
    }
}
