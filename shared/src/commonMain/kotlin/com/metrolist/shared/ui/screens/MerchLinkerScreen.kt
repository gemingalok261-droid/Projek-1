package com.metrolist.shared.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.metrolist.shared.haptics.HapticEvent
import com.metrolist.shared.haptics.rememberMetrolistHaptics
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.ui.icons.MetrolistIcons

enum class MerchTemplate(val title: String, val icon: String) {
    VINYL("Piringan Hitam (Vinyl)", "💿"),
    POSTER("Poster Lirik Estetik", "🖼️"),
    CASSETTE("Kaset Pita Retro", "📼"),
    KEYCHAIN("Gantungan Kunci (Tag)", "🏷️"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MerchLinkerScreen(
    currentSong: SongItem?,
    onBack: () -> Unit,
    onShareMerch: (String) -> Unit,
    onWriteNfcTag: (songId: String, songTitle: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val haptic = rememberMetrolistHaptics()
    var selectedTemplate by remember { mutableStateOf(MerchTemplate.VINYL) }
    var showNfcDialog by remember { mutableStateOf(false) }

    val songTitle = currentSong?.title ?: "Satu Bulan"
    val artistName = currentSong?.artists?.firstOrNull()?.name ?: "Bernadya"
    val songId = currentSong?.id ?: "bern_satubulan"
    val artworkUrl = currentSong?.thumbnail ?: "https://files.catbox.moe/ttm6ek.jpg"

    val targetUrl = "https://music.youtube.com/watch?v=$songId"
    val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=350x350&margin=12&data=${io.ktor.http.encodeURLQueryComponent(targetUrl)}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Merch Linker (QR & NFC)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("🏷️", fontSize = 20.sp)
                        }
                        Text(
                            text = "Hubungkan musik digital ke merchandise fisik nyata",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(MetrolistIcons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Template Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MerchTemplate.entries.forEach { template ->
                    val isSelected = template == selectedTemplate
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedTemplate = template
                                haptic.perform(HapticEvent.PRESS)
                            },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        ) {
                            Text(template.icon, fontSize = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = template.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Physical Merch Preview Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF64B5F6).copy(alpha = 0.6f), Color(0xFFBA68C8).copy(alpha = 0.4f), Color(0xFFFF9800).copy(alpha = 0.5f)),
                        ),
                        RoundedCornerShape(28.dp),
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (selectedTemplate) {
                        MerchTemplate.VINYL -> {
                            // Vinyl Record Simulation
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF121212))
                                    .border(4.dp, Color(0xFF242424), CircleShape)
                                    .border(16.dp, Color(0xFF181818), CircleShape),
                            ) {
                                // Center Label with QR Code
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(8.dp),
                                ) {
                                    AsyncImage(
                                        model = qrCodeUrl,
                                        contentDescription = "QR Code Lagu",
                                        modifier = Modifier.size(90.dp),
                                    )
                                }
                            }
                        }

                        MerchTemplate.POSTER -> {
                            // Aesthetic Poster
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth().height(260.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    AsyncImage(
                                        model = artworkUrl,
                                        contentDescription = songTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(130.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White,
                                            modifier = Modifier.padding(4.dp),
                                        ) {
                                            AsyncImage(
                                                model = qrCodeUrl,
                                                contentDescription = "QR Code",
                                                modifier = Modifier.size(90.dp).padding(4.dp),
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Scan untuk Putar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        MerchTemplate.CASSETTE -> {
                            // Cassette Tape
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E1E1E),
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        "SIDE A • ACCRETION AUDIO CASSETTE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF9800),
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF333333)))
                                        Spacer(Modifier.width(16.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            modifier = Modifier.padding(2.dp),
                                        ) {
                                            AsyncImage(
                                                model = qrCodeUrl,
                                                contentDescription = "QR Code",
                                                modifier = Modifier.size(64.dp).padding(2.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF333333)))
                                    }

                                    Text(
                                        "$artistName - $songTitle",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        MerchTemplate.KEYCHAIN -> {
                            // Keychain Tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(160.dp, 220.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.padding(12.dp),
                                ) {
                                    // Keychain hole
                                    Box(
                                        modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)),
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        modifier = Modifier.padding(4.dp),
                                    ) {
                                        AsyncImage(
                                            model = qrCodeUrl,
                                            contentDescription = "QR Code",
                                            modifier = Modifier.size(100.dp).padding(4.dp),
                                        )
                                    }

                                    Text(
                                        songTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        songTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Scan dengan kamera ponsel untuk langsung memutar musik di Accretion!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons (NFC & Share QR)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.perform(HapticEvent.PRESS)
                        showNfcDialog = true
                        onWriteNfcTag(songId, songTitle)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Tulis Tag NFC 📡")
                }

                Button(
                    onClick = {
                        haptic.perform(HapticEvent.PRESS)
                        val textToShare = """
                            🏷️ Accretion Physical Merch Linker:
                            
                            Lagu: $songTitle - $artistName
                            Link Musik: $targetUrl
                            QR Code: $qrCodeUrl
                            
                            Cetak QR ini pada stiker, poster, atau gantungan kunci kamu!
                        """.trimIndent()
                        onShareMerch(textToShare)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Bagikan Desain 📤")
                }
            }

            Spacer(Modifier.height(30.dp))
        }

        // NFC Writer instruction modal
        if (showNfcDialog) {
            ModalBottomSheet(
                onDismissRequest = { showNfcDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("📡", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tempelkan Tag NFC",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Dekatkan stiker atau kartu NFC ke bagian belakang ponsel Anda untuk menulis tautan lagu '$songTitle'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 Tips Penggunaan Merch:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("• Tempelkan stiker NFC pada casing HP, gantungan kunci, atau kaset.", style = MaterialTheme.typography.bodySmall)
                            Text("• Siapapun yang menempelkan ponselnya akan langsung diarahkan untuk memutar lagu ini secara otomatis!", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { showNfcDialog = false },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Selesai")
                    }
                }
            }
        }
    }
}
