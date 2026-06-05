package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.ui.LandingScreen
import com.example.ui.LoginScreen
import com.example.ui.StaffRegistrationScreen
import com.example.ui.DashboardScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppRuntimeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLandingScreenIsolated() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNotNull(viewModel)

        composeTestRule.setContent {
            MyApplicationTheme {
                LandingScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {},
                    onNavigateToRegister = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("landing_signin_btn").assertExists()
        composeTestRule.onNodeWithTag("landing_register_btn").assertExists()
    }

    @Test
    fun testLoginScreenIsolated() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNotNull(viewModel)

        composeTestRule.setContent {
            MyApplicationTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onLoginSuccess = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("login_card").assertExists()
        composeTestRule.onNodeWithTag("email_input").assertExists()
        composeTestRule.onNodeWithTag("password_input").assertExists()
    }

    @Test
    fun testStaffRegistrationScreenIsolated() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNotNull(viewModel)

        composeTestRule.setContent {
            MyApplicationTheme {
                StaffRegistrationScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onRegistrationSuccess = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("staff_registration_card").assertExists()
        composeTestRule.onNodeWithTag("reg_fullName_input").assertExists()
        composeTestRule.onNodeWithTag("reg_email_input").assertExists()
    }

    @Test
    fun testDashboardScreenIsolated() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNotNull(viewModel)

        composeTestRule.setContent {
            MyApplicationTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddFarmer = {},
                    onNavigateToProfile = {},
                    onNavigateToStaffProfile = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("dashboard_root").assertExists()
        composeTestRule.onNodeWithTag("staff_profile_button").assertExists()
    }

    @Test
    fun testStaffProfileUpdateScreenIsolated() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNotNull(viewModel)

        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.StaffProfileUpdateScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onLogout = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("staff_profile_update_root").assertExists()
        composeTestRule.onNodeWithTag("staff_profile_card").assertExists()
        composeTestRule.onNodeWithTag("profile_fullName_input").assertExists()
        composeTestRule.onNodeWithTag("save_profile_button").assertExists()
    }
}
