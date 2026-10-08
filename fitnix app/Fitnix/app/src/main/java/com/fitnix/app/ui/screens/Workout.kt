package com.fitnix.app.ui.screens

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Rowing
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.material.icons.rounded.SportsMma
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.data.Exercise
import com.fitnix.app.data.SetEntry
import com.fitnix.app.data.Store
import com.fitnix.app.data.Workouts
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun muscleIcon(muscle: String): ImageVector = when (muscle) {
    "Back" -> Icons.Rounded.Rowing
    "Legs" -> Icons.AutoMirrored.Rounded.DirectionsRun
    "Shoulders" -> Icons.Rounded.SportsGymnastics
    "Arms" -> Icons.Rounded.SportsMma
    "Core" -> Icons.Rounded.SelfImprovement
    else -> Icons.Rounded.FitnessCenter
}

@Composable
fun WorkoutScreen(store: Store, openLog: (Exercise) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Workout")
        PillTabs(listOf("Plans", "Exercises", "History"), tab, { tab = it }, Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        when (tab) {
            0 -> PlanTab(store, openLog)
            1 -> LibraryTab(openLog)
            else -> HistoryTab(store)
        }
    }
}

@Composable
private fun PlanTab(store: Store, openLog: (Exercise) -> Unit) {
    val c = LocalPalette.current
    val d = store.data
    val day = d.planDay
    val exercises = Workouts.days[day]
    val doneToday = d.workouts.filter { it.date == store.today }.map { it.exercise }.toSet()
    val week = d.workouts.firstOrNull()?.let {
        runCatching { ChronoUnit.WEEKS.between(LocalDate.parse(it.date), LocalDate.now()).toInt() + 1 }.getOrDefault(1)
    } ?: 1
    var menu by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FCard(Modifier.fillMaxWidth(), onClick = { menu = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Current Plan", color = c.textDim, fontSize = 12.sp)
                        Text(Workouts.PLAN_NAME, color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Week $week • Day ${day + 1} (${Workouts.dayNames[day]})", color = c.textDim, fontSize = 13.sp)
                    }
                    Box {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(c.cardAlt),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Rounded.KeyboardArrowDown, "Change day", tint = c.text) }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            Workouts.dayNames.forEachIndexed { i, name ->
                                DropdownMenuItem(
                                    text = { Text("Day ${i + 1} • $name", color = if (i == day) c.primary else c.text) },
                                    onClick = { store.setPlanDay(i); menu = false },
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            val next = exercises.firstOrNull { it.name !in doneToday }
            PrimaryButton(if (next == null) "Day complete — next day" else "Start Workout") {
                if (next == null) store.setPlanDay((day + 1) % Workouts.days.size) else openLog(next)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            SectionHeader("Today's Exercises", "${exercises.count { it.name in doneToday }}/${exercises.size} done")
        }
        items(exercises, key = { it.name }) { ex -> ExerciseRow(ex, ex.name in doneToday) { openLog(ex) } }
    }
}

@Composable
private fun ExerciseRow(ex: Exercise, done: Boolean, onClick: () -> Unit) {
    val c = LocalPalette.current
    FCard(Modifier.fillMaxWidth(), onClick = onClick, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(muscleIcon(ex.muscle), c.primary, size = 50.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ex.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${ex.sets} sets • ${ex.reps} ${if (ex.name == "Plank") "sec" else "reps"}", color = c.textDim, fontSize = 12.sp)
            }
            if (done) Icon(Icons.Rounded.CheckCircle, "Done", tint = c.primary)
            else Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.textDim)
        }
    }
}

@Composable
private fun LibraryTab(openLog: (Exercise) -> Unit) {
    var muscle by rememberSaveable { mutableIntStateOf(0) }
    val list = Workouts.library.filter { muscle == 0 || it.muscle == Workouts.muscles[muscle] }
    Column(Modifier.fillMaxSize()) {
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Workouts.muscles.size) { i -> FChip(Workouts.muscles[i], i == muscle) { muscle = i } }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(list, key = { it.name }) { ex -> ExerciseRow(ex, false) { openLog(ex) } }
        }
    }
}

