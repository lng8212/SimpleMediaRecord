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