package com.fitnix.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnix.app.ui.theme.LocalPalette

val CardShape = RoundedCornerShape(18.dp)

@Composable
fun FCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalPalette.current
    Column(
        modifier
            .clip(CardShape)
            .background(c.card)
            .border(1.dp, c.stroke, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    arrow: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val c = LocalPalette.current
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary,
            contentColor = c.onPrimary,
            disabledContainerColor = c.cardAlt,
            disabledContentColor = c.textDim,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        if (arrow) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
        }
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalPalette.current
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, c.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.primary),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun PillTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalPalette.current
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.card)
            .border(1.dp, c.stroke, shape)
            .padding(4.dp),
    ) {
        tabs.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (sel) c.primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (sel) c.onPrimary else c.textDim,
                    fontSize = 13.sp,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun FChip(text: String, selected: Boolean, onClick: (() -> Unit)? = null) {
    val c = LocalPalette.current
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) c.primary else c.card)
            .border(1.dp, if (selected) c.primary else c.stroke, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text,
            color = if (selected) c.onPrimary else c.text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
fun Ring(
    progress: Float,
    diameter: Dp,
    stroke: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    track: Color = LocalPalette.current.cardAlt,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val anim by animateFloatAsState(progress.coerceIn(0f, 1f), tween(800), label = "ring")
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val topLeft = Offset(s / 2, s / 2)
            val arcSize = Size(size.width - s, size.height - s)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(s))
            if (anim > 0f) {
                drawArc(color, -90f, 360f * anim, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

@Composable
fun ProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val c = LocalPalette.current
    val anim by animateFloatAsState(progress.coerceIn(0f, 1f), tween(800), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(c.cardAlt),
    ) {
        Box(
            Modifier
                .fillMaxWidth(anim)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    val c = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = if (onBack != null) 8.dp else 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = c.text)
            }
        }
        Text(
            title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = c.text,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    val c = LocalPalette.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                color = c.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun IconTile(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.52f))
    }
}

@Composable
fun EmojiBubble(emoji: String, size: Dp = 46.dp) {
    val c = LocalPalette.current
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(c.cardAlt),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.48f).sp)
    }
}

@Composable
fun MenuRow(
    icon: ImageVector,
    title: String,
    tint: Color = LocalPalette.current.primary,
    titleColor: Color = LocalPalette.current.text,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val c = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, tint, size = 36.dp)
        Spacer(Modifier.width(14.dp))
        Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (trailing != null) trailing() else {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.textDim)
        }
    }
}

@Composable
fun NumField(value: String, onChange: (String) -> Unit, decimal: Boolean, modifier: Modifier = Modifier) {
    val c = LocalPalette.current
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        value = value,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() || (decimal && it == '.') }.take(6)
            if (filtered.count { it == '.' } <= 1) onChange(filtered)
        },
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(c.cardAlt)
            .border(1.dp, c.stroke, shape),
        singleLine = true,
        textStyle = TextStyle(color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        cursorBrush = SolidColor(c.primary),
        decorationBox = { inner -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() } },
    )
}

@Composable
fun fieldColors(): TextFieldColors {
    val c = LocalPalette.current
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = c.primary,
        unfocusedBorderColor = c.stroke,
        cursorColor = c.primary,
        focusedTextColor = c.text,
        unfocusedTextColor = c.text,
        focusedContainerColor = c.card,
        unfocusedContainerColor = c.card,
        focusedLabelColor = c.primary,
        unfocusedLabelColor = c.textDim,
        focusedPlaceholderColor = c.textDim,
        unfocusedPlaceholderColor = c.textDim,
        focusedLeadingIconColor = c.textDim,
        unfocusedLeadingIconColor = c.textDim,
        focusedTrailingIconColor = c.textDim,
        unfocusedTrailingIconColor = c.textDim,
        focusedSuffixColor = c.textDim,
        unfocusedSuffixColor = c.textDim,
    )
}

@Composable
fun NumberDialog(
    title: String,
    initial: String,
    suffix: String,
    range: ClosedFloatingPointRange<Double>,
    decimal: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    val c = LocalPalette.current
    var text by remember { mutableStateOf(initial) }
    val value = text.toDoubleOrNull()
    val valid = value != null && value in range
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = c.text) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { raw ->
                        val f = raw.replace(',', '.').filter { it.isDigit() || (decimal && it == '.') }.take(7)
                        if (f.count { it == '.' } <= 1) text = f
                    },
                    singleLine = true,
                    suffix = { Text(suffix) },
                    keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
                    colors = fieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Allowed: ${range.start.trimmed()} – ${range.endInclusive.trimmed()} $suffix",
                    color = if (valid || text.isEmpty()) c.textDim else c.error,
                    fontSize = 12.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (valid) { onConfirm(value!!); onDismiss() } }, enabled = valid) {
                Text("Save", color = if (valid) c.primary else c.textDim, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = c.textDim) } },
    )
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    val c = LocalPalette.current
    FCard(modifier.fillMaxWidth(), padding = 24.dp) {
        Text(emoji, fontSize = 36.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        Text(title, color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            color = c.textDim,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = LocalPalette.current.text) {
    val c = LocalPalette.current
    FCard(modifier, padding = 14.dp) {
        Text(label, color = c.textDim, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Rounded.Home),
    Food("Food", Icons.Rounded.Restaurant),
    Train("Train", Icons.Rounded.FitnessCenter),
    Progress("Progress", Icons.Rounded.BarChart),
    Profile("Profile", Icons.Rounded.Person),
}

@Composable
fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    val c = LocalPalette.current
    Column(Modifier.fillMaxWidth().background(c.card)) {
        HorizontalDivider(color = c.stroke, thickness = 1.dp)
        Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
            Tab.entries.forEach { tab ->
                val sel = tab == selected
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) c.primary.copy(alpha = 0.16f) else Color.Transparent)
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                    ) {
                        Icon(tab.icon, tab.label, tint = if (sel) c.primary else c.textDim, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = if (sel) c.primary else c.textDim,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
