package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
import com.example.mutqin.core.model.UserRole
import com.example.mutqin.core.model.UserSession
import com.example.mutqin.core.security.PasswordSecurity
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val dao: MutqinDao) {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    suspend fun login(email: String, password: String): Resource<UserSession> {
        val trimmedEmail = email.trim().lowercase()
        val teacher = dao.getTeacherByEmail(trimmedEmail)
            ?: return Resource.Error("Akun dengan email ini tidak ditemukan.")

        if (!teacher.isActive) {
            return Resource.Error("Akun ini telah dinonaktifkan oleh Admin. Hubungi Kepala Program.")
        }

        val isValid = PasswordSecurity.verifyPassword(password, teacher.salt, teacher.passwordHash)
        if (!isValid) {
            return Resource.Error("Kata sandi yang Anda masukkan salah.")
        }

        val role = if (teacher.role == "ADMIN") UserRole.ADMIN else UserRole.TEACHER
        val assignedGroups = if (role == UserRole.TEACHER) {
            dao.getAssignedGroupIdsForTeacherDirect(teacher.id)
        } else {
            emptyList()
        }

        val session = UserSession(
            userId = teacher.id,
            name = teacher.name,
            email = teacher.email,
            role = role,
            assignedGroupIds = assignedGroups,
            isActive = teacher.isActive
        )
        _currentSession.value = session

        // Log audit login
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_auth_${System.currentTimeMillis()}",
                userId = teacher.id,
                userName = teacher.name,
                userRole = teacher.role,
                actionType = "LOGIN",
                entityType = "AUTH",
                entityId = teacher.id,
                description = "Login berhasil untuk ${teacher.name} (${teacher.role})"
            )
        )

        return Resource.Success(session)
    }

    suspend fun refreshSession(): UserSession? {
        val current = _currentSession.value ?: return null
        val teacher = dao.getTeacherById(current.userId) ?: return null
        if (!teacher.isActive) {
            logout()
            return null
        }
        val assignedGroups = dao.getAssignedGroupIdsForTeacherDirect(teacher.id)
        val refreshed = current.copy(
            name = teacher.name,
            email = teacher.email,
            assignedGroupIds = assignedGroups,
            isActive = teacher.isActive
        )
        _currentSession.value = refreshed
        return refreshed
    }

    suspend fun logout() {
        val current = _currentSession.value
        if (current != null) {
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "log_logout_${System.currentTimeMillis()}",
                    userId = current.userId,
                    userName = current.name,
                    userRole = current.role.name,
                    actionType = "LOGOUT",
                    entityType = "AUTH",
                    entityId = current.userId,
                    description = "Logout akun ${current.name}"
                )
            )
        }
        _currentSession.value = null
    }

    suspend fun changePassword(oldPass: String, newPass: String): Resource<Unit> {
        val current = _currentSession.value ?: return Resource.Error("Sesi tidak aktif.")
        val teacher = dao.getTeacherById(current.userId) ?: return Resource.Error("Pengguna tidak ditemukan.")

        if (!PasswordSecurity.verifyPassword(oldPass, teacher.salt, teacher.passwordHash)) {
            return Resource.Error("Kata sandi lama tidak cocok.")
        }
        if (newPass.length < 6) {
            return Resource.Error("Kata sandi baru minimal 6 karakter.")
        }

        val newSalt = PasswordSecurity.generateSalt()
        val newHash = PasswordSecurity.hashPassword(newPass, newSalt)
        dao.updateTeacher(
            teacher.copy(
                passwordHash = newHash,
                salt = newSalt,
                updatedAt = System.currentTimeMillis()
            )
        )
        return Resource.Success(Unit)
    }
}
