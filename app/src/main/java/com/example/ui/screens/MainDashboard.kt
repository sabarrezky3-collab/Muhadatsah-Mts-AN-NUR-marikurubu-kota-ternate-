package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SavedRecording
import com.example.data.model.DialogLine
import com.example.data.model.Topic
import com.example.data.model.VocabularyItem
import com.example.ui.viewmodel.MuhadatsahViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: MuhadatsahViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("materi") }

    // Check recording permissions
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Izin Mikrofon ditolak. Fitur rekaman suara tidak bisa digunakan.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "materi",
                    onClick = { currentTab = "materi" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Materi Muhadatsah") },
                    label = { Text("Materi", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_materi")
                )
                NavigationBarItem(
                    selected = currentTab == "kamus",
                    onClick = { currentTab = "kamus" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Kamus Kosakata") },
                    label = { Text("Kamus", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_kamus")
                )
                NavigationBarItem(
                    selected = currentTab == "rekaman",
                    onClick = { currentTab = "rekaman" },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Rekaman Suara") },
                    label = { Text("Rekaman", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_rekaman")
                )
                NavigationBarItem(
                    selected = currentTab == "profil",
                    onClick = { currentTab = "profil" },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Profil MTs AN-NUR") },
                    label = { Text("AN-NUR", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_profil")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                "materi" -> MateriScreen(
                    viewModel = viewModel,
                    hasPermission = hasRecordPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
                "kamus" -> KamusScreen(viewModel = viewModel)
                "rekaman" -> RekamanScreen(viewModel = viewModel)
                "profil" -> ProfilScreen()
            }
        }
    }
}

// =========================================================================
// 1. MATERI SCREEN (MUHADATSAH CURRICULUM)
// =========================================================================
@Composable
fun MateriScreen(
    viewModel: MuhadatsahViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val topics = viewModel.topics
    val selectedTopic by viewModel.selectedTopic.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingLineId by viewModel.recordingLineId.collectAsStateWithLifecycle()
    val playingPath by viewModel.playingFilePath.collectAsStateWithLifecycle()
    val savedRecordings by viewModel.savedRecordings.collectAsStateWithLifecycle()
    val latestEvaluation by viewModel.latestEvaluationResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // App / School Header Banner
        SchoolHeader(
            subTitle = "Bahan Ajar Muhadatsah Interaktif",
            title = "MTs AN-NUR MARIKURUBU"
        )

        // Horizontal topic choices
        Text(
            text = "Pilih Topik Percakapan:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(topics) { topic ->
                val isSelected = topic.id == selectedTopic.id
                TopicChip(
                    topic = topic,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTopic(topic.id) }
                )
            }
        }

        // Active Topic Details card
        val selectedTopicIndex = remember(selectedTopic, topics) {
            val idx = topics.indexOf(selectedTopic)
            if (idx != -1) idx + 1 else 1
        }
        val completedCount = remember(selectedTopic, savedRecordings) {
            selectedTopic.dialogs.count { line ->
                savedRecordings.any { it.topicId == selectedTopic.id && it.dialogId == line.id }
            }
        }
        val progressPercentage = remember(selectedTopic, completedCount) {
            if (selectedTopic.dialogs.isEmpty()) 0 
            else (completedCount * 100) / selectedTopic.dialogs.size
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .shadow(1.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "MUHADATSAH ${String.format("%02d", selectedTopicIndex)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Text(
                            text = selectedTopic.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Progres",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "$progressPercentage%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Description
                Text(
                    text = selectedTopic.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )

                // Visual micro-progress bar
                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            }
        }

        Text(
            text = "Percakapan & Latihan Praktik:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        // Conversation Dialogue Stream
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(selectedTopic.dialogs) { line ->
                // Check if user has a recording saved for this line
                val associatedRecording = savedRecordings.find {
                    it.topicId == selectedTopic.id && it.dialogId == line.id
                }

                // Even lines on the left, odd lines on the right for visual dynamic roleplay
                val isLeft = line.id % 2 != 0
                DialogueBubble(
                    line = line,
                    isLeft = isLeft,
                    isRecordingThis = isRecording && recordingLineId == line.id,
                    isRecordingAny = isRecording,
                    hasPermission = hasPermission,
                    isPlayingThis = playingPath == associatedRecording?.filePath,
                    isPlayingAny = playingPath != null,
                    recordingFile = associatedRecording,
                    onRequestPermission = onRequestPermission,
                    onStartRecord = { viewModel.startRecording(line) },
                    onStopRecord = { viewModel.stopAndSaveRecording(line) },
                    onCancelRecord = { viewModel.cancelRecording() },
                    onPlayRecord = { associatedRecording?.let { viewModel.playRecording(it.filePath) } },
                    onStopPlay = { viewModel.stopPlayback() },
                    onSpeakStandard = { viewModel.speakStandardArabic(line.arabic) }
                )
            }
        }
    }

    if (latestEvaluation != null) {
        PronunciationReportDialog(
            result = latestEvaluation!!,
            onDismiss = { viewModel.clearLatestEvaluation() },
            onPlayReference = { viewModel.speakStandardArabic(latestEvaluation!!.wordFeedbacks.joinToString(" ") { it.word }) },
            onPlayMyAudio = {
                val lastSaved = savedRecordings.lastOrNull()
                lastSaved?.let { viewModel.playRecording(it.filePath) }
            },
            onStopPlay = { viewModel.stopPlayback() },
            isPlayingMyAudio = playingPath != null
        )
    }
}

@Composable
fun TopicChip(
    topic: Topic,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val borderStroke = if (isSelected) 0.dp else 1.dp

    Box(
        modifier = Modifier
            .shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("topic_chip_${topic.id}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = topic.titleArabic,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = topic.title.substringBefore(" ("),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DialogueBubble(
    line: DialogLine,
    isLeft: Boolean,
    isRecordingThis: Boolean,
    isRecordingAny: Boolean,
    hasPermission: Boolean,
    isPlayingThis: Boolean,
    isPlayingAny: Boolean,
    recordingFile: SavedRecording?,
    onRequestPermission: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onCancelRecord: () -> Unit,
    onPlayRecord: () -> Unit,
    onStopPlay: () -> Unit,
    onSpeakStandard: () -> Unit
) {
    val alignment = if (isLeft) Alignment.Start else Alignment.End
    val bubbleColor = if (isLeft) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    val bubbleShape = if (isLeft) {
        RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp)
    } else {
        RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_line_${line.id}"),
        horizontalAlignment = alignment
    ) {
        // Speaker Indicator Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (line.gender == "F") Color(0xFFEC4899) else MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = line.speaker.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Text(
                text = line.speaker,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }

        // Message Main Box
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            border = if (isLeft) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)) else null,
            modifier = Modifier
                .widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Arabic Text Row with Reference Pronunciation trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSpeakStandard,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                            .testTag("speak_standard_${line.id}")
                    ) {
                        Text(
                            text = "🔊",
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = line.arabic,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.weight(1f),
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                    )
                }

                // Latin pronunciation guide
                Text(
                    text = "/ ${line.transliteration} /",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                // Indonesian meaning translation
                Text(
                    text = line.translation,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Recording Practice Area
                AnimatedContent(targetState = isRecordingThis) { activeRecording ->
                    if (activeRecording) {
                        // Pulsing red indicator while recording microphone
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition()
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(scale)
                                    .background(Color.Red, CircleShape)
                            )
                            
                            // Visualizing waves matching our "Sleek Interface" trace
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val heights = listOf(10, 18, 30, 14, 24)
                                heights.forEachIndexed { index, baseHeight ->
                                    val waveVal by infiniteTransition.animateFloat(
                                        initialValue = baseHeight * 0.4f,
                                        targetValue = baseHeight * 1.3f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(400 + index * 70, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height(waveVal.dp)
                                            .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                                    )
                                }
                            }

                            IconButton(
                                onClick = onStopRecord,
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color.Red, CircleShape)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Simpan", tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                            IconButton(
                                onClick = onCancelRecord,
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color.DarkGray, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Batal", tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                        }
                    } else {
                        // Un-recorded state, ready to practice
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (recordingFile != null) {
                                // Already recorded trace, show Play/Stop
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (isPlayingThis) onStopPlay() else onPlayRecord()
                                        }
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "Buka Rekaman",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Hasil Latihanmu",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // Microphone triggers
                            Button(
                                onClick = {
                                    if (isRecordingAny) return@Button
                                    if (hasPermission) {
                                        onStartRecord()
                                    } else {
                                        onRequestPermission()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (recordingFile == null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .shadow(2.dp, CircleShape),
                                shape = CircleShape,
                                enabled = !isRecordingAny && !isPlayingAny
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (recordingFile == null) "Praktik" else "Ulangi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. KAMUS SCREEN (INTERACTIVE DICTIONARY & CUSTOM BUILDER)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KamusScreen(viewModel: MuhadatsahViewModel) {
    val query by viewModel.dictionaryQuery.collectAsStateWithLifecycle()
    val chosenCategory by viewModel.selectedDictionaryCategory.collectAsStateWithLifecycle()
    val isFavoriteOnly by viewModel.isFavoriteOnly.collectAsStateWithLifecycle()
    val filteredDict by viewModel.filteredDictionary.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteWordIds.collectAsStateWithLifecycle()

    var showAddCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SchoolHeader(
            subTitle = "Kamus Kosakata Interaktif Arab-Indo",
            title = "KOSAKATA SISWA MTs"
        )

        // Custom vocabulary button floaters
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = { showAddCustomDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_vocab_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Tambah Kosakata Baru Mandiri",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Search Bar Input
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setDictionaryQuery(it) },
            label = { Text("Cari Kosakata...") },
            placeholder = { Text("Ketik kata Arab, Latin, atau Indonesia") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setDictionaryQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus tulisan")
                    }
                }
            },
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("dictionary_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )

        // Category scroll selections
        val categories = listOf("Semua", "Sekolah", "Rumah", "Harian", "Kata Kerja", "Ditambahkan")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isActive = chosenCategory == category
                FilterChip(
                    selected = isActive,
                    onClick = { viewModel.setDictionaryCategory(category) },
                    label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Favorite Toggle Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(
                checked = isFavoriteOnly,
                onCheckedChange = { viewModel.toggleFavoriteOnlyFilter() },
                modifier = Modifier.testTag("favorite_switch")
            )
            Text(
                text = "Tampilkan Favorit Saja",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Vocabulary List displays
        if (filteredDict.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.Search,
                text = "Kosakata tidak ditemukan.",
                tip = "Silakan ganti kata kunci pencarian atau bersihkan filter."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredDict, key = { it.id }) { item ->
                    val isFav = favoriteIds.contains(item.id)
                    VocabularyCard(
                        item = item,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleFavorite(item) },
                        onDeleteCustom = { viewModel.deleteCustomWord(item.id) },
                        onSpeak = { viewModel.speakStandardArabic(item.arabic) }
                    )
                }
            }
        }
    }

    // Modal dialog to add custom vocabularies
    if (showAddCustomDialog) {
        CustomVocabDialog(
            onDismiss = { showAddCustomDialog = false },
            onSave = { arabic, latin, translation, category ->
                viewModel.addCustomWord(arabic, latin, translation, category)
                showAddCustomDialog = false
            }
        )
    }
}

@Composable
fun VocabularyCard(
    item: VocabularyItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDeleteCustom: () -> Unit,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .testTag("vocab_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Tags
                Box(
                    modifier = Modifier
                        .background(
                            if (item.isCustom) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.isCustom) "Mandiri: ${item.category}" else "Kurikulum: ${item.category}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorite Star Button
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.Star,
                            contentDescription = "Favorit",
                            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                    }

                    // Trash button if item is custom added
                    if (item.isCustom) {
                        IconButton(onClick = onDeleteCustom) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus kosakata",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Arabic text row with standard tts speech play button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                        .testTag("speak_vocab_${item.id}")
                ) {
                    Text("🔊", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.arabic,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.weight(1f),
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )
            }

            // Latin rendering
            Text(
                text = "/ ${item.transliteration} /",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left
            )

            // Meaning
            Text(
                text = item.translation,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Left
            )

            // Dynamic Context Sentence display if provided
            if (item.exampleArabic.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Contoh Kalimat:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.exampleArabic,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = item.exampleTranslation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomVocabDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var arabic by remember { mutableStateOf("") }
    var latin by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("Harian") }
    val categories = listOf("Sekolah", "Rumah", "Harian", "Kata Kerja")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .testTag("custom_vocab_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tambah Kosakata Mandiri",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = arabic,
                    onValueChange = { arabic = it },
                    label = { Text("Ketik Lafadz Arab") },
                    placeholder = { Text("Contoh: كِتَابٌ") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_arabic"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                OutlinedTextField(
                    value = latin,
                    onValueChange = { latin = it },
                    label = { Text("Cara Membaca (Latin)") },
                    placeholder = { Text("Contoh: Kitābun") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_latin"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text("Terjemahan Indonesia") },
                    placeholder = { Text("Contoh: Buku") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_translation"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                Column {
                    Text(
                        text = "Kategori Kosakata:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (selectedCat == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCat = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCat == cat) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (arabic.isNotBlank() && latin.isNotBlank() && translation.isNotBlank()) {
                                onSave(arabic, latin, translation, selectedCat)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_vocab_btn_modal")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. REKAMAN SAYA SCREEN (AUDIO LAB PRACTICE RECORDS)
// =========================================================================
@Composable
fun RekamanScreen(viewModel: MuhadatsahViewModel) {
    val recordings by viewModel.savedRecordings.collectAsStateWithLifecycle()
    val playingPath by viewModel.playingFilePath.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SchoolHeader(
            subTitle = "Catatan Hasil Latihan Praktikmu",
            title = "REKAMAN LATIHAN SAYA"
        )

        if (recordings.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.PlayArrow,
                text = "Belum Ada Rekaman Latihan.",
                tip = "Silakan klik tombol 'Mulai Praktik Rekam' di halaman Materi untuk merekam suaramu."
            )
        } else {
            Text(
                text = "Daftar Rekaman Suara Anda (${recordings.size}):",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(recordings, key = { it.id }) { rec ->
                    val isPlaying = playingPath == rec.filePath
                    RecordingRowCard(
                        recording = rec,
                        isPlaying = isPlaying,
                        onPlay = { viewModel.playRecording(rec.filePath) },
                        onStop = { viewModel.stopPlayback() },
                        onDelete = { viewModel.deleteRecording(rec.id, rec.filePath) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingRowCard(
    recording: SavedRecording,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = remember(recording.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(recording.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .testTag("recording_card_${recording.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle play feedback icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    )
                    .clickable { if (isPlaying) onStop() else onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    tint = if (isPlaying) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text metadata details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recording.topicTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Dialog: ${recording.speakerName} • $dateText",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recording.arabicText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )
                Text(
                    text = recording.translationText,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }

            // Trash delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_rec_${recording.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus rekaman",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// =========================================================================
// 4. INSTITUTION PROFILE & GUIDE SCREEN (MTs AN-NUR MARIKURUBU)
// =========================================================================
@Composable
fun ProfilScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Big School Identity Badge Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MemorySafeImage(
                        id = com.example.R.drawable.img_app_icon_1779557473788,
                        contentDescription = "Logo MTs AN-NUR",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "MTs AN-NUR MARIKURUBU",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "KOTA TERNATE, MALUKU UTARA",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Aplikasi Pendamping Belajar Muhadatsah Interaktif khusus siswa Madrasah Tsanawiyah (MTs) sebagai sarana latihan percakapan Arab terbaik.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section: Visi, Misi & Alamat
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Profil Madrasah",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Text(
                        text = "📍 Lokasi : Kelurahan Marikurubu, Kota Ternate, Maluku Utara.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Visi Madrasah :",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "\"Mewujudkan Madrasah yang Religius, Unggul dalam IPTEK, Berkarakter Islami, dan Berbudaya Lingkungan.\"",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section: Cara Penggunaan & Panduan Muhadatsah
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Panduan Belajar Praktik Muhadatsah",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    GuideItem(
                        step = "1",
                        title = "Pilih Topik Materi",
                        desc = "Eksplorasi pilihan bab percakapan dari At-Ta'aruf, Sekolah, hingga Perpustakaan."
                    )

                    GuideItem(
                        step = "2",
                        title = "Pahami Teks & Cara Membaca",
                        desc = "Perhatikan lafadz Arab yang ber-harokat, simak tuntunan baca (latin), dan pahami maknanya."
                    )

                    GuideItem(
                        step = "3",
                        title = "Rekam Suara Latihanmu",
                        desc = "Tekan tombol 'Praktik' di sudut kanan balon percakapan, berikan izin mikrofon, lalu bacalah percakapan dengan lantang."
                    )

                    GuideItem(
                        step = "4",
                        title = "Mainkan & Evaluasi Mandiri",
                        desc = "Setelah menyimpan, mainkan hasil rekamannya. Bandingkan kefasihan bacamu dengan teks panduan Arab."
                    )

                    GuideItem(
                        step = "5",
                        title = "Eksplorasi Kosakata",
                        desc = "Buka menu Kamus untuk mencari makna kosakata, menyimpannya ke favorit, atau membuat kamus kosakata pribadimu sendiri!"
                    )
                }
            }
        }

        // Section Footer Developer
        item {
            Text(
                text = "© 2026 MTs AN-NUR MARIKURUBU • Versi Aplikasi 1.0.0",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GuideItem(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )
        }
    }
}

// =========================================================================
// STANDARD SHARED COMPOSABLES
// =========================================================================
@Composable
fun SchoolHeader(title: String, subTitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MemorySafeImage(
                id = com.example.R.drawable.img_app_icon_1779557473788,
                contentDescription = "Logo MTs AN-NUR",
                modifier = Modifier
                    .size(42.dp)
                    .shadow(2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            Column(
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = subTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        
        // Bell Icon on the right
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .clickable { /* Notification tap Feedback */ }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔔",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EmptyStatePlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tip: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tip,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationReportDialog(
    result: com.example.ui.components.PronunciationResult,
    onDismiss: () -> Unit,
    onPlayReference: () -> Unit,
    onPlayMyAudio: () -> Unit,
    onStopPlay: () -> Unit,
    isPlayingMyAudio: Boolean
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .testTag("pronunciation_report_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Title inside Sleek Theme
                Text(
                    text = "Laporan Penilaian Pelafalan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Circular Progress Score Card
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.primary
                                )
                            ),
                            CircleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${result.score}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = if (result.score >= 80) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Skor",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                // Grade Title representing classic Islamic learning
                Box(
                    modifier = Modifier
                        .background(
                            if (result.score >= 80) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.grade,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.score >= 80) Color(0xFF137333) else MaterialTheme.colorScheme.primary
                    )
                }

                // Instructions
                Text(
                    text = "Ketuk masing-masing kata untuk saran Makhraj:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                // Word level chip wrapping using standard FlowRow
                var selectedWordNote by remember { mutableStateOf<String?>(null) }
                var selectedWordName by remember { mutableStateOf<String?>(null) }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    result.wordFeedbacks.forEach { fb ->
                        val (chipBg, chipTextColor) = when (fb.status) {
                            com.example.ui.components.WordStatus.CORRECT -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                            com.example.ui.components.WordStatus.PARTIAL -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
                            com.example.ui.components.WordStatus.INCORRECT -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .background(chipBg, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedWordName = fb.word
                                    selectedWordNote = fb.note
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = fb.word,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipTextColor,
                                style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                            )
                        }
                    }
                }

                // Word static note section
                AnimatedVisibility(
                    visible = selectedWordNote != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Detail Kata: ${selectedWordName ?: ""}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                                )
                                IconButton(
                                    onClick = {
                                        selectedWordNote = null
                                        selectedWordName = null
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Tutup saran", modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedWordNote ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // General advice feedback
                Text(
                    text = result.feedback,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Interactive Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dengar Contoh (Play reference reciting TTS)
                    OutlinedButton(
                        onClick = onPlayReference,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("play_ref_tts"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔊", fontSize = 12.sp)
                            Text("Dengar Contoh", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Play my audio
                    Button(
                        onClick = { if (isPlayingMyAudio) onStopPlay() else onPlayMyAudio() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("play_my_record"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isPlayingMyAudio) "⏹" else "▶", fontSize = 12.sp)
                            Text(if (isPlayingMyAudio) "Stop Replay" else "Suaraku", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("dismiss_evaluation_report"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lanjut Belajar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MemorySafeImage(
    @androidx.annotation.DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Fit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageBitmap = remember(id) {
        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeResource(context.resources, id, options)
            var scale = 1
            val targetSize = 120
            while (options.outWidth / scale > targetSize || options.outHeight / scale > targetSize) {
                scale *= 2
            }
            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, id, options)
            bitmap?.let { it.asImageBitmap() }
        } catch (e: Exception) {
            android.util.Log.e("MemorySafeImage", "Failed to decode drawable", e)
            null
        }
    }
    if (imageBitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        androidx.compose.foundation.layout.Box(
            modifier = modifier.background(Color.Gray)
        )
    }
}
