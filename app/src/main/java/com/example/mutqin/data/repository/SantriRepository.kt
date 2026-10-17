package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.GroupTransferHistoryEntity
import com.example.mutqin.data.local.entity.SantriEntity
import kotlinx.coroutines.flow.Flow

class SantriRepository(private val dao: MutqinDao) {

    val allSantri: Flow<List<SantriEntity>> = dao.getAllSantri()

    fun getSantriByGroup(groupId: String): Flow<List<SantriEntity>> =
        dao.getSantriByGroup(groupId)

    fun getActiveSantriInGroups(groupIds: List<String>): Flow<List<SantriEntity>> =
        dao.getActiveSantriInGroups(groupIds)

    fun getSantriByIdFlow(id: String): Flow<SantriEntity?> =
        dao.getSantriByIdFlow(id)

    suspend fun getSantriById(id: String): SantriEntity? =
        dao.getSantriById(id)

    fun getTransferHistory(santriId: String): Flow<List<GroupTransferHistoryEntity>> =
        dao.getTransferHistory(santriId)

    suspend fun checkPotentialDuplicate(nis: String, name: String): String? {
        val byNis = dao.getSantriByNis(nis.trim())
        if (byNis != null) {
            return "NIS ${nis.trim()} sudah digunakan oleh santri: ${byNis.name} (Kelompok: ${byNis.currentGroupId})."
        }
        return null
    }

    suspend fun createSantri(
        adminId: String,
        adminName: String,
        nis: String,
        name: String,
        gender: String,
        schoolClass: String,
        joinDate: String,
        admissionYear: String,
        academicYearId: String,
        groupId: String,
        responsibleTeacherId: String,
        notes: String
    ): Resource<SantriEntity> {
        val trimmedNis = nis.trim()
        val trimmedName = name.trim()

        if (trimmedNis.isBlank() || trimmedName.isBlank()) {
            return Resource.Error("NIS dan Nama Lengkap santri wajib diisi.")
        }

        val existing = dao.getSantriByNis(trimmedNis)
        if (existing != null) {
            return Resource.Error("Gagal: NIS '$trimmedNis' sudah terdaftar atas nama ${existing.name}.")
        }

        val now = System.currentTimeMillis()
        val santriId = "snt_${now}_${trimmedNis.takeLast(4)}"

        val santri = SantriEntity(
            id = santriId,
            nis = trimmedNis,
            name = trimmedName,
            gender = gender,
            schoolClass = schoolClass.trim(),
            joinDate = joinDate,
            admissionYear = admissionYear,
            academicYearId = academicYearId,
            currentGroupId = groupId,
            responsibleTeacherId = responsibleTeacherId,
            status = "AKTIF",
            notes = notes.trim(),
            createdAt = now,
            updatedAt = now
        )

        dao.insertSantri(santri)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_snt_create_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "CREATE",
                entityType = "SANTRI",
                entityId = santriId,
                description = "Pendaftaran santri baru: $trimmedName (NIS: $trimmedNis, Kelompok: $groupId)"
            )
        )

        return Resource.Success(santri)
    }

    suspend fun updateSantri(
        adminId: String,
        adminName: String,
        santriId: String,
        name: String,
        gender: String,
        schoolClass: String,
        responsibleTeacherId: String,
        status: String,
        notes: String
    ): Resource<Unit> {
        val current = dao.getSantriById(santriId) ?: return Resource.Error("Santri tidak ditemukan.")

        val now = System.currentTimeMillis()
        val updated = current.copy(
            name = name.trim(),
            gender = gender,
            schoolClass = schoolClass.trim(),
            responsibleTeacherId = responsibleTeacherId,
            status = status,
            notes = notes.trim(),
            updatedAt = now
        )
        dao.updateSantri(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_snt_update_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "UPDATE",
                entityType = "SANTRI",
                entityId = santriId,
                description = "Pembaruan profil santri: ${updated.name} (Status: $status)"
            )
        )
        return Resource.Success(Unit)
    }

    suspend fun transferGroup(
        adminId: String,
        adminName: String,
        santriId: String,
        targetGroupId: String,
        reason: String,
        academicYearId: String,
        transferDate: String
    ): Resource<Unit> {
        val santri = dao.getSantriById(santriId) ?: return Resource.Error("Santri tidak ditemukan.")
        if (santri.currentGroupId == targetGroupId) {
            return Resource.Error("Santri sudah berada di kelompok tujuan.")
        }

        dao.transferSantriGroup(
            santriId = santriId,
            targetGroupId = targetGroupId,
            reason = reason.trim(),
            adminUserId = adminId,
            academicYearId = academicYearId,
            date = transferDate
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_snt_tf_${System.currentTimeMillis()}",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "TRANSFER",
                entityType = "SANTRI",
                entityId = santriId,
                description = "Pemindahan kelompok santri ${santri.name} dari ${santri.currentGroupId} ke $targetGroupId. Alasan: $reason"
            )
        )

        return Resource.Success(Unit)
    }
}
