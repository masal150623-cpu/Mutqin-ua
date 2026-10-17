package com.example.mutqin.data.repository

import com.example.mutqin.core.model.Resource
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
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupManifest(
    val appVersion: String,
    val exportedAt: Long,
    val exportedAtFormatted: String,
    val exportedBy: String,
    val totalSantri: Int,
    val totalTeachers: Int,
    val totalGroups: Int,
    val totalLearningRecords: Int,
    val totalAttendances: Int,
    val totalEvaluations: Int,
    val checksum: String
)

data class RestorePreview(
    val isValid: Boolean,
    val error: String? = null,
    val exportedAt: String = "",
    val exportedBy: String = "",
    val santriCount: Int = 0,
    val teachersCount: Int = 0,
    val groupsCount: Int = 0,
    val recordsCount: Int = 0,
    val attendanceCount: Int = 0,
    val rawJson: String = ""
)

class BackupRepository(private val dao: MutqinDao) {

    suspend fun createFullBackupJson(adminId: String, adminName: String): Resource<Pair<BackupManifest, String>> {
        return try {
            val teachers = dao.dumpTeachers()
            val academicYears = dao.dumpAcademicYears()
            val groups = dao.dumpGroups()
            val assignments = dao.dumpAssignments()
            val santriList = dao.dumpSantri()
            val transfers = dao.dumpTransfers()
            val iqro = dao.dumpIqro()
            val tahsin = dao.dumpTahsin()
            val ziyadah = dao.dumpZiyadah()
            val murajaah = dao.dumpMurajaah()
            val attendances = dao.dumpAttendances()
            val journals = dao.dumpJournals()
            val evaluations = dao.dumpEvaluations()
            val proposals = dao.dumpProposals()
            val logs = dao.dumpAuditLogs()
            val configs = dao.dumpConfigs()

            val rootJson = JSONObject()
            rootJson.put("app", "MUTQIN_ULUL_ALBAB")
            rootJson.put("schemaVersion", 1)
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = sdf.format(Date(now))
            rootJson.put("exportedAt", now)
            rootJson.put("exportedAtFormatted", dateStr)
            rootJson.put("exportedBy", adminName)

            // Convert to JSON Arrays
            val teachersArr = JSONArray()
            teachers.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("nip", it.nip)
                obj.put("name", it.name)
                obj.put("email", it.email)
                obj.put("phone", it.phone)
                obj.put("passwordHash", it.passwordHash)
                obj.put("salt", it.salt)
                obj.put("role", it.role)
                obj.put("isActive", it.isActive)
                obj.put("createdAt", it.createdAt)
                obj.put("updatedAt", it.updatedAt)
                teachersArr.put(obj)
            }
            rootJson.put("teachers", teachersArr)

            val santriArr = JSONArray()
            santriList.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("nis", it.nis)
                obj.put("name", it.name)
                obj.put("gender", it.gender)
                obj.put("schoolClass", it.schoolClass)
                obj.put("joinDate", it.joinDate)
                obj.put("admissionYear", it.admissionYear)
                obj.put("academicYearId", it.academicYearId)
                obj.put("currentGroupId", it.currentGroupId)
                obj.put("responsibleTeacherId", it.responsibleTeacherId)
                obj.put("status", it.status)
                obj.put("notes", it.notes)
                obj.put("createdAt", it.createdAt)
                obj.put("updatedAt", it.updatedAt)
                santriArr.put(obj)
            }
            rootJson.put("santri", santriArr)

