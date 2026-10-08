package com.fitnix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.data.Meal
import com.fitnix.app.data.Nutrients
import com.fitnix.app.data.Store
import com.fitnix.app.data.totals
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette
import java.time.LocalTime
import kotlin.math.roundToInt

@Composable
fun HomeScreen(store: Store, openWater: () -> Unit, openFood: (Meal?) -> Unit, openTab: (Tab) -> Unit) {
    val c = LocalPalette.current
    val ctx = LocalContext.current
    val d = store.data
    val p = d.profile
    val logs = store.foodsOn()
    val t = logs.totals()
    var editing by remember { mutableStateOf<String?>(null) }
    var guide by rememberSaveable { mutableIntStateOf(0) }

    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting, color = c.textDim, fontSize = 14.sp)
                    Text("${p.name} 👋", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(headerDate(), color = c.textDim, fontSize = 12.sp)
                }
                IconButton(onClick = { ctx.toast("You're all caught up 🎉") }) {
                    Icon(Icons.Rounded.NotificationsNone, "Notifications", tint = c.text)
                }
                Avatar(p.name, 42) { openTab(Tab.Profile) }
            }
        }

        item {
            val progress = if (p.calories > 0) (t.kcal / p.calories).toFloat() else 0f
            val remaining = p.calories - t.kcal.roundToInt()
            FCard(Modifier.fillMaxWidth(), onClick = { openFood(null) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Calories", color = c.textDim, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(t.kcal.grouped(), color = c.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Text(
                                " / ${p.calories.grouped()} kcal",
                                color = c.textDim,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        ProgressBar(progress, c.primary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (remaining >= 0) "${remaining.grouped()} kcal remaining" else "${(-remaining).grouped()} kcal over target",
                            color = if (remaining >= 0) c.textDim else c.warning,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Ring(progress, 86.dp, 9.dp, c.primary) {
                        Text("${(progress * 100).roundToInt()}%", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroCard("Protein", t.protein, p.protein, c.primary, Modifier.weight(1f))
                MacroCard("Carbs", t.carbs, p.carbs, c.warning, Modifier.weight(1f))
                MacroCard("Fats", t.fat, p.fat, c.info, Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat(Icons.Rounded.WaterDrop, c.water, "Water", "${litres(store.waterOn())} / ${litres(p.waterMl)} L", Modifier.weight(1f), openWater)
                MiniStat(Icons.AutoMirrored.Rounded.DirectionsWalk, c.primary, "Steps", (d.steps[store.today] ?: 0).grouped(), Modifier.weight(1f)) { editing = "steps" }
                MiniStat(Icons.Rounded.Bedtime, c.info, "Sleep", d.sleep[store.today]?.let { sleepText(it) } ?: "—", Modifier.weight(1f)) { editing = "sleep" }
            }
        }

        item { SectionHeader("Today's Meals", "See all") { openFood(null) } }

        items(Meal.entries) { meal ->
            val entries = logs.filter { it.meal == meal }
            FCard(Modifier.fillMaxWidth(), onClick = { openFood(meal) }, padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiBubble(entries.firstOrNull()?.emoji ?: meal.emoji)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(meal.label, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (entries.isEmpty()) "Nothing logged yet" else entries.joinToString(", ") { it.name },
                            color = c.textDim,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (entries.isNotEmpty()) {
                            Text("${entries.sumOf { it.kcal }.grouped()} kcal", color = c.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(c.primary)
                            .clickable { openFood(meal) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Add, "Add to ${meal.label}", tint = c.onPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            SectionHeader("Vitamins & minerals")
        }
        item {
            Text(
                "Daily reference targets for adults. These are guides, not amounts measured from your meals.",
                color = c.textDim,
                fontSize = 12.sp,
            )
        }
        item {
            PillTabs(listOf("Vitamins", "Protein", "Minerals"), guide, { guide = it })
        }
        if (guide == 1) {
            item {
                FCard(Modifier.fillMaxWidth(), padding = 14.dp) {
                    Text("Your protein goal", color = c.textDim, fontSize = 12.sp)
                    Text("${p.protein} g / day", color = c.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Logged today: ${t.protein.roundToInt()} g. Best sources in the food list:",
                        color = c.textDim,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        items(
            when (guide) {
                0 -> Nutrients.vitamins(p.male)
                1 -> Nutrients.proteinFoods()
                else -> Nutrients.minerals(p.male)
            },
            key = { "${guide}_${it.name}" },
        ) { ref ->
            FCard(Modifier.fillMaxWidth(), padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(ref.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(ref.source, color = c.textDim, fontSize = 12.sp)
                    }
                    Text(ref.amount, color = c.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    when (editing) {
        "steps" -> NumberDialog(
            "Steps today", (d.steps[store.today] ?: 0).toString(), "steps", 0.0..100_000.0, decimal = false,
            onDismiss = { editing = null },
        ) { store.setSteps(it.toInt()) }
        "sleep" -> NumberDialog(
            "Sleep last night", (d.sleep[store.today] ?: 7.5).trimmed(), "hours", 0.0..16.0,
            onDismiss = { editing = null },
        ) { store.setSleep(it) }
    }
}

@Composable
fun Avatar(name: String, sizeDp: Int, onClick: (() -> Unit)? = null) {
    val c = LocalPalette.current
    Box(
        Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(c.primary, c.primaryDark)))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "F",
            color = Color(0xFF04140B),
            fontWeight = FontWeight.Bold,
            fontSize = (sizeDp * 0.42f).sp,
        )
    }
}

@Composable
private fun MacroCard(label: String, value: Double, goal: Int, color: Color, modifier: Modifier) {
    val c = LocalPalette.current
    val progress = if (goal > 0) (value / goal).toFloat() else 0f
    FCard(modifier, padding = 12.dp) {
        Text(label, color = c.textDim, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value.roundToInt().toString(), color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(" /${goal}g", color = c.textDim, fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(8.dp))
        Ring(progress, 46.dp, 6.dp, color, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun MiniStat(icon: ImageVector, tint: Color, label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    val c = LocalPalette.current
    FCard(modifier, onClick = onClick, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = c.textDim, fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
