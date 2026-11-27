package com.metrolist.shared.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.shared.crossword.ClueDirection
import com.metrolist.shared.crossword.CrosswordClue
import com.metrolist.shared.crossword.CrosswordStats
import com.metrolist.shared.crossword.LyricCrosswordGenerator
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.ui.haptics.HapticEvent
import com.metrolist.shared.ui.haptics.rememberMetrolistHaptics
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.ui.playback.PlaybackController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun LyricCrosswordScreen(
    database: MusicDatabase?,
    playbackController: PlaybackController?,
    onBack: () -> Unit,
    onShareScore: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberMetrolistHaptics()
    val scope = rememberCoroutineScope()

    var clues by remember { mutableStateOf<List<CrosswordClue>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var activeIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    val solvedClues = remember { mutableStateMapOf<Int, Boolean>() }

    var isGameFinished by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Audio hint state
    var audioPlayingClueId by remember { mutableStateOf<Int?>(null) }
    var audioCountdown by remember { mutableIntStateOf(3) }

    // Initialize or reset game
    fun startNewGame() {
        isLoading = true
        isGameFinished = false
        elapsedSeconds = 0
        isTimerRunning = true
        userAnswers.clear()
        solvedClues.clear()
        activeIndex = 0
        audioPlayingClueId = null

        scope.launch {
            val generated = LyricCrosswordGenerator.generateGameClues(database, targetCount = 6)
            clues = generated
            generated.forEach { clue ->
                userAnswers[clue.id] = ""
                solvedClues[clue.id] = false
            }
            isLoading = false
            Settings.shared.crosswordGamesPlayed += 1
        }
    }

    LaunchedEffect(Unit) {
        startNewGame()
    }

    // Timer loop
    LaunchedEffect(isTimerRunning, isGameFinished) {
        while (isTimerRunning && !isGameFinished) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    // Audio preview countdown
    LaunchedEffect(audioPlayingClueId) {
        if (audioPlayingClueId != null) {
            audioCountdown = 3
            while (audioCountdown > 0) {
                delay(1000L)
                audioCountdown -= 1
            }
            audioPlayingClueId = null
        }
    }

    val activeClue = clues.getOrNull(activeIndex)

    // Handle user typing letter
    fun onLetterInput(char: Char) {
        val clue = activeClue ?: return
        val current = userAnswers[clue.id].orEmpty()
        if (current.length < clue.answer.length) {
            val updated = current + char.uppercaseChar()
            userAnswers[clue.id] = updated
            haptic.perform(HapticEvent.PRESS)

            // Check if correct
            if (updated.equals(clue.answer, ignoreCase = true)) {
                solvedClues[clue.id] = true
                haptic.perform(HapticEvent.CONFIRM)

                // Check if all solved
                val allDone = clues.all { solvedClues[it.id] == true }
                if (allDone) {
                    isGameFinished = true
                    isTimerRunning = false
                    val points = 100
                    Settings.shared.crosswordGamesWon += 1
                    Settings.shared.crosswordTotalScore += points
                    Settings.shared.crosswordStreak += 1
                } else {
                    // Auto advance to next unsolved clue
                    val nextUnsolved = clues.indexOfFirst { solvedClues[it.id] != true }
                    if (nextUnsolved >= 0) {
                        activeIndex = nextUnsolved
                    }
                }
            }
        }
    }

    fun onBackspace() {
        val clue = activeClue ?: return
        val current = userAnswers[clue.id].orEmpty()
        if (current.isNotEmpty()) {
            userAnswers[clue.id] = current.dropLast(1)
            haptic.perform(HapticEvent.PRESS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Lyric Crossword",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("🧩", fontSize = 20.sp)
                        }
                        Text(
                            text = "Tebak kata lirik lagu yang hilang",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(MetrolistIcons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Timer & Score
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Trophy / Leaderboard button
                    IconButton(onClick = { showLeaderboard = true }) {
                        Text("🏆", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧩", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Menyiapkan Teka-Teki Lirik...",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                // Horizontal Quick Clue Switcher
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        itemsIndexed(clues) { index, clue ->
                            val isSolved = solvedClues[clue.id] == true
                            val isSelected = index == activeIndex

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    isSolved -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                },
                                contentColor = when {
                                    isSolved || isSelected -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        activeIndex = index
                                        haptic.perform(HapticEvent.PRESS)
                                    },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = "#${clue.number}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                    if (isSolved) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = MetrolistIcons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Clue Card
                activeClue?.let { clue ->
                    val isSolved = solvedClues[clue.id] == true
                    val answerLength = clue.answer.length
                    val typed = userAnswers[clue.id].orEmpty()

                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSolved) Color(0xFF4CAF50).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(24.dp),
                                ),
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Direction & Song info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    ) {
                                        Text(
                                            text = if (clue.direction == ClueDirection.ACROSS) "Mendatar (Across)" else "Menurun (Down)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }

                                    Text(
                                        text = "${clue.answer.length} Huruf",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                // Song & Artist
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎵", fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${clue.artistName} - ${clue.songTitle}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Lyric Clue with blank
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        text = "“${clue.clueText}”",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 17.sp,
                                            lineHeight = 24.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // Audio Snippet Button (3 Seconds)
                                val isPlayingAudio = audioPlayingClueId == clue.id
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isPlayingAudio) Color(0xFFFF9800).copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                haptic.perform(HapticEvent.PRESS)
                                                audioPlayingClueId = clue.id
                                                // Trigger seek to 3 seconds before timestamp
                                                val targetMs = maxOf(0L, clue.audioTimestampMs - 3000L)
                                                playbackController?.seekTo(targetMs)
                                            },
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        ) {
                                            Text(
                                                text = if (isPlayingAudio) "🔊" else "🎧",
                                                fontSize = 18.sp,
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = if (isPlayingAudio) "Memutar Audio (${audioCountdown}s)..." else "Bantuan Audio (3 Detik)",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = if (isPlayingAudio) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSecondaryContainer,
                                            )
                                            if (isPlayingAudio) {
                                                Spacer(Modifier.width(8.dp))
                                                Text("ılılllıı", fontSize = 14.sp, color = Color(0xFFFF9800))
                                            }
                                        }
                                    }

                                    if (isSolved) {
                                        Text(
                                            text = "BENAR! ✓",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50),
                                            fontSize = 14.sp,
                                        )
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                // Interactive Letter Boxes
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    for (i in 0 until answerLength) {
                                        val char = typed.getOrNull(i)
                                        val isCurrentSlot = i == typed.length && !isSolved

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    when {
                                                        isSolved -> Color(0xFF4CAF50).copy(alpha = 0.25f)
                                                        isCurrentSlot -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                        char != null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    },
                                                )
                                                .border(
                                                    width = if (isCurrentSlot) 2.dp else 1.dp,
                                                    color = when {
                                                        isSolved -> Color(0xFF4CAF50)
                                                        isCurrentSlot -> MaterialTheme.colorScheme.primary
                                                        char != null -> MaterialTheme.colorScheme.outline
                                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                ),
                                        ) {
                                            Text(
                                                text = char?.toString() ?: "",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 22.sp,
                                                ),
                                                color = if (isSolved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Virtual Alphabet Keyboard for fast touch input
                item {
                    val rows = listOf(
                        "QWERTYUIOP",
                        "ASDFGHJKL",
                        "ZXCVBNM",
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        rows.forEachIndexed { rowIndex, rowStr ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            ) {
                                if (rowIndex == 2) {
                                    // Backspace button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(width = 44.dp, height = 44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onBackspace() },
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("⌫", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                }

                                rowStr.forEach { letter ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier
                                            .size(width = 32.dp, height = 44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onLetterInput(letter) },
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = letter.toString(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            )
                                        }
                                    }
                                }

                                if (rowIndex == 2) {
                                    // Skip / Next button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(width = 44.dp, height = 44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                activeIndex = (activeIndex + 1) % clues.size
                                                haptic.perform(HapticEvent.PRESS)
                                            },
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("➜", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Completion Share Card Dialog / BottomSheet
        if (isGameFinished) {
            ModalBottomSheet(
                onDismissRequest = { isGameFinished = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "TEKA-TEKI SELESAI!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "Kamu berhasil menyelesaikan seluruh teka-teki lirik!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(20.dp))

                    // Aesthetic Share Score Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xFF64B5F6), Color(0xFFBA68C8), Color(0xFFFF9800)),
                                ),
                                RoundedCornerShape(24.dp),
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "ACCRETION LYRIC CROSSWORD",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SKOR", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "100",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF4CAF50),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("WAKTU", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SOAL", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "${clues.size}/${clues.size}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            val rank = CrosswordStats(gamesWon = Settings.shared.crosswordGamesWon).rankTitle
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "Gelar: $rank",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                isGameFinished = false
                                startNewGame()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("Main Lagi 🔄")
                        }

                        Button(
                            onClick = {
                                val rank = CrosswordStats(gamesWon = Settings.shared.crosswordGamesWon).rankTitle
                                val shareMessage = """
                                    🧩 Aku baru aja menyelesaikan Lyric Crossword di Accretion!
                                    
                                    🏆 Skor: 100/100
                                    ⏱️ Waktu: ${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}
                                    🎖️ Gelar: $rank
                                    
                                    Mainkan sekarang di Accretion Music App!
                                """.trimIndent()
                                onShareScore(shareMessage)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("Share Skor 📤")
                        }
                    }
                }
            }
        }

        // Leaderboard / Peringkat Dialog
        if (showLeaderboard) {
            LyricCrosswordLeaderboardDialog(
                onDismiss = { showLeaderboard = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricCrosswordLeaderboardDialog(
    onDismiss: () -> Unit,
) {
    val stats = CrosswordStats(
        gamesPlayed = Settings.shared.crosswordGamesPlayed,
        gamesWon = Settings.shared.crosswordGamesWon,
        totalScore = Settings.shared.crosswordTotalScore,
        currentStreak = Settings.shared.crosswordStreak,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", fontSize = 24.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Peringkat Jawara Lirik",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // User's Current Status Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("STATUS KAMU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                stats.rankTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Text(
                            "${stats.totalScore} Pts",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("🎮 Game Selesai: ${stats.gamesWon}", style = MaterialTheme.typography.bodyMedium)
                        Text("🔥 Win Streak: ${stats.currentStreak}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Hall of Fame Jawara Accretion",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )

            Spacer(Modifier.height(12.dp))

            // Top Ranks List
            val mockTopUsers = listOf(
                Triple("👑 Ray (@rayytampann)", "2,850 Pts", "Dewa Accretion"),
                Triple("🤖 Antigravity AI", "2,400 Pts", "Pujangga Lirik"),
                Triple("⚡ Ganzvlty", "1,950 Pts", "Master Karaoke"),
                Triple("Kamu", "${stats.totalScore} Pts", stats.rankTitle),
            )

            mockTopUsers.forEachIndexed { index, (name, pts, title) ->
                val isMe = name == "Kamu"
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (index) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "#${index + 1}"
                                },
                                fontSize = 18.sp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(name, fontWeight = if (isMe) FontWeight.ExtraBold else FontWeight.Bold)
                                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(pts, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
