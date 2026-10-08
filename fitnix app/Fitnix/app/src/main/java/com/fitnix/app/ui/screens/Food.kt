package com.fitnix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.data.FoodDb
import com.fitnix.app.data.FoodItem
import com.fitnix.app.data.Meal
import com.fitnix.app.data.Store
import com.fitnix.app.data.totals
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette
import java.time.LocalTime

private val filters = listOf("All", "Favourites", "Recent", "Indian", "High Protein")

private fun mealForNow(): Meal {
    val h = LocalTime.now().hour
    return when {
        h < 11 -> Meal.Breakfast
        h < 16 -> Meal.Lunch
        h < 19 -> Meal.Snacks
        else -> Meal.Dinner
    }
}

@Composable
fun FoodScreen(store: Store, presetMeal: Meal?, openSearch: Boolean) {
    var tab by rememberSaveable { mutableIntStateOf(if (openSearch) 1 else 0) }
    val defaultMeal = presetMeal ?: mealForNow()

    Column(Modifier.fillMaxSize()) {
        TopBar("Food") {
            IconButton(onClick = { tab = 1 }) {
                Icon(Icons.Rounded.Search, "Search", tint = LocalPalette.current.text)
            }
        }
        PillTabs(listOf("Log", "Search"), tab, { tab = it }, Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        if (tab == 0) FoodLog(store) { tab = 1 } else FoodSearch(store, defaultMeal)
    }
}

@Composable
private fun FoodLog(store: Store, openSearch: () -> Unit) {
    val c = LocalPalette.current
    val p = store.data.profile
    val logs = store.foodsOn()
    val t = logs.totals()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FCard(Modifier.fillMaxWidth()) {
                Text("Today's totals", color = c.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(t.kcal.grouped(), color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(" / ${p.calories.grouped()} kcal", color = c.textDim, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.height(10.dp))
                MacroLine("Protein", t.protein, p.protein, c.primary)
                MacroLine("Carbs", t.carbs, p.carbs, c.warning)
                MacroLine("Fats", t.fat, p.fat, c.info)
            }
        }
        if (logs.isEmpty()) {
            item {
                EmptyState("🍽️", "No meals logged today", "Search the food list and tap + to add your first meal.")
            }
            item { PrimaryButton("Search food", onClick = openSearch) }
        }
        Meal.entries.forEach { meal ->
            val entries = logs.withIndex().filter { it.value.meal == meal }
            if (entries.isNotEmpty()) {
                item(key = "h_${meal.name}") {
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${meal.emoji}  ${meal.label}", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("${entries.sumOf { it.value.kcal }.grouped()} kcal", color = c.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                items(entries, key = { "e_${it.index}_${it.value.name}" }) { (index, f) ->
                    FCard(Modifier.fillMaxWidth(), padding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmojiBubble(f.emoji, 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${f.name} × ${f.qty.trimmed()}", color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${f.kcal.grouped()} kcal • P ${f.protein.fmt(1)}g • C ${f.carbs.fmt(1)}g • F ${f.fat.fmt(1)}g",
                                    color = c.textDim, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { store.removeFood(store.today, index) }) {
                                Icon(Icons.Rounded.Close, "Remove", tint = c.textDim)
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Nutrition values are approximate. Home recipes vary, so adjust servings to match your portion.",
                color = c.textDim, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun MacroLine(label: String, value: Double, goal: Int, color: androidx.compose.ui.graphics.Color) {
    val c = LocalPalette.current
    Column(Modifier.padding(vertical = 4.dp)) {
        Row {
            Text(label, color = c.textDim, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${value.fmt(0)} / ${goal} g", color = c.text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        ProgressBar(if (goal > 0) (value / goal).toFloat() else 0f, color, height = 6.dp)
    }
}

@Composable
private fun FoodSearch(store: Store, defaultMeal: Meal) {
    val c = LocalPalette.current
    val ctx = LocalContext.current
    val d = store.data
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf<FoodItem?>(null) }

    val list = remember(query, filter, d.favourites, d.recent) {
        val matches = FoodDb.items.filter { it.name.contains(query.trim(), ignoreCase = true) }
        when (filter) {
            1 -> matches.filter { it.id in d.favourites }
            2 -> d.recent.mapNotNull { id -> matches.firstOrNull { it.id == id } }
            3 -> matches.filter { it.indian }
            4 -> matches.filter { it.highProtein }.sortedByDescending { it.protein / it.kcal }
            else -> matches
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it.take(40) },
            placeholder = { Text("Search food (e.g. chicken, dal, rice)") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(filters) { i, label -> FChip(label, i == filter) { filter = i } }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
            if (list.isEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    EmptyState(
                        if (filter == 1) "💚" else "🔍",
                        when (filter) {
                            1 -> "No favourites yet"
                            2 -> "Nothing recent"
                            else -> "No food found"
                        },
                        when (filter) {
                            1 -> "Tap a food and use the heart to save it here."
                            2 -> "Foods you add will show up here for quick access."
                            else -> "Try a different search term."
                        },
                    )
                }
            }
            items(list, key = { it.id }) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { adding = item }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmojiBubble(item.emoji)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                            if (item.id in d.favourites) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Rounded.Favorite, null, tint = c.error, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("${item.unit} • ${item.kcal.grouped()} kcal", color = c.textDim, fontSize = 12.sp)
                        Text("P ${item.protein.trimmed()}g   C ${item.carbs.trimmed()}g   F ${item.fat.trimmed()}g", color = c.textDim, fontSize = 11.sp)
                    }
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, c.primary, CircleShape)
                            .clickable { adding = item },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Add, "Add ${item.name}", tint = c.primary, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = c.stroke)
            }
        }
    }

    adding?.let { item ->
        AddFoodDialog(
            item = item,
            favourite = item.id in d.favourites,
            defaultMeal = defaultMeal,
            onToggleFavourite = { store.toggleFavourite(item.id) },
            onDismiss = { adding = null },
        ) { qty, meal ->
            store.addFood(item, qty, meal)
            ctx.toast("Added ${item.name} to ${meal.label}")
            adding = null
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddFoodDialog(
    item: FoodItem,
    favourite: Boolean,
    defaultMeal: Meal,
    onToggleFavourite: () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (Double, Meal) -> Unit,
) {
    val c = LocalPalette.current
    var qty by remember { mutableDoubleStateOf(1.0) }
    var meal by remember { mutableStateOf(defaultMeal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.emoji, fontSize = 26.sp)
                Spacer(Modifier.width(10.dp))
                Text(item.name, color = c.text, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleFavourite) {
                    Icon(
                        if (favourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        "Favourite",
                        tint = if (favourite) c.error else c.textDim,
                    )
                }
            }
        },
        text = {
            Column {
                Text("Serving: ${item.unit}", color = c.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    StepButton(Icons.Rounded.Remove) { qty = (qty - 0.5).coerceAtLeast(0.5) }
                    Text(
                        "${qty.trimmed()}×",
                        color = c.text, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    StepButton(Icons.Rounded.Add) { qty = (qty + 0.5).coerceAtMost(20.0) }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.cardAlt)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    NutrientCell("kcal", (item.kcal * qty).grouped())
                    NutrientCell("Protein", "${(item.protein * qty).fmt(1)}g")
                    NutrientCell("Carbs", "${(item.carbs * qty).fmt(1)}g")
                    NutrientCell("Fat", "${(item.fat * qty).fmt(1)}g")
                }
                Spacer(Modifier.height(14.dp))
                Text("Add to", color = c.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Meal.entries.forEach { m -> FChip(m.label, m == meal) { meal = m } }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(qty, meal) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text("Add food", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = c.textDim) } },
    )
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val c = LocalPalette.current
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(c.cardAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = c.primary)
    }
}

@Composable
private fun NutrientCell(label: String, value: String) {
    val c = LocalPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = c.textDim, fontSize = 11.sp)
    }
}
