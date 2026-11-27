package com.metrolist.shared.playback

import com.metrolist.shared.settings.Settings
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object StreakTracker {
    /**
     * Call when a song plays to track daily music listening streaks.
     */
    fun recordListeningSession() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val lastDateStr = Settings.shared.lastListeningDate
        val currentStreak = Settings.shared.listeningStreakDays

        if (lastDateStr.isBlank()) {
            Settings.shared.listeningStreakDays = 1
            Settings.shared.lastListeningDate = today.toString()
            return
        }

        val lastDate = runCatching { LocalDate.parse(lastDateStr) }.getOrNull()
        if (lastDate == null) {
            Settings.shared.listeningStreakDays = 1
            Settings.shared.lastListeningDate = today.toString()
            return
        }

        val daysDiff = today.toEpochDays() - lastDate.toEpochDays()
        when {
            daysDiff == 0L -> {
                // Already listened today! If for some reason streak is 0, make it at least 1
                if (currentStreak < 1) {
                    Settings.shared.listeningStreakDays = 1
                }
            }
            daysDiff == 1L -> {
                // Consecutive day! Increment streak!
                Settings.shared.listeningStreakDays = (currentStreak + 1).coerceAtLeast(1)
                Settings.shared.lastListeningDate = today.toString()
            }
            daysDiff > 1L -> {
                // Streak broken; reset streak to 1 for today
                Settings.shared.listeningStreakDays = 1
                Settings.shared.lastListeningDate = today.toString()
            }
        }
    }
}
