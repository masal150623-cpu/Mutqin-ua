package com.example.mutqin.feature.admin

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mutqin.core.model.ProgramConstants
import com.example.mutqin.core.model.UserSession
import com.example.mutqin.data.local.entity.GroupEntity
import com.example.mutqin.data.local.entity.PromotionProposalEntity
import com.example.mutqin.data.local.entity.SantriEntity
import com.example.mutqin.data.local.entity.TeacherEntity
import com.example.mutqin.ui.components.EmptyState
import com.example.mutqin.ui.components.GroupBadge
import com.example.mutqin.ui.components.MetricStatCard
import com.example.mutqin.ui.components.MutqinTopBar
import com.example.mutqin.ui.components.SectionHeader
import com.example.mutqin.ui.components.StatusChip
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Gold100
import com.example.ui.theme.Gold700
import com.example.ui.theme.Gold800
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    session: UserSession,
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    var selectedBottomNav by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userMsg by viewModel.userMessage.collectAsState()

    val santriList by viewModel.allSantri.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()
    val groups by viewModel.allGroups.collectAsState()
    val activeYear by viewModel.activeYear.collectAsState()
    val pendingProposals by viewModel.pendingProposals.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    // Modals
    var showAddSantriModal by remember { mutableStateOf(false) }
    var showTransferSantriModal by remember { mutableStateOf<SantriEntity?>(null) }
    var showAddTeacherModal by remember { mutableStateOf(false) }
    var showAddGroupModal by remember { mutableStateOf(false) }
    var showDecideProposalModal by remember { mutableStateOf<PromotionProposalEntity?>(null) }
    var showBackupModal by remember { mutableStateOf(false) }
    var showRestoreModal by remember { mutableStateOf(false) }

    LaunchedEffect(userMsg) {
        userMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            MutqinTopBar(
                title = "MUTQIN - KEPALA PROGRAM",
                subtitle = "${session.name} • TA: ${activeYear?.name ?: "2026/2027"}",
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("btn_logout")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = PureWhite)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomNav == 0,
                    onClick = { selectedBottomNav = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Beranda", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        indicatorColor = Emerald100
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 1,
                    onClick = { selectedBottomNav = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Santri") },
                    label = { Text("Santri (${santriList.size})", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        indicatorColor = Emerald100
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 2,
                    onClick = { selectedBottomNav = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = "Pengajar") },
                    label = { Text("Pengajar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        indicatorColor = Emerald100
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 3,
                    onClick = { selectedBottomNav = 3 },
                    icon = {
                        if (pendingProposals.isNotEmpty()) {
                            BadgedBox(badge = { Badge { Text("${pendingProposals.size}") } }) {
                                Icon(Icons.Default.TrendingUp, contentDescription = "Kenaikan")
                            }
                        } else {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Kenaikan")
                        }
                    },
                    label = { Text("Kenaikan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        indicatorColor = Emerald100
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 4,
                    onClick = { selectedBottomNav = 4 },
                    icon = { Icon(Icons.Default.Backup, contentDescription = "Sistem") },
                    label = { Text("Sistem", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald800,
                        indicatorColor = Emerald100
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedBottomNav == 1) {
                FloatingActionButton(
                    onClick = { showAddSantriModal = true },
                    containerColor = Emerald800,
                    contentColor = PureWhite,
                    modifier = Modifier.testTag("fab_add_santri")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Santri")
                }
            } else if (selectedBottomNav == 2) {
                FloatingActionButton(
                    onClick = { showAddTeacherModal = true },
                    containerColor = Emerald800,
                    contentColor = PureWhite,
                    modifier = Modifier.testTag("fab_add_teacher")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Pengajar")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate50)
        ) {
            when (selectedBottomNav) {
                0 -> AdminHomeTab(
                    santriList = santriList,
                    teachers = teachers,
                    groups = groups,
                    pendingProposals = pendingProposals,
                    onNavigateToSantri = { selectedBottomNav = 1 },
                    onNavigateToTeachers = { selectedBottomNav = 2 },
                    onNavigateToProposals = { selectedBottomNav = 3 },
                    onNavigateToBackup = { selectedBottomNav = 4 }
                )
                1 -> AdminSantriTab(
                    santriList = santriList,
                    groups = groups,
                    onTransferClick = { showTransferSantriModal = it }
                )
                2 -> AdminTeacherTab(
                    teachers = teachers,
                    groups = groups,
                    onToggleActive = { teacher ->
                        viewModel.toggleTeacherActive(session, teacher.id, teacher.isActive)
                    }
                )
                3 -> AdminPromotionTab(
                    proposals = pendingProposals,
                    santriList = santriList,
                    groups = groups,
                    onDecide = { showDecideProposalModal = it }
                )
                4 -> AdminSystemTab(
                    groups = groups,
                    auditLogs = auditLogs,
                    onTriggerBackup = { viewModel.generateBackup(session) },
                    onTriggerRestore = { showRestoreModal = true }
                )
            }
        }
    }

    // Modal: Tambah Santri
    if (showAddSantriModal) {
        AddSantriDialog(
            groups = groups,
            activeYearId = activeYear?.id ?: ProgramConstants.DEFAULT_ACADEMIC_YEAR_ID,
            onDismiss = { showAddSantriModal = false },
            onConfirm = { nis, name, gender, sClass, jDate, admYear, gid, respTchr, notes ->
                viewModel.addSantri(
                    admin = session,
                    nis = nis,
                    name = name,
                    gender = gender,
                    schoolClass = sClass,
                    joinDate = jDate,
                    admissionYear = admYear,
                    academicYearId = activeYear?.id ?: ProgramConstants.DEFAULT_ACADEMIC_YEAR_ID,
                    groupId = gid,
                    responsibleTeacherId = respTchr,
                    notes = notes
                ) {
                    showAddSantriModal = false
                }
            }
        )
    }

    // Modal: Transfer Kelompok
    if (showTransferSantriModal != null) {
        val targetSantri = showTransferSantriModal!!
        TransferGroupDialog(
            santri = targetSantri,
            groups = groups,
            onDismiss = { showTransferSantriModal = null },
            onConfirm = { targetGid, reason, date ->
                viewModel.transferSantri(
                    admin = session,
                    santriId = targetSantri.id,
                    targetGroupId = targetGid,
                    reason = reason,
                    academicYearId = activeYear?.id ?: ProgramConstants.DEFAULT_ACADEMIC_YEAR_ID,
                    transferDate = date
                ) {
                    showTransferSantriModal = null
                }
            }
        )
    }

    // Modal: Tambah Pengajar
    if (showAddTeacherModal) {
        AddTeacherDialog(
            groups = groups,
            onDismiss = { showAddTeacherModal = false },
            onConfirm = { nip, name, email, phone, pass, assignedGroupIds ->
                viewModel.addTeacher(
                    admin = session,
                    nip = nip,
                    name = name,
                    email = email,
                    phone = phone,
                    initialPass = pass,
                    assignedGroups = assignedGroupIds,
                    academicYearId = activeYear?.id ?: ProgramConstants.DEFAULT_ACADEMIC_YEAR_ID
                ) {
                    showAddTeacherModal = false
                }
            }
        )
    }

    // Modal: Keputusan Kenaikan Kelompok
    if (showDecideProposalModal != null) {
        val prop = showDecideProposalModal!!
        DecidePromotionDialog(
            proposal = prop,
            onDismiss = { showDecideProposalModal = null },
            onDecide = { status, notes ->
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.decidePromotion(
                    admin = session,
                    proposal = prop,
                    newStatus = status,
                    decisionNotes = notes,
                    decisionDate = today
                )
                showDecideProposalModal = null
            }
        )
    }

    // Modal: Restore Data JSON
    if (showRestoreModal) {
        RestoreJsonDialog(
            onDismiss = { showRestoreModal = false },
            onRestore = { jsonStr ->
                viewModel.restoreFromJson(session, jsonStr) {
                    showRestoreModal = false
                }
            }
        )
    }
}

// -------------------------------------------------------------
// SUB-TAB 0: BERANDA / DASHBOARD ADMIN
// -------------------------------------------------------------
@Composable
private fun AdminHomeTab(
    santriList: List<SantriEntity>,
    teachers: List<TeacherEntity>,
    groups: List<GroupEntity>,
    pendingProposals: List<PromotionProposalEntity>,
    onNavigateToSantri: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToProposals: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val activeSantri = santriList.count { it.status == "AKTIF" }
    val activeTeachers = teachers.count { it.isActive && it.role == "TEACHER" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Status
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
                            text = "Status Program Ulul Albab",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            color = Gold100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Tersinkronisasi Lokal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Gold800,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Monitoring perkembangan harian Iqro', perbaikan bacaan (Tahsin), setoran hafalan (Ziyadah), dan murajaah berjalan lancar.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Emerald100, lineHeight = 18.sp)
                    )
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Santri Aktif",
                    value = "$activeSantri",
                    icon = Icons.Default.People,
                    contentColor = Emerald800,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSantri
                )
                MetricStatCard(
                    title = "Ustaz / Pengajar",
                    value = "$activeTeachers",
                    icon = Icons.Default.School,
                    contentColor = Gold700,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTeachers
                )
            }
        }

        // Proposal Alert Card
        if (pendingProposals.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Gold100),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToProposals
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Gold700, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${pendingProposals.size}",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ada Usulan Kenaikan Kelompok!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Gold800
                                )
                            )
                            Text(
                                text = "${pendingProposals.size} usulan dari ustaz menunggu persetujuan Kepala Program.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                            )
                        }
                    }
                }
            }
        }

        // Distribusi Santri per Kelompok
        item {
            SectionHeader(
                title = "Distribusi Santri per Kelompok",
                subtitle = "Empat kelompok tahapan pembelajaran Al-Qur'an"
            )
        }

        items(groups) { group ->
            val count = santriList.count { it.currentGroupId == group.id && it.status == "AKTIF" }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GroupBadge(code = group.code)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = group.focus,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "$count Santri",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    )
                }
            }
        }

        // Pintasan Cepat
        item {
            SectionHeader(title = "Pintasan Administratif")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToBackup,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cadangan Data", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToSantri,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kelola Santri", fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB 1: KELOLA SANTRI
// -------------------------------------------------------------
@Composable
private fun AdminSantriTab(
    santriList: List<SantriEntity>,
    groups: List<GroupEntity>,
    onTransferClick: (SantriEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val filtered = santriList.filter { s ->
        val matchSearch = searchQuery.isBlank() || s.name.contains(searchQuery, ignoreCase = true) || s.nis.contains(searchQuery)
        val matchGroup = selectedGroupFilter == null || s.currentGroupId == selectedGroupFilter
        val matchStatus = selectedStatusFilter == null || s.status == selectedStatusFilter
        matchSearch && matchGroup && matchStatus
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Bar
        Surface(color = PureWhite, shadowElevation = 2.dp) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama atau NIS santri...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_santri"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Group Chips
                ScrollableTabRow(
                    selectedTabIndex = if (selectedGroupFilter == null) 0 else groups.indexOfFirst { it.id == selectedGroupFilter } + 1,
                    edgePadding = 0.dp,
                    divider = {},
                    containerColor = PureWhite
                ) {
                    Tab(
                        selected = selectedGroupFilter == null,
                        onClick = { selectedGroupFilter = null },
                        text = { Text("Semua Kelompok (${santriList.size})", fontSize = 12.sp) }
                    )
                    groups.forEach { g ->
                        val count = santriList.count { it.currentGroupId == g.id }
                        Tab(
                            selected = selectedGroupFilter == g.id,
                            onClick = { selectedGroupFilter = g.id },
                            text = { Text("${g.code} ($count)", fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptyState(
                message = "Tidak ada santri yang cocok",
                subMessage = "Coba ubah kata kunci pencarian atau filter kelompok di atas."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { santri ->
                    val grp = groups.find { it.id == santri.currentGroupId }
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
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                StatusChip(status = santri.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "NIS: ${santri.nis} • Kelas: ${santri.schoolClass.ifBlank { "-" }} • Gender: ${if (santri.gender == "L") "Ikhwan (L)" else "Akhwat (P)"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )

                            if (santri.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Catatan: ${santri.notes}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Slate100)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onTransferClick(santri) },
                                    modifier = Modifier.testTag("btn_transfer_${santri.id}")
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pindah Kelompok", fontSize = 12.sp, color = Emerald800)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB 2: KELOLA PENGAJAR
// -------------------------------------------------------------
@Composable
private fun AdminTeacherTab(
    teachers: List<TeacherEntity>,
    groups: List<GroupEntity>,
    onToggleActive: (TeacherEntity) -> Unit
) {
    val teacherList = teachers.filter { it.role == "TEACHER" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Daftar Ustaz & Ustazah Pengajar",
                subtitle = "${teacherList.size} pengajar terdaftar. Akun non-aktif mempertahankan histori."
            )
        }

        if (teacherList.isEmpty()) {
            item {
                EmptyState(
                    message = "Belum ada pengajar tambahan",
                    subMessage = "Klik tombol '+' di pojok kanan bawah untuk mengundang pengajar baru."
                )
            }
        }

        items(teacherList) { teacher ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = teacher.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "NIP: ${teacher.nip} • ${teacher.email}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                            if (teacher.phone.isNotBlank()) {
                                Text(
                                    text = "HP: ${teacher.phone}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (teacher.isActive) "Aktif" else "Nonaktif",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (teacher.isActive) Emerald800 else ErrorRed
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = teacher.isActive,
                                onCheckedChange = { onToggleActive(teacher) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Emerald800,
                                    checkedTrackColor = Emerald100
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB 3: USULAN KENAIKAN KELOMPOK
// -------------------------------------------------------------
@Composable
private fun AdminPromotionTab(
    proposals: List<PromotionProposalEntity>,
    santriList: List<SantriEntity>,
    groups: List<GroupEntity>,
    onDecide: (PromotionProposalEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Usulan Kenaikan Kelompok",
                subtitle = "Verifikasi rekomendasi capaian santri dari ustaz pengajar sebelum disetujui."
            )
        }

        if (proposals.isEmpty()) {
            item {
                EmptyState(
                    message = "Tidak ada usulan kenaikan tertunda",
                    subMessage = "Semua usulan telah diproses atau belum ada usulan baru dari pengajar."
                )
            }
        }

        items(proposals) { prop ->
            val santri = santriList.find { it.id == prop.santriId }
            val grpAsal = groups.find { it.id == prop.currentGroupId }
            val grpTujuan = groups.find { it.id == prop.targetGroupId }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = santri?.name ?: "Santri ID: ${prop.santriId}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        StatusChip(status = prop.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GroupBadge(code = grpAsal?.code ?: "?")
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 6.dp).size(18.dp),
                            tint = Slate500
                        )
                        GroupBadge(code = grpTujuan?.code ?: "?")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanggal: ${prop.proposalDate}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Alasan & Justifikasi Pengajar:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate700)
                    )
                    Text(
                        text = prop.justification,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate900)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onDecide(prop) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tinjau & Tetapkan Keputusan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB 4: SISTEM, BACKUP & AUDIT LOG
// -------------------------------------------------------------
@Composable
private fun AdminSystemTab(
    groups: List<GroupEntity>,
    auditLogs: List<com.example.mutqin.data.local.entity.AuditLogEntity>,
    onTriggerBackup: () -> Unit,
    onTriggerRestore: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Backup & Restore Cards
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Manajemen Cadangan & Pemulihan (Backup/Restore)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ekspor data lengkap mencakup data santri, akun pengajar, histori perpindahan, riwayat hafalan & evaluasi dalam format JSON aman dengan verifikasi checksum SHA-256.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, lineHeight = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTriggerBackup,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Cadangan")
                        }
                        OutlinedButton(
                            onClick = onTriggerRestore,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pulihkan (Restore)", color = Emerald800)
                        }
                    }
                }
            }
        }

        // Audit Log Section
        item {
            SectionHeader(
                title = "Log Audit & Keamanan Sistem",
                subtitle = "Riwayat aktivitas penting (login, perubahan profil, transfer, usulan)"
            )
        }

        if (auditLogs.isEmpty()) {
            item {
                EmptyState(message = "Belum ada riwayat audit log tercatat.")
            }
        } else {
            items(auditLogs) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.actionType,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                            )
                            Text(
                                text = sdf.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate900)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Oleh: ${log.userName} (${log.userRole})",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG MODALS
// -------------------------------------------------------------

@Composable
private fun AddSantriDialog(
    groups: List<GroupEntity>,
    activeYearId: String,
    onDismiss: () -> Unit,
    onConfirm: (nis: String, name: String, gender: String, sClass: String, jDate: String, admYear: String, gid: String, respTchr: String, notes: String) -> Unit
) {
    var nis by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("L") }
    var sClass by remember { mutableStateOf("SD Kelas 3") }
    var joinDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var admissionYear by remember { mutableStateOf("2026") }
    var selectedGroupId by remember { mutableStateOf(groups.firstOrNull()?.id ?: ProgramConstants.GROUP_D_ID) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Data Santri Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nis,
                    onValueChange = { nis = it },
                    label = { Text("Nomor Induk Santri (NIS)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Santri*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { gender = "L" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (gender == "L") Emerald100 else PureWhite
                        )
                    ) {
                        Text("Ikhwan (L)")
                    }
                    OutlinedButton(
                        onClick = { gender = "P" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (gender == "P") Emerald100 else PureWhite
                        )
                    ) {
                        Text("Akhwat (P)")
                    }
                }
                OutlinedTextField(
                    value = sClass,
                    onValueChange = { sClass = it },
                    label = { Text("Kelas Sekolah Formal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pilih Kelompok Awal:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groups.forEach { grp ->
                        OutlinedButton(
                            onClick = { selectedGroupId = grp.id },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedGroupId == grp.id) Emerald100 else PureWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(grp.code, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Latar Belakang") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nis.isNotBlank() && name.isNotBlank()) {
                        onConfirm(nis, name, gender, sClass, joinDate, admissionYear, selectedGroupId, "", notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun TransferGroupDialog(
    santri: SantriEntity,
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (targetGid: String, reason: String, date: String) -> Unit
) {
    var selectedTargetGroupId by remember { mutableStateOf(groups.firstOrNull { it.id != santri.currentGroupId }?.id ?: "") }
    var reason by remember { mutableStateOf("Kenaikan tingkat setelah evaluasi berkala.") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pindah Kelompok Santri", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Santri: ${santri.name} (${santri.nis})", fontWeight = FontWeight.Bold)
                Text("Kelompok saat ini: ${santri.currentGroupId}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Pilih Kelompok Tujuan:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groups.forEach { grp ->
                        val isCurrent = grp.id == santri.currentGroupId
                        OutlinedButton(
                            onClick = { if (!isCurrent) selectedTargetGroupId = grp.id },
                            modifier = Modifier.weight(1f),
                            enabled = !isCurrent,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTargetGroupId == grp.id) Emerald100 else PureWhite
                            )
                        ) {
                            Text(grp.code, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Perpindahan*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Histori lama dan catatan pembelajaran di kelompok sebelumnya akan tetap aman tersimpan.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTargetGroupId.isNotBlank() && reason.isNotBlank()) {
                        onConfirm(selectedTargetGroupId, reason, today)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Konfirmasi Pindah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun AddTeacherDialog(
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (nip: String, name: String, email: String, phone: String, pass: String, assignedGids: List<String>) -> Unit
) {
    var nip by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialPass by remember { mutableStateOf("ustaz123") }
    val selectedGroupIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Akun Pengajar", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pengajar / Ustaz*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nip,
                    onValueChange = { nip = it },
                    label = { Text("NIP Pengajar (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Login*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialPass,
                    onValueChange = { initialPass = it },
                    label = { Text("Kata Sandi Awal* (min 6 karakter)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Penugasan Kelompok (Boleh lebih dari satu):", style = MaterialTheme.typography.labelSmall)
                groups.forEach { grp ->
                    val isChecked = selectedGroupIds.contains(grp.id)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { check ->
                                if (check) selectedGroupIds.add(grp.id) else selectedGroupIds.remove(grp.id)
                            }
                        )
                        Text("${grp.code} - ${grp.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && initialPass.length >= 6) {
                        onConfirm(nip, name, email, phone, initialPass, selectedGroupIds.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Daftarkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun DecidePromotionDialog(
    proposal: PromotionProposalEntity,
    onDismiss: () -> Unit,
    onDecide: (status: String, notes: String) -> Unit
) {
    var decisionNotes by remember { mutableStateOf("Memenuhi syarat kenaikan kelompok berdasarkan evaluasi.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tetapkan Keputusan Kenaikan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Justifikasi pengajar: ${proposal.justification}")
                OutlinedTextField(
                    value = decisionNotes,
                    onValueChange = { decisionNotes = it },
                    label = { Text("Catatan Keputusan Admin*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onDecide("REJECTED", decisionNotes) }
                ) {
                    Text("Tolak", color = ErrorRed)
                }
                Button(
                    onClick = { onDecide("APPROVED", decisionNotes) },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Setujui")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun RestoreJsonDialog(
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pulihkan Data dari JSON", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tempelkan isi JSON file cadangan (backup) yang sah di bawah ini. Integritas data akan diperiksa sebelum restore.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    label = { Text("Isi JSON Cadangan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (jsonText.isNotBlank()) onRestore(jsonText) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Periksa & Pulihkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
