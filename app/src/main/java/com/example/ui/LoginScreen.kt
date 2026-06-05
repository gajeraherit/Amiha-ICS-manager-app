package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.LightBackground
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAnimated by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isAnimated = true
    }

    // Dynamic gradient backgrounds for earth aesthetic (Professional Polish)
    val earthGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE2F0D9), // Soft elegant herbal green
            LightBackground,   // Theme Background
            LightBackground
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(earthGradient)
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryGreen
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isAnimated,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -50 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Localization.getString(currentLang, "login_title"),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        letterSpacing = 2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Localization.getString(currentLang, "login_subtitle"),
                        fontSize = 13.sp,
                        color = Color(0xFF4A554A), // TextEcoGray
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium Pill Language Selector (No-Line)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = currentLang == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) PrimaryGreen else Color.Transparent)
                                    .clickable { viewModel.selectedLanguage.value = lang }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lang.nativeName,
                                    color = if (isSelected) Color.White else Color(0xFF4A554A),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isAnimated,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp), // Tonal depth / No shadow border
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Localization.getString(currentLang, "staff_auth"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Username Entry - Filled modern custom style (No-Line)
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text(Localization.getString(currentLang, "username_label")) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "EmailIcon", tint = PrimaryGreen)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen.copy(alpha = 0.3f),
                                focusedLabelColor = PrimaryGreen,
                                unfocusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color(0xFFF0F4EE),
                                focusedContainerColor = Color(0xFFE8F0E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input")
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Entry - Filled modern custom style (No-Line)
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text(Localization.getString(currentLang, "password_label")) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "PasswordIcon", tint = PrimaryGreen)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = PrimaryGreen
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen.copy(alpha = 0.3f),
                                focusedLabelColor = PrimaryGreen,
                                unfocusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color(0xFFF0F4EE),
                                focusedContainerColor = Color(0xFFE8F0E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input")
                        )

                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Main Sign-In Button with Premium Gradient style
                        Button(
                            onClick = {
                                val emailStr = email.trim().lowercase()
                                val firebaseAuth = com.example.data.FirebaseManager.auth
                                val firestore = com.example.data.FirebaseManager.firestore
                                when {
                                    email.isBlank() -> {
                                        errorMessage = if (currentLang == AppLanguage.ENGLISH) "Please enter an email address." else "કૃપા કરીને ઈમેઈલ આઈડી દાખલ કરો."
                                    }
                                    firebaseAuth != null && firestore != null -> {
                                        errorMessage = null
                                        firebaseAuth.signInWithEmailAndPassword(emailStr, password)
                                            .addOnSuccessListener { authResult ->
                                                val uid = authResult.user?.uid ?: ""
                                                firestore.collection("staff").document(uid).get()
                                                    .addOnSuccessListener { doc ->
                                                        val fullName = doc.getString("fullName") ?: "Staff Member"
                                                        val role = doc.getString("role") ?: "Field Officer"
                                                        viewModel.login(emailStr, fullName, role)
                                                        android.widget.Toast.makeText(context, "Login Successful!", android.widget.Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess()
                                                    }
                                                    .addOnFailureListener {
                                                        viewModel.login(emailStr, "Team Member", "Field Officer")
                                                        android.widget.Toast.makeText(context, "Login Successful!", android.widget.Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess()
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                val match = viewModel.registeredStaff.value[emailStr]
                                                if (match != null && match.password == password) {
                                                    viewModel.login(emailStr, match.fullName, match.role)
                                                    android.widget.Toast.makeText(context, "Login Successful!", android.widget.Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                } else {
                                                    errorMessage = e.localizedMessage ?: "Authentication failed."
                                                }
                                            }
                                    }
                                    else -> {
                                        val match = viewModel.registeredStaff.value[emailStr]
                                        when {
                                            match == null -> {
                                                errorMessage = if (currentLang == AppLanguage.ENGLISH) "No staff account registered with this email." else "આ ઈમેઈલ આઈડી સાથે કોઈ સ્ટાફ એકાઉન્ટ રજીસ્ર થયેલ નથી."
                                            }
                                            match.password != password -> {
                                                errorMessage = if (currentLang == AppLanguage.ENGLISH) "Incorrect password. Please try again." else "ખોટો પાસવર્ડ. કૃપા કરીને ફરી પ્રયાસ કરો."
                                            }
                                            else -> {
                                                viewModel.login(emailStr, match.fullName, match.role)
                                                android.widget.Toast.makeText(context, "Login Successful!", android.widget.Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(PrimaryGreen, SecondaryGreen)
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Localization.getString(currentLang, "sign_in_btn"),
                                    fontSize = 16.sp,
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
}

