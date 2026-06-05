package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import com.example.ui.theme.StatusCertified
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRegistered
import com.example.ui.theme.StatusSuspended

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    viewModel: MainViewModel,
    farmerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToInspection: (String) -> Unit
) {
    val farmerDetailsFlow = viewModel.getFarmerByIdFlow(farmerId).collectAsState(initial = null)
    val farmerWithDetails = farmerDetailsFlow.value ?: return

    val currentLang by viewModel.selectedLanguage.collectAsState()
    val farmer = farmerWithDetails.farmer
    val members = farmerWithDetails.householdMembers
    val plots = farmerWithDetails.plots
    val role by viewModel.userRole.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val statusColor = when (farmer.certificationStatus) {
        "Certified" -> StatusCertified
        "Under Inspection" -> StatusPending
        "Suspended" -> StatusSuspended
        else -> StatusRegistered
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(farmer.fullName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Export compliance report button (FR-26)
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.exportFarmerReport(farmerId, context) { fileName, success ->
                            coroutineScope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Exported compliance report to: $fileName")
                                } else {
                                    snackbarHostState.showSnackbar("Export failed: $fileName")
                                }
                            }
                        }
                    }, modifier = Modifier.testTag("export_report_button")) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Report", tint = PrimaryGreen)
                    }

                    // Field Officers can edit profiles (FR-26)
                    if (role == "Field Officer") {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToEdit(farmerId)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PrimaryGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            // Inspectors conduct inspections (FR-15, FR-26)
            if (role == "Inspector") {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToInspection(farmerId)
                    },
                    containerColor = SecondaryGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("start_inspection_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = "Inspect")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString(currentLang, "conduct_inspection"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // High Visibility Identity Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Placeholder
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape)
                            .border(1.5.dp, PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = farmer.fullName.take(1),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = farmer.fullName,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${Localization.getString(currentLang, "registration_id")}: ${farmer.farmerId}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            val statusKey = when (farmer.certificationStatus) {
                                "Certified" -> "certified"
                                "Under Inspection" -> "under_inspection"
                                "Suspended" -> "suspended"
                                else -> "registered"
                            }
                            Text(
                                text = Localization.getString(currentLang, statusKey).uppercase(),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Total land area and livestock count stats cards
            val totalArea = plots.sumOf { it.landArea }
            val totalLivestockCount = farmer.livestock.sumOf { it.count }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Land Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Landscape, contentDescription = "Land Area", tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "%.2f Ac".format(totalArea),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total Land Area",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Livestock Head Count Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SecondaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlaylistAddCheck, contentDescription = "Livestock", tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$totalLivestockCount Head",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Livestock Assets",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Sync Warning Block (FR-22)
            AnimatedVisibility(visible = farmer.syncStatus == "Pending") {
                Card(
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusPending.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, StatusPending.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Pending Sync", tint = StatusPending)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString(currentLang, "un_saved_warning"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // 1. Personal & Contact details
            SectionHeader(title = Localization.getString(currentLang, "primary_profile_identity"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    DetailRow(label = Localization.getString(currentLang, "dob_label"), value = farmer.dob)
                    DetailRow(label = "Gender", value = farmer.gender)
                    DetailRow(label = "National ID Number", value = farmer.nationalId)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${Localization.getString(currentLang, "mobile_label")}: ", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = farmer.primaryPhone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (farmer.secondaryPhone != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.width(22.dp))
                            Text(text = "Alt Phone: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text(text = farmer.secondaryPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (farmer.whatsappNumber != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.width(22.dp))
                            Text(text = "WhatsApp: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text(text = farmer.whatsappNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (farmer.email != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Email: ", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(text = farmer.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 2. Village & GPS details
            SectionHeader(title = Localization.getString(currentLang, "geographic_info"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    DetailRow(label = Localization.getString(currentLang, "village_location"), value = farmer.villageName)
                    DetailRow(label = Localization.getString(currentLang, "gram_panchayat"), value = farmer.gramPanchayat)
                    DetailRow(label = Localization.getString(currentLang, "taluka"), value = farmer.taluka)
                    DetailRow(label = Localization.getString(currentLang, "district"), value = farmer.district)
                    DetailRow(label = Localization.getString(currentLang, "state"), value = farmer.state)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = "GPS Map", modifier = Modifier.size(18.dp), tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${Localization.getString(currentLang, "geographic_gps")}: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "%.5f, %.5f".format(farmer.gpsLatitude, farmer.gpsLongitude),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            // 3. Household details (FR-07)
            SectionHeader(title = "${Localization.getString(currentLang, "household_members_sec")} (${members.size})")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (members.isEmpty()) {
                        Text(
                            text = "No household members registered currently.",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        members.forEachIndexed { i, m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(SecondaryGreen.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = (i + 1).toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = m.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${m.relationship} • ${m.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Text(
                                    text = "${m.age} Yrs",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (i < members.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            // 4. Plots details (FR-11 Map View)
            SectionHeader(title = "${Localization.getString(currentLang, "farm_plots_sec")} (${plots.size})")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (plots.isEmpty()) {
                        Text(
                            text = "No agricultural plots mapped for this farmer profile.",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Plot visualization drawing
                        Text(
                            text = "MAPPED PLOTS GEOMETRIC SHAPES (FR-11)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, PrimaryGreen, RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                plots.forEachIndexed { idx, p ->
                                    val startX = 50f + (idx * 220f) % (size.width - 220f)
                                    val startY = 40f + (idx * 20f) % (size.height - 60f)
                                    
                                    val drawPath = Path().apply {
                                        moveTo(startX, startY)
                                        lineTo(startX + 150f, startY + 10f)
                                        lineTo(startX + 130f, startY + 60f)
                                        lineTo(startX + 20f, startY + 50f)
                                        close()
                                    }
                                    
                                    drawPath(path = drawPath, color = SecondaryGreen.copy(alpha = 0.25f))
                                    drawPath(path = drawPath, color = PrimaryGreen, style = Stroke(width = 2.5f))
                                    
                                    // text positioning
                                    drawCircle(
                                        color = Color.White,
                                        radius = 8f,
                                        center = Offset(startX + 80f, startY + 30f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        plots.forEachIndexed { index, p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Landscape, contentDescription = "plot", modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "Plot ID: ${p.plotId}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Water: ${p.irrigationSource} • Soil: ${p.soilType ?: "N/A"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Text(
                                    text = "${p.landArea} ac (${p.ownershipType})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                            if (index < plots.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            // 5. Crops & Practices details (FR-12, FR-13)
            SectionHeader(title = Localization.getString(currentLang, "crops_farming_practices"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Custom Canvas Donut Chart for Crop Land Sharing
                    CropLandSharingChart(
                        primaryCrops = farmer.primaryCrops,
                        secondaryCrops = farmer.secondaryCrops,
                        totalArea = totalArea
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(text = "${Localization.getString(currentLang, "primary_crops_mapped")}: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (farmer.primaryCrops.isEmpty()) {
                        Text("None", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            farmer.primaryCrops.forEach { crop ->
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = crop, color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "${Localization.getString(currentLang, "intercrops_secondary")}: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (farmer.secondaryCrops.isEmpty()) {
                        Text("None", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            farmer.secondaryCrops.forEach { crop ->
                                Box(
                                    modifier = Modifier
                                        .background(SecondaryGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = crop, color = SecondaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    val localizedMethod = when (farmer.farmingMethod) {
                        "Organic" -> Localization.getString(currentLang, "organic")
                        "In Transition" -> Localization.getString(currentLang, "transitional")
                        else -> Localization.getString(currentLang, "conventional")
                    }
                    DetailRow(label = Localization.getString(currentLang, "farming_method"), value = localizedMethod)
                    DetailRow(label = Localization.getString(currentLang, "seeds_sourcing"), value = farmer.seedSource)
                    DetailRow(
                        label = Localization.getString(currentLang, "previous_yield"),
                        value = farmer.previousSeasonYield ?: Localization.getString(currentLang, "no_yield_logs")
                    )
                    DetailRow(
                        label = Localization.getString(currentLang, "is_chem_pesticides_used"),
                        value = if (farmer.useOfPesticides) Localization.getString(currentLang, "yes") else Localization.getString(currentLang, "no")
                    )
                    if (farmer.useOfPesticides) {
                        DetailRow(
                            label = Localization.getString(currentLang, "pesticide_log"),
                            value = farmer.pesticideDetails ?: Localization.getString(currentLang, "none_listed")
                        )
                    }
                    DetailRow(
                        label = Localization.getString(currentLang, "is_chem_fertilizers_used"),
                        value = if (farmer.useOfChemicalFertilizers) Localization.getString(currentLang, "yes") else Localization.getString(currentLang, "no")
                    )
                    if (farmer.useOfChemicalFertilizers) {
                        DetailRow(
                            label = Localization.getString(currentLang, "fertilizer_log"),
                            value = farmer.chemicalFertilizerDetails ?: Localization.getString(currentLang, "none_listed")
                        )
                    }
                }
            }

            // 6. Livestock inventory (FR-14)
            SectionHeader(title = Localization.getString(currentLang, "livestock_asset_details"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (farmer.livestock.isEmpty()) {
                        Text(
                            text = "No livestock or animals logged.",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        farmer.livestock.forEachIndexed { i, l ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = l.count.toString(), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = l.type, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Use: ${l.primaryUse}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Text(
                                    text = "Fodder: ${l.fodderSource}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (i < farmer.livestock.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            // 7. ICS Certification & Compliance notes (FR-15, FR-16)
            SectionHeader(title = Localization.getString(currentLang, "ics_group_compliance_record"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (farmer.inspectionResult == "Pass") Icons.Default.CheckCircle else Icons.Default.Description,
                            contentDescription = "compliance",
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString(currentLang, "certification_file"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val localizedInspectionResult = when (farmer.inspectionResult) {
                        "Pass" -> Localization.getString(currentLang, "pass")
                        "Fail" -> Localization.getString(currentLang, "fail")
                        else -> Localization.getString(currentLang, "pending_inspection")
                    }
                    DetailRow(label = Localization.getString(currentLang, "ics_group_name"), value = farmer.icsGroup)
                    DetailRow(label = Localization.getString(currentLang, "reg_mapped_date"), value = farmer.registrationDate)
                    DetailRow(
                        label = Localization.getString(currentLang, "last_active_inspection"),
                        value = farmer.lastInspectionDate ?: Localization.getString(currentLang, "never_inspected")
                    )
                    DetailRow(label = Localization.getString(currentLang, "inspection_result_status"), value = localizedInspectionResult)
                    DetailRow(
                        label = Localization.getString(currentLang, "next_scheduled_audit"),
                        value = farmer.nextScheduledInspectionDate ?: Localization.getString(currentLang, "unscheduled")
                    )

                    // Non conformance notes
                    if (farmer.nonConformanceNotes != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Localization.getString(currentLang, "red_flags_log"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = farmer.nonConformanceNotes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryGreen,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp, top = 10.dp)
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun CropLandSharingChart(primaryCrops: List<String>, secondaryCrops: List<String>, totalArea: Double) {
    val crops = (primaryCrops + secondaryCrops).distinct()
    if (crops.isEmpty() || totalArea <= 0.0) return

    val shares = remember(primaryCrops, secondaryCrops) {
        val list = mutableListOf<Pair<String, Double>>()
        val totalCropsCount = (primaryCrops.size + secondaryCrops.size).toDouble()
        if (totalCropsCount > 0) {
            val weights = mutableMapOf<String, Double>()
            primaryCrops.forEach { weights[it] = 1.5 }
            secondaryCrops.forEach { weights[it] = weights.getOrDefault(it, 0.0) + 1.0 }
            
            val totalWeight = weights.values.sum()
            weights.forEach { (crop, weight) ->
                list.add(crop to (weight / totalWeight))
            }
        }
        list
    }

    val colors = listOf(
        Color(0xFF2E7D32), // Dark Green
        Color(0xFF81C784), // Light Green
        Color(0xFFFFB74D), // Orange/Gold
        Color(0xFF4DB6AC), // Teal
        Color(0xFFFF8A65), // Coral
        Color(0xFFAED581)  // Lime
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                shares.forEachIndexed { index, (_, share) ->
                    val sweepAngle = (share * 360f).toFloat()
                    val color = colors[index % colors.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24f)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f".format(totalArea),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Acres",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            shares.forEachIndexed { index, (crop, share) ->
                val color = colors[index % colors.size]
                val areaValue = totalArea * share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = crop,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "%.1f ac (%.0f%%)".format(areaValue, share * 100),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
