package com.fitnix.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.data.Store
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterScreen(store: Store, onBack: () -> Unit) {
    val c = LocalPalette.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var custom by remember { mutableStateOf(false) }
    val ml = store.waterOn()
    val goal = store.data.profile.waterMl

    Column(Modifier.fillMaxSize()) {
        TopBar("Water", onBack)
        PillTabs(listOf("Today", "History"), tab, { tab = it }, Modifier.padding(horizontal = 20.dp))
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (tab == 0) {
                WaterRing(ml, goal)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (ml >= goal) "Goal reached — great job! 💧" else "${litres(goal - ml)} L to go",
                    color = if (ml >= goal) c.primary else c.textDim,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(250 to "+ 250 ml", 500 to "+ 500 ml", 1000 to "+ 1 L").forEach { (amount, label) ->
                        FCard(Modifier.weight(1f), onClick = { store.addWater(amount) }, padding = 12.dp) {
                            Text(label, color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Quick Add", color = c.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickTile(Icons.Rounded.LocalDrink, "Glass", "250 ml", Modifier.weight(1f)) { store.addWater(250) }
                    QuickTile(Icons.Rounded.WaterDrop, "Bottle", "500 ml", Modifier.weight(1f)) { store.addWater(500) }
                    QuickTile(Icons.Rounded.Water, "Large Bottle", "1 L", Modifier.weight(1f)) { store.addWater(1000) }
                    QuickTile(Icons.Rounded.Edit, "Custom", "ml", Modifier.weight(1f)) { custom = true }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { store.addWater(-250) }, enabled = ml > 0) {
                    Text("Undo 250 ml", color = if (ml > 0) c.textDim else c.stroke)
                }
                Spacer(Modifier.height(4.dp))
                FCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.WaterDrop, c.water)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Stay hydrated!", color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                "Water helps with performance, recovery and overall health. Drink more on training days and in hot weather.",
                                color = c.textDim, fontSize = 12.sp, lineHeight = 17.sp,
                            )
                        }
                    }
                }
            } else {
                WaterHistory(store, goal)
            }
        }
    }

    if (custom) {
        NumberDialog("Custom amount", "300", "ml", 10.0..3000.0, decimal = false, onDismiss = { custom = false }) {
            store.addWater(it.toInt())
        }
    }
}

@Composable
private fun WaterRing(ml: Int, goal: Int) {
    val c = LocalPalette.current
    val level by animateFloatAsState((ml.toFloat() / goal).coerceIn(0f, 1f), tween(900), label = "level")
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "phase",
    )
    Box(Modifier.size(232.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(c.cardAlt, 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            if (level > 0f) drawArc(c.water, -90f, 360f * level, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))

            val innerR = size.minDimension / 2 - stroke - 8.dp.toPx()
            val circle = Path().apply { addOval(Rect(center, innerR)) }
            clipPath(circle) {
                drawCircle(c.card, innerR, center)
                if (level > 0f) {
                    val top = center.y + innerR - 2 * innerR * level
                    val amp = 7.dp.toPx() * if (level in 0.03f..0.97f) 1f else 0.3f
                    fun wave(shift: Float, scale: Float) = Path().apply {
                        val left = center.x - innerR
                        val right = center.x + innerR
                        moveTo(left, size.height)
                        lineTo(left, top)
                        var x = left
                        while (x <= right) {
                            lineTo(x, top + amp * scale * sin((x - left) / innerR * 2.2f + phase + shift))
                            x += 4f
                        }
                        lineTo(right, size.height)
                        close()
                    }
                    drawPath(wave(PI.toFloat(), 0.8f), c.water.copy(alpha = 0.35f))
                    drawPath(
                        wave(0f, 1f),
                        Brush.verticalGradient(listOf(c.water.copy(alpha = 0.9f), Color(0xFF1D4ED8)), startY = top, endY = center.y + innerR),
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.WaterDrop, null, tint = if (level > 0.62f) Color.White else c.water, modifier = Modifier.size(30.dp))
            Text("${litres(ml)} L", color = if (level > 0.45f) Color.White else c.text, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("of ${litres(goal)} L", color = if (level > 0.35f) Color.White.copy(alpha = 0.85f) else c.textDim, fontSize = 13.sp)
        }
    }
}

@Composable
private fun QuickTile(icon: ImageVector, label: String, amount: String, modifier: Modifier, onClick: () -> Unit) {
    val c = LocalPalette.current
    FCard(modifier, onClick = onClick, padding = 10.dp) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = c.water, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, color = c.text, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(amount, color = c.textDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun WaterHistory(store: Store, goal: Int) {
    val c = LocalPalette.current
    val fmt = DateTimeFormatter.ofPattern("EEE d", Locale.US)
    val days = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    val values = days.map { store.waterOn(it.toString()) }
    val avg = values.average().toInt()
    val hit = values.count { it >= goal }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatBox("7-day average", "${litres(avg)} L", Modifier.weight(1f))
        StatBox("Goal reached", "$hit / 7 days", Modifier.weight(1f), c.primary)
    }
    Spacer(Modifier.height(14.dp))
    FCard(Modifier.fillMaxWidth()) {
        days.zip(values).reversed().forEach { (day, ml) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (day == LocalDate.now()) "Today" else day.format(fmt),
                    color = c.text, fontSize = 13.sp, modifier = Modifier.width(64.dp),
                )
                ProgressBar((ml.toFloat() / goal), if (ml >= goal) c.primary else c.water, Modifier.weight(1f), height = 10.dp)
                Text("${litres(ml)} L", color = c.textDim, fontSize = 12.sp, modifier = Modifier.width(56.dp).padding(start = 10.dp))
            }
        }
    }
}
