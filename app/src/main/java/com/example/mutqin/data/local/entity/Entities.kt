package com.example.mutqin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "teachers",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["nip"], unique = false)
    ]
)
data class TeacherEntity(
    @PrimaryKey val id: String,
    val nip: String,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val salt: String,
    val role: String, // "ADMIN" or "TEACHER"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "academic_years")
data class AcademicYearEntity(
    @PrimaryKey val id: String,
    val name: String, // e.g. "2026/2027"
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "groups",
    indices = [Index(value = ["code", "academicYearId"], unique = true)]
)
data class GroupEntity(
    @PrimaryKey val id: String,
    val code: String, // "D", "C", "B", "A"
    val name: String,
    val description: String,
    val focus: String,
    val targetMinVerses: Int = 5,
    val evaluationIndicators: String,
    val academicYearId: String,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "teacher_group_assignments",
    primaryKeys = ["teacherId", "groupId", "academicYearId"]
)
data class TeacherGroupCrossRef(
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val assignedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "santri",
    indices = [
        Index(value = ["nis"], unique = true),
        Index(value = ["currentGroupId"]),
        Index(value = ["status"])
    ]
)
data class SantriEntity(
    @PrimaryKey val id: String,
    val nis: String,
    val name: String,
    val gender: String, // "L" or "P"
    val schoolClass: String,
    val joinDate: String,
    val admissionYear: String,
    val academicYearId: String,
    val currentGroupId: String,
    val responsibleTeacherId: String = "",
    val status: String = "AKTIF", // "CALON", "AKTIF", "CUTI", "KELUAR", "LULUS"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_transfer_history",
    indices = [Index(value = ["santriId"])]
)
data class GroupTransferHistoryEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val fromGroupId: String,
    val toGroupId: String,
    val academicYearId: String,
    val transferDate: String,
    val reason: String,
    val changedByUserId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "iqro_records",
    indices = [Index(value = ["santriId"]), Index(value = ["groupId"])]
)
data class IqroRecordEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val date: String,
    val volume: Int, // 1 - 6
    val pageOrLesson: String,
    val fluencyLevel: String, // Sangat Lancar, Lancar, Cukup, Kurang
    val readingMistakes: String = "",
    val notes: String = "",
    val followUpAction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tahsin_records",
    indices = [Index(value = ["santriId"]), Index(value = ["groupId"])]
)
data class TahsinRecordEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val date: String,
    val surahName: String,
    val ayahStart: Int,
    val ayahEnd: Int,
    val fluencyScore: Int = 80, // 0 - 100
    val makhrajScore: Int = 80, // 0 - 100
    val tajwidScore: Int = 80,  // 0 - 100
    val mistakesDescription: String = "",
    val notes: String = "",
    val practiceRecommendations: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ziyadah_records",
    indices = [Index(value = ["santriId"]), Index(value = ["groupId"])]
)
data class ZiyadahRecordEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val date: String,
    val surahName: String,
    val ayahStart: Int,
    val ayahEnd: Int,
    val totalAyahs: Int,
    val fluencyScore: Int = 85,
    val tajwidScore: Int = 85,
    val makhrajScore: Int = 85,
    val mistakeCount: Int = 0,
    val status: String = "TERCAPAI", // TERCAPAI, BELUM_LANCAR, PERLU_MURAJAAH
    val isMastered: Boolean = true,
    val notes: String = "",
    val followUpAction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "murajaah_records",
    indices = [Index(value = ["santriId"]), Index(value = ["groupId"])]
)
data class MurajaahRecordEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val date: String,
    val surahName: String,
    val ayahStart: Int,
    val ayahEnd: Int,
    val fluencyLevel: String = "Lancar",
    val mistakeCount: Int = 0,
    val tajwidScore: Int = 85,
    val notes: String = "",
    val followUpAction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendances",
    indices = [
        Index(value = ["santriId", "date", "sessionType"], unique = true),
        Index(value = ["groupId", "date"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val date: String,
    val sessionType: String = "Sore",
    val status: String = "HADIR", // HADIR, IZIN, SAKIT, ALPA
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "teacher_journals",
    indices = [Index(value = ["teacherId", "date"])]
)
data class TeacherJournalEntity(
    @PrimaryKey val id: String,
    val teacherId: String,
    val groupId: String,
    val academicYearId: String,
    val date: String,
    val topic: String,
    val activities: String,
    val studentAttendanceCount: Int = 0,
    val obstacles: String = "",
    val learningResults: String = "",
    val followUpPlan: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "evaluations",
    indices = [
        Index(value = ["santriId", "periodMonthYear"], unique = true),
        Index(value = ["groupId"])
    ]
)
data class EvaluationEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val evaluatorTeacherId: String,
    val groupId: String,
    val academicYearId: String,
    val periodMonthYear: String, // e.g. "Oktober 2026"
    val iqroScore: Int = 0,
    val quranFluencyScore: Int = 0,
    val makhrajScore: Int = 0,
    val tajwidScore: Int = 0,
    val ziyadahScore: Int = 0,
    val murajaahScore: Int = 0,
    val attendanceScore: Int = 0,
    val overallNotes: String = "",
    val teacherRecommendations: String = "",
    val approvalStatus: String = "DISETUJUI", // DRAFT, DIAJUKAN, DISETUJUI, DITOLAK
    val approvedByAdminId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "promotion_proposals",
    indices = [Index(value = ["santriId", "status"])]
)
data class PromotionProposalEntity(
    @PrimaryKey val id: String,
    val santriId: String,
    val proposingTeacherId: String,
    val currentGroupId: String,
    val targetGroupId: String,
    val academicYearId: String,
    val proposalDate: String,
    val justification: String,
    val evaluationRefId: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, POSTPONED
    val decisionDate: String = "",
    val decisionByAdminId: String = "",
    val decisionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val actionType: String, // CREATE, UPDATE, DELETE, TRANSFER, APPROVE, REJECT, BACKUP, RESTORE
    val entityType: String, // SANTRI, TEACHER, GROUP, RECORD, EVALUATION
    val entityId: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "program_configs")
data class ProgramConfigEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
