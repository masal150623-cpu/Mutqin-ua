package com.example.mutqin.feature.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mutqin.core.model.ProgramConstants
import com.example.mutqin.core.model.UserSession
import com.example.mutqin.data.local.entity.GroupEntity
import com.example.mutqin.data.local.entity.SantriEntity
import com.example.mutqin.ui.components.EmptyState
import com.example.mutqin.ui.components.GroupBadge
import com.example.mutqin.ui.components.MutqinTopBar
import com.example.mutqin.ui.components.SectionHeader
import com.example.mutqin.ui.components.StatusChip
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold100
import com.example.ui.theme.Gold700
import com.example.ui.theme.Gold800
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    session: UserSession,
    viewModel: TeacherViewModel,
    onLogout: () -> Unit
) {
    val assignedGroups by viewModel.assignedGroups.collectAsState()
    val mySantri by viewModel.mySantri.collectAsState()
    val userMsg by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog state
    var selectedSantriForZiyadah by remember { mutableStateOf<SantriEntity?>(null) }
    var selectedSantriForMurajaah by remember { mutableStateOf<SantriEntity?>(null) }
    var selectedSantriForIqro by remember { mutableStateOf<SantriEntity?>(null) }
    var selectedSantriForTahsin by remember { mutableStateOf<SantriEntity?>(null) }
    var selectedSantriForAttendance by remember { mutableStateOf<SantriEntity?>(null) }
    var selectedSantriForProposal by remember { mutableStateOf<SantriEntity?>(null) }
    var showJournalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMsg) {
        userMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            MutqinTopBar(
                title = "PORTAL PENGAJAR",
                subtitle = session.name,
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("btn_teacher_logout")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = PureWhite)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate50),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Surface(
                                color = Emerald100,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Ustaz Aktif",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Emerald900,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kelompok Binaan Anda:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Emerald100)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (assignedGroups.isEmpty()) {
                                Text(
                                    text = "Belum ada kelompok yang ditugaskan oleh Kepala Program.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = PureWhite)
                                )
                            } else {
                                assignedGroups.forEach { grp ->
                                    GroupBadge(code = grp.code)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Activity Actions
            item {
                SectionHeader(title = "Aktivitas Pembelajaran Hari Ini ($todayDate)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showJournalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Isi Jurnal", fontSize = 12.sp)
                    }
                }
            }

            // Santri Binaan List
            item {
                SectionHeader(
                    title = "Daftar Santri Tanggung Jawab (${mySantri.size})",
                    subtitle = "Pilih santri untuk input Ziyadah, Murajaah, Iqro', Tahsin, atau Presensi."
                )
            }

            if (mySantri.isEmpty()) {
                item {
                    EmptyState(
                        message = "Belum ada santri di kelompok Anda",
                        subMessage = "Santri akan otomatis muncul ketika Kepala Program mendaftarkan atau memindahkan santri ke kelompok binaan Anda."
                    )
                }
            }

            items(mySantri) { santri ->
                val grp = assignedGroups.find { it.id == santri.currentGroupId }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GroupBadge(code = grp?.code ?: "?")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = santri.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            StatusChip(status = santri.status)
                        }

                        Text(
                            text = "NIS: ${santri.nis} • Kelas: ${santri.schoolClass.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedSantriForZiyadah = santri },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ziyadah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                            }
                            OutlinedButton(
                                onClick = { selectedSantriForMurajaah = santri },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Murajaah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold700)
                            }
                            OutlinedButton(
                                onClick = { selectedSantriForIqro = santri },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Iqro'", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { selectedSantriForTahsin = santri },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Tahsin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TextButton(
                                onClick = { selectedSantriForAttendance = santri },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Presensi", fontSize = 12.sp, color = Slate700)
                            }
                            TextButton(
                                onClick = { selectedSantriForProposal = santri },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Usul Kenaikan", fontSize = 12.sp, color = Emerald800, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Setoran Ziyadah
    if (selectedSantriForZiyadah != null) {
        val s = selectedSantriForZiyadah!!
        ZiyadahDialog(
            santri = s,
            onDismiss = { selectedSantriForZiyadah = null },
            onConfirm = { surah, start, end, total, flScore, tajScore, makhScore, mistakes, stat, mastered, notes, followUp ->
                viewModel.saveZiyadah(
                    santriId = s.id,
                    groupId = s.currentGroupId,
                    academicYearId = s.academicYearId,
                    date = todayDate,
                    surahName = surah,
                    ayahStart = start,
                    ayahEnd = end,
                    totalAyahs = total,
                    fluencyScore = flScore,
                    tajwidScore = tajScore,
                    makhrajScore = makhScore,
                    mistakeCount = mistakes,
                    status = stat,
                    isMastered = mastered,
                    notes = notes,
                    followUp = followUp
                ) {
                    selectedSantriForZiyadah = null
                }
            }
        )
    }

    // Modal: Murajaah
    if (selectedSantriForMurajaah != null) {
        val s = selectedSantriForMurajaah!!
        MurajaahDialog(
            santri = s,
            onDismiss = { selectedSantriForMurajaah = null },
            onConfirm = { surah, start, end, flLevel, mistakes, tajScore, notes, followUp ->
                viewModel.saveMurajaah(
                    santriId = s.id,
                    groupId = s.currentGroupId,
                    academicYearId = s.academicYearId,
                    date = todayDate,
                    surahName = surah,
                    ayahStart = start,
                    ayahEnd = end,
                    fluencyLevel = flLevel,
                    mistakeCount = mistakes,
                    tajwidScore = tajScore,
                    notes = notes,
                    followUp = followUp
                ) {
                    selectedSantriForMurajaah = null
                }
            }
        )
    }

    // Modal: Iqro'
    if (selectedSantriForIqro != null) {
        val s = selectedSantriForIqro!!
        IqroDialog(
            santri = s,
            onDismiss = { selectedSantriForIqro = null },
            onConfirm = { vol, page, flLevel, mistakes, notes, followUp ->
                viewModel.saveIqro(
                    santriId = s.id,
                    groupId = s.currentGroupId,
                    academicYearId = s.academicYearId,
                    date = todayDate,
                    volume = vol,
                    pageOrLesson = page,
                    fluencyLevel = flLevel,
                    readingMistakes = mistakes,
                    notes = notes,
                    followUp = followUp
                ) {
                    selectedSantriForIqro = null
                }
            }
        )
    }

    // Modal: Tahsin
    if (selectedSantriForTahsin != null) {
        val s = selectedSantriForTahsin!!
        TahsinDialog(
            santri = s,
            onDismiss = { selectedSantriForTahsin = null },
            onConfirm = { surah, start, end, flScore, makhScore, tajScore, mistakes, notes, recs ->
                viewModel.saveTahsin(
                    santriId = s.id,
                    groupId = s.currentGroupId,
                    academicYearId = s.academicYearId,
                    date = todayDate,
                    surahName = surah,
                    ayahStart = start,
                    ayahEnd = end,
                    fluencyScore = flScore,
                    makhrajScore = makhScore,
                    tajwidScore = tajScore,
                    mistakes = mistakes,
                    notes = notes,
                    recommendations = recs
                ) {
                    selectedSantriForTahsin = null
                }
            }
        )
    }

    // Modal: Presensi
    if (selectedSantriForAttendance != null) {
        val s = selectedSantriForAttendance!!
        AttendanceDialog(
            santri = s,
            onDismiss = { selectedSantriForAttendance = null },
            onConfirm = { sessionType, status, notes ->
                viewModel.saveAttendance(
                    santriId = s.id,
                    groupId = s.currentGroupId,
                    academicYearId = s.academicYearId,
                    date = todayDate,
                    sessionType = sessionType,
                    status = status,
                    notes = notes
                ) {
                    selectedSantriForAttendance = null
                }
            }
        )
    }

    // Modal: Usulan Kenaikan
    if (selectedSantriForProposal != null) {
        val s = selectedSantriForProposal!!
        val targetGid = when (s.currentGroupId) {
            ProgramConstants.GROUP_D_ID -> ProgramConstants.GROUP_C_ID
            ProgramConstants.GROUP_C_ID -> ProgramConstants.GROUP_B_ID
            ProgramConstants.GROUP_B_ID -> ProgramConstants.GROUP_A_ID
            else -> ProgramConstants.GROUP_A_ID
        }
        TeacherPromotionDialog(
            santri = s,
            targetGroupId = targetGid,
            onDismiss = { selectedSantriForProposal = null },
            onConfirm = { justif ->
                viewModel.proposePromotion(
                    santriId = s.id,
                    currentGroupId = s.currentGroupId,
                    targetGroupId = targetGid,
                    academicYearId = s.academicYearId,
                    proposalDate = todayDate,
                    justification = justif
                ) {
                    selectedSantriForProposal = null
                }
            }
        )
    }

    // Modal: Jurnal Pengajar
    if (showJournalDialog) {
        val firstGid = assignedGroups.firstOrNull()?.id ?: ProgramConstants.GROUP_D_ID
        JournalDialog(
            assignedGroups = assignedGroups,
            defaultGroupId = firstGid,
            onDismiss = { showJournalDialog = false },
            onConfirm = { gid, topic, activities, count, obstacles, results, plan ->
                viewModel.saveJournal(
                    groupId = gid,
                    academicYearId = ProgramConstants.DEFAULT_ACADEMIC_YEAR_ID,
                    date = todayDate,
                    topic = topic,
                    activities = activities,
                    studentCount = count,
                    obstacles = obstacles,
                    results = results,
                    followUp = plan
                ) {
                    showJournalDialog = false
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TEACHER RECORDING MODALS
// -------------------------------------------------------------

@Composable
private fun ZiyadahDialog(
    santri: SantriEntity,
    onDismiss: () -> Unit,
    onConfirm: (surah: String, start: Int, end: Int, total: Int?, flScore: Int, tajScore: Int, makhScore: Int, mistakes: Int, stat: String, mastered: Boolean, notes: String, followUp: String) -> Unit
) {
    var surah by remember { mutableStateOf("An-Naba'") }
    var ayahStart by remember { mutableStateOf("1") }
    var ayahEnd by remember { mutableStateOf("10") }
    var mistakes by remember { mutableStateOf("0") }
    var isMastered by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    val startInt = ayahStart.toIntOrNull() ?: 1
    val endInt = ayahEnd.toIntOrNull() ?: 1
    val calculatedTotal = (endInt - startInt + 1).coerceAtLeast(0)
    val meetsMinTarget = calculatedTotal >= 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setoran Hafalan Baru (Ziyadah)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name} (${santri.nis})", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = surah,
                    onValueChange = { surah = it },
                    label = { Text("Nama Surah*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ayahStart,
                        onValueChange = { ayahStart = it },
                        label = { Text("Ayat Awal") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ayahEnd,
                        onValueChange = { ayahEnd = it },
                        label = { Text("Ayat Akhir") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Live Target Feedback Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (meetsMinTarget) Emerald100 else Gold100
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (meetsMinTarget)
                            "✓ Jumlah: $calculatedTotal ayat (Target standar minimal 5 ayat tercapai)"
                        else
                            "⚠ Jumlah: $calculatedTotal ayat (Di bawah target minimal 5 ayat/sesi)",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (meetsMinTarget) Emerald900 else Gold800
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMastered, onCheckedChange = { isMastered = it })
                    Text("Dinyatakan Mutqin / Dikuasai", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Koreksi Makhraj & Tajwid") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (surah.isNotBlank()) {
                        onConfirm(
                            surah, startInt, endInt, calculatedTotal, 85, 85, 85,
                            mistakes.toIntOrNull() ?: 0,
                            if (meetsMinTarget) "TERCAPAI" else "BELUM_LANCAR",
                            isMastered, notes, ""
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Simpan Setoran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun MurajaahDialog(
    santri: SantriEntity,
    onDismiss: () -> Unit,
    onConfirm: (surah: String, start: Int, end: Int, flLevel: String, mistakes: Int, tajScore: Int, notes: String, followUp: String) -> Unit
) {
    var surah by remember { mutableStateOf("Juz 30 (An-Nas s/d An-Naba)") }
    var ayahStart by remember { mutableStateOf("1") }
    var ayahEnd by remember { mutableStateOf("30") }
    var fluencyLevel by remember { mutableStateOf("Lancar") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pencatatan Murajaah", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name}", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = surah,
                    onValueChange = { surah = it },
                    label = { Text("Surah / Bagian Juz yang Diulang*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tingkat Kelancaran:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Sangat Lancar", "Lancar", "Cukup", "Perlu Ulang").forEach { lvl ->
                        OutlinedButton(
                            onClick = { fluencyLevel = lvl },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (fluencyLevel == lvl) Gold100 else PureWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            Text(lvl, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold800)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Pengajar") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (surah.isNotBlank()) {
                        onConfirm(
                            surah, ayahStart.toIntOrNull() ?: 1, ayahEnd.toIntOrNull() ?: 1,
                            fluencyLevel, 0, 85, notes, ""
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Simpan Murajaah")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun IqroDialog(
    santri: SantriEntity,
    onDismiss: () -> Unit,
    onConfirm: (vol: Int, page: String, flLevel: String, mistakes: String, notes: String, followUp: String) -> Unit
) {
    var volume by remember { mutableIntStateOf(1) }
    var page by remember { mutableStateOf("Halaman 10") }
    var fluency by remember { mutableStateOf("Lancar") }
    var mistakes by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pencatatan Iqro'", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name}", fontWeight = FontWeight.Bold)

                Text("Pilih Jilid Iqro':", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..6).forEach { v ->
                        OutlinedButton(
                            onClick = { volume = v },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (volume == v) Emerald100 else PureWhite
                            ),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("$v", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = page,
                    onValueChange = { page = it },
                    label = { Text("Halaman / Baris*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Kelancaran:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Sangat Lancar", "Lancar", "Cukup", "Kurang").forEach { f ->
                        OutlinedButton(
                            onClick = { fluency = f },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (fluency == f) Emerald100 else PureWhite
                            ),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text(f, fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = mistakes,
                    onValueChange = { mistakes = it },
                    label = { Text("Kesalahan Bacaan (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Tindak Lanjut / Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(volume, page, fluency, mistakes, notes, "") },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Simpan Iqro'")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun TahsinDialog(
    santri: SantriEntity,
    onDismiss: () -> Unit,
    onConfirm: (surah: String, start: Int, end: Int, flScore: Int, makhScore: Int, tajScore: Int, mistakes: String, notes: String, recs: String) -> Unit
) {
    var surah by remember { mutableStateOf("Al-Baqarah") }
    var ayahStart by remember { mutableStateOf("1") }
    var ayahEnd by remember { mutableStateOf("5") }
    var mistakes by remember { mutableStateOf("") }
    var recs by remember { mutableStateOf("Latihan panjang mad thobi'i dan dengung nun bertasydid") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Perbaikan Bacaan Al-Qur'an (Tahsin)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name}", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = surah,
                    onValueChange = { surah = it },
                    label = { Text("Nama Surah*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ayahStart,
                        onValueChange = { ayahStart = it },
                        label = { Text("Ayat Awal") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ayahEnd,
                        onValueChange = { ayahEnd = it },
                        label = { Text("Ayat Akhir") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = mistakes,
                    onValueChange = { mistakes = it },
                    label = { Text("Kesalahan Makhraj / Tajwid") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recs,
                    onValueChange = { recs = it },
                    label = { Text("Rekomendasi Latihan di Rumah") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        surah, ayahStart.toIntOrNull() ?: 1, ayahEnd.toIntOrNull() ?: 5,
                        85, 85, 85, mistakes, "", recs
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Simpan Tahsin")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AttendanceDialog(
    santri: SantriEntity,
    onDismiss: () -> Unit,
    onConfirm: (sessionType: String, status: String, notes: String) -> Unit
) {
    var status by remember { mutableStateOf("HADIR") }
    var sessionType by remember { mutableStateOf("Sore") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Presensi Kehadiran Santri", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name}", fontWeight = FontWeight.Bold)

                Text("Pilih Sesi:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Sore", "Pagi").forEach { ses ->
                        OutlinedButton(
                            onClick = { sessionType = ses },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (sessionType == ses) Emerald100 else PureWhite
                            )
                        ) {
                            Text("Sesi $ses")
                        }
                    }
                }

                Text("Status Kehadiran:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("HADIR", "IZIN", "SAKIT", "ALPA").forEach { st ->
                        OutlinedButton(
                            onClick = { status = st },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (status == st) Emerald100 else PureWhite
                            ),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(sessionType, status, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Catat Presensi")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun TeacherPromotionDialog(
    santri: SantriEntity,
    targetGroupId: String,
    onDismiss: () -> Unit,
    onConfirm: (justification: String) -> Unit
) {
    var justification by remember { mutableStateOf("Santri telah menuntaskan target dan bacaan sudah sangat lancar serta mutqin.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usulkan Kenaikan Kelompok", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name}", fontWeight = FontWeight.Bold)
                Text("Kelompok asal: ${santri.currentGroupId} ➔ Kelompok target: $targetGroupId")

                OutlinedTextField(
                    value = justification,
                    onValueChange = { justification = it },
                    label = { Text("Alasan / Justifikasi Pengajar*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Usulan ini akan masuk ke dashboard Kepala Program untuk diverifikasi dan disetujui.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (justification.isNotBlank()) onConfirm(justification) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Ajukan Usulan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun JournalDialog(
    assignedGroups: List<GroupEntity>,
    defaultGroupId: String,
    onDismiss: () -> Unit,
    onConfirm: (gid: String, topic: String, activities: String, count: Int, obstacles: String, results: String, plan: String) -> Unit
) {
    var selectedGid by remember { mutableStateOf(defaultGroupId) }
    var topic by remember { mutableStateOf("Makharijul Huruf & Simakan Juz 30") }
    var activities by remember { mutableStateOf("Talaqqi bersama, pembacaan giliran, dan koreksi waqaf ibtida.") }
    var studentCount by remember { mutableStateOf("15") }
    var obstacles by remember { mutableStateOf("Beberapa santri masih tertukar huruf tsa dan sin.") }
    var results by remember { mutableStateOf("Target tilawah dan simakan tercapai dengan baik.") }
    var plan by remember { mutableStateOf("Fokus latihan pengucapan huruf safir pada pertemuan berikutnya.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Isi Jurnal Harian Pengajar", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Pilih Kelompok:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    assignedGroups.forEach { grp ->
                        OutlinedButton(
                            onClick = { selectedGid = grp.id },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedGid == grp.id) Emerald100 else PureWhite
                            )
                        ) {
                            Text(grp.code, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Materi / Topik*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = activities,
                    onValueChange = { activities = it },
                    label = { Text("Kegiatan Pembelajaran*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = studentCount,
                    onValueChange = { studentCount = it },
                    label = { Text("Jumlah Santri Hadir") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = obstacles,
                    onValueChange = { obstacles = it },
                    label = { Text("Kendala yang Dihadapi") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plan,
                    onValueChange = { plan = it },
                    label = { Text("Rencana Tindak Lanjut") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank() && activities.isNotBlank()) {
                        onConfirm(
                            selectedGid, topic, activities, studentCount.toIntOrNull() ?: 0,
                            obstacles, results, plan
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Simpan Jurnal")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
