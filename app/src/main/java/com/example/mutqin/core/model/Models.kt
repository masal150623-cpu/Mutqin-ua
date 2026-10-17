package com.example.mutqin.core.model

enum class UserRole {
    ADMIN,
    TEACHER;

    val displayName: String
        get() = when (this) {
            ADMIN -> "Kepala Program / Admin"
            TEACHER -> "Ustaz / Ustazah"
        }
}

data class UserSession(
    val userId: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val assignedGroupIds: List<String> = emptyList(),
    val isActive: Boolean = true
)

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

object ProgramConstants {
    const val DEFAULT_ACADEMIC_YEAR_ID = "ay_2026_2027"
    const val DEFAULT_ACADEMIC_YEAR_NAME = "2026/2027"

    // Default 4 Kelompok (D, C, B, A) as required
    const val GROUP_D_ID = "grp_d_iqro"
    const val GROUP_D_CODE = "D"
    const val GROUP_D_NAME = "Kelompok D - Pembelajaran Iqro'"
    const val GROUP_D_FOCUS = "Mengenal huruf hijaiyah, makharijul huruf dasar, dan kelancaran membaca Iqro' jilid 1-6"
    const val GROUP_D_TARGET = "Menuntaskan Iqro' 1-6 dengan kelancaran membaca dan makhraj yang benar"

    const val GROUP_C_ID = "grp_c_tahsin"
    const val GROUP_C_CODE = "C"
    const val GROUP_C_NAME = "Kelompok C - Perbaikan Bacaan Al-Qur'an (Tahsin)"
    const val GROUP_C_FOCUS = "Penerapan hukum tajwid, panjang-pendek (mad), ghunnah, dan kelancaran tilawah Al-Qur'an"
    const val GROUP_C_TARGET = "Mampu membaca Al-Qur'an secara tartil sesuai kaidah tajwid standar"

    const val GROUP_B_ID = "grp_b_ziyadah"
    const val GROUP_B_CODE = "B"
    const val GROUP_B_NAME = "Kelompok B - Persiapan & Setoran Hafalan (Ziyadah)"
    const val GROUP_B_FOCUS = "Penambahan hafalan baru (ziyadah) dengan target minimal 5 ayat lancar per sesi"
    const val GROUP_B_TARGET = "Menambah hafalan juz 30 / juz pilihan dengan ketepatan tajwid dan kelancaran prima"

    const val GROUP_A_ID = "grp_a_murajaah"
    const val GROUP_A_CODE = "A"
    const val GROUP_A_NAME = "Kelompok A - Murajaah & Pemeliharaan Hafalan"
    const val GROUP_A_FOCUS = "Menjaga dan mengulang hafalan yang sudah mutqin agar tidak mudah lupa"
    const val GROUP_A_TARGET = "Penguatan hafalan mutqin berkala minimal 1/4 juz hingga 1 juz per sesi"
}
