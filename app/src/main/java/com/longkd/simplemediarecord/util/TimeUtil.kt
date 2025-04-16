package com.longkd.simplemediarecord.util

import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun millisecondsToStopwatchString(milliseconds: Long): String {
    return milliseconds.toDuration(DurationUnit.MILLISECONDS)
        .toComponents { hours, minutes, seconds, _ ->
            if (hours == 0L) {
                "%02d:%02d".format(minutes, seconds)
            } else {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            }
        }
}

fun timeStringToMillis(time: String): Long {
    val parts = time.split(":")
    if (parts.size != 2) return 0L
    val minutes = parts[0].toLongOrNull() ?: 0L
    val seconds = parts[1].toLongOrNull() ?: 0L
    return (minutes * 60 + seconds) * 1000
}

fun calculateProgressPercent(current: String, total: String): Int {
    val currentMillis = timeStringToMillis(current)
    val totalMillis = timeStringToMillis(total)

    if (totalMillis == 0L) return 0
    return ((currentMillis.toDouble() / totalMillis) * 100).toInt()
}