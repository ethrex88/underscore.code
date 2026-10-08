package com.fitnix.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.data.Health
import com.fitnix.app.data.Store
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette

@Composable
fun ProfileScreen(store: Store, onEdit: () -> Unit, openTab: (Tab) -> Unit, onReset: () -> Unit) {
    val c = LocalPalette.current
    val p = store.data.profile
    var dialog by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar("Profile") {
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Settings, "Settings", tint = c.text) }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(p.name, 62)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("${p.weightKg.trimmed()} kg • ${p.heightCm} cm • ${p.age} yrs", color = c.textDim, fontSize = 13.sp)
                        }
                        IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, "Edit profile", tint = c.textDim) }
                    }
                }
            }
            item {
                FCard(Modifier.fillMaxWidth(), onClick = onEdit, padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.FitnessCenter, c.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Goal", color = c.textDim, fontSize = 12.sp)
                            Text(p.goal.label, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Target: ${p.calories.grouped()} kcal • ${p.protein}g protein", color = c.textDim, fontSize = 12.sp)
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.textDim)
                    }
                }
            }
            item {
                FCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                    MenuRow(Icons.Rounded.Insights, "My Stats") { dialog = "stats" }
                    HorizontalDivider(color = c.stroke)
                    MenuRow(Icons.Rounded.Restaurant, "Nutrition Targets") { dialog = "targets" }
                    HorizontalDivider(color = c.stroke)
                    MenuRow(Icons.Rounded.FitnessCenter, "Workout Plans") { openTab(Tab.Train) }
                    HorizontalDivider(color = c.stroke)
                    MenuRow(Icons.Rounded.Backup, "Backup & Restore") { dialog = "backup" }
                    HorizontalDivider(color = c.stroke)
                    MenuRow(
                        Icons.Rounded.DarkMode, "Dark Mode",
                        trailing = {
                            Switch(
                                checked = p.darkMode,
                                onCheckedChange = { store.updateProfile(p.copy(darkMode = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = c.onPrimary,
                                    checkedTrackColor = c.primary,
                                    uncheckedThumbColor = c.textDim,
                                    uncheckedTrackColor = c.cardAlt,
                                    uncheckedBorderColor = c.stroke,
                                ),
                            )
                        },
                    ) { store.updateProfile(p.copy(darkMode = !p.darkMode)) }
                    HorizontalDivider(color = c.stroke)
                    MenuRow(Icons.Rounded.Info, "About Fitnix") { dialog = "about" }
                }
            }
            item {
                FCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                    MenuRow(Icons.Rounded.RestartAlt, "Reset all data", tint = c.error, titleColor = c.error) { dialog = "reset" }
                }
            }
            item {
                Text(
                    "Fitnix v1.0 • All data stays on this phone",
                    color = c.textDim, fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }

    when (dialog) {
        "stats" -> StatsDialog(store) { dialog = null }
        "targets" -> TargetsDialog(store) { dialog = null }
        "backup" -> BackupDialog(store) { dialog = null }
        "about" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("About Fitnix") },
            text = {
                Text(
                    "Fuel. Train. Transform.\n\nFitnix is a fully offline fitness companion: track meals, water, workouts and weight. " +
                        "No account, no ads, no internet needed — everything is stored privately on your phone.\n\nVersion 1.0",
                )
            },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Close", color = c.primary) } },
        )
        "reset" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Reset all data?") },
            text = { Text("This permanently deletes your profile, meals, water, workouts and weight history from this phone.") },
            confirmButton = {
                TextButton(onClick = { dialog = null; store.reset(); onReset() }) { Text("Reset", color = c.error, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel", color = c.textDim) } },
        )
    }
}