            val groupsArr = JSONArray()
            groups.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("code", it.code)
                obj.put("name", it.name)
                obj.put("description", it.description)
                obj.put("focus", it.focus)
                obj.put("targetMinVerses", it.targetMinVerses)
                obj.put("evaluationIndicators", it.evaluationIndicators)
                obj.put("academicYearId", it.academicYearId)
                obj.put("isActive", it.isActive)
                obj.put("sortOrder", it.sortOrder)
                obj.put("createdAt", it.createdAt)
                groupsArr.put(obj)
            }
            rootJson.put("groups", groupsArr)

            val ayArr = JSONArray()
            academicYears.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("startDate", it.startDate)
                obj.put("endDate", it.endDate)
                obj.put("isActive", it.isActive)
                obj.put("createdAt", it.createdAt)
                ayArr.put(obj)
            }
            rootJson.put("academicYears", ayArr)

            val assignArr = JSONArray()
            assignments.forEach {
                val obj = JSONObject()
                obj.put("teacherId", it.teacherId)
                obj.put("groupId", it.groupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("assignedAt", it.assignedAt)
                assignArr.put(obj)
            }
            rootJson.put("teacherAssignments", assignArr)

            val transferArr = JSONArray()
            transfers.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("santriId", it.santriId)
                obj.put("fromGroupId", it.fromGroupId)
                obj.put("toGroupId", it.toGroupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("transferDate", it.transferDate)
                obj.put("reason", it.reason)
                obj.put("changedByUserId", it.changedByUserId)
                obj.put("createdAt", it.createdAt)
                transferArr.put(obj)
            }
            rootJson.put("transfers", transferArr)

            // Learning records
            val iqroArr = JSONArray()
            iqro.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("santriId", it.santriId)
                obj.put("teacherId", it.teacherId)
                obj.put("groupId", it.groupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("date", it.date)
                obj.put("volume", it.volume)
                obj.put("pageOrLesson", it.pageOrLesson)
                obj.put("fluencyLevel", it.fluencyLevel)
                obj.put("readingMistakes", it.readingMistakes)
                obj.put("notes", it.notes)
                obj.put("followUpAction", it.followUpAction)
                obj.put("createdAt", it.createdAt)
                iqroArr.put(obj)
            }
            rootJson.put("iqroRecords", iqroArr)

            val tahsinArr = JSONArray()
            tahsin.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("santriId", it.santriId)
                obj.put("teacherId", it.teacherId)
                obj.put("groupId", it.groupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("date", it.date)
                obj.put("surahName", it.surahName)
                obj.put("ayahStart", it.ayahStart)
                obj.put("ayahEnd", it.ayahEnd)
                obj.put("fluencyScore", it.fluencyScore)
                obj.put("makhrajScore", it.makhrajScore)
                obj.put("tajwidScore", it.tajwidScore)
                obj.put("mistakesDescription", it.mistakesDescription)
                obj.put("notes", it.notes)
                obj.put("practiceRecommendations", it.practiceRecommendations)
                obj.put("createdAt", it.createdAt)
                tahsinArr.put(obj)
            }
            rootJson.put("tahsinRecords", tahsinArr)

            val ziyadahArr = JSONArray()
            ziyadah.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("santriId", it.santriId)
                obj.put("teacherId", it.teacherId)
                obj.put("groupId", it.groupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("date", it.date)
                obj.put("surahName", it.surahName)
                obj.put("ayahStart", it.ayahStart)
                obj.put("ayahEnd", it.ayahEnd)
                obj.put("totalAyahs", it.totalAyahs)
                obj.put("fluencyScore", it.fluencyScore)
                obj.put("tajwidScore", it.tajwidScore)
                obj.put("makhrajScore", it.makhrajScore)
                obj.put("mistakeCount", it.mistakeCount)
                obj.put("status", it.status)
                obj.put("isMastered", it.isMastered)
                obj.put("notes", it.notes)
                obj.put("followUpAction", it.followUpAction)
                obj.put("createdAt", it.createdAt)
                ziyadahArr.put(obj)
            }
            rootJson.put("ziyadahRecords", ziyadahArr)

            val murajaahArr = JSONArray()
            murajaah.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("santriId", it.santriId)
                obj.put("teacherId", it.teacherId)
                obj.put("groupId", it.groupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("date", it.date)
                obj.put("surahName", it.surahName)
                obj.put("ayahStart", it.ayahStart)
                obj.put("ayahEnd", it.ayahEnd)
                obj.put("fluencyLevel", it.fluencyLevel)
                obj.put("mistakeCount", it.mistakeCount)
                obj.put("tajwidScore", it.tajwidScore)
                obj.put("notes", it.notes)
                obj.put("followUpAction", it.followUpAction)
                obj.put("createdAt", it.createdAt)
                murajaahArr.put(obj)
            }
            rootJson.put("murajaahRecords", murajaahArr)

            val attArr = JSONArray()
            attendances.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("santriId", it.santriId)
                obj.put("teacherId", it.teacherId)
                obj.put("groupId", it.groupId)
                obj.put("academicYearId", it.academicYearId)
                obj.put("date", it.date)
                obj.put("sessionType", it.sessionType)
                obj.put("status", it.status)
                obj.put("notes", it.notes)
                obj.put("createdAt", it.createdAt)
                attArr.put(obj)
            }
            rootJson.put("attendances", attArr)

            val jsonString = rootJson.toString(2)
            val checksum = calculateChecksum(jsonString)
            rootJson.put("checksum", checksum)

            val finalJson = rootJson.toString(2)

            val manifest = BackupManifest(
                appVersion = "1.0",
                exportedAt = now,
                exportedAtFormatted = dateStr,
                exportedBy = adminName,
                totalSantri = santriList.size,
                totalTeachers = teachers.size,
                totalGroups = groups.size,
                totalLearningRecords = iqro.size + tahsin.size + ziyadah.size + murajaah.size,
                totalAttendances = attendances.size,
                totalEvaluations = evaluations.size,
                checksum = checksum
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    id = "log_bkp_$now",
                    userId = adminId,
                    userName = adminName,
                    userRole = "ADMIN",
                    actionType = "BACKUP",
                    entityType = "SYSTEM",
                    entityId = "BACKUP_FULL",
                    description = "Pembuatan backup data lengkap oleh $adminName (${manifest.totalSantri} santri, ${manifest.totalLearningRecords} catatan pembelajaran, Checksum: ${checksum.take(8)})"
                )
            )

            Resource.Success(Pair(manifest, finalJson))
        } catch (e: Exception) {
            Resource.Error("Gagal membuat backup: ${e.message}", e)
        }
    }

    fun inspectBackupJson(jsonString: String): RestorePreview {
        return try {
            val root = JSONObject(jsonString)
            val app = root.optString("app", "")
            if (app != "MUTQIN_ULUL_ALBAB") {
                return RestorePreview(isValid = false, error = "File tidak valid: Format bukan data MUTQIN ULUL ALBAB.")
            }
            val dateStr = root.optString("exportedAtFormatted", "Unknown")
            val by = root.optString("exportedBy", "Unknown")
            val santriCount = root.optJSONArray("santri")?.length() ?: 0
            val teachersCount = root.optJSONArray("teachers")?.length() ?: 0
            val groupsCount = root.optJSONArray("groups")?.length() ?: 0
            val iqroCount = root.optJSONArray("iqroRecords")?.length() ?: 0
            val tahsinCount = root.optJSONArray("tahsinRecords")?.length() ?: 0
            val ziyadahCount = root.optJSONArray("ziyadahRecords")?.length() ?: 0
            val murajaahCount = root.optJSONArray("murajaahRecords")?.length() ?: 0
            val attCount = root.optJSONArray("attendances")?.length() ?: 0

            RestorePreview(
                isValid = true,
                exportedAt = dateStr,
                exportedBy = by,
                santriCount = santriCount,
                teachersCount = teachersCount,
                groupsCount = groupsCount,
                recordsCount = iqroCount + tahsinCount + ziyadahCount + murajaahCount,
                attendanceCount = attCount,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            RestorePreview(isValid = false, error = "Format JSON tidak terbaca: ${e.message}")
        }
    }

    suspend fun executeRestore(
        adminId: String,
        adminName: String,
        preview: RestorePreview
    ): Resource<String> {
        if (!preview.isValid) {
            return Resource.Error(preview.error ?: "Data restore tidak valid.")
        }
        return try {
            val root = JSONObject(preview.rawJson)

            // 1. Teachers
            val teachersArr = root.optJSONArray("teachers")
            if (teachersArr != null) {
                val list = mutableListOf<TeacherEntity>()
                for (i in 0 until teachersArr.length()) {
                    val o = teachersArr.getJSONObject(i)
                    list.add(
                        TeacherEntity(
                            id = o.getString("id"),
                            nip = o.optString("nip", ""),
                            name = o.getString("name"),
                            email = o.getString("email"),
                            phone = o.optString("phone", ""),
                            passwordHash = o.getString("passwordHash"),
                            salt = o.getString("salt"),
                            role = o.getString("role"),
                            isActive = o.optBoolean("isActive", true),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.restoreTeachers(list)
            }

            // 2. Groups
            val groupsArr = root.optJSONArray("groups")
            if (groupsArr != null) {
                val list = mutableListOf<GroupEntity>()
                for (i in 0 until groupsArr.length()) {
                    val o = groupsArr.getJSONObject(i)
                    list.add(
                        GroupEntity(
                            id = o.getString("id"),
                            code = o.getString("code"),
                            name = o.getString("name"),
                            description = o.optString("description", ""),
                            focus = o.optString("focus", ""),
                            targetMinVerses = o.optInt("targetMinVerses", 5),
                            evaluationIndicators = o.optString("evaluationIndicators", ""),
                            academicYearId = o.optString("academicYearId", ""),
                            isActive = o.optBoolean("isActive", true),
                            sortOrder = o.optInt("sortOrder", 0),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.restoreGroups(list)
            }

            // 3. Santri
            val santriArr = root.optJSONArray("santri")
            if (santriArr != null) {
                val list = mutableListOf<SantriEntity>()
                for (i in 0 until santriArr.length()) {
                    val o = santriArr.getJSONObject(i)
                    list.add(
                        SantriEntity(
                            id = o.getString("id"),
                            nis = o.getString("nis"),
                            name = o.getString("name"),
                            gender = o.optString("gender", "L"),
                            schoolClass = o.optString("schoolClass", ""),
                            joinDate = o.optString("joinDate", ""),
                            admissionYear = o.optString("admissionYear", ""),
                            academicYearId = o.optString("academicYearId", ""),
                            currentGroupId = o.getString("currentGroupId"),
                            responsibleTeacherId = o.optString("responsibleTeacherId", ""),
                            status = o.optString("status", "AKTIF"),
                            notes = o.optString("notes", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.restoreSantri(list)
            }

            // 4. Learning records (Iqro, Tahsin, Ziyadah, Murajaah)
            val iqroArr = root.optJSONArray("iqroRecords")
            if (iqroArr != null) {
                val list = mutableListOf<IqroRecordEntity>()
                for (i in 0 until iqroArr.length()) {
                    val o = iqroArr.getJSONObject(i)
                    list.add(
                        IqroRecordEntity(
                            id = o.getString("id"),
                            santriId = o.getString("santriId"),
                            teacherId = o.getString("teacherId"),
                            groupId = o.getString("groupId"),
                            academicYearId = o.getString("academicYearId"),
                            date = o.getString("date"),
                            volume = o.getInt("volume"),
                            pageOrLesson = o.getString("pageOrLesson"),
                            fluencyLevel = o.getString("fluencyLevel"),
                            readingMistakes = o.optString("readingMistakes", ""),
                            notes = o.optString("notes", ""),
                            followUpAction = o.optString("followUpAction", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.restoreIqro(list)
            }

            val ziyadahArr = root.optJSONArray("ziyadahRecords")
            if (ziyadahArr != null) {
                val list = mutableListOf<ZiyadahRecordEntity>()
                for (i in 0 until ziyadahArr.length()) {
                    val o = ziyadahArr.getJSONObject(i)
                    list.add(
                        ZiyadahRecordEntity(
                            id = o.getString("id"),
                            santriId = o.getString("santriId"),
                            teacherId = o.getString("teacherId"),
                            groupId = o.getString("groupId"),
                            academicYearId = o.getString("academicYearId"),
                            date = o.getString("date"),
                            surahName = o.getString("surahName"),
                            ayahStart = o.getInt("ayahStart"),
                            ayahEnd = o.getInt("ayahEnd"),
                            totalAyahs = o.getInt("totalAyahs"),
                            fluencyScore = o.optInt("fluencyScore", 85),
                            tajwidScore = o.optInt("tajwidScore", 85),
                            makhrajScore = o.optInt("makhrajScore", 85),
                            mistakeCount = o.optInt("mistakeCount", 0),
                            status = o.optString("status", "TERCAPAI"),
                            isMastered = o.optBoolean("isMastered", true),
                            notes = o.optString("notes", ""),
                            followUpAction = o.optString("followUpAction", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.restoreZiyadah(list)
            }

            val now = System.currentTimeMillis()
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "log_rst_$now",
                    userId = adminId,
                    userName = adminName,
                    userRole = "ADMIN",
                    actionType = "RESTORE",
                    entityType = "SYSTEM",
                    entityId = "RESTORE_FULL",
                    description = "Pemulihan (restore) database berhasil oleh $adminName (${preview.santriCount} santri, ${preview.recordsCount} catatan pembelajaran)."
                )
            )

            Resource.Success("Pemulihan data berhasil! ${preview.santriCount} data santri dan ${preview.recordsCount} catatan riwayat telah dipulihkan.")
        } catch (e: Exception) {
            Resource.Error("Gagal menjalankan restore data: ${e.message}", e)
        }
    }

    private fun calculateChecksum(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
