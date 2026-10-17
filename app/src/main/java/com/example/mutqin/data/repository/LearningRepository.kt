package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AttendanceEntity
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.IqroRecordEntity
import com.example.mutqin.data.local.entity.MurajaahRecordEntity
import com.example.mutqin.data.local.entity.TahsinRecordEntity
import com.example.mutqin.data.local.entity.TeacherJournalEntity
import com.example.mutqin.data.local.entity.ZiyadahRecordEntity
import kotlinx.coroutines.flow.Flow

class LearningRepository(private val dao: MutqinDao) {

    // --- IQRO ---
    fun getIqroRecords(santriId: String): Flow<List<IqroRecordEntity>> =
        dao.getIqroRecordsForSantri(santriId)

    suspend fun recordIqro(
        santriId: String,
        teacherId: String,
        teacherName: String,
        groupId: String,
        academicYearId: String,
        date: String,
        volume: Int,
        pageOrLesson: String,
        fluencyLevel: String,
        readingMistakes: String,
        notes: String,
        followUpAction: String
    ): Resource<IqroRecordEntity> {
        val now = System.currentTimeMillis()
        val record = IqroRecordEntity(
            id = "iqr_${now}_${santriId.takeLast(4)}",
            santriId = santriId,
            teacherId = teacherId,
            groupId = groupId,
            academicYearId = academicYearId,
            date = date,
            volume = volume,
            pageOrLesson = pageOrLesson.trim(),
            fluencyLevel = fluencyLevel,
            readingMistakes = readingMistakes.trim(),
            notes = notes.trim(),
            followUpAction = followUpAction.trim(),
            createdAt = now
        )
        dao.insertIqroRecord(record)
        return Resource.Success(record)
    }

    // --- TAHSIN ---
    fun getTahsinRecords(santriId: String): Flow<List<TahsinRecordEntity>> =
        dao.getTahsinRecordsForSantri(santriId)

    suspend fun recordTahsin(
        santriId: String,
        teacherId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        surahName: String,
        ayahStart: Int,
        ayahEnd: Int,
        fluencyScore: Int,
        makhrajScore: Int,
        tajwidScore: Int,
        mistakesDescription: String,
        notes: String,
        practiceRecommendations: String
    ): Resource<TahsinRecordEntity> {
        if (ayahEnd < ayahStart) {
            return Resource.Error("Ayat akhir tidak boleh lebih kecil dari ayat awal.")
        }
        val now = System.currentTimeMillis()
        val record = TahsinRecordEntity(
            id = "ths_${now}_${santriId.takeLast(4)}",
            santriId = santriId,
            teacherId = teacherId,
            groupId = groupId,
            academicYearId = academicYearId,
            date = date,
            surahName = surahName.trim(),
            ayahStart = ayahStart,
            ayahEnd = ayahEnd,
            fluencyScore = fluencyScore.coerceIn(0, 100),
            makhrajScore = makhrajScore.coerceIn(0, 100),
            tajwidScore = tajwidScore.coerceIn(0, 100),
            mistakesDescription = mistakesDescription.trim(),
            notes = notes.trim(),
            practiceRecommendations = practiceRecommendations.trim(),
            createdAt = now
        )
        dao.insertTahsinRecord(record)
        return Resource.Success(record)
    }

    // --- ZIYADAH ---
    fun getZiyadahRecords(santriId: String): Flow<List<ZiyadahRecordEntity>> =
        dao.getZiyadahRecordsForSantri(santriId)

    val recentZiyadahRecords: Flow<List<ZiyadahRecordEntity>> =
        dao.getRecentZiyadahRecords(15)

    suspend fun recordZiyadah(
        santriId: String,
        teacherId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        surahName: String,
        ayahStart: Int,
        ayahEnd: Int,
        totalAyahsInput: Int?,
        fluencyScore: Int,
        tajwidScore: Int,
        makhrajScore: Int,
        mistakeCount: Int,
        status: String,
        isMastered: Boolean,
        notes: String,
        followUpAction: String
    ): Resource<ZiyadahRecordEntity> {
        if (ayahEnd < ayahStart) {
            return Resource.Error("Rentang ayat tidak valid (ayat akhir < ayat awal).")
        }
        val calculatedAyahs = (ayahEnd - ayahStart + 1).coerceAtLeast(1)
        val finalTotalAyahs = totalAyahsInput ?: calculatedAyahs

        val now = System.currentTimeMillis()
        val record = ZiyadahRecordEntity(
            id = "zyd_${now}_${santriId.takeLast(4)}",
            santriId = santriId,
            teacherId = teacherId,
            groupId = groupId,
            academicYearId = academicYearId,
            date = date,
            surahName = surahName.trim(),
            ayahStart = ayahStart,
            ayahEnd = ayahEnd,
            totalAyahs = finalTotalAyahs,
            fluencyScore = fluencyScore.coerceIn(0, 100),
            tajwidScore = tajwidScore.coerceIn(0, 100),
            makhrajScore = makhrajScore.coerceIn(0, 100),
            mistakeCount = mistakeCount.coerceAtLeast(0),
            status = status,
            isMastered = isMastered,
            notes = notes.trim(),
            followUpAction = followUpAction.trim(),
            createdAt = now
        )
        dao.insertZiyadahRecord(record)
        return Resource.Success(record)
    }

