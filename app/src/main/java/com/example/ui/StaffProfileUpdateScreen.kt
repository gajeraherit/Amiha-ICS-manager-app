package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import com.example.ui.theme.TextEcoGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffProfileUpdateScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val currentEmail by viewModel.userEmail.collectAsState()
    val profiles by viewModel.registeredStaff.collectAsState()

    val currentProfile = remember(currentEmail, profiles) {
        profiles[currentEmail.lowercase()] ?: StaffProfile(
            fullName = viewModel.userName.value,
            role = viewModel.userRole.value,
            password = "password",
            phone = ""
        )
    }

    var fullName by remember(currentProfile) { mutableStateOf(currentProfile.fullName) }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile.phone) }
    var password by remember(currentProfile) { mutableStateOf(currentProfile.password) }
    var selectedRole by remember(currentProfile) { mutableStateOf(currentProfile.role) }
    var clearanceToken by remember { mutableStateOf("AMIHA-2026") }

    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val earthGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F3DF), // Elegant soft green
            Color(0xFFF7FBF2)
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentLang == AppLanguage.ENGLISH) "Update Profile" else "પ્રોફાઇલ અપડેટ કરો",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F3DF)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(earthGradient)
                .padding(innerPadding)
                .testTag("staff_profile_update_root")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular profile decorative icon with initials
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, PrimaryGreen, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name and Active Role mode display
                Text(
                    text = fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = currentEmail,
                    fontSize = 13.sp,
                    color = TextEcoGray,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(SecondaryGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (selectedRole == "Field Officer") Icons.Default.Agriculture else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = PrimaryGreen
                    )
                    Text(
                        text = if (selectedRole == "Field Officer") {
                            if (currentLang == AppLanguage.ENGLISH) "Field Officer Role" else "ફીલ્ડ ઓફિસર"
                        } else {
                            if (currentLang == AppLanguage.ENGLISH) "Inspector Role" else "નિરીક્ષક"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_profile_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.ENGLISH) "Profile Details" else "પ્રોફાઇલ વિગતો",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        // ---- Full Name ----
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = null
                            },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "Full Name" else "પૂરું નામ") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_fullName_input")
                        )

                        // ---- Phone Number ----
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "Contact Number" else "મોબાઈલ નંબર") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_phone_input")
                        )

                        // ---- Password ----
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "Password" else "પાસવર્ડ") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_password_input")
                        )

                        // ---- Select Role Segment ----
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (currentLang == AppLanguage.ENGLISH) "System Role" else "સિસ્ટમ ભૂમિકા",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGreen,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Field Officer", "Inspector").forEach { role ->
                                    val isSelected = selectedRole == role
                                    Button(
                                        onClick = { selectedRole = role },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) PrimaryGreen else Color.Transparent,
                                            contentColor = if (isSelected) Color.White else Color(0xFF4B5563)
                                        ),
                                        modifier = Modifier.weight(1f).testTag("role_btn_$role"),
                                        elevation = null,
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = if (role == "Field Officer") {
                                                if (currentLang == AppLanguage.ENGLISH) "Field Officer" else "ફીલ્ડ ઓફિસર"
                                            } else {
                                                if (currentLang == AppLanguage.ENGLISH) "Inspector" else "નિરીક્ષક"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // ---- Clearance Token ----
                        OutlinedTextField(
                            value = clearanceToken,
                            onValueChange = {
                                clearanceToken = it
                                errorMessage = null
                            },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "Clearance Token" else "મંજૂરી ટોકન") },
                            leadingIcon = { Icon(Icons.Default.PublishedWithChanges, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            supportingText = {
                                Text(
                                    text = if (currentLang == AppLanguage.ENGLISH) "Required clearance token: AMIHA-2026" else "ડિફોલ્ટ: AMIHA-2026",
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_token_input")
                        )

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Save Button
                        Button(
                            onClick = {
                                when {
                                    fullName.isBlank() -> {
                                        errorMessage = "Please enter your full name."
                                    }
                                    password.length < 4 -> {
                                        errorMessage = "Password must be at least 4 characters."
                                    }
                                    clearanceToken != "AMIHA-2026" -> {
                                        errorMessage = "Invalid Clearance Token."
                                    }
                                    else -> {
                                        viewModel.updateStaffProfile(
                                            email = currentEmail,
                                            fullName = fullName,
                                            phone = phone,
                                            password = password,
                                            role = selectedRole
                                        )
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Profile updated successfully!")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.ENGLISH) "SAVE PROFILE" else "પ્રોફાઇલ સાચવો",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_settings_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.ENGLISH) "System & Language" else "સિસ્ટમ અને ભાષા",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        // Language Selection Row
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = Localization.getString(currentLang, "switch_lang") ?: "Switch Language",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextEcoGray
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = currentLang == lang
                                    Button(
                                        onClick = { viewModel.selectedLanguage.value = lang },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) PrimaryGreen else Color.Transparent,
                                            contentColor = if (isSelected) Color.White else Color(0xFF4B5563)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lang_btn_${lang.name}"),
                                        elevation = null,
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                        // Logout Button
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("profile_logout_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Localization.getString(currentLang, "logout") ?: "Logout",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
