package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isLoggedInInitial = remember { viewModel.isLoggedIn.value }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedInInitial) "dashboard" else "landing"
    ) {
        // 0. Brand Landing Page
        composable("landing") {
            LandingScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToRegister = {
                    navController.navigate("staff_registration")
                }
            )
        }

        // 1. Authentication splash
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }

        // 1b. Staff Account Registration Page
        composable("staff_registration") {
            StaffRegistrationScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = {
                    navController.navigate("login") {
                        popUpTo("landing") { inclusive = false }
                    }
                }
            )
        }

        // 2. Main Dashboard Panel
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAddFarmer = {
                    navController.navigate("registration_form")
                },
                onNavigateToProfile = { id ->
                    navController.navigate("profile_detail/$id")
                },
                onNavigateToStaffProfile = {
                    navController.navigate("staff_profile_update")
                }
            )
        }

        // 2b. Staff Profile Detail & Update Page
        composable("staff_profile_update") {
            StaffProfileUpdateScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("landing") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        // 3. New Registration or Edit mode form
        composable(
            route = "registration_form?farmerId={farmerId}",
            arguments = listOf(
                navArgument("farmerId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val farmerId = backStackEntry.arguments?.getString("farmerId")
            RegistrationScreen(
                viewModel = viewModel,
                editFarmerId = farmerId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Scrollable Single Profile Detail (Compliance View)
        composable(
            route = "profile_detail/{farmerId}",
            arguments = listOf(
                navArgument("farmerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val farmerId = backStackEntry.arguments?.getString("farmerId") ?: ""
            ProfileDetailScreen(
                viewModel = viewModel,
                farmerId = farmerId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate("registration_form?farmerId=$id")
                },
                onNavigateToInspection = { id ->
                    navController.navigate("inspection/$id")
                }
            )
        }

        // 5. Conduct Inspection Form Screen
        composable(
            route = "inspection/{farmerId}",
            arguments = listOf(
                navArgument("farmerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val farmerId = backStackEntry.arguments?.getString("farmerId") ?: ""
            InspectionFormScreen(
                viewModel = viewModel,
                farmerId = farmerId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