@Composable
private fun HistoryTab(store: Store) {
    val c = LocalPalette.current
    val sessions = store.data.workouts.asReversed()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (sessions.isEmpty()) {
            item { EmptyState("🏋️", "No workouts yet", "Log your first exercise from the Plans tab and it will appear here.") }
        }
        items(sessions.size) { i ->
            val s = sessions[i]
            FCard(Modifier.fillMaxWidth(), padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.exercise, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(prettyDate(s.date), color = c.textDim, fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(s.sets.joinToString("  ·  ") { "${it.kg.trimmed()}×${it.reps}" }, color = c.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("${s.sets.size} sets • Volume ${s.volume.grouped()} kg", color = c.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private class SetRow(kg: String, reps: String) {
    var kg by mutableStateOf(kg)
    var reps by mutableStateOf(reps)
}

@Composable
fun LogExerciseScreen(store: Store, ex: Exercise, onClose: () -> Unit) {
    val c = LocalPalette.current
    val ctx = LocalContext.current
    val last = remember(ex.name) { store.lastSession(ex.name) }
    val rows = remember(ex.name) {
        mutableStateListOf<SetRow>().apply {
            if (last != null) last.sets.forEach { add(SetRow(it.kg.trimmed(), it.reps.toString())) }
            else repeat(ex.sets) { add(SetRow(if (ex.equipment == "Bodyweight") "0" else "20", ex.reps.substringAfter('-'))) }
        }
    }
    var restTotal by rememberSaveable { mutableIntStateOf(90) }
    var remaining by rememberSaveable { mutableIntStateOf(90) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        if (running && remaining == 0) {
            running = false
            remaining = restTotal
            ctx.getSystemService(Vibrator::class.java)?.vibrate(VibrationEffect.createOneShot(450, VibrationEffect.DEFAULT_AMPLITUDE))
            ctx.toast("Rest over — next set! 💪")
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = c.text) }
            Text("Log Exercise", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Close", color = c.primary, fontWeight = FontWeight.SemiBold) }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text(ex.name, color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FChip(ex.muscle, true)
                FChip(ex.equipment, false)
                FChip(ex.type, false)
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(c.primary.copy(alpha = 0.28f), c.card))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(muscleIcon(ex.muscle), null, tint = c.primary, modifier = Modifier.size(80.dp))
                Text(
                    if (last != null) "Last time: ${last.sets.joinToString(" · ") { "${it.kg.trimmed()}×${it.reps}" }}" else "Target: ${ex.sets} × ${ex.reps}",
                    color = c.text,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Set", color = c.textDim, fontSize = 12.sp, modifier = Modifier.width(44.dp))
                Text("Weight (kg)", color = c.textDim, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Text("Reps", color = c.textDim, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(6.dp))
            rows.forEachIndexed { i, row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(c.cardAlt),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    Spacer(Modifier.width(12.dp))
                    NumField(row.kg, { row.kg = it }, decimal = true, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    NumField(row.reps, { row.reps = it }, decimal = false, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (rows.size > 1) rows.removeAt(i) }, enabled = rows.size > 1) {
                        Icon(Icons.Rounded.Close, "Remove set", tint = if (rows.size > 1) c.textDim else c.stroke)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, c.stroke, RoundedCornerShape(14.dp))
                    .clickable { val l = rows.lastOrNull(); rows.add(SetRow(l?.kg ?: "20", l?.reps ?: "10")) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, null, tint = c.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Set", color = c.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))

            FCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Rest Timer", color = c.textDim, fontSize = 13.sp)
                        Text(
                            "%02d:%02d".format(remaining / 60, remaining % 60),
                            color = if (running) c.primary else c.text,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    SmallRound(Icons.Rounded.Remove) {
                        restTotal = (restTotal - 15).coerceAtLeast(15)
                        if (!running) remaining = restTotal else remaining = (remaining - 15).coerceAtLeast(1)
                    }
                    Spacer(Modifier.width(8.dp))
                    SmallRound(Icons.Rounded.Add) {
                        restTotal = (restTotal + 15).coerceAtMost(600)
                        if (!running) remaining = restTotal else remaining += 15
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(c.primary)
                            .clickable { running = !running },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (running) "Pause" else "Start", tint = c.onPrimary, modifier = Modifier.size(28.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton("Save Workout", Modifier.padding(20.dp), arrow = false) {
            val sets = rows.mapNotNull { r ->
                val kg = r.kg.toDoubleOrNull()
                val reps = r.reps.toIntOrNull()
                if (kg != null && reps != null && reps > 0) SetEntry(kg, reps) else null
            }
            if (sets.isEmpty()) {
                ctx.toast("Enter weight and reps for at least one set")
            } else {
                store.saveWorkout(ex.name, sets)
                ctx.toast("${ex.name} saved ✅")
                onClose()
            }
        }
    }
}

@Composable
private fun SmallRound(icon: ImageVector, onClick: () -> Unit) {
    val c = LocalPalette.current
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(c.cardAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.text, modifier = Modifier.size(18.dp)) }
}