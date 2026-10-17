package com.example.mutqin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface MutqinDao {

    // --- TEACHERS ---
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE email = :email LIMIT 1")
    suspend fun getTeacherByEmail(email: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE role = 'TEACHER' AND isActive = 1")
    fun getActiveTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    // --- ACADEMIC YEARS ---
    @Query("SELECT * FROM academic_years ORDER BY startDate DESC")
    fun getAllAcademicYears(): Flow<List<AcademicYearEntity>>

    @Query("SELECT * FROM academic_years WHERE isActive = 1 LIMIT 1")
    fun getActiveAcademicYearFlow(): Flow<AcademicYearEntity?>

    @Query("SELECT * FROM academic_years WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAcademicYear(): AcademicYearEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(year: AcademicYearEntity)

    @Update
    suspend fun updateAcademicYear(year: AcademicYearEntity)

    @Query("UPDATE academic_years SET isActive = 0")
    suspend fun deactivateAllAcademicYears()

    @Transaction
    suspend fun setActiveAcademicYear(yearId: String) {
        deactivateAllAcademicYears()
        setYearActive(yearId)
    }

    @Query("UPDATE academic_years SET isActive = 1 WHERE id = :yearId")
    suspend fun setYearActive(yearId: String)

    // --- GROUPS ---
    @Query("SELECT * FROM groups WHERE isActive = 1 ORDER BY sortOrder ASC, code ASC")
    fun getActiveGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups ORDER BY sortOrder ASC, code ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    // --- TEACHER GROUP ASSIGNMENTS ---
    @Query("SELECT groupId FROM teacher_group_assignments WHERE teacherId = :teacherId")
    fun getAssignedGroupIdsForTeacher(teacherId: String): Flow<List<String>>

    @Query("SELECT groupId FROM teacher_group_assignments WHERE teacherId = :teacherId")
    suspend fun getAssignedGroupIdsForTeacherDirect(teacherId: String): List<String>

    @Query("SELECT * FROM groups WHERE id IN (SELECT groupId FROM teacher_group_assignments WHERE teacherId = :teacherId) AND isActive = 1")
    fun getGroupsForTeacher(teacherId: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM teachers WHERE id IN (SELECT teacherId FROM teacher_group_assignments WHERE groupId = :groupId)")
    fun getTeachersForGroup(groupId: String): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun assignTeacherToGroup(crossRef: TeacherGroupCrossRef)

    @Query("DELETE FROM teacher_group_assignments WHERE teacherId = :teacherId AND groupId = :groupId")
    suspend fun unassignTeacherFromGroup(teacherId: String, groupId: String)

    @Query("DELETE FROM teacher_group_assignments WHERE teacherId = :teacherId")
    suspend fun clearTeacherAssignments(teacherId: String)

    // --- SANTRI ---
    @Query("SELECT * FROM santri ORDER BY name ASC")
    fun getAllSantri(): Flow<List<SantriEntity>>

    @Query("SELECT * FROM santri WHERE currentGroupId = :groupId ORDER BY name ASC")
    fun getSantriByGroup(groupId: String): Flow<List<SantriEntity>>

    @Query("SELECT * FROM santri WHERE currentGroupId IN (:groupIds) AND status = 'AKTIF' ORDER BY name ASC")
    fun getActiveSantriInGroups(groupIds: List<String>): Flow<List<SantriEntity>>

    @Query("SELECT * FROM santri WHERE id = :id")
    suspend fun getSantriById(id: String): SantriEntity?

    @Query("SELECT * FROM santri WHERE id = :id")
    fun getSantriByIdFlow(id: String): Flow<SantriEntity?>

    @Query("SELECT * FROM santri WHERE nis = :nis LIMIT 1")
    suspend fun getSantriByNis(nis: String): SantriEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSantri(santri: SantriEntity)

    @Update
    suspend fun updateSantri(santri: SantriEntity)

    // --- GROUP TRANSFERS ---
    @Query("SELECT * FROM group_transfer_history WHERE santriId = :santriId ORDER BY createdAt DESC")
    fun getTransferHistory(santriId: String): Flow<List<GroupTransferHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransferHistory(history: GroupTransferHistoryEntity)

    @Transaction
    suspend fun transferSantriGroup(
        santriId: String,
        targetGroupId: String,
        reason: String,
        adminUserId: String,
        academicYearId: String,
        date: String
    ) {
        val santri = getSantriById(santriId) ?: return
        val history = GroupTransferHistoryEntity(
            id = "tf_${System.currentTimeMillis()}_${santriId.take(6)}",
            santriId = santriId,
            fromGroupId = santri.currentGroupId,
            toGroupId = targetGroupId,
            academicYearId = academicYearId,
            transferDate = date,
            reason = reason,
            changedByUserId = adminUserId
        )
        insertTransferHistory(history)
        updateSantri(santri.copy(currentGroupId = targetGroupId, updatedAt = System.currentTimeMillis()))
    }

    // --- LEARNING RECORDS: IQRO ---
    @Query("SELECT * FROM iqro_records WHERE santriId = :santriId ORDER BY date DESC, createdAt DESC")
    fun getIqroRecordsForSantri(santriId: String): Flow<List<IqroRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIqroRecord(record: IqroRecordEntity)

    // --- LEARNING RECORDS: TAHSIN ---
    @Query("SELECT * FROM tahsin_records WHERE santriId = :santriId ORDER BY date DESC, createdAt DESC")
    fun getTahsinRecordsForSantri(santriId: String): Flow<List<TahsinRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTahsinRecord(record: TahsinRecordEntity)

    // --- LEARNING RECORDS: ZIYADAH ---
    @Query("SELECT * FROM ziyadah_records WHERE santriId = :santriId ORDER BY date DESC, createdAt DESC")
    fun getZiyadahRecordsForSantri(santriId: String): Flow<List<ZiyadahRecordEntity>>

    @Query("SELECT * FROM ziyadah_records ORDER BY date DESC LIMIT :limit")
    fun getRecentZiyadahRecords(limit: Int = 10): Flow<List<ZiyadahRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZiyadahRecord(record: ZiyadahRecordEntity)

    // --- LEARNING RECORDS: MURAJAAH ---
    @Query("SELECT * FROM murajaah_records WHERE santriId = :santriId ORDER BY date DESC, createdAt DESC")
    fun getMurajaahRecordsForSantri(santriId: String): Flow<List<MurajaahRecordEntity>>

    @Query("SELECT * FROM murajaah_records ORDER BY date DESC LIMIT :limit")
    fun getRecentMurajaahRecords(limit: Int = 10): Flow<List<MurajaahRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMurajaahRecord(record: MurajaahRecordEntity)

    // --- ATTENDANCE ---
    @Query("SELECT * FROM attendances WHERE santriId = :santriId ORDER BY date DESC")
    fun getAttendanceForSantri(santriId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE groupId = :groupId AND date = :date")
    fun getAttendanceForGroupAndDate(groupId: String, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE santriId = :santriId AND date = :date AND sessionType = :sessionType LIMIT 1")
    suspend fun getExistingAttendance(santriId: String, date: String, sessionType: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceBatch(attendances: List<AttendanceEntity>)

    // --- TEACHER JOURNAL ---
    @Query("SELECT * FROM teacher_journals WHERE teacherId = :teacherId ORDER BY date DESC")
    fun getJournalsForTeacher(teacherId: String): Flow<List<TeacherJournalEntity>>

    @Query("SELECT * FROM teacher_journals ORDER BY date DESC LIMIT 20")
    fun getAllRecentJournals(): Flow<List<TeacherJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: TeacherJournalEntity)

    // --- EVALUATIONS ---
    @Query("SELECT * FROM evaluations WHERE santriId = :santriId ORDER BY periodMonthYear DESC")
    fun getEvaluationsForSantri(santriId: String): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluations ORDER BY createdAt DESC")
    fun getAllEvaluations(): Flow<List<EvaluationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: EvaluationEntity)

    // --- PROMOTION PROPOSALS ---
    @Query("SELECT * FROM promotion_proposals ORDER BY createdAt DESC")
    fun getAllPromotionProposals(): Flow<List<PromotionProposalEntity>>

    @Query("SELECT * FROM promotion_proposals WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingPromotionProposals(): Flow<List<PromotionProposalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotionProposal(proposal: PromotionProposalEntity)

    @Update
    suspend fun updatePromotionProposal(proposal: PromotionProposalEntity)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- PROGRAM CONFIGS ---
    @Query("SELECT value FROM program_configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: ProgramConfigEntity)

    // --- BACKUP & RESTORE DUMP QUERIES ---
    @Query("SELECT * FROM teachers")
    suspend fun dumpTeachers(): List<TeacherEntity>

    @Query("SELECT * FROM academic_years")
    suspend fun dumpAcademicYears(): List<AcademicYearEntity>

    @Query("SELECT * FROM groups")
    suspend fun dumpGroups(): List<GroupEntity>

    @Query("SELECT * FROM teacher_group_assignments")
    suspend fun dumpAssignments(): List<TeacherGroupCrossRef>

    @Query("SELECT * FROM santri")
    suspend fun dumpSantri(): List<SantriEntity>

    @Query("SELECT * FROM group_transfer_history")
    suspend fun dumpTransfers(): List<GroupTransferHistoryEntity>

    @Query("SELECT * FROM iqro_records")
    suspend fun dumpIqro(): List<IqroRecordEntity>

    @Query("SELECT * FROM tahsin_records")
    suspend fun dumpTahsin(): List<TahsinRecordEntity>

    @Query("SELECT * FROM ziyadah_records")
    suspend fun dumpZiyadah(): List<ZiyadahRecordEntity>

    @Query("SELECT * FROM murajaah_records")
    suspend fun dumpMurajaah(): List<MurajaahRecordEntity>

    @Query("SELECT * FROM attendances")
    suspend fun dumpAttendances(): List<AttendanceEntity>

    @Query("SELECT * FROM teacher_journals")
    suspend fun dumpJournals(): List<TeacherJournalEntity>

    @Query("SELECT * FROM evaluations")
    suspend fun dumpEvaluations(): List<EvaluationEntity>

    @Query("SELECT * FROM promotion_proposals")
    suspend fun dumpProposals(): List<PromotionProposalEntity>

    @Query("SELECT * FROM audit_logs")
    suspend fun dumpAuditLogs(): List<AuditLogEntity>

    @Query("SELECT * FROM program_configs")
    suspend fun dumpConfigs(): List<ProgramConfigEntity>

    // Restore inserts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreTeachers(items: List<TeacherEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAcademicYears(items: List<AcademicYearEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreGroups(items: List<GroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAssignments(items: List<TeacherGroupCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreSantri(items: List<SantriEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreTransfers(items: List<GroupTransferHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreIqro(items: List<IqroRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreTahsin(items: List<TahsinRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreZiyadah(items: List<ZiyadahRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreMurajaah(items: List<MurajaahRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAttendances(items: List<AttendanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreJournals(items: List<TeacherJournalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreEvaluations(items: List<EvaluationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreProposals(items: List<PromotionProposalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAuditLogs(items: List<AuditLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreConfigs(items: List<ProgramConfigEntity>)
}
