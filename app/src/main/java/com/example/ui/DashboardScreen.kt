package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FarmerWithDetails
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import com.example.ui.theme.StatusCertified
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRegistered
import com.example.ui.theme.StatusSuspended
import com.example.ui.theme.TextEcoGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToAddFarmer: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToStaffProfile: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val farmers by viewModel.filteredFarmers.collectAsState()
    val allFarmersRaw by viewModel.allFarmers.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentRole by viewModel.userRole.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedVillage by viewModel.selectedVillage.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    val villagesList by viewModel.availableVillages.collectAsState()
    val cropsList by viewModel.availableCrops.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Aggregate key stats for FO and Inspector
    val totalFarmers = allFarmersRaw.size
    val pendingSyncCount = allFarmersRaw.count { it.farmer.syncStatus == "Pending" }
    val certifiedCount = allFarmersRaw.count { it.farmer.certificationStatus == "Certified" }
    val underInspectionCount = allFarmersRaw.count { it.farmer.certificationStatus == "Under Inspection" }

    Scaffold(
        floatingActionButton = {
            // ONLY Field Officers can register new farmers! (FR-26)
            if (currentRole == "Field Officer") {
                Button(
                    onClick = onNavigateToAddFarmer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("add_farmer_fab")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(PrimaryGreen, SecondaryGreen)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Farmer", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.getString(currentLang, "add_farmer"),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .testTag("dashboard_root")
        ) {
            // 1. Theme Header (Professional Polish Design)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Branding App Logo
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "AMIHA App Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        )

                        // App Title & Role Mode Subtitle
                        Column {
                            Text(
                                text = Localization.getString(currentLang, "app_title"),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Green pulse/status dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF22C55E), CircleShape)
                                )
                                Text(
                                    text = if (currentRole == "Field Officer") {
                                        Localization.getString(currentLang, "field_officer_mode")
                                    } else {
                                        Localization.getString(currentLang, "inspector_mode")
                                    },
                                    color = TextEcoGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Top Right Utilities (Manual sync trigger & Logout)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val context = LocalContext.current

                        // Export Farmers Button
                        IconButton(
                            onClick = {
                                viewModel.exportAllFarmersReport(context) { fileName, success ->
                                    val message = if (success) {
                                        "Report saved: $fileName in Documents folder"
                                    } else {
                                        "Export failed: $fileName"
                                    }
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export All Farmers",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Edit Staff Profile Button
                        IconButton(
                            onClick = onNavigateToStaffProfile,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                                .testTag("staff_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Edit Profile",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Queue Sync Status Button
                        IconButton(
                            onClick = { viewModel.triggerManualSync() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    color = PrimaryGreen,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Sync records",
                                        tint = TextEcoGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    if (pendingSyncCount > 0) {
                                        Badge(
                                            modifier = Modifier.align(Alignment.TopEnd),
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text(pendingSyncCount.toString(), color = Color.White, fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Analytical Dashboard stats summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = Localization.getString(currentLang, "total_farmers"),
                    value = totalFarmers.toString(),
                    icon = Icons.Default.Group,
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = Localization.getString(currentLang, "pending_syncs"),
                    value = pendingSyncCount.toString(),
                    icon = Icons.Default.CloudQueue,
                    color = if (pendingSyncCount > 0) StatusPending else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = Localization.getString(currentLang, "certified_farmers"),
                    value = certifiedCount.toString(),
                    icon = Icons.Default.VerifiedUser,
                    color = StatusCertified,
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive Search & Advanced Filter Section (No-Line & Glass-inspired)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text(Localization.getString(currentLang, "search_farmer_placeholder")) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon", tint = PrimaryGreen) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = PrimaryGreen)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen.copy(alpha = 0.3f),
                                focusedLabelColor = PrimaryGreen,
                                unfocusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color(0xFFF0F4EE),
                                focusedContainerColor = Color(0xFFE8F0E5)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Toggle button for advanced filters
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (showFilters) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (showFilters) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Collapsible Advanced dropdown filters (FR-19)
                    AnimatedVisibility(
                        visible = showFilters,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = "Filter By Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Village Dropdown
                                FilterDropdown(
                                    label = "Village",
                                    selectedValue = selectedVillage,
                                    onSelectedChange = { viewModel.selectedVillage.value = it },
                                    options = listOf("All") + villagesList,
                                    modifier = Modifier.weight(1f)
                                )

                                // Crop Dropdown
                                FilterDropdown(
                                    label = "Crop",
                                    selectedValue = selectedCrop,
                                    onSelectedChange = { viewModel.selectedCrop.value = it },
                                    options = listOf("All") + cropsList,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Certification Status Dropdown
                                FilterDropdown(
                                    label = "Status",
                                    selectedValue = selectedStatus,
                                    onSelectedChange = { viewModel.selectedStatus.value = it },
                                    options = listOf("All", "Registered", "Under Inspection", "Certified", "Suspended"),
                                    modifier = Modifier.weight(1.5f)
                                )

                                // Reset Button (No-Line secondary styling)
                                Button(
                                    onClick = {
                                        viewModel.selectedVillage.value = "All"
                                        viewModel.selectedCrop.value = "All"
                                        viewModel.selectedStatus.value = "All"
                                        viewModel.searchQuery.value = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryGreen.copy(alpha = 0.08f),
                                        contentColor = PrimaryGreen
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reset", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Farmers list
            if (farmers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Agriculture,
                            contentDescription = "No farmers",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Farm Records Found",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try clearing tags or registering a new farmer.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(farmers, key = { it.farmer.farmerId }) { farmerDetails ->
                        FarmerCard(
                            farmerDetails = farmerDetails,
                            currentLang = currentLang,
                            onClick = { onNavigateToProfile(farmerDetails.farmer.farmerId) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isPendingSync = title.contains("Pending")
    val hasPending = isPendingSync && value != "0"
    
    val containerBg = when {
        hasPending -> StatusPending.copy(alpha = 0.12f)
        title.contains("Total") || title.contains("કુલ") -> PrimaryGreen.copy(alpha = 0.08f)
        title.contains("Certified") || title.contains("પ્રમાણિત") -> StatusCertified.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val textAndIconColor = when {
        hasPending -> StatusPending
        title.contains("Total") || title.contains("કુલ") -> PrimaryGreen
        title.contains("Certified") || title.contains("પ્રમાણિત") -> StatusCertified
        else -> color
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(if (hasPending) 1.dp else 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textAndIconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textAndIconColor,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textAndIconColor.copy(alpha = 0.85f),
                lineHeight = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String,
    onSelectedChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen.copy(alpha = 0.3f),
                focusedLabelColor = PrimaryGreen,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color(0xFFF0F4EE),
                focusedContainerColor = Color(0xFFE8F0E5)
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selection ->
                DropdownMenuItem(
                    text = { Text(selection) },
                    onClick = {
                        onSelectedChange(selection)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FarmerCard(
    farmerDetails: FarmerWithDetails,
    currentLang: AppLanguage,
    onClick: () -> Unit
) {
    val farmer = farmerDetails.farmer
    val plotCount = farmerDetails.plots.size
    val totalAcres = farmerDetails.plots.sumOf { it.landArea }

    val statusColor = when (farmer.certificationStatus) {
        "Certified" -> StatusCertified
        "Under Inspection" -> StatusPending
        "Suspended" -> StatusSuspended
        else -> StatusRegistered
    }

    val (avatarBg, avatarText) = remember(farmer.fullName) {
        val hash = farmer.fullName.hashCode().coerceAtLeast(0)
        val pairs = listOf(
            Color(0xFFEFF6FF) to Color(0xFF1E40AF), // Blue-50 and Blue-800
            Color(0xFFFEF3C7) to Color(0xFF92400E), // Amber-50 and Amber-800
            Color(0xFFECFDF5) to Color(0xFF065F46), // Green-50 and Green-800
            Color(0xFFF5F3FF) to Color(0xFF5B21B6), // Violet-50 and Violet-800
            Color(0xFFFFF1F2) to Color(0xFF9F1239)  // Rose-50 and Rose-800
        )
        pairs[hash % pairs.size]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("farmer_card_${farmer.farmerId}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp), // Soft premium depth drop shadow
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // Forces the left status bar to match the content height
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Soul: 4px status colored vertical accent bar on the left edge
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left block with letter avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(avatarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (farmer.fullName.isNotEmpty()) farmer.fullName.substring(0, 1) else "F",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarText
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Body info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = farmer.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                        
                        // Sync Status Indicator (FR-22)
                        SyncIndicator(status = farmer.syncStatus, currentLang = currentLang)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(14.dp),
                            tint = TextEcoGray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${farmer.villageName}, ${farmer.state}",
                            fontSize = 12.sp,
                            color = TextEcoGray
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val localizedMethod = when (farmer.farmingMethod) {
                        "Organic" -> Localization.getString(currentLang, "organic")
                        "In Transition" -> Localization.getString(currentLang, "transitional")
                        else -> Localization.getString(currentLang, "conventional")
                    }

                    Text(
                        text = "ID: ${farmer.farmerId}  •  ${plotCount} ${Localization.getString(currentLang, "crops").lowercase()} (${totalAcres} ac)  •  $localizedMethod",
                        fontSize = 11.sp,
                        color = TextEcoGray.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Highlegible colored compliance pill (FR-17)
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val statusKey = when (farmer.certificationStatus) {
                            "Certified" -> "certified"
                            "Under Inspection" -> "under_inspection"
                            "Suspended" -> "suspended"
                            else -> "registered"
                        }
                        val transStatus = Localization.getString(currentLang, statusKey)
                        Text(
                            text = transStatus.uppercase(),
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SyncIndicator(status: String, currentLang: AppLanguage) {
    val animateStatusColor by animateColorAsState(
        targetValue = when (status) {
            "Synced" -> StatusCertified
            "Pending" -> StatusPending
            else -> StatusSuspended
        }, label = "SyncStatusIndicator"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = when (status) {
                "Synced" -> Icons.Default.CloudDone
                "Pending" -> Icons.Default.Loop // spin?
                else -> Icons.Default.ErrorOutline
            },
            contentDescription = "Sync State: $status",
            tint = animateStatusColor,
            modifier = Modifier.size(16.dp)
        )
        val statusText = when (status) {
            "Synced" -> "Synced"
            "Pending" -> Localization.getString(currentLang, "pending")
            else -> status
        }
        Text(
            text = statusText,
            color = animateStatusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