@Composable
private fun StatsDialog(store: Store, onDismiss: () -> Unit) {
    val c = LocalPalette.current
    val d = store.data
    val p = d.profile
    val bmi = Health.bmi(p)
    val rows = listOf(
        "BMI" to "${bmi.fmt(1)} (${Health.bmiCategory(bmi)})",
        "BMR" to "${Health.bmr(p).grouped()} kcal",
        "Maintenance" to "${Health.tdee(p).grouped()} kcal",
        "Days with meals logged" to d.foods.count { it.value.isNotEmpty() }.toString(),
        "Exercises logged" to d.workouts.size.toString(),
        "Weigh-ins" to d.weights.size.toString(),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Stats") },
        text = {
            Column {
                rows.forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(k, color = c.textDim, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(v, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = c.primary) } },
    )
}

@Composable
private fun TargetsDialog(store: Store, onDismiss: () -> Unit) {
    val c = LocalPalette.current
    val p = store.data.profile
    var kcal by remember { mutableStateOf(p.calories.toString()) }
    var protein by remember { mutableStateOf(p.protein.toString()) }
    var carbs by remember { mutableStateOf(p.carbs.toString()) }
    var fat by remember { mutableStateOf(p.fat.toString()) }
    var water by remember { mutableStateOf(p.waterMl.toString()) }

    val values = listOf(kcal, protein, carbs, fat, water).map { it.toIntOrNull() }
    val ranges = listOf(1000..6000, 20..400, 20..900, 10..300, 1000..8000)
    val valid = values.zip(ranges).all { (v, r) -> v != null && v in r }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nutrition Targets") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TargetField("Calories", kcal, "kcal") { kcal = it }
                TargetField("Protein", protein, "g") { protein = it }
                TargetField("Carbs", carbs, "g") { carbs = it }
                TargetField("Fats", fat, "g") { fat = it }
                TargetField("Water", water, "ml") { water = it }
                TextButton(onClick = {
                    val t = Health.withTargets(p)
                    kcal = t.calories.toString(); protein = t.protein.toString(); carbs = t.carbs.toString()
                    fat = t.fat.toString(); water = t.waterMl.toString()
                }) { Text("Recalculate from my profile", color = c.primary) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    store.updateProfile(
                        p.copy(calories = values[0]!!, protein = values[1]!!, carbs = values[2]!!, fat = values[3]!!, waterMl = values[4]!!),
                    )
                    onDismiss()
                },
            ) { Text("Save", color = if (valid) c.primary else c.textDim, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = c.textDim) } },
    )
}

@Composable
private fun TargetField(label: String, value: String, suffix: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit).take(5)) },
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = fieldColors(),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BackupDialog(store: Store, onDismiss: () -> Unit) {
    val c = LocalPalette.current
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var restoreText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup & Restore") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Save a copy of all your data. Share it to Drive, email or WhatsApp to keep it safe.", color = c.textDim, fontSize = 13.sp)
                SecondaryButton("Share backup") {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Fitnix backup")
                        putExtra(Intent.EXTRA_TEXT, store.exportJson())
                    }
                    ctx.startActivity(Intent.createChooser(send, "Save Fitnix backup"))
                }
                SecondaryButton("Copy backup to clipboard") {
                    clipboard.setText(AnnotatedString(store.exportJson()))
                    ctx.toast("Backup copied")
                }
                HorizontalDivider(color = c.stroke)
                Text("To restore, paste your backup text below. This replaces all current data.", color = c.textDim, fontSize = 13.sp)
                OutlinedTextField(
                    value = restoreText,
                    onValueChange = { restoreText = it },
                    placeholder = { Text("Paste backup here") },
                    maxLines = 4,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = restoreText.isNotBlank(),
                onClick = {
                    if (store.importJson(restoreText)) {
                        ctx.toast("Data restored ✅")
                        onDismiss()
                    } else {
                        ctx.toast("That doesn't look like a Fitnix backup")
                    }
                },
            ) { Text("Restore", color = if (restoreText.isNotBlank()) c.primary else c.textDim) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = c.textDim) } },
    )
}
