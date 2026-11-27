package com.metrolist.shared.crossword

import com.metrolist.shared.model.SongItem
import kotlinx.serialization.Serializable

enum class ClueDirection {
    ACROSS, // Mendatar
    DOWN,   // Menurun
}

@Serializable
data class CrosswordClue(
    val id: Int,
    val number: Int,
    val songTitle: String,
    val artistName: String,
    val fullLyric: String,
    val clueText: String,
    val answer: String,
    val direction: ClueDirection = ClueDirection.ACROSS,
    val audioTimestampMs: Long = 0L,
    val songId: String? = null,
)

data class CrosswordLeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
    val gamesCompleted: Int,
    val badge: String,
    val isCurrentUser: Boolean = false,
)

@Serializable
data class CrosswordStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalScore: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
) {
    val rankTitle: String
        get() = when {
            gamesCompletedRank >= 20 -> "👑 Dewa Accretion"
            gamesCompletedRank >= 12 -> "✨ Pujangga Lirik"
            gamesCompletedRank >= 7 -> "🎤 Master Karaoke"
            gamesCompletedRank >= 3 -> "🎵 Penikmat Melodi"
            gamesCompletedRank >= 1 -> "🌱 Pendengar Lirik"
            else -> "✨ Pemula Penasaran"
        }

    private val gamesCompletedRank: Int get() = gamesWon
}
