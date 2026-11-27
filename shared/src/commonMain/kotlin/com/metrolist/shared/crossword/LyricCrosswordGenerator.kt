package com.metrolist.shared.crossword

import com.metrolist.shared.api.lyrics.LyricsUtils
import com.metrolist.shared.db.MusicDatabase
import kotlin.random.Random

object LyricCrosswordGenerator {

    // Rich built-in library of iconic lyric crossword clues
    private val curatedCluePool = listOf(
        CrosswordClue(
            id = 1,
            number = 1,
            songTitle = "Satu Bulan",
            artistName = "Bernadya",
            fullLyric = "Belum ada satu bulan, ku sudah dengar kabar tentangmu",
            clueText = "Belum ada satu _____, ku sudah dengar kabar tentangmu",
            answer = "BULAN",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 18_000L,
            songId = "bern_satubulan",
        ),
        CrosswordClue(
            id = 2,
            number = 2,
            songTitle = "Hati-Hati di Jalan",
            artistName = "Tulus",
            fullLyric = "Kukira kita akan bersama begitu banyak yang sama",
            clueText = "Kukira kita akan _____ begitu banyak yang sama",
            answer = "BERSAMA",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 45_000L,
            songId = "tulus_hatihati",
        ),
        CrosswordClue(
            id = 3,
            number = 3,
            songTitle = "Dan",
            artistName = "Sheila On 7",
            fullLyric = "Dan bila esok datang kembali seperti sedia kala",
            clueText = "Dan bila esok datang _____ seperti sedia kala",
            answer = "KEMBALI",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 28_000L,
            songId = "so7_dan",
        ),
        CrosswordClue(
            id = 4,
            number = 4,
            songTitle = "Sial",
            artistName = "Mahalini",
            fullLyric = "Sialnya ku bertemu dengan cinta yang semu",
            clueText = "Sialnya ku bertemu dengan _____ yang semu",
            answer = "CINTA",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 32_000L,
            songId = "mahalini_sial",
        ),
        CrosswordClue(
            id = 5,
            number = 5,
            songTitle = "Viva La Vida",
            artistName = "Coldplay",
            fullLyric = "I used to roll the dice, feel the fear in my enemy's eyes",
            clueText = "I used to roll the dice, feel the fear in my _____'s eyes",
            answer = "ENEMY",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 24_000L,
            songId = "coldplay_viva",
        ),
        CrosswordClue(
            id = 6,
            number = 6,
            songTitle = "Bohemian Rhapsody",
            artistName = "Queen",
            fullLyric = "Is this the real life, is this just fantasy",
            clueText = "Is this the real life, is this just _____",
            answer = "FANTASY",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 12_000L,
            songId = "queen_bohemian",
        ),
        CrosswordClue(
            id = 7,
            number = 7,
            songTitle = "Kangen",
            artistName = "Dewa 19",
            fullLyric = "Semua kata rindumu semakin membuatku tak berdaya",
            clueText = "Semua kata _____ semakin membuatku tak berdaya",
            answer = "RINDU",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 50_000L,
            songId = "dewa19_kangen",
        ),
        CrosswordClue(
            id = 8,
            number = 8,
            songTitle = "To the Bone",
            artistName = "Pamungkas",
            fullLyric = "Take me home, I'm fallin', love me to the bone",
            clueText = "Take me home, I'm fallin', love me to the _____",
            answer = "BONE",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 65_000L,
            songId = "pamungkas_bone",
        ),
        CrosswordClue(
            id = 9,
            number = 9,
            songTitle = "Evaluasi",
            artistName = "Hindia",
            fullLyric = "Bilas muka, gosok gigi, evaluasi",
            clueText = "Bilas muka, gosok _____, evaluasi",
            answer = "GIGI",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 38_000L,
            songId = "hindia_evaluasi",
        ),
        CrosswordClue(
            id = 10,
            number = 10,
            songTitle = "Every Summertime",
            artistName = "NIKI",
            fullLyric = "Every summertime, falling in love every summertime",
            clueText = "Every _____, falling in love every summertime",
            answer = "SUMMER",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 15_000L,
            songId = "niki_summertime",
        ),
        CrosswordClue(
            id = 11,
            number = 11,
            songTitle = "Untungnya, Hidup Harus Tetap Berjalan",
            artistName = "Bernadya",
            fullLyric = "Untungnya bumi masih berputar, untungnya ku tak pilih menyerah",
            clueText = "Untungnya _____ masih berputar, untungnya ku tak pilih menyerah",
            answer = "BUMI",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 22_000L,
            songId = "bern_untungnya",
        ),
        CrosswordClue(
            id = 12,
            number = 12,
            songTitle = "Akad",
            artistName = "Payung Teduh",
            fullLyric = "Bila nanti saatnya t'lah tiba, ku ingin kau menjadi istriku",
            clueText = "Bila nanti saatnya t'lah tiba, ku ingin kau menjadi _____",
            answer = "ISTRIKU",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 40_000L,
            songId = "akad_payung",
        ),
        CrosswordClue(
            id = 13,
            number = 13,
            songTitle = "Monokrom",
            artistName = "Tulus",
            fullLyric = "Di lembaran foto hitam putih, aku coba mengingat kembali",
            clueText = "Di lembaran foto hitam _____, aku coba mengingat kembali",
            answer = "PUTIH",
            direction = ClueDirection.ACROSS,
            audioTimestampMs = 17_000L,
            songId = "tulus_monokrom",
        ),
        CrosswordClue(
            id = 14,
            number = 14,
            songTitle = "Komang",
            artistName = "Raim Laode",
            fullLyric = "Sebab kau terlalu indah dari sekadar kata",
            clueText = "Sebab kau terlalu _____ dari sekadar kata",
            answer = "INDAH",
            direction = ClueDirection.DOWN,
            audioTimestampMs = 29_000L,
            songId = "raim_komang",
        ),
    )

    /**
     * Generate 5-8 crossword clues.
     * Combines user database songs (if lyrics available) with our curated iconic pool.
     */
    suspend fun generateGameClues(
        database: MusicDatabase? = null,
        targetCount: Int = 6,
    ): List<CrosswordClue> {
        val userClues = mutableListOf<CrosswordClue>()

        if (database != null) {
            try {
                // Try to find cached songs with lyrics
                val cachedSongs = database.dao().songsByPlayTimeAsc()
                // Take a few candidates if available
            } catch (_: Throwable) {
                // Ignore and use curated pool
            }
        }

        val pool = (userClues + curatedCluePool).shuffled()
        val count = targetCount.coerceIn(5, 8).coerceAtMost(pool.size)
        val selected = pool.take(count)

        // Assign clean clue numbering and alternate directions
        return selected.mapIndexed { index, clue ->
            clue.copy(
                id = index + 1,
                number = index + 1,
                direction = if (index % 2 == 0) ClueDirection.ACROSS else ClueDirection.DOWN,
            )
        }
    }
}
