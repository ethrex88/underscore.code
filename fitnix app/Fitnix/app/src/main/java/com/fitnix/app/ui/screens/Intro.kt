package com.fitnix.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.R
import com.fitnix.app.data.Goal
import com.fitnix.app.data.Health
import com.fitnix.app.data.Store
import com.fitnix.app.ui.*
import com.fitnix.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(800))
        delay(900)
        onDone()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF0D3A25), Color(0xFF040806)), radius = 1100f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val base = size.height * 0.82f
            for (i in 0 until 6) {
                val path = Path()
                val amp = 26f + i * 9f
                val shift = i * 0.55f
                path.moveTo(0f, base)
                var x = 0f
                while (x <= size.width) {
                    path.lineTo(x, base + i * 14f + amp * sin((x / size.width) * 2f * PI.toFloat() + shift))
                    x += 6f
                }
                drawPath(path, Color(0xFF5DE8A5).copy(alpha = 0.10f + i * 0.06f * appear.value), style = Stroke(2f + i * 0.5f))
            }
        }
        Image(
            painterResource(R.drawable.logo),
            contentDescription = "Fitnix",
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    alpha = appear.value
                    val s = 0.88f + 0.12f * appear.value
                    scaleX = s
                    scaleY = s
                },
        )
    }
}

private data class OnboardingPage(val first: String, val second: String, val body: String, val icon: ImageVector?)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = LocalPalette.current
    val pages = listOf(
        OnboardingPage("A Healthier", "Stronger You", "Track your nutrition, workouts, progress and build the best version of yourself.", null),
        OnboardingPage("Fuel Your", "Body Right", "Log Indian and everyday meals in seconds and hit your daily calorie and protein goals.", Icons.Rounded.Restaurant),
        OnboardingPage("Train &", "Transform", "Follow a Push Pull Legs plan, log every set and watch your progress grow week by week.", Icons.Rounded.FitnessCenter),
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
            if (last) Spacer(Modifier.height(48.dp)) else TextButton(onClick = onDone) { Text("Skip", color = c.textDim) }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { i ->
            val page = pages[i]
            Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.radialGradient(listOf(c.primary.copy(alpha = 0.28f), Color.Transparent))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (page.icon == null) {
                        Image(painterResource(R.drawable.logo), null, Modifier.size(250.dp))
                    } else {
                        Ring(progress = if (i == 1) 0.72f else 0.9f, diameter = 220.dp, stroke = 14.dp, color = c.primary) {
                            Icon(page.icon, null, tint = c.primary, modifier = Modifier.size(96.dp))
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text(page.first, color = c.text, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                Text(page.second, color = c.primary, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                Spacer(Modifier.height(12.dp))
                Text(page.body, color = c.textDim, fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(12.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                val sel = i == pager.currentPage
                val w by animateDpAsState(if (sel) 22.dp else 8.dp, label = "dot")
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(w)
                        .clip(CircleShape)
                        .background(if (sel) c.primary else c.stroke),
                )
            }
        }
        PrimaryButton(
            if (last) "Get Started" else "Next",
            Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
        }
    }
}

@Composable
fun SetupScreen(store: Store, edit: Boolean, onBack: (() -> Unit)?, onDone: () -> Unit) {
    val c = LocalPalette.current
    val start = store.data.profile
    var name by rememberSaveable { mutableStateOf(start.name) }
    var male by rememberSaveable { mutableStateOf(start.male) }
    var age by rememberSaveable { mutableIntStateOf(start.age) }
    var height by rememberSaveable { mutableIntStateOf(start.heightCm) }
    var weight by rememberSaveable { mutableDoubleStateOf(start.weightKg) }
    var goal by rememberSaveable { mutableStateOf(start.goal) }
    var editing by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = c.text)
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }
            Text(if (edit) "Edit your profile" else "Tell us about yourself", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("This helps us personalize your plan.", color = c.textDim, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(24) },
                label = { Text("Your name") },
                singleLine = true,
                colors = fieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard("Male", Icons.Rounded.Male, male, Modifier.weight(1f)) { male = true }
                ChoiceCard("Female", Icons.Rounded.Female, !male, Modifier.weight(1f)) { male = false }
            }
            Spacer(Modifier.height(16.dp))

            InfoRow("Age", "$age years") { editing = "age" }
            Spacer(Modifier.height(10.dp))
            InfoRow("Height", "$height cm") { editing = "height" }
            Spacer(Modifier.height(10.dp))
            InfoRow("Weight", "${weight.trimmed()} kg") { editing = "weight" }
            Spacer(Modifier.height(22.dp))

            Text("Your goal", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceCard("Build Muscle", Icons.Rounded.FitnessCenter, goal == Goal.BuildMuscle, Modifier.weight(1f), small = true) { goal = Goal.BuildMuscle }
                ChoiceCard("Lose Weight", Icons.Rounded.LocalFireDepartment, goal == Goal.LoseWeight, Modifier.weight(1f), small = true) { goal = Goal.LoseWeight }
                ChoiceCard("Stay Fit", Icons.Rounded.Favorite, goal == Goal.StayFit, Modifier.weight(1f), small = true) { goal = Goal.StayFit }
            }
            Spacer(Modifier.height(16.dp))

            val preview = Health.withTargets(start.copy(male = male, age = age, heightCm = height, weightKg = weight, goal = goal))
            FCard(Modifier.fillMaxWidth(), padding = 14.dp) {
                Text("Your daily targets", color = c.textDim, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${preview.calories.grouped()} kcal • ${preview.protein} g protein • ${preview.carbs} g carbs • ${preview.fat} g fat",
                    color = c.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Text("Water ${litres(preview.waterMl)} L • you can fine-tune these in Profile", color = c.textDim, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            if (edit) "Save" else "Continue",
            Modifier.padding(20.dp),
            enabled = name.isNotBlank(),
        ) {
            store.completeSetup(start.copy(name = name.trim(), male = male, age = age, heightCm = height, weightKg = weight, goal = goal))
            onDone()
        }
    }

    when (editing) {
        "age" -> NumberDialog("Age", age.toString(), "years", 13.0..100.0, decimal = false, onDismiss = { editing = null }) { age = it.toInt() }
        "height" -> NumberDialog("Height", height.toString(), "cm", 120.0..230.0, decimal = false, onDismiss = { editing = null }) { height = it.toInt() }
        "weight" -> NumberDialog("Weight", weight.trimmed(), "kg", 30.0..250.0, onDismiss = { editing = null }) { weight = it }
    }
}

@Composable
private fun ChoiceCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    onClick: () -> Unit,
) {
    val c = LocalPalette.current
    val shape = RoundedCornerShape(16.dp)
    val border by animateColorAsState(if (selected) c.primary else c.stroke, label = "border")
    Column(
        modifier
            .clip(shape)
            .background(if (selected) c.primary.copy(alpha = 0.12f) else c.card)
            .border(if (selected) 1.5.dp else 1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = if (small) 14.dp else 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = if (selected) c.primary else c.textDim, modifier = Modifier.size(if (small) 24.dp else 30.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = if (selected) c.text else c.textDim,
            fontSize = if (small) 12.sp else 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: () -> Unit) {
    val c = LocalPalette.current
    FCard(Modifier.fillMaxWidth(), onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, color = c.textDim, fontSize = 12.sp)
                Text(value, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.textDim)
        }
    }
}