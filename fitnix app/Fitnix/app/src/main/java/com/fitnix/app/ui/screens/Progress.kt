package com.fitnix.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.data.Health
import com.fitnix.app.data.Store
import com.fitnix.app.data.WeightEntry
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun ProgressScreen(store: Store) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Progress")
        PillTabs(listOf("Weight", "Body Stats", "Workouts"), tab, { tab = it }, Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        when (tab) {
            0 -> WeightTab(store)
            1 -> BodyTab(store)
            else -> TrainingTab(store)
        }
    }
}

@Composable
private fun WeightTab(store: Store) {
    val c = LocalPalette.current
    val weights = store.data.weights
    var range by rememberSaveable { mutableIntStateOf(1) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<WeightEntry?>(null) }

    val days = when (range) { 0 -> 7L; 1 -> 30L; else -> 365L }
    val cutoff = LocalDate.now().minusDays(days).toString()
    val shown = weights.filter { it.date >= cutoff }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PillTabs(listOf("Week", "Month", "Year"), range, { range = it }) }
        item {
            FCard(Modifier.fillMaxWidth(), padding = 12.dp) {
                WeightChart(shown)
            }
        }
        item {
            val start = weights.firstOrNull()?.kg
            val current = weights.lastOrNull()?.kg ?: store.data.profile.weightKg
            val change = if (start != null) current - start else 0.0
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Start Weight", "${(start ?: current).trimmed()} kg", Modifier.weight(1f))
                StatBox("Current Weight", "${current.trimmed()} kg", Modifier.weight(1f))
                StatBox(
                    "Change",
                    "${if (change > 0) "+" else ""}${change.trimmed()} kg",
                    Modifier.weight(1f),
                    if (change == 0.0) c.text else c.primary,
                )
            }
        }
        item { SectionHeader("Weight Log", "+ Add") { adding = true } }
        if (weights.isEmpty()) {
            item { EmptyState("⚖️", "No weigh-ins yet", "Add your weight regularly — weekly averages show the real trend.") }
        }
        items(weights.asReversed(), key = { it.date }) { entry ->
            FCard(Modifier.fillMaxWidth(), padding = 4.dp) {
                Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(prettyDate(entry.date), color = c.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("${entry.kg.trimmed()} kg", color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { deleting = entry }) {
                        Icon(Icons.Rounded.DeleteOutline, "Delete", tint = c.textDim, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (adding) {
        NumberDialog("Today's weight", store.data.profile.weightKg.trimmed(), "kg", 30.0..250.0, onDismiss = { adding = false }) {
            store.addWeight(it)
        }
    }
    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete weigh-in?") },
            text = { Text("${entry.kg.trimmed()} kg on ${prettyDate(entry.date)} will be removed.") },
            confirmButton = {
                TextButton(onClick = { store.removeWeight(entry); deleting = null }) { Text("Delete", color = c.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel", color = c.textDim) } },
        )
    }
}

@Composable
private fun WeightChart(entries: List<WeightEntry>) {
    val c = LocalPalette.current
    if (entries.size < 2) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text(
                "Add at least two weigh-ins in this range to see your trend 📈",
                color = c.textDim, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp),
            )
        }
        return
    }
    val measurer = rememberTextMeasurer()
    val minV = floor(entries.minOf { it.kg } - 1)
    val maxV = ceil(entries.maxOf { it.kg } + 1)
    val labelStyle = TextStyle(color = c.textDim, fontSize = 10.sp)
    val valueStyle = TextStyle(color = c.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)

    Canvas(Modifier.fillMaxWidth().height(220.dp)) {
        val left = 30.dp.toPx()
        val right = size.width - 12.dp.toPx()
        val top = 30.dp.toPx()
        val bottom = size.height - 22.dp.toPx()

        for (i in 0..3) {
            val v = minV + (maxV - minV) * i / 3
            val y = bottom - (bottom - top) * i / 3
            drawLine(c.stroke, Offset(left, y), Offset(right, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
            drawText(measurer, v.fmt(0), Offset(0f, y - 7.dp.toPx()), labelStyle)
        }

        val t0 = LocalDate.parse(entries.first().date).toEpochDay()
        val t1 = LocalDate.parse(entries.last().date).toEpochDay()
        val span = (t1 - t0).coerceAtLeast(1).toFloat()
        val pts = entries.map {
            val x = left + (right - left) * (LocalDate.parse(it.date).toEpochDay() - t0) / span
            val y = bottom - (bottom - top) * ((it.kg - minV) / (maxV - minV)).toFloat()
            Offset(x, y)
        }

        val line = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(pts.last().x, bottom)
            lineTo(pts.first().x, bottom)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(c.primary.copy(alpha = 0.35f), Color.Transparent), top, bottom))
        drawPath(line, c.primary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        pts.forEach { drawCircle(c.primary, 3.dp.toPx(), it) }

        val lastPt = pts.last()
        drawCircle(c.primary.copy(alpha = 0.25f), 9.dp.toPx(), lastPt)
        drawCircle(c.primary, 5.dp.toPx(), lastPt)
        val label = "${entries.last().kg.trimmed()} kg"
        val layout = measurer.measure(label, valueStyle)
        val lx = (lastPt.x - layout.size.width / 2f).coerceIn(left, size.width - layout.size.width.toFloat())
        drawText(layout, topLeft = Offset(lx, (lastPt.y - 26.dp.toPx()).coerceAtLeast(0f)))

        val xLabels = listOf(entries.first(), entries[entries.size / 2], entries.last()).distinct()
        xLabels.forEachIndexed { i, e ->
            val p = pts[entries.indexOf(e)]
            val text = measurer.measure(shortDate(e.date), labelStyle)
            val x = when (i) {
                0 -> p.x
                xLabels.lastIndex -> p.x - text.size.width
                else -> p.x - text.size.width / 2f
            }.coerceIn(0f, size.width - text.size.width)
            drawText(text, topLeft = Offset(x, bottom + 6.dp.toPx()))
        }
    }
}

@Composable
private fun BodyTab(store: Store) {
    val c = LocalPalette.current
    val p = store.data.profile
    val bmi = Health.bmi(p)
    val (lo, hi) = Health.healthyWeightRange(p)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { InsightCard(Icons.Rounded.MonitorWeight, "BMI", "${bmi.fmt(1)} • ${Health.bmiCategory(bmi)}", "Based on ${p.heightCm} cm and ${p.weightKg.trimmed()} kg. BMI doesn't account for muscle mass.") }
        item { InsightCard(Icons.Rounded.Straighten, "Healthy weight range", "${lo.fmt(0)} – ${hi.fmt(0)} kg", "BMI 18.5 – 24.9 for your height.") }
        item { InsightCard(Icons.Rounded.LocalFireDepartment, "BMR", "${Health.bmr(p).grouped()} kcal", "Energy your body uses at complete rest.") }
        item { InsightCard(Icons.Rounded.Speed, "Maintenance (TDEE)", "${Health.tdee(p).grouped()} kcal", "Estimated with moderate activity (training 3–5 days a week).") }
        item {
            InsightCard(
                Icons.Rounded.EmojiEvents, "Daily targets",
                "${p.calories.grouped()} kcal • ${p.protein} g protein",
                "Carbs ${p.carbs} g • Fats ${p.fat} g • Water ${litres(p.waterMl)} L — goal: ${p.goal.label}",
            )
        }
        item {
            Text(
                "These are estimates, not medical advice. Adjust targets based on your weekly weight trend.",
                color = c.textDim, fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun TrainingTab(store: Store) {
    val c = LocalPalette.current
    val sessions = store.data.workouts
    val weekAgo = LocalDate.now().minusDays(7).toString()
    val thisWeek = sessions.filter { it.date > weekAgo }
    val best = sessions.groupBy { it.exercise }
        .mapValues { (_, list) -> list.maxOf { s -> s.sets.maxOfOrNull { it.kg } ?: 0.0 } }
        .entries.sortedByDescending { it.value }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Exercises logged", sessions.size.toString(), Modifier.weight(1f))
                StatBox("Training days", sessions.map { it.date }.distinct().size.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("This week", "${thisWeek.size} lifts", Modifier.weight(1f), c.primary)
                StatBox("Week volume", "${thisWeek.sumOf { it.volume }.grouped()} kg", Modifier.weight(1f))
            }
        }
        item { SectionHeader("Personal Bests") }
        if (best.isEmpty()) {
            item { EmptyState("🏆", "No records yet", "Your heaviest set for each exercise will show up here.") }
        }
        items(best, key = { it.key }) { (name, kg) ->
            FCard(Modifier.fillMaxWidth(), padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text("${kg.trimmed()} kg", color = c.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InsightCard(icon: ImageVector, title: String, value: String, note: String) {
    val c = LocalPalette.current
    FCard(Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, c.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = c.textDim, fontSize = 12.sp)
                Text(value, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(note, color = c.textDim, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}
