package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffRegistrationScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val registeredStaff by viewModel.registeredStaff.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Field Officer") } // "Field Officer" or "Inspector"
    var clearanceToken by remember { mutableStateOf("AMIHA-2026") } // Pre-filled for seamless testing
    
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val earthGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F3DF), // Elegant soft green
            Color(0xFFF7FBF2)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentLang == AppLanguage.ENGLISH) "Staff Account Registration" else "સ્ટાફ એકાઉન્ટ રજીસ્ટ્રેશન",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular small decorative logo badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "AMIHA App Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (currentLang == AppLanguage.ENGLISH) "Register as AMIHA Team Member" else "AMIHA ટીમના સભ્ય તરીકે નોંધણી કરો",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (currentLang == AppLanguage.ENGLISH) {
                        "Gain secure app access to register organic growers, plot scopes, and record peer review compliance reports."
                    } else {
                        "ખેડૂતોની નોંધણી, જીપીએસ મેપિંગ અને કમ્પ્લાયન્સ રિપોર્ટ્સ રેકોર્ડ કરવા માટે સુરક્ષિત ઓન-ડિવાઈસ એક્સેસ મેળવો."
                    },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_registration_card"),
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
                            modifier = Modifier.fillMaxWidth().testTag("reg_fullName_input")
                        )

                        // ---- Email / Username ----
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it.trim()
                                errorMessage = null
                            },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "Email Address / Username" else "ઈમેઈલ આઈડી / યુઝરનેમ") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
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
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                        )

                        // ---- Phone Number ----
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "Contact Number (Optional)" else "મોબાઈલ નંબર") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ---- Select Role Segment ----
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (currentLang == AppLanguage.ENGLISH) "Assign System Role" else "સિસ્ટમ ભૂમિકા સોંપો",
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
                                        modifier = Modifier.weight(1f),
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

                        // ---- ICS clearance Token Validation ----
                        OutlinedTextField(
                            value = clearanceToken,
                            onValueChange = {
                                clearanceToken = it
                                errorMessage = null
                            },
                            label = { Text(if (currentLang == AppLanguage.ENGLISH) "ICS Authorization Clearance Token" else "ICS મંજૂરી ટોકન") },
                            leadingIcon = { Icon(Icons.Default.PublishedWithChanges, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            ),
                            supportingText = {
                                Text(
                                    text = if (currentLang == AppLanguage.ENGLISH) "Required to activate accounts locally. Default token: AMIHA-2026" else "સ્થાનિક સક્રિયકરણ માટે જરૂરી. ડિફોલ્ટ: AMIHA-2026",
                                    fontSize = 10.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("reg_token_input")
                        )

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val emailStr = email.trim().lowercase()
                                val firebaseAuth = com.example.data.FirebaseManager.auth
                                val firestore = com.example.data.FirebaseManager.firestore
                                when {
                                    fullName.isBlank() -> {
                                        errorMessage = "Please enter your full name."
                                    }
                                    email.isBlank() -> {
                                        errorMessage = "Please enter an email address / username."
                                    }
                                    password.length < 4 -> {
                                        errorMessage = "Password must be at least 4 characters."
                                    }
                                    clearanceToken != "AMIHA-2026" -> {
                                        errorMessage = "Invalid Agency Clearance Token."
                                    }
                                    firebaseAuth != null && firestore != null -> {
                                        errorMessage = null
                                        firebaseAuth.createUserWithEmailAndPassword(emailStr, password)
                                            .addOnSuccessListener { authResult ->
                                                val uid = authResult.user?.uid ?: ""
                                                val staffData = mapOf(
                                                    "fullName" to fullName,
                                                    "role" to selectedRole,
                                                    "phone" to phone,
                                                    "email" to emailStr
                                                )
                                                firestore.collection("staff").document(uid).set(staffData)
                                                    .addOnSuccessListener {
                                                        android.widget.Toast.makeText(context, "Staff Account Registered Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                                        showSuccessDialog = true
                                                    }
                                                    .addOnFailureListener { e ->
                                                        errorMessage = "Failed to save profile: ${e.message}"
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = e.localizedMessage ?: "Registration failed."
                                            }
                                    }
                                    else -> {
                                        if (registeredStaff.containsKey(emailStr)) {
                                            errorMessage = "An account with this email/username already exists."
                                        } else {
                                            val updatedStaff = registeredStaff.toMutableMap()
                                            updatedStaff[emailStr] = StaffProfile(fullName, selectedRole, password, phone)
                                            viewModel.registeredStaff.value = updatedStaff
                                            android.widget.Toast.makeText(context, "Staff Account Registered Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                            showSuccessDialog = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_registration_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.ENGLISH) "REGISTER ACCOUNT" else "એકાઉન્ટ રજીસ્ટર કરો",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ---- Modern M3 Success AlertDialog ----
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* Force explicit dismiss click */ },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        onClick = {
                            showSuccessDialog = false
                            onRegistrationSuccess()
                        }
                    ) {
                        Text("LOGIN NOW")
                    }
                },
                title = {
                    Text(
                        text = "Registration Successful!",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                },
                text = {
                    Text(
                        text = "Congratulations, $fullName! Your staff account has been securely registered as a $selectedRole. You can now use your credentials ($email) to sign into the AMIHA Internal Control System."
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(44.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}
