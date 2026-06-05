package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import com.example.ui.theme.StatusCertified
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuspended
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionFormScreen(
    viewModel: MainViewModel,
    farmerId: String,
    onNavigateBack: () -> Unit
) {
    val farmerDetailsFlow = viewModel.getFarmerByIdFlow(farmerId).collectAsState(initial = null)
    val farmerWithDetails = farmerDetailsFlow.value ?: return

    val currentLang by viewModel.selectedLanguage.collectAsState()
    val farmer = farmerWithDetails.farmer
    val inspectorName by viewModel.userName.collectAsState()
    val haptic = LocalHapticFeedback.current

    var inspectionResult by remember { mutableStateOf("Pass") } // Pass, Fail, Pending
    var observationNotes by remember { mutableStateOf("") }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString(currentLang, "log_farm_inspection"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Farmer Metadata Brief Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = Localization.getString(currentLang, "audit_target_farmer"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = farmer.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: ${farmer.farmerId}  •  ${Localization.getString(currentLang, "village")}: ${farmer.villageName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Inspection selection grid helper (FR-15)
            Text(
                text = Localization.getString(currentLang, "select_audit_result"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pass Choice Card
                InspectionChoiceCard(
                    title = Localization.getString(currentLang, "pass"),
                    subtitle = Localization.getString(currentLang, "full_complies"),
                    icon = Icons.Default.CheckCircle,
                    color = StatusCertified,
                    isSelected = inspectionResult == "Pass",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        inspectionResult = "Pass"
                    },
                    modifier = Modifier.weight(1f)
                )

                // Fail Choice Card
                InspectionChoiceCard(
                    title = Localization.getString(currentLang, "fail"),
                    subtitle = Localization.getString(currentLang, "red_flags"),
                    icon = Icons.Default.Cancel,
                    color = StatusSuspended,
                    isSelected = inspectionResult == "Fail",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        inspectionResult = "Fail"
                    },
                    modifier = Modifier.weight(1f)
                )

                // Pending Choice Card
                InspectionChoiceCard(
                    title = Localization.getString(currentLang, "pending").uppercase(),
                    subtitle = Localization.getString(currentLang, "need_verification"),
                    icon = Icons.Default.HourglassEmpty,
                    color = StatusPending,
                    isSelected = inspectionResult == "Pending",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        inspectionResult = "Pending"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Non-conformance Notes Text Input (FR-16, conditional highlights)
            AnimatedVisibility(
                visible = inspectionResult == "Fail",
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusSuspended.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    border = BorderStroke(
                        1.dp,
                        StatusSuspended.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Error, contentDescription = "Alert", tint = StatusSuspended)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = Localization.getString(currentLang, "critical_compliance_notice"),
                                fontWeight = FontWeight.Bold,
                                color = StatusSuspended,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Localization.getString(currentLang, "fail_notice_text"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = observationNotes,
                onValueChange = { observationNotes = it },
                label = { Text(if (inspectionResult == "Fail") Localization.getString(currentLang, "obs_notes_fail") else Localization.getString(currentLang, "obs_notes_optional")) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inspection_notes_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Metadata info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = Localization.getString(currentLang, "auditor_name"), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(inspectorName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = Localization.getString(currentLang, "audit_date"), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(currentDate, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Save Actions
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // Save result back to Room database
                    viewModel.saveInspectionResult(
                        farmerId = farmerId,
                        result = inspectionResult,
                        notes = observationNotes,
                        inspectorName = inspectorName
                    )
                    onNavigateBack() // Pop screen
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_inspection_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(14.dp),
                enabled = inspectionResult != "Fail" || observationNotes.isNotBlank()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Done, contentDescription = "Save Action")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.getString(currentLang, "submit_compliance_report"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InspectionChoiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick)
            .testTag("inspection_choice_$title"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 11.sp
            )
        }
    }
}