    // --- MURAJAAH ---
    fun getMurajaahRecords(santriId: String): Flow<List<MurajaahRecordEntity>> =
        dao.getMurajaahRecordsForSantri(santriId)

    val recentMurajaahRecords: Flow<List<MurajaahRecordEntity>> =
        dao.getRecentMurajaahRecords(15)

    suspend fun recordMurajaah(
        santriId: String,
        teacherId: String,
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
        followUpAction: String
    ): Resource<MurajaahRecordEntity> {
        val now = System.currentTimeMillis()
        val record = MurajaahRecordEntity(
            id = "mrj_${now}_${santriId.takeLast(4)}",
            santriId = santriId,
            teacherId = teacherId,
            groupId = groupId,
            academicYearId = academicYearId,
            date = date,
            surahName = surahName.trim(),
            ayahStart = ayahStart,
            ayahEnd = ayahEnd,
            fluencyLevel = fluencyLevel,
            mistakeCount = mistakeCount.coerceAtLeast(0),
            tajwidScore = tajwidScore.coerceIn(0, 100),
            notes = notes.trim(),
            followUpAction = followUpAction.trim(),
            createdAt = now
        )
        dao.insertMurajaahRecord(record)
        return Resource.Success(record)
    }

    // --- ATTENDANCE ---
    fun getAttendanceForSantri(santriId: String): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForSantri(santriId)

    fun getAttendanceForGroupAndDate(groupId: String, date: String): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForGroupAndDate(groupId, date)

    suspend fun recordAttendance(
        santriId: String,
        teacherId: String,
        groupId: String,
        academicYearId: String,
        date: String,
        sessionType: String,
        status: String,
        notes: String
    ): Resource<AttendanceEntity> {
        val existing = dao.getExistingAttendance(santriId, date, sessionType)
        val now = System.currentTimeMillis()

        val record = if (existing != null) {
            existing.copy(status = status, notes = notes.trim(), teacherId = teacherId)
        } else {
            AttendanceEntity(
                id = "att_${now}_${santriId.takeLast(4)}",
                santriId = santriId,
                teacherId = teacherId,
                groupId = groupId,
                academicYearId = academicYearId,
                date = date,
                sessionType = sessionType,
                status = status,
                notes = notes.trim(),
                createdAt = now
            )
        }
        dao.insertAttendance(record)
        return Resource.Success(record)
    }

    // --- TEACHER JOURNAL ---
    fun getJournalsForTeacher(teacherId: String): Flow<List<TeacherJournalEntity>> =
        dao.getJournalsForTeacher(teacherId)

    val allRecentJournals: Flow<List<TeacherJournalEntity>> =
        dao.getAllRecentJournals()

    suspend fun recordJournal(
        teacherId: String,
        teacherName: String,
        groupId: String,
        academicYearId: String,
        date: String,
        topic: String,
        activities: String,
        studentAttendanceCount: Int,
        obstacles: String,
        learningResults: String,
        followUpPlan: String
    ): Resource<TeacherJournalEntity> {
        if (topic.isBlank() || activities.isBlank()) {
            return Resource.Error("Topik dan kegiatan jurnal pengajar wajib diisi.")
        }
        val now = System.currentTimeMillis()
        val journal = TeacherJournalEntity(
            id = "jrn_${now}_${teacherId.takeLast(4)}",
            teacherId = teacherId,
            groupId = groupId,
            academicYearId = academicYearId,
            date = date,
            topic = topic.trim(),
            activities = activities.trim(),
            studentAttendanceCount = studentAttendanceCount.coerceAtLeast(0),
            obstacles = obstacles.trim(),
            learningResults = learningResults.trim(),
            followUpPlan = followUpPlan.trim(),
            createdAt = now
        )
        dao.insertJournal(journal)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_jrn_$now",
                userId = teacherId,
                userName = teacherName,
                userRole = "TEACHER",
                actionType = "CREATE",
                entityType = "JOURNAL",
                entityId = journal.id,
                description = "Pengajar $teacherName mencatat jurnal harian: $topic ($date)"
            )
        )
        return Resource.Success(journal)
    }
}
