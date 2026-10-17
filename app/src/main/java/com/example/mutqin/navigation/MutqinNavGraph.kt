package com.example.mutqin.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mutqin.core.model.UserRole
import com.example.mutqin.data.local.AppDatabase
import com.example.mutqin.data.repository.AuthRepository
import com.example.mutqin.data.repository.BackupRepository
import com.example.mutqin.data.repository.EvaluationRepository
import com.example.mutqin.data.repository.GroupRepository
import com.example.mutqin.data.repository.LearningRepository
import com.example.mutqin.data.repository.SantriRepository
import com.example.mutqin.data.repository.TeacherRepository
import com.example.mutqin.feature.admin.AdminDashboardScreen
import com.example.mutqin.feature.admin.AdminViewModel
import com.example.mutqin.feature.auth.AuthViewModel
import com.example.mutqin.feature.auth.LoginScreen
import com.example.mutqin.feature.teacher.TeacherDashboardScreen
import com.example.mutqin.feature.teacher.TeacherViewModel

sealed class Screen {
    object Login : Screen()
    object AdminDashboard : Screen()
    object TeacherDashboard : Screen()
}

@Composable
fun MutqinNavGraph(database: AppDatabase) {
    val dao = remember { database.mutqinDao() }

    // Repositories
    val authRepo = remember { AuthRepository(dao) }
    val teacherRepo = remember { TeacherRepository(dao) }
    val groupRepo = remember { GroupRepository(dao) }
    val santriRepo = remember { SantriRepository(dao) }
    val learningRepo = remember { LearningRepository(dao) }
    val evalRepo = remember { EvaluationRepository(dao) }
    val backupRepo = remember { BackupRepository(dao) }

    // ViewModels
    val authViewModel = remember { AuthViewModel(authRepo) }
    val authState by authViewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (val screen = currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    currentScreen = when (role) {
                        UserRole.ADMIN -> Screen.AdminDashboard
                        UserRole.TEACHER -> Screen.TeacherDashboard
                    }
                }
            )
        }
        is Screen.AdminDashboard -> {
            val session = authState.session
            if (session == null || session.role != UserRole.ADMIN) {
                currentScreen = Screen.Login
            } else {
                val adminViewModel = remember {
                    AdminViewModel(
                        teacherRepo = teacherRepo,
                        groupRepo = groupRepo,
                        santriRepo = santriRepo,
                        evaluationRepo = evalRepo,
                        backupRepo = backupRepo,
                        dao = dao
                    )
                }

                BackHandler {
                    authViewModel.logout {
                        currentScreen = Screen.Login
                    }
                }

                AdminDashboardScreen(
                    session = session,
                    viewModel = adminViewModel,
                    onLogout = {
                        authViewModel.logout {
                            currentScreen = Screen.Login
                        }
                    }
                )
            }
        }
        is Screen.TeacherDashboard -> {
            val session = authState.session
            if (session == null || session.role != UserRole.TEACHER) {
                currentScreen = Screen.Login
            } else {
                val teacherViewModel = remember(session.userId) {
                    TeacherViewModel(
                        session = session,
                        groupRepo = groupRepo,
                        santriRepo = santriRepo,
                        learningRepo = learningRepo,
                        evalRepo = evalRepo,
                        dao = dao
                    )
                }

                BackHandler {
                    authViewModel.logout {
                        currentScreen = Screen.Login
                    }
                }

                TeacherDashboardScreen(
                    session = session,
                    viewModel = teacherViewModel,
                    onLogout = {
                        authViewModel.logout {
                            currentScreen = Screen.Login
                        }
                    }
                )
            }
        }
    }
}
