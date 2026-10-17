package com.example.mutqin.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mutqin.core.model.Resource
import com.example.mutqin.core.model.UserSession
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AcademicYearEntity
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.GroupEntity
import com.example.mutqin.data.local.entity.PromotionProposalEntity
import com.example.mutqin.data.local.entity.SantriEntity
import com.example.mutqin.data.local.entity.TeacherEntity
import com.example.mutqin.data.repository.BackupManifest
import com.example.mutqin.data.repository.BackupRepository
import com.example.mutqin.data.repository.EvaluationRepository
import com.example.mutqin.data.repository.GroupRepository
import com.example.mutqin.data.repository.SantriRepository
import com.example.mutqin.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val teacherRepo: TeacherRepository,
    private val groupRepo: GroupRepository,
    private val santriRepo: SantriRepository,
    private val evaluationRepo: EvaluationRepository,
    private val backupRepo: BackupRepository,
    private val dao: MutqinDao
) : ViewModel() {

    val allTeachers: StateFlow<List<TeacherEntity>> = teacherRepo.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGroups: StateFlow<List<GroupEntity>> = groupRepo.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGroups: StateFlow<List<GroupEntity>> = groupRepo.activeGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAcademicYears: StateFlow<List<AcademicYearEntity>> = groupRepo.allAcademicYears
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeYear: StateFlow<AcademicYearEntity?> = groupRepo.activeAcademicYear
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSantri: StateFlow<List<SantriEntity>> = santriRepo.allSantri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingProposals: StateFlow<List<PromotionProposalEntity>> = evaluationRepo.pendingPromotionProposals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = dao.getRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operation status message (e.g. snackbar or toast)
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Backup states
    private val _backupJson = MutableStateFlow<String?>(null)
    val backupJson: StateFlow<String?> = _backupJson.asStateFlow()

    private val _backupManifest = MutableStateFlow<BackupManifest?>(null)
    val backupManifest: StateFlow<BackupManifest?> = _backupManifest.asStateFlow()

    fun clearMessage() {
        _userMessage.value = null
    }

    // --- SANTRI ACTIONS ---
    fun addSantri(
        admin: UserSession,
        nis: String,
        name: String,
        gender: String,
        schoolClass: String,
        joinDate: String,
        admissionYear: String,
        academicYearId: String,
        groupId: String,
        responsibleTeacherId: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = santriRepo.createSantri(
                adminId = admin.userId,
                adminName = admin.name,
                nis = nis,
                name = name,
                gender = gender,
                schoolClass = schoolClass,
                joinDate = joinDate,
                admissionYear = admissionYear,
                academicYearId = academicYearId,
                groupId = groupId,
                responsibleTeacherId = responsibleTeacherId,
                notes = notes
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Santri ${res.data.name} berhasil ditambahkan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateSantri(
        admin: UserSession,
        santriId: String,
        name: String,
        gender: String,
        schoolClass: String,
        responsibleTeacherId: String,
        status: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = santriRepo.updateSantri(
                adminId = admin.userId,
                adminName = admin.name,
                santriId = santriId,
                name = name,
                gender = gender,
                schoolClass = schoolClass,
                responsibleTeacherId = responsibleTeacherId,
                status = status,
                notes = notes
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Profil santri berhasil diperbarui."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun transferSantri(
        admin: UserSession,
        santriId: String,
        targetGroupId: String,
        reason: String,
        academicYearId: String,
        transferDate: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = santriRepo.transferGroup(
                adminId = admin.userId,
                adminName = admin.name,
                santriId = santriId,
                targetGroupId = targetGroupId,
                reason = reason,
                academicYearId = academicYearId,
                transferDate = transferDate
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Santri berhasil dipindahkan ke kelompok baru."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    // --- TEACHER ACTIONS ---
    fun addTeacher(
        admin: UserSession,
        nip: String,
        name: String,
        email: String,
        phone: String,
        initialPass: String,
        assignedGroups: List<String>,
        academicYearId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = teacherRepo.createTeacher(
                adminId = admin.userId,
                adminName = admin.name,
                nip = nip,
                name = name,
                email = email,
                phone = phone,
                initialPassword = initialPass,
                assignedGroupIds = assignedGroups,
                academicYearId = academicYearId
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Pengajar ${res.data.name} berhasil dibuat."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateTeacherProfile(
        admin: UserSession,
        teacherId: String,
        name: String,
        nip: String,
        phone: String,
        assignedGroups: List<String>,
        academicYearId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = teacherRepo.updateTeacherProfile(
                adminId = admin.userId,
                adminName = admin.name,
                teacherId = teacherId,
                name = name,
                nip = nip,
                phone = phone,
                assignedGroupIds = assignedGroups,
                academicYearId = academicYearId
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Profil pengajar berhasil diperbarui."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun toggleTeacherActive(admin: UserSession, teacherId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentStatus
            when (val res = teacherRepo.toggleTeacherActiveStatus(admin.userId, admin.name, teacherId, newStatus)) {
                is Resource.Success -> {
                    _userMessage.value = if (newStatus) "Akun pengajar diaktifkan." else "Akun pengajar dinonaktifkan."
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    // --- GROUP ACTIONS ---
    fun addGroup(
        admin: UserSession,
        code: String,
        name: String,
        description: String,
        focus: String,
        targetMinVerses: Int,
        indicators: String,
        academicYearId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = groupRepo.createGroup(
                adminId = admin.userId,
                adminName = admin.name,
                code = code,
                name = name,
                description = description,
                focus = focus,
                targetMinVerses = targetMinVerses,
                evaluationIndicators = indicators,
                academicYearId = academicYearId
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Kelompok ${res.data.name} berhasil dibuat."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun toggleGroupActive(admin: UserSession, groupId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentStatus
            when (val res = groupRepo.toggleGroupActiveStatus(admin.userId, admin.name, groupId, newStatus)) {
                is Resource.Success -> {
                    _userMessage.value = if (newStatus) "Kelompok diaktifkan." else "Kelompok dinonaktifkan (arsip aman)."
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    // --- ACADEMIC YEAR ---
    fun addAcademicYear(
        admin: UserSession,
        name: String,
        startDate: String,
        endDate: String,
        setAsActive: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = groupRepo.createAcademicYear(
                adminId = admin.userId,
                adminName = admin.name,
                name = name,
                startDate = startDate,
                endDate = endDate,
                setAsActive = setAsActive
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Tahun ajaran ${res.data.name} berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun activateAcademicYear(admin: UserSession, yearId: String) {
        viewModelScope.launch {
            when (val res = groupRepo.activateAcademicYear(admin.userId, admin.name, yearId)) {
                is Resource.Success -> _userMessage.value = "Tahun ajaran aktif telah diperbarui."
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    // --- PROMOTION DECISION ---
    fun decidePromotion(
        admin: UserSession,
        proposal: PromotionProposalEntity,
        newStatus: String,
        decisionNotes: String,
        decisionDate: String
    ) {
        viewModelScope.launch {
            when (val res = evaluationRepo.processPromotionDecision(
                adminId = admin.userId,
                adminName = admin.name,
                proposal = proposal,
                newStatus = newStatus,
                decisionNotes = decisionNotes,
                decisionDate = decisionDate
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Keputusan usulan kenaikan berhasil disimpan ($newStatus)."
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    // --- BACKUP & RESTORE ---
    fun generateBackup(admin: UserSession) {
        viewModelScope.launch {
            when (val res = backupRepo.createFullBackupJson(admin.userId, admin.name)) {
                is Resource.Success -> {
                    _backupManifest.value = res.data.first
                    _backupJson.value = res.data.second
                    _userMessage.value = "Backup data berhasil dibuat! (${res.data.first.totalSantri} santri, Checksum: ${res.data.first.checksum.take(8)})"
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun restoreFromJson(admin: UserSession, jsonString: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val preview = backupRepo.inspectBackupJson(jsonString)
            if (!preview.isValid) {
                _userMessage.value = preview.error ?: "File JSON tidak valid."
                return@launch
            }
            when (val res = backupRepo.executeRestore(admin.userId, admin.name, preview)) {
                is Resource.Success -> {
                    _userMessage.value = res.data
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }
}
