package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
import com.example.mutqin.core.security.PasswordSecurity
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.TeacherEntity
import com.example.mutqin.data.local.entity.TeacherGroupCrossRef
import kotlinx.coroutines.flow.Flow

class TeacherRepository(private val dao: MutqinDao) {

    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers()
    val activeTeachers: Flow<List<TeacherEntity>> = dao.getActiveTeachers()

    fun getAssignedGroupIds(teacherId: String): Flow<List<String>> =
        dao.getAssignedGroupIdsForTeacher(teacherId)

    suspend fun getAssignedGroupIdsDirect(teacherId: String): List<String> =
        dao.getAssignedGroupIdsForTeacherDirect(teacherId)

    suspend fun createTeacher(
        adminId: String,
        adminName: String,
        nip: String,
        name: String,
        email: String,
        phone: String,
        initialPassword: String,
        assignedGroupIds: List<String>,
        academicYearId: String
    ): Resource<TeacherEntity> {
        val trimmedEmail = email.trim().lowercase()
        val existing = dao.getTeacherByEmail(trimmedEmail)
        if (existing != null) {
            return Resource.Error("Email sudah terdaftar untuk pengajar lain.")
        }
        if (name.isBlank()) return Resource.Error("Nama pengajar wajib diisi.")
        if (initialPassword.length < 6) return Resource.Error("Kata sandi awal minimal 6 karakter.")

        val now = System.currentTimeMillis()
        val salt = PasswordSecurity.generateSalt()
        val hash = PasswordSecurity.hashPassword(initialPassword, salt)
        val teacherId = "tchr_${now}_${nip.filter { it.isLetterOrDigit() }.takeLast(4)}"

        val newTeacher = TeacherEntity(
            id = teacherId,
            nip = nip.ifBlank { "UST-${now % 10000}" },
            name = name.trim(),
            email = trimmedEmail,
            phone = phone.trim(),
            passwordHash = hash,
            salt = salt,
            role = "TEACHER",
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        dao.insertTeacher(newTeacher)

        // Assign to groups
        for (gid in assignedGroupIds) {
            dao.assignTeacherToGroup(
                TeacherGroupCrossRef(
                    teacherId = teacherId,
                    groupId = gid,
                    academicYearId = academicYearId,
                    assignedAt = now
                )
            )
        }

        // Audit log
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_tchr_create_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "CREATE",
                entityType = "TEACHER",
                entityId = teacherId,
                description = "Admin $adminName menambahkan pengajar baru: ${newTeacher.name} ($trimmedEmail) dengan ${assignedGroupIds.size} penugasan kelompok."
            )
        )

        return Resource.Success(newTeacher)
    }

    suspend fun updateTeacherProfile(
        adminId: String,
        adminName: String,
        teacherId: String,
        name: String,
        nip: String,
        phone: String,
        assignedGroupIds: List<String>,
        academicYearId: String
    ): Resource<Unit> {
        val current = dao.getTeacherById(teacherId)
            ?: return Resource.Error("Data pengajar tidak ditemukan.")

        val now = System.currentTimeMillis()
        val updated = current.copy(
            name = name.trim(),
            nip = nip.trim(),
            phone = phone.trim(),
            updatedAt = now
        )
        dao.updateTeacher(updated)

        // Update group assignments: clear and reassign
        dao.clearTeacherAssignments(teacherId)
        for (gid in assignedGroupIds) {
            dao.assignTeacherToGroup(
                TeacherGroupCrossRef(
                    teacherId = teacherId,
                    groupId = gid,
                    academicYearId = academicYearId,
                    assignedAt = now
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_tchr_update_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "UPDATE",
                entityType = "TEACHER",
                entityId = teacherId,
                description = "Perubahan profil/penugasan pengajar ${updated.name} oleh $adminName"
            )
        )
        return Resource.Success(Unit)
    }

    suspend fun toggleTeacherActiveStatus(
        adminId: String,
        adminName: String,
        teacherId: String,
        newStatus: Boolean
    ): Resource<Unit> {
        val current = dao.getTeacherById(teacherId)
            ?: return Resource.Error("Data pengajar tidak ditemukan.")

        val now = System.currentTimeMillis()
        dao.updateTeacher(current.copy(isActive = newStatus, updatedAt = now))

        val actionDesc = if (newStatus) "mengaktifkan kembali" else "menonaktifkan"
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_tchr_status_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "UPDATE_STATUS",
                entityType = "TEACHER",
                entityId = teacherId,
                description = "Admin $adminName $actionDesc akun pengajar ${current.name} (ID tetap dipertahankan, riwayat aman)"
            )
        )
        return Resource.Success(Unit)
    }
}
