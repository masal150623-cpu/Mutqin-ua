package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
import com.example.mutqin.data.local.dao.MutqinDao
import com.example.mutqin.data.local.entity.AuditLogEntity
import com.example.mutqin.data.local.entity.EvaluationEntity
import com.example.mutqin.data.local.entity.PromotionProposalEntity
import kotlinx.coroutines.flow.Flow

class EvaluationRepository(private val dao: MutqinDao) {

    val allEvaluations: Flow<List<EvaluationEntity>> = dao.getAllEvaluations()
    val allPromotionProposals: Flow<List<PromotionProposalEntity>> = dao.getAllPromotionProposals()
    val pendingPromotionProposals: Flow<List<PromotionProposalEntity>> = dao.getPendingPromotionProposals()

    fun getEvaluationsForSantri(santriId: String): Flow<List<EvaluationEntity>> =
        dao.getEvaluationsForSantri(santriId)

    suspend fun saveEvaluation(
        evaluatorId: String,
        evaluatorName: String,
        santriId: String,
        groupId: String,
        academicYearId: String,
        periodMonthYear: String,
        iqroScore: Int,
        quranFluencyScore: Int,
        makhrajScore: Int,
        tajwidScore: Int,
        ziyadahScore: Int,
        murajaahScore: Int,
        attendanceScore: Int,
        overallNotes: String,
        teacherRecommendations: String
    ): Resource<EvaluationEntity> {
        val now = System.currentTimeMillis()
        val evaluationId = "evl_${now}_${santriId.takeLast(4)}"

        val evaluation = EvaluationEntity(
            id = evaluationId,
            santriId = santriId,
            evaluatorTeacherId = evaluatorId,
            groupId = groupId,
            academicYearId = academicYearId,
            periodMonthYear = periodMonthYear.trim(),
            iqroScore = iqroScore.coerceIn(0, 100),
            quranFluencyScore = quranFluencyScore.coerceIn(0, 100),
            makhrajScore = makhrajScore.coerceIn(0, 100),
            tajwidScore = tajwidScore.coerceIn(0, 100),
            ziyadahScore = ziyadahScore.coerceIn(0, 100),
            murajaahScore = murajaahScore.coerceIn(0, 100),
            attendanceScore = attendanceScore.coerceIn(0, 100),
            overallNotes = overallNotes.trim(),
            teacherRecommendations = teacherRecommendations.trim(),
            approvalStatus = "DISETUJUI",
            createdAt = now
        )
        dao.insertEvaluation(evaluation)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_evl_$now",
                userId = evaluatorId,
                userName = evaluatorName,
                userRole = "TEACHER",
                actionType = "CREATE",
                entityType = "EVALUATION",
                entityId = evaluationId,
                description = "Pencatatan evaluasi santri ($periodMonthYear) oleh $evaluatorName"
            )
        )
        return Resource.Success(evaluation)
    }

    suspend fun submitPromotionProposal(
        teacherId: String,
        teacherName: String,
        santriId: String,
        currentGroupId: String,
        targetGroupId: String,
        academicYearId: String,
        proposalDate: String,
        justification: String,
        evaluationRefId: String
    ): Resource<PromotionProposalEntity> {
        if (targetGroupId == currentGroupId) {
            return Resource.Error("Kelompok tujuan tidak boleh sama dengan kelompok asal.")
        }
        if (justification.isBlank()) {
            return Resource.Error("Alasan dan justifikasi kenaikan kelompok wajib diisi.")
        }

        val now = System.currentTimeMillis()
        val proposal = PromotionProposalEntity(
            id = "prop_${now}_${santriId.takeLast(4)}",
            santriId = santriId,
            proposingTeacherId = teacherId,
            currentGroupId = currentGroupId,
            targetGroupId = targetGroupId,
            academicYearId = academicYearId,
            proposalDate = proposalDate,
            justification = justification.trim(),
            evaluationRefId = evaluationRefId,
            status = "PENDING",
            createdAt = now
        )
        dao.insertPromotionProposal(proposal)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_prop_$now",
                userId = teacherId,
                userName = teacherName,
                userRole = "TEACHER",
                actionType = "PROPOSE",
                entityType = "PROMOTION",
                entityId = proposal.id,
                description = "Pengajar $teacherName mengusulkan kenaikan kelompok santri ($currentGroupId -> $targetGroupId)"
            )
        )
        return Resource.Success(proposal)
    }

    suspend fun processPromotionDecision(
        adminId: String,
        adminName: String,
        proposal: PromotionProposalEntity,
        newStatus: String, // "APPROVED", "REJECTED", "POSTPONED"
        decisionNotes: String,
        decisionDate: String
    ): Resource<Unit> {
        val now = System.currentTimeMillis()
        val updatedProposal = proposal.copy(
            status = newStatus,
            decisionDate = decisionDate,
            decisionByAdminId = adminId,
            decisionNotes = decisionNotes.trim()
        )
        dao.updatePromotionProposal(updatedProposal)

        // If approved, execute real group transfer!
        if (newStatus == "APPROVED") {
            dao.transferSantriGroup(
                santriId = proposal.santriId,
                targetGroupId = proposal.targetGroupId,
                reason = "Kenaikan kelompok disetujui: ${proposal.justification} (Catatan: $decisionNotes)",
                adminUserId = adminId,
                academicYearId = proposal.academicYearId,
                date = decisionDate
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_dec_$now",
                userId = adminId,
                userName = adminName,
                userRole = "ADMIN",
                actionType = "DECISION_$newStatus",
                entityType = "PROMOTION",
                entityId = proposal.id,
                description = "Kepala Program $adminName menetapkan status usulan kenaikan santri menjadi $newStatus. Catatan: $decisionNotes"
            )
        )
        return Resource.Success(Unit)
    }
}
