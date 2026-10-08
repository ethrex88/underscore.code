package com.fitnix.app.ui

import android.content.Context
import android.widget.Toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun Double.fmt(decimals: Int = 0): String = String.format(Locale.US, "%.${decimals}f", this)

fun Double.trimmed(): String =
    if (this % 1.0 == 0.0) toLong().toString() else String.format(Locale.US, "%.1f", this)

fun Int.grouped(): String = String.format(Locale.US, "%,d", this)

fun Double.grouped(): String = roundToInt().grouped()

fun litres(ml: Int): String = when {
    ml % 1000 == 0 -> (ml / 1000).toString()
    ml % 100 == 0 -> (ml / 1000.0).fmt(1)
    else -> (ml / 1000.0).fmt(2)
}

fun sleepText(hours: Double): String {
    val totalMin = (hours * 60).roundToInt()
    return "${totalMin / 60}h ${totalMin % 60}m"
}

private val longDate = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
private val shortDateFmt = DateTimeFormatter.ofPattern("d MMM", Locale.US)
private val headerDateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.US)

fun prettyDate(iso: String): String = runCatching { LocalDate.parse(iso).format(longDate) }.getOrDefault(iso)

fun shortDate(iso: String): String = runCatching { LocalDate.parse(iso).format(shortDateFmt) }.getOrDefault(iso)

fun headerDate(): String = LocalDate.now().format(headerDateFmt)

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
