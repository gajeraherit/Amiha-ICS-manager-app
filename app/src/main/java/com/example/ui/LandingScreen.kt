package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import com.example.ui.theme.LightBackground
import com.example.ui.theme.TertiaryAmber

@Composable
fun LandingScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    var isAnimated by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isAnimated = true
    }

    // High-end vibrant earthy gradient
    val earthGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE2F0D9), // Soft minty green
            LightBackground,   // Theme LightBackground
            Color.White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(earthGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ---- Header Spacer (Language selector removed) ----
            Spacer(modifier = Modifier.height(32.dp))

            // ---- Body Block: Branding Logo and Typography ----
            AnimatedVisibility(
                visible = isAnimated,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -60 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Logo Box with ambient styling (No-Line)
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(28.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "AMIHA App Logo",
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "AMIHA",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        letterSpacing = 4.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )

                    Text(
                        text = "INTERNAL CONTROL SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGreen,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 2.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ---- Footer Segment: Navigation Choices ----
            AnimatedVisibility(
                visible = isAnimated,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Premium Gradient Sign In Button (PrimaryGreen to TertiaryAmber)
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("landing_signin_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PrimaryGreen, TertiaryAmber)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = Localization.getString(currentLang, "sign_in_btn") ?: "SIGN IN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Register Device/User - Custom modern secondary button (No-Line)
                    Button(
                        onClick = onNavigateToRegister,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen.copy(alpha = 0.08f),
                            contentColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("landing_register_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentInd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == AppLanguage.ENGLISH) "Register Staff Account" else "સ્ટાફ એકાઉન્ટ રજીસ્ટર કરો",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Footer trademark / security context
                    Text(
                        text = "Internal Control System (ICS) Standards • Certified Organic",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
