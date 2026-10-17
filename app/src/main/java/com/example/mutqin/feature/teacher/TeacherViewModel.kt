package com.example.mutqin.feature.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mutqin.core.model.Resource
import com.example.mutqin.core.model.UserSession
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AttendanceEntity
import com.example.mutqin.data.local.entity.GroupEntity
import com.example.mutqin.data.local.entity.IqroRecordEntity
import com.example.mutqin.data.local.entity.MurajaahRecordEntity
import com.example.mutqin.data.local.entity.SantriEntity
import com.example.mutqin.data.local.entity.TahsinRecordEntity
import com.example.mutqin.data.local.entity.TeacherJournalEntity
import com.example.mutqin.data.local.entity.ZiyadahRecordEntity
import com.example.mutqin.data.repository.EvaluationRepository
import com.example.mutqin.data.repository.GroupRepository
import com.example.mutqin.data.repository.LearningRepository
import com.example.mutqin.data.repository.SantriRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeacherViewModel(
    val session: UserSession,
    private val groupRepo: GroupRepository,
    private val santriRepo: SantriRepository,
    private val learningRepo: LearningRepository,
    private val evalRepo: EvaluationRepository,
    private val dao: MutqinDao
) : ViewModel() {

    val assignedGroups: StateFlow<List<GroupEntity>> = groupRepo.getGroupsForTeacher(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mySantri: StateFlow<List<SantriEntity>> = santriRepo.getActiveSantriInGroups(session.assignedGroupIds)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myJournals: StateFlow<List<TeacherJournalEntity>> = learningRepo.getJournalsForTeacher(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearMessage() {
        _userMessage.value = null
    }

    // Records observation for a specific student
    fun getIqroForSantri(santriId: String): StateFlow<List<IqroRecordEntity>> =
        learningRepo.getIqroRecords(santriId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTahsinForSantri(santriId: String): StateFlow<List<TahsinRecordEntity>> =
        learningRepo.getTahsinRecords(santriId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getZiyadahForSantri(santriId: String): StateFlow<List<ZiyadahRecordEntity>> =
        learningRepo.getZiyadahRecords(santriId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMurajaahForSantri(santriId: String): StateFlow<List<MurajaahRecordEntity>> =
        learningRepo.getMurajaahRecords(santriId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getAttendanceForSantri(santriId: String): StateFlow<List<AttendanceEntity>> =
        learningRepo.getAttendanceForSantri(santriId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- RECORDING ACTIONS ---
    fun saveIqro(
        santriId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        volume: Int,
        pageOrLesson: String,
        fluencyLevel: String,
        readingMistakes: String,
        notes: String,
        followUp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = learningRepo.recordIqro(
                santriId = santriId,
                teacherId = session.userId,
                teacherName = session.name,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                volume = volume,
                pageOrLesson = pageOrLesson,
                fluencyLevel = fluencyLevel,
                readingMistakes = readingMistakes,
                notes = notes,
                followUpAction = followUp
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Catatan Iqro' berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveTahsin(
        santriId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        surahName: String,
        ayahStart: Int,
        ayahEnd: Int,
        fluencyScore: Int,
        makhrajScore: Int,
        tajwidScore: Int,
        mistakes: String,
        notes: String,
        recommendations: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = learningRepo.recordTahsin(
                santriId = santriId,
                teacherId = session.userId,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                surahName = surahName,
                ayahStart = ayahStart,
                ayahEnd = ayahEnd,
                fluencyScore = fluencyScore,
                makhrajScore = makhrajScore,
                tajwidScore = tajwidScore,
                mistakesDescription = mistakes,
                notes = notes,
                practiceRecommendations = recommendations
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Catatan perbaikan bacaan (Tahsin) berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveZiyadah(
        santriId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        surahName: String,
        ayahStart: Int,
        ayahEnd: Int,
        totalAyahs: Int?,
        fluencyScore: Int,
        tajwidScore: Int,
        makhrajScore: Int,
        mistakeCount: Int,
        status: String,
        isMastered: Boolean,
        notes: String,
        followUp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = learningRepo.recordZiyadah(
                santriId = santriId,
                teacherId = session.userId,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                surahName = surahName,
                ayahStart = ayahStart,
                ayahEnd = ayahEnd,
                totalAyahsInput = totalAyahs,
                fluencyScore = fluencyScore,
                tajwidScore = tajwidScore,
                makhrajScore = makhrajScore,
                mistakeCount = mistakeCount,
                status = status,
                isMastered = isMastered,
                notes = notes,
                followUpAction = followUp
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Setoran Ziyadah berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveMurajaah(
        santriId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        surahName: String,
        ayahStart: Int,
        ayahEnd: Int,
        fluencyLevel: String,
        mistakeCount: Int,
        tajwidScore: Int,
        notes: String,
        followUp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = learningRepo.recordMurajaah(
                santriId = santriId,
                teacherId = session.userId,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                surahName = surahName,
                ayahStart = ayahStart,
                ayahEnd = ayahEnd,
                fluencyLevel = fluencyLevel,
                mistakeCount = mistakeCount,
                tajwidScore = tajwidScore,
                notes = notes,
                followUpAction = followUp
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Catatan Murajaah berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveAttendance(
        santriId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        sessionType: String,
        status: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = learningRepo.recordAttendance(
                santriId = santriId,
                teacherId = session.userId,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                sessionType = sessionType,
                status = status,
                notes = notes
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Kehadiran santri dicatat (${res.data.status})."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveJournal(
        groupId: String,
        academicYearId: String,
        date: String,
        topic: String,
        activities: String,
        studentCount: Int,
        obstacles: String,
        results: String,
        followUp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = learningRepo.recordJournal(
                teacherId = session.userId,
                teacherName = session.name,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                topic = topic,
                activities = activities,
                studentAttendanceCount = studentCount,
                obstacles = obstacles,
                learningResults = results,
                followUpPlan = followUp
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Jurnal harian pengajar berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveEvaluation(
        santriId: String,
        groupId: String,
        academicYearId: String,
        period: String,
        iqroScore: Int,
        quranFluencyScore: Int,
        makhrajScore: Int,
        tajwidScore: Int,
        ziyadahScore: Int,
        murajaahScore: Int,
        attendanceScore: Int,
        notes: String,
        recommendations: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = evalRepo.saveEvaluation(
                evaluatorId = session.userId,
                evaluatorName = session.name,
                santriId = santriId,
                groupId = groupId,
                academicYearId = academicYearId,
                periodMonthYear = period,
                iqroScore = iqroScore,
                quranFluencyScore = quranFluencyScore,
                makhrajScore = makhrajScore,
                tajwidScore = tajwidScore,
                ziyadahScore = ziyadahScore,
                murajaahScore = murajaahScore,
                attendanceScore = attendanceScore,
                overallNotes = notes,
                teacherRecommendations = recommendations
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Evaluasi perkembangan bulanan santri berhasil disimpan."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }

    fun proposePromotion(
        santriId: String,
        currentGroupId: String,
        targetGroupId: String,
        academicYearId: String,
        proposalDate: String,
        justification: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = evalRepo.submitPromotionProposal(
                teacherId = session.userId,
                teacherName = session.name,
                santriId = santriId,
                currentGroupId = currentGroupId,
                targetGroupId = targetGroupId,
                academicYearId = academicYearId,
                proposalDate = proposalDate,
                justification = justification,
                evaluationRefId = ""
            )) {
                is Resource.Success -> {
                    _userMessage.value = "Usulan kenaikan kelompok berhasil diajukan ke Kepala Program."
                    onSuccess()
                }
                is Resource.Error -> _userMessage.value = res.message
                is Resource.Loading -> Unit
            }
        }
    }
}
