package com.example.mutqin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mutqin.core.model.ProgramConstants
import com.example.mutqin.core.security.PasswordSecurity
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AcademicYearEntity
import com.example.mutqin.data.local.entity.AttendanceEntity
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.EvaluationEntity
import com.example.mutqin.data.local.entity.GroupEntity
import com.example.mutqin.data.local.entity.GroupTransferHistoryEntity
import com.example.mutqin.data.local.entity.IqroRecordEntity
import com.example.mutqin.data.local.entity.MurajaahRecordEntity
import com.example.mutqin.data.local.entity.ProgramConfigEntity
import com.example.mutqin.data.local.entity.PromotionProposalEntity
import com.example.mutqin.data.local.entity.SantriEntity
import com.example.mutqin.data.local.entity.TahsinRecordEntity
import com.example.mutqin.data.local.entity.TeacherEntity
import com.example.mutqin.data.local.entity.TeacherGroupCrossRef
import com.example.mutqin.data.local.entity.TeacherJournalEntity
import com.example.mutqin.data.local.entity.ZiyadahRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TeacherEntity::class,
        AcademicYearEntity::class,
        GroupEntity::class,
        TeacherGroupCrossRef::class,
        SantriEntity::class,
        GroupTransferHistoryEntity::class,
        IqroRecordEntity::class,
        TahsinRecordEntity::class,
        ZiyadahRecordEntity::class,
        MurajaahRecordEntity::class,
        AttendanceEntity::class,
        TeacherJournalEntity::class,
        EvaluationEntity::class,
        PromotionProposalEntity::class,
        AuditLogEntity::class,
        ProgramConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mutqinDao(): MutqinDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mutqin_ulul_albab.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database.mutqinDao())
                }
            }
        }

        private suspend fun seedInitialData(dao: MutqinDao) {
            val now = System.currentTimeMillis()

            // 1. Tahun Ajaran 2026/2027
            val defaultYear = AcademicYearEntity(
                id = ProgramConstants.DEFAULT_ACADEMIC_YEAR_ID,
                name = ProgramConstants.DEFAULT_ACADEMIC_YEAR_NAME,
                startDate = "2026-07-01",
                endDate = "2027-06-30",
                isActive = true,
                createdAt = now
            )
            dao.insertAcademicYear(defaultYear)

            // 2. Default 4 Kelompok (D, C, B, A)
            val groupD = GroupEntity(
                id = ProgramConstants.GROUP_D_ID,
                code = ProgramConstants.GROUP_D_CODE,
                name = ProgramConstants.GROUP_D_NAME,
                description = "Tahap pondasi pengenalan huruf hijaiyah, makharijul huruf dasar, dan kelancaran membaca Iqro'.",
                focus = ProgramConstants.GROUP_D_FOCUS,
                targetMinVerses = 0,
                evaluationIndicators = "Pengenalan huruf, kelancaran bacaan, ketepatan makhraj dasar",
                academicYearId = defaultYear.id,
                isActive = true,
                sortOrder = 1,
                createdAt = now
            )
            val groupC = GroupEntity(
                id = ProgramConstants.GROUP_C_ID,
                code = ProgramConstants.GROUP_C_CODE,
                name = ProgramConstants.GROUP_C_NAME,
                description = "Tahap perbaikan bacaan Al-Qur'an (Tahsin), tajwid dasar, panjang-pendek (mad), dan ghunnah.",
                focus = ProgramConstants.GROUP_C_FOCUS,
                targetMinVerses = 0,
                evaluationIndicators = "Tartil bacaan, hukum nun sukun/tanwin, hukum mad, waqaf & ibtida",
                academicYearId = defaultYear.id,
                isActive = true,
                sortOrder = 2,
                createdAt = now
            )
            val groupB = GroupEntity(
                id = ProgramConstants.GROUP_B_ID,
                code = ProgramConstants.GROUP_B_CODE,
                name = ProgramConstants.GROUP_B_NAME,
                description = "Tahap persiapan dan setoran hafalan baru (Ziyadah). Target awal minimal 5 ayat lancar per sesi.",
                focus = ProgramConstants.GROUP_B_FOCUS,
                targetMinVerses = 5,
                evaluationIndicators = "Kelancaran hafalan baru, ketepatan tajwid, makhraj, dan pemenuhan target minimal 5 ayat",
                academicYearId = defaultYear.id,
                isActive = true,
                sortOrder = 3,
                createdAt = now
            )
            val groupA = GroupEntity(
                id = ProgramConstants.GROUP_A_ID,
                code = ProgramConstants.GROUP_A_CODE,
                name = ProgramConstants.GROUP_A_NAME,
                description = "Tahap murajaah intensif dan pemeliharaan hafalan yang sudah mutqin agar tidak mudah hilang.",
                focus = ProgramConstants.GROUP_A_FOCUS,
                targetMinVerses = 20,
                evaluationIndicators = "Kekuatan hafalan mutqin, ketahanan sima'an, ketepatan tajwid tanpa terbata-bata",
                academicYearId = defaultYear.id,
                isActive = true,
                sortOrder = 4,
                createdAt = now
            )
            dao.insertGroup(groupD)
            dao.insertGroup(groupC)
            dao.insertGroup(groupB)
            dao.insertGroup(groupA)

            // 3. Admin Account
            val adminSalt = PasswordSecurity.generateSalt()
            val adminHash = PasswordSecurity.hashPassword("admin123", adminSalt)
            val admin = TeacherEntity(
                id = "usr_admin_01",
                nip = "ADM-2026-001",
                name = "Ustaz H. Abdullah, M.Pd.I",
                email = "admin@ululalbab.id",
                phone = "081234567890",
                passwordHash = adminHash,
                salt = adminSalt,
                role = "ADMIN",
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            dao.insertTeacher(admin)

            // 4. Contoh Pengajar Pertama
            val teacherSalt = PasswordSecurity.generateSalt()
            val teacherHash = PasswordSecurity.hashPassword("ustaz123", teacherSalt)
            val teacher1 = TeacherEntity(
                id = "usr_tchr_01",
                nip = "UST-2026-001",
                name = "Ustaz Ahmad Farhan, S.Pd",
                email = "ustaz.ahmad@ululalbab.id",
                phone = "081298765432",
                passwordHash = teacherHash,
                salt = teacherSalt,
                role = "TEACHER",
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            dao.insertTeacher(teacher1)

            // Tugaskan teacher1 ke Kelompok D dan C
            dao.assignTeacherToGroup(
                TeacherGroupCrossRef(
                    teacherId = teacher1.id,
                    groupId = groupD.id,
                    academicYearId = defaultYear.id,
                    assignedAt = now
                )
            )
            dao.assignTeacherToGroup(
                TeacherGroupCrossRef(
                    teacherId = teacher1.id,
                    groupId = groupC.id,
                    academicYearId = defaultYear.id,
                    assignedAt = now
                )
            )

            // 5. Audit Log Inisialisasi
            val auditInit = AuditLogEntity(
                id = "log_init_${now}",
                userId = admin.id,
                userName = admin.name,
                userRole = admin.role,
                actionType = "CREATE",
                entityType = "SYSTEM",
                entityId = "MUTQIN_INIT",
                description = "Inisialisasi sistem Mutqin Ulul Albab, tahun ajaran 2026/2027, dan 4 kelompok dasar (D, C, B, A).",
                timestamp = now
            )
            dao.insertAuditLog(auditInit)
        }
    }
}
