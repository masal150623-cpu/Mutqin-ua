package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AcademicYearEntity
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val dao: MutqinDao) {

    val activeGroups: Flow<List<GroupEntity>> = dao.getActiveGroups()
    val allGroups: Flow<List<GroupEntity>> = dao.getAllGroups()
    val allAcademicYears: Flow<List<AcademicYearEntity>> = dao.getAllAcademicYears()
    val activeAcademicYear: Flow<AcademicYearEntity?> = dao.getActiveAcademicYearFlow()

    fun getGroupsForTeacher(teacherId: String): Flow<List<GroupEntity>> =
        dao.getGroupsForTeacher(teacherId)

    suspend fun createGroup(
        adminId: String,
        adminName: String,
        code: String,
        name: String,
        description: String,
        focus: String,
        targetMinVerses: Int,
        evaluationIndicators: String,
        academicYearId: String
    ): Resource<GroupEntity> {
        if (code.isBlank() || name.isBlank()) {
            return Resource.Error("Kode dan nama kelompok wajib diisi.")
        }
        val now = System.currentTimeMillis()
        val groupId = "grp_${code.trim().lowercase()}_${now % 10000}"
        val group = GroupEntity(
            id = groupId,
            code = code.trim().uppercase(),
            name = name.trim(),
            description = description.trim(),
            focus = focus.trim(),
            targetMinVerses = targetMinVerses.coerceAtLeast(0),
            evaluationIndicators = evaluationIndicators.trim(),
            academicYearId = academicYearId,
            isActive = true,
            sortOrder = 10,
            createdAt = now
        )
        dao.insertGroup(group)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_grp_create_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "CREATE",
                entityType = "GROUP",
                entityId = groupId,
                description = "Penambahan kelompok baru: ${group.name} (${group.code})"
            )
        )
        return Resource.Success(group)
    }

    suspend fun updateGroup(
        adminId: String,
        adminName: String,
        groupId: String,
        name: String,
        description: String,
        focus: String,
        targetMinVerses: Int,
        evaluationIndicators: String
    ): Resource<Unit> {
        val current = dao.getGroupById(groupId) ?: return Resource.Error("Kelompok tidak ditemukan.")
        val updated = current.copy(
            name = name.trim(),
            description = description.trim(),
            focus = focus.trim(),
            targetMinVerses = targetMinVerses.coerceAtLeast(0),
            evaluationIndicators = evaluationIndicators.trim()
        )
        dao.updateGroup(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_grp_update_${System.currentTimeMillis()}",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "UPDATE",
                entityType = "GROUP",
                entityId = groupId,
                description = "Pembaruan data kelompok ${updated.name}"
            )
        )
        return Resource.Success(Unit)
    }

    suspend fun toggleGroupActiveStatus(
        adminId: String,
        adminName: String,
        groupId: String,
        isActive: Boolean
    ): Resource<Unit> {
        val current = dao.getGroupById(groupId) ?: return Resource.Error("Kelompok tidak ditemukan.")
        dao.updateGroup(current.copy(isActive = isActive))

        val actionDesc = if (isActive) "mengaktifkan" else "menonaktifkan (arsip histori tetap aman)"
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_grp_status_${System.currentTimeMillis()}",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "UPDATE_STATUS",
                entityType = "GROUP",
                entityId = groupId,
                description = "Admin $adminName $actionDesc kelompok ${current.name}"
            )
        )
        return Resource.Success(Unit)
    }

    suspend fun createAcademicYear(
        adminId: String,
        adminName: String,
        name: String,
        startDate: String,
        endDate: String,
        setAsActive: Boolean
    ): Resource<AcademicYearEntity> {
        if (name.isBlank()) return Resource.Error("Nama tahun ajaran wajib diisi.")
        val now = System.currentTimeMillis()
        val yearId = "ay_${name.filter { it.isLetterOrDigit() }.lowercase()}_${now % 1000}"
        val year = AcademicYearEntity(
            id = yearId,
            name = name.trim(),
            startDate = startDate,
            endDate = endDate,
            isActive = false,
            createdAt = now
        )
        dao.insertAcademicYear(year)
        if (setAsActive) {
            dao.setActiveAcademicYear(yearId)
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_ay_create_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "CREATE",
                entityType = "ACADEMIC_YEAR",
                entityId = yearId,
                description = "Membuat tahun ajaran baru: ${year.name} (Aktif: $setAsActive)"
            )
        )
        return Resource.Success(year)
    }

    suspend fun activateAcademicYear(
        adminId: String,
        adminName: String,
        yearId: String
    ): Resource<Unit> {
        dao.setActiveAcademicYear(yearId)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_ay_act_${System.currentTimeMillis()}",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "ACTIVATE",
                entityType = "ACADEMIC_YEAR",
                entityId = yearId,
                description = "Mengaktifkan tahun ajaran $yearId sebagai periode aktif tunggal."
            )
        )
        return Resource.Success(Unit)
    }
}
