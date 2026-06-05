package com.example.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FarmPlotEntity
import com.example.data.FarmerEntity
import com.example.data.HouseholdMemberEntity
import com.example.data.LivestockItem
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: MainViewModel,
    editFarmerId: String?,
    onNavigateBack: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val haptic = LocalHapticFeedback.current
    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        Localization.getString(currentLang, "personal_contact_tab"),
        Localization.getString(currentLang, "village_gps_tab"),
        Localization.getString(currentLang, "household_info_tab"),
        Localization.getString(currentLang, "plots_tab"),
        Localization.getString(currentLang, "crops_animals_tab")
    )

    // Personal & Contact States
    var fullName by remember { mutableStateOf("") }
    var farmerId by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1980-01-01") }
    var gender by remember { mutableStateOf("Male") }
    var nationalId by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var primaryPhone by remember { mutableStateOf("") }
    var secondaryPhone by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var primaryPhoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var nationalIdError by remember { mutableStateOf<String?>(null) }

    // Village & Location States
    var villageName by remember { mutableStateOf("") }
    var gramPanchayat by remember { mutableStateOf("") }
    var taluka by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var gpsLatitude by remember { mutableStateOf(0.0) }
    var gpsLongitude by remember { mutableStateOf(0.0) }
    var isGpsCaptured by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Camera image capture launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = File(context.cacheDir, "farmer_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                profilePhotoUri = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            android.widget.Toast.makeText(context, "Camera permission is required to take photos", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Fused Location Client capture launchers
    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

    fun fetchRealLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // Attempt to get last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    gpsLatitude = loc.latitude
                    gpsLongitude = loc.longitude
                    isGpsCaptured = true
                    android.widget.Toast.makeText(context, "GPS Coordinates locked!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    // Fallback to simulated location immediately if last location is null (common on emulators)
                    gpsLatitude = 16.2142 + Random.nextDouble(-0.005, 0.005)
                    gpsLongitude = 74.8211 + Random.nextDouble(-0.005, 0.005)
                    isGpsCaptured = true
                    android.widget.Toast.makeText(context, "GPS Coordinates locked (Simulated)!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Fallback to simulated location on failure
                gpsLatitude = 16.2142 + Random.nextDouble(-0.005, 0.005)
                gpsLongitude = 74.8211 + Random.nextDouble(-0.005, 0.005)
                isGpsCaptured = true
                android.widget.Toast.makeText(context, "GPS Coordinates locked (Simulated)!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchRealLocation()
        } else {
            android.widget.Toast.makeText(context, "Location permission is required to capture GPS", android.widget.Toast.LENGTH_SHORT).show()
        }
    }


    // Crops & Practices States
    var primaryCropsSelected by remember { mutableStateOf(setOf<String>()) }
    val cropOptions = listOf("Cotton", "Rice", "Wheat", "Sugarcane", "Sorghum", "Millet", "Pigeon Pea", "Lentils", "Mustard")
    var secondaryCropsSelected by remember { mutableStateOf(setOf<String>()) }
    var farmingMethod by remember { mutableStateOf("Organic") }
    var useOfPesticides by remember { mutableStateOf(false) }
    var pesticideDetails by remember { mutableStateOf("") }
    var useOfChemicalFertilizers by remember { mutableStateOf(false) }
    var chemicalFertilizerDetails by remember { mutableStateOf("") }
    var seedSource by remember { mutableStateOf("Own Saved") }
    val seedSourcesList = listOf("Own Saved", "Purchased", "Government")
    var previousSeasonYield by remember { mutableStateOf("") }
    var icsGroup by remember { mutableStateOf("District Coop Cluster") }

    // Lists logic
    var householdMembers by remember { mutableStateOf(listOf<HouseholdMemberEntity>()) }
    var farmPlots by remember { mutableStateOf(listOf<FarmPlotEntity>()) }
    var livestockList by remember { mutableStateOf(listOf<LivestockItem>()) }

    var isEditMode by remember { mutableStateOf(false) }
    var registrationDate by remember { mutableStateOf("") }
    var certificationStatus by remember { mutableStateOf("Registered") }
    var lastInspectionDate by remember { mutableStateOf<String?>(null) }
    var lastInspectionResult by remember { mutableStateOf<String?>(null) }
    var nonConformanceNotes by remember { mutableStateOf<String?>(null) }
    var nextScheduledInspectionDate by remember { mutableStateOf<String?>(null) }

    // Populate data if in EDIT mode
    LaunchedEffect(editFarmerId) {
        if (editFarmerId != null) {
            isEditMode = true
            val data = viewModel.allFarmers.value.find { it.farmer.farmerId == editFarmerId }
            if (data != null) {
                val f = data.farmer
                fullName = f.fullName
                farmerId = f.farmerId
                dob = f.dob
                gender = f.gender
                nationalId = f.nationalId
                profilePhotoUri = f.profilePhotoUri
                primaryPhone = f.primaryPhone
                secondaryPhone = f.secondaryPhone ?: ""
                whatsappNumber = f.whatsappNumber ?: ""
                email = f.email ?: ""
                villageName = f.villageName
                gramPanchayat = f.gramPanchayat
                taluka = f.taluka
                district = f.district
                state = f.state
                gpsLatitude = f.gpsLatitude
                gpsLongitude = f.gpsLongitude
                isGpsCaptured = (f.gpsLatitude != 0.0)

                primaryCropsSelected = f.primaryCrops.toSet()
                secondaryCropsSelected = f.secondaryCrops.toSet()
                farmingMethod = f.farmingMethod
                useOfPesticides = f.useOfPesticides
                pesticideDetails = f.pesticideDetails ?: ""
                useOfChemicalFertilizers = f.useOfChemicalFertilizers
                chemicalFertilizerDetails = f.chemicalFertilizerDetails ?: ""
                seedSource = f.seedSource
                previousSeasonYield = f.previousSeasonYield ?: ""
                icsGroup = f.icsGroup

                householdMembers = data.householdMembers
                farmPlots = data.plots
                livestockList = f.livestock

                registrationDate = f.registrationDate
                certificationStatus = f.certificationStatus
                lastInspectionDate = f.lastInspectionDate
                lastInspectionResult = f.inspectionResult
                nonConformanceNotes = f.nonConformanceNotes
                nextScheduledInspectionDate = f.nextScheduledInspectionDate
            }
        } else {
            // New Farmer setup: auto-generate unique registration code
            val randNum = Random.nextInt(1000, 9999)
            farmerId = "FO-26-$randNum"
            registrationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = if (isEditMode) Localization.getString(currentLang, "edit_farmer_rec") else Localization.getString(currentLang, "new_farmer_reg"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
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
        ) {
            // Form Tabs Navigation (Categories Row)
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryGreen,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    0 -> PersonalTab(
                        currentLang = currentLang,
                        fullName = fullName, onFullNameChange = { fullName = it },
                        fullNameError = fullNameError,
                        farmerId = farmerId, onFarmerIdChange = { farmerId = it },
                        dob = dob, onDobChange = { dob = it },
                        gender = gender, onGenderChange = { gender = it },
                        nationalId = nationalId, onNationalIdChange = { nationalId = it },
                        nationalIdError = nationalIdError,
                        profilePhotoUri = profilePhotoUri,
                        onCapturePhoto = {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        primaryPhone = primaryPhone, onPrimaryPhoneChange = { primaryPhone = it },
                        primaryPhoneError = primaryPhoneError,
                        secondaryPhone = secondaryPhone, onSecondaryPhoneChange = { secondaryPhone = it },
                        whatsappNumber = whatsappNumber, onWhatsappChange = { whatsappNumber = it },
                        email = email, onEmailChange = { email = it },
                        emailError = emailError
                    )

                    1 -> LocationTab(
                        currentLang = currentLang,
                        villageName = villageName, onVillageNameChange = { villageName = it },
                        gramPanchayat = gramPanchayat, onGramChange = { gramPanchayat = it },
                        taluka = taluka, onTalukaChange = { taluka = it },
                        district = district, onDistrictChange = { district = it },
                        state = state, onStateChange = { state = it },
                        latitude = gpsLatitude, longitude = gpsLongitude,
                        isCaptured = isGpsCaptured,
                        onCaptureGps = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                                androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                fetchRealLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )

                    2 -> HouseholdTab(
                        currentLang = currentLang,
                        members = householdMembers,
                        onAddMember = { m -> householdMembers = householdMembers + m },
                        onRemoveMember = { idx -> householdMembers = householdMembers.filterIndexed { i, _ -> i != idx } }
                    )

                    3 -> PlotsTab(
                        currentLang = currentLang,
                        plots = farmPlots,
                        onAddPlot = { p -> farmPlots = farmPlots + p },
                        onRemovePlot = { idx -> farmPlots = farmPlots.filterIndexed { i, _ -> i != idx } }
                    )

                    4 -> CropsPracticesTab(
                        currentLang = currentLang,
                        cropsOptions = cropOptions,
                        primarySelected = primaryCropsSelected,
                        onPrimaryToggle = { primaryCropsSelected = if (primaryCropsSelected.contains(it)) primaryCropsSelected - it else primaryCropsSelected + it },
                        secondarySelected = secondaryCropsSelected,
                        onSecondaryToggle = { secondaryCropsSelected = if (secondaryCropsSelected.contains(it)) secondaryCropsSelected - it else secondaryCropsSelected + it },
                        farmingMethod = farmingMethod, onMethodChange = { farmingMethod = it },
                        usePesticides = useOfPesticides, onPesticidesToggle = { useOfPesticides = it },
                        pesticideDetails = pesticideDetails, onPesticideDetailChange = { pesticideDetails = it },
                        useFertilizers = useOfChemicalFertilizers, onFertilizersToggle = { useOfChemicalFertilizers = it },
                        fertilizerDetails = chemicalFertilizerDetails, onFertilizerDetailChange = { chemicalFertilizerDetails = it },
                        seedSource = seedSource, onSeedChange = { seedSource = it },
                        previousSeasonYield = previousSeasonYield, onYieldChange = { previousSeasonYield = it },
                        icsGroup = icsGroup, onIcsGroupChange = { icsGroup = it },
                        livestock = livestockList,
                        onAddLivestock = { l -> livestockList = livestockList + l },
                        onRemoveLivestock = { idx -> livestockList = livestockList.filterIndexed { i, _ -> i != idx } }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Core control button flows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (activeTab > 0) {
                        OutlinedButton(
                            onClick = { activeTab -= 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Localization.getString(currentLang, "previous"))
                        }
                    }

                    if (activeTab < 4) {
                        Button(
                            onClick = { activeTab += 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(start = if (activeTab > 0) 8.dp else 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Localization.getString(currentLang, "next_sec"))
                        }
                    } else {
                        // Submit Farmer action (FR-02 compliance validation checks)
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                fullNameError = if (fullName.isBlank() || fullName.trim().length < 3) "Name must be at least 3 characters" else null
                                primaryPhoneError = if (primaryPhone.isBlank() || !primaryPhone.trim().replace(" ", "").replace("+91", "").matches(Regex("^\\d{10}$"))) "Phone number must be exactly 10 digits" else null
                                emailError = if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Invalid email format" else null
                                nationalIdError = if (nationalId.isNotBlank() && !nationalId.trim().replace("-", "").replace(" ", "").matches(Regex("^\\d{12}$"))) "Aadhaar must be a 12-digit number" else null

                                if (fullNameError != null || primaryPhoneError != null || emailError != null || nationalIdError != null) {
                                    activeTab = 0
                                } else if (villageName.isBlank()) {
                                    activeTab = 1
                                } else {
                                    val localFarmer = FarmerEntity(
                                        farmerId = farmerId,
                                        fullName = fullName,
                                        dob = dob,
                                        gender = gender,
                                        nationalId = nationalId,
                                        profilePhotoUri = profilePhotoUri,
                                        primaryPhone = primaryPhone,
                                        secondaryPhone = secondaryPhone.takeIf { it.isNotBlank() },
                                        whatsappNumber = whatsappNumber.takeIf { it.isNotBlank() },
                                        email = email.takeIf { it.isNotBlank() },
                                        villageName = villageName,
                                        gramPanchayat = gramPanchayat,
                                        taluka = taluka,
                                        district = district,
                                        state = state,
                                        gpsLatitude = gpsLatitude,
                                        gpsLongitude = gpsLongitude,
                                        primaryCrops = primaryCropsSelected.toList(),
                                        secondaryCrops = secondaryCropsSelected.toList(),
                                        farmingMethod = farmingMethod,
                                        useOfPesticides = useOfPesticides,
                                        pesticideDetails = pesticideDetails.takeIf { it.isNotBlank() },
                                        useOfChemicalFertilizers = useOfChemicalFertilizers,
                                        chemicalFertilizerDetails = chemicalFertilizerDetails.takeIf { it.isNotBlank() },
                                        seedSource = seedSource,
                                        previousSeasonYield = previousSeasonYield.takeIf { it.isNotBlank() },
                                        livestock = livestockList,
                                        icsGroup = icsGroup,
                                        registrationDate = registrationDate,
                                        certificationStatus = certificationStatus,
                                        lastInspectionDate = lastInspectionDate,
                                        inspectionResult = lastInspectionResult,
                                        nonConformanceNotes = nonConformanceNotes,
                                        nextScheduledInspectionDate = nextScheduledInspectionDate,
                                        syncStatus = "Pending" // Set status queue for auto/manual backup (FR-21)
                                    )

                                    viewModel.saveFarmer(localFarmer, householdMembers, farmPlots)
                                    android.widget.Toast.makeText(context, "Farmer Registered Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .padding(start = 8.dp)
                                .testTag("submit_farmer_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Done, contentDescription = "Register")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.getString(currentLang, "complete_sync"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Personal Info Category
@Composable
fun PersonalTab(
    currentLang: AppLanguage,
    fullName: String, onFullNameChange: (String) -> Unit,
    fullNameError: String?,
    farmerId: String, onFarmerIdChange: (String) -> Unit,
    dob: String, onDobChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    nationalId: String, onNationalIdChange: (String) -> Unit,
    nationalIdError: String?,
    profilePhotoUri: String?, onCapturePhoto: () -> Unit,
    primaryPhone: String, onPrimaryPhoneChange: (String) -> Unit,
    primaryPhoneError: String?,
    secondaryPhone: String, onSecondaryPhoneChange: (String) -> Unit,
    whatsappNumber: String, onWhatsappChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    emailError: String?
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(Localization.getString(currentLang, "personal_profile_identity"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Real photo capture section (FR-03)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(1.dp, PrimaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUri != null) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = "Farmer Profile Pic",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Cam", modifier = Modifier.size(28.dp), tint = PrimaryGreen)
                    }
                }
                Column {
                    Text("Farmer Profile Pic", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onCapturePhoto,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Take Profile Photo", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text(Localization.getString(currentLang, "full_name_label") + " *") },
                isError = fullNameError != null,
                supportingText = { fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) } },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_full_name"),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = farmerId,
                onValueChange = onFarmerIdChange,
                label = { Text(Localization.getString(currentLang, "registration_id")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = onDobChange,
                label = { Text(Localization.getString(currentLang, "dob_label") + " (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Gender Segmented Selection
            Text("Gender Selector", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("Male", "Female", "Other").forEach { choice ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == choice, onClick = { onGenderChange(choice) })
                        Text(choice)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nationalId,
                onValueChange = onNationalIdChange,
                label = { Text("National ID / Aadhaar Number") },
                isError = nationalIdError != null,
                supportingText = { nationalIdError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(Localization.getString(currentLang, "personal_contact_tab"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = primaryPhone,
                onValueChange = onPrimaryPhoneChange,
                label = { Text(Localization.getString(currentLang, "mobile_label") + " *") },
                isError = primaryPhoneError != null,
                supportingText = { primaryPhoneError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = secondaryPhone,
                onValueChange = onSecondaryPhoneChange,
                label = { Text("Secondary Phone (Optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = whatsappNumber,
                onValueChange = onWhatsappChange,
                label = { Text("WhatsApp Number (Optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email Address") },
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

// Village & Geo-Coordinate location captures
@Composable
fun LocationTab(
    currentLang: AppLanguage,
    villageName: String, onVillageNameChange: (String) -> Unit,
    gramPanchayat: String, onGramChange: (String) -> Unit,
    taluka: String, onTalukaChange: (String) -> Unit,
    district: String, onDistrictChange: (String) -> Unit,
    state: String, onStateChange: (String) -> Unit,
    latitude: Double, longitude: Double,
    isCaptured: Boolean,
    onCaptureGps: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(Localization.getString(currentLang, "geographic_info"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = villageName,
                onValueChange = onVillageNameChange,
                label = { Text(Localization.getString(currentLang, "village_label") + " *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gramPanchayat,
                onValueChange = onGramChange,
                label = { Text(Localization.getString(currentLang, "gram_panchayat")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = taluka,
                onValueChange = onTalukaChange,
                label = { Text(Localization.getString(currentLang, "taluka")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = district,
                onValueChange = onDistrictChange,
                label = { Text(Localization.getString(currentLang, "district")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state,
                onValueChange = onStateChange,
                label = { Text(Localization.getString(currentLang, "state")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(Localization.getString(currentLang, "geographic_gps"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCaptured) SecondaryGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isCaptured) Localization.getString(currentLang, "gps_coords_label") + " (Locked)" else "No GPS Captured",
                                fontWeight = FontWeight.Bold,
                                color = if (isCaptured) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lat: ${if (isCaptured) "%.5f".format(latitude) else "0.0"}  •  Lon: ${if (isCaptured) "%.5f".format(longitude) else "0.0"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onCaptureGps,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LocationSearching, contentDescription = "GPS Lock", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.getString(currentLang, "grab_gps_btn"), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Household Information Tab
@Composable
fun HouseholdTab(
    currentLang: AppLanguage,
    members: List<HouseholdMemberEntity>,
    onAddMember: (HouseholdMemberEntity) -> Unit,
    onRemoveMember: (Int) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var memberRelationship by remember { mutableStateOf("Spouse") }
    var memberAge by remember { mutableStateOf("") }
    var memberGender by remember { mutableStateOf("Female") }
    var memberRole by remember { mutableStateOf("Farming") }

    val relationshipsList = listOf("Spouse", "Son", "Daughter", "Parent", "Sibling", "Other")

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(Localization.getString(currentLang, "household_members_sec"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Inline entry list form
            OutlinedTextField(
                value = memberName,
                onValueChange = { memberName = it },
                label = { Text(Localization.getString(currentLang, "member_name")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = memberAge,
                    onValueChange = { memberAge = it },
                    label = { Text(Localization.getString(currentLang, "age")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Box(modifier = Modifier.weight(1.5f)) {
                    Column {
                        Text(Localization.getString(currentLang, "relationship"), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            relationshipsList.take(3).forEach { rel ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (memberRelationship == rel) PrimaryGreen else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(1.dp, PrimaryGreen, RoundedCornerShape(6.dp))
                                        .clickable { memberRelationship = rel }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        rel,
                                        fontSize = 11.sp,
                                        color = if (memberRelationship == rel) Color.White else PrimaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Member Role
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = memberRole == "Farming", onCheckedChange = { checked ->
                        memberRole = if (checked) "Farming" else "Non-Farming"
                    })
                    Text("Participates in Farming")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (memberName.isNotBlank() && memberAge.isNotBlank()) {
                        val ageInt = memberAge.toIntOrNull() ?: 30
                        onAddMember(
                            HouseholdMemberEntity(
                                farmerId = "",
                                name = memberName,
                                relationship = memberRelationship,
                                age = ageInt,
                                gender = memberGender,
                                role = memberRole
                            )
                        )
                        // Reset Inline State
                        memberName = ""
                        memberAge = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Household")
                Spacer(modifier = Modifier.width(4.dp))
                Text(Localization.getString(currentLang, "add_member_btn"))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (members.isNotEmpty()) {
                Text("Added Members List:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    members.forEachIndexed { index, m ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("(${m.relationship} • ${m.age} yrs • ${m.role})", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                IconButton(onClick = { onRemoveMember(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Farm & Plot Management Tab
@Composable
fun PlotsTab(
    currentLang: AppLanguage,
    plots: List<FarmPlotEntity>,
    onAddPlot: (FarmPlotEntity) -> Unit,
    onRemovePlot: (Int) -> Unit
) {
    var plotNo by remember { mutableStateOf("") }
    var plotAcres by remember { mutableStateOf("") }
    var plotAcresError by remember { mutableStateOf<String?>(null) }
    var plotOwnership by remember { mutableStateOf("Own") }
    var plotIrrigation by remember { mutableStateOf("Rain-fed") }
    var soilTypeName by remember { mutableStateOf("") }

    val ownerships = listOf("Own", "Leased", "Shared")
    val irrigations = listOf("Rain-fed", "Drip", "Canal", "Borewell")

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(Localization.getString(currentLang, "farm_plots_sec"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = plotNo,
                onValueChange = { plotNo = it },
                label = { Text(Localization.getString(currentLang, "plot_id") + " / Khasra Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = plotAcres,
                    onValueChange = { 
                        plotAcres = it 
                        plotAcresError = null
                    },
                    label = { Text(Localization.getString(currentLang, "total_acres")) },
                    isError = plotAcresError != null,
                    supportingText = { plotAcresError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = soilTypeName,
                    onValueChange = { soilTypeName = it },
                    label = { Text(Localization.getString(currentLang, "soil_health") + " (e.g. Clay)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Ownership", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ownerships.forEach { own ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = plotOwnership == own, onClick = { plotOwnership = own })
                        Text(own, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Irrigation Type", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                irrigations.forEach { irr ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = plotIrrigation == irr, onClick = { plotIrrigation = irr })
                        Text(irr, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val acresDouble = plotAcres.toDoubleOrNull()
                    if (plotNo.isBlank()) {
                        // ignore or check
                    } else if (acresDouble == null || acresDouble <= 0.0) {
                        plotAcresError = "Must be a positive number"
                    } else {
                        plotAcresError = null
                        onAddPlot(
                            FarmPlotEntity(
                                farmerId = "",
                                plotId = plotNo,
                                landArea = acresDouble,
                                ownershipType = plotOwnership,
                                gpsLatitude = 16.2145 + Random.nextDouble(-0.005, 0.005),
                                gpsLongitude = 74.8215 + Random.nextDouble(-0.005, 0.005),
                                irrigationSource = plotIrrigation,
                                soilType = soilTypeName
                            )
                        )
                        // Reset Inline
                        plotNo = ""
                        plotAcres = ""
                        soilTypeName = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Plot")
                Spacer(modifier = Modifier.width(4.dp))
                Text(Localization.getString(currentLang, "add_plot_btn"))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plot Visual map simulation on Canvas! (FR-11)
            if (plots.isNotEmpty()) {
                Text("Simulated Plot Boundaries Mapping Diagram (FO-11)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 3f)
                        
                        // Drawing dynamic farm boundaries
                        plots.forEachIndexed { i, p ->
                            val startX = 60f + (i * 200f) % (size.width - 200f)
                            val startY = 50f + (i * 30f) % (size.height - 80f)
                            
                            val drawPath = Path().apply {
                                moveTo(startX, startY)
                                lineTo(startX + 140f, startY - 10f)
                                lineTo(startX + 160f, startY + 60f)
                                lineTo(startX + 30f, startY + 70f)
                                close()
                            }
                            
                            // Draw boundary fill
                            drawPath(
                                path = drawPath,
                                color = SecondaryGreen.copy(alpha = 0.3f)
                            )
                            // Draw boundary line
                            drawPath(
                                path = drawPath,
                                color = PrimaryGreen,
                                style = stroke
                            )
                            // Text placement label
                            drawCircle(
                                color = Color.White,
                                radius = 10f,
                                center = Offset(startX + 80f, startY + 30f)
                            )
                            drawCircle(
                                color = PrimaryGreen,
                                radius = 4f,
                                center = Offset(startX + 80f, startY + 30f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                Text("Plots Queue List:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plots.forEachIndexed { index, p ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Plot: ${p.plotId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("(${p.landArea} ac • ${p.ownershipType} • ${p.irrigationSource} • ${p.soilType ?: "N/A"})", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                IconButton(onClick = { onRemovePlot(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Plot", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Crops and practices and Livestocks
@Composable
fun CropsPracticesTab(
    currentLang: AppLanguage,
    cropsOptions: List<String>,
    primarySelected: Set<String>,
    onPrimaryToggle: (String) -> Unit,
    secondarySelected: Set<String>,
    onSecondaryToggle: (String) -> Unit,
    farmingMethod: String, onMethodChange: (String) -> Unit,
    usePesticides: Boolean, onPesticidesToggle: (Boolean) -> Unit,
    pesticideDetails: String, onPesticideDetailChange: (String) -> Unit,
    useFertilizers: Boolean, onFertilizersToggle: (Boolean) -> Unit,
    fertilizerDetails: String, onFertilizerDetailChange: (String) -> Unit,
    seedSource: String, onSeedChange: (String) -> Unit,
    previousSeasonYield: String, onYieldChange: (String) -> Unit,
    icsGroup: String, onIcsGroupChange: (String) -> Unit,
    livestock: List<LivestockItem>,
    onAddLivestock: (LivestockItem) -> Unit,
    onRemoveLivestock: (Int) -> Unit
) {
    var lType by remember { mutableStateOf("Cattle") }
    var lCount by remember { mutableStateOf("") }
    var lUse by remember { mutableStateOf("Dairy & Draught") }
    var lFodder by remember { mutableStateOf("Own Farm") }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(Localization.getString(currentLang, "primary_crops_mapped") + " (Multi-select)", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Display chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cropsOptions.chunked(3).forEach { rowList ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowList.forEach { crop ->
                                val isSelected = primarySelected.contains(crop)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) PrimaryGreen else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, PrimaryGreen, RoundedCornerShape(8.dp))
                                        .clickable { onPrimaryToggle(crop) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        Text(crop, fontSize = 11.sp, color = if (isSelected) Color.White else PrimaryGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(Localization.getString(currentLang, "intercrops_secondary"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cropsOptions.chunked(3).forEach { rowList ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowList.forEach { crop ->
                                val isSelected = secondarySelected.contains(crop)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) SecondaryGreen else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, SecondaryGreen, RoundedCornerShape(8.dp))
                                        .clickable { onSecondaryToggle(crop) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        Text(crop, fontSize = 11.sp, color = if (isSelected) Color.White else SecondaryGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(Localization.getString(currentLang, "crops_farming_practices"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 15.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Organic", "Transitional", "Conventional").forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = farmingMethod == item, onClick = { onMethodChange(item) })
                        Text(item, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Non organic details
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(Localization.getString(currentLang, "is_chem_pesticides_used"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Switch(checked = usePesticides, onCheckedChange = onPesticidesToggle)
            }
            if (usePesticides) {
                OutlinedTextField(
                    value = pesticideDetails,
                    onValueChange = onPesticideDetailChange,
                    label = { Text(Localization.getString(currentLang, "pesticide_log")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(Localization.getString(currentLang, "is_chem_fertilizers_used"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Switch(checked = useFertilizers, onCheckedChange = onFertilizersToggle)
            }
            if (useFertilizers) {
                OutlinedTextField(
                    value = fertilizerDetails,
                    onValueChange = onFertilizerDetailChange,
                    label = { Text(Localization.getString(currentLang, "fertilizer_log")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(Localization.getString(currentLang, "seeds_sourcing"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Own Saved", "Purchased", "Government").forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = seedSource == s, onClick = { onSeedChange(s) })
                        Text(s, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = previousSeasonYield,
                onValueChange = onYieldChange,
                label = { Text(Localization.getString(currentLang, "previous_yield") + " (In kg / bags)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = icsGroup,
                onValueChange = onIcsGroupChange,
                label = { Text(Localization.getString(currentLang, "ics_group_label")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(Localization.getString(currentLang, "livestock_sec"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Livestock inline addition
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lType,
                    onValueChange = { lType = it },
                    label = { Text(Localization.getString(currentLang, "animal_type")) },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = lCount,
                    onValueChange = { lCount = it },
                    label = { Text(Localization.getString(currentLang, "animal_count")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lUse,
                    onValueChange = { lUse = it },
                    label = { Text("Primary Utility (Dairy/Meat)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = lFodder,
                    onValueChange = { lFodder = it },
                    label = { Text("Fodder Source") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (lType.isNotBlank() && lCount.isNotBlank()) {
                        onAddLivestock(
                            LivestockItem(
                                type = lType,
                                count = lCount.toIntOrNull() ?: 2,
                                primaryUse = lUse,
                                fodderSource = lFodder
                            )
                        )
                        lCount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(Localization.getString(currentLang, "add_livestock_btn"))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (livestock.isNotEmpty()) {
                Text("Added Animals List:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    livestock.forEachIndexed { index, l ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${l.type} - Count: ${l.count}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Use: ${l.primaryUse}  •  Fodder: ${l.fodderSource}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                IconButton(onClick = { onRemoveLivestock(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Livestock", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
