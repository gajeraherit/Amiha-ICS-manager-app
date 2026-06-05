package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FarmPlotEntity
import com.example.data.FarmerEntity
import com.example.data.FarmerRepository
import com.example.data.FarmerWithDetails
import com.example.data.HouseholdMemberEntity
import com.example.data.LivestockItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StaffProfile(
    val fullName: String,
    val role: String,
    val password: String,
    val phone: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FarmerRepository(db.farmerDao())

    private val prefs = application.getSharedPreferences("ics_prefs", android.content.Context.MODE_PRIVATE)

    // Role-based auth states
    val userRole = MutableStateFlow(prefs.getString("user_role", "Field Officer") ?: "Field Officer") // Field Officer or Inspector
    val userName = MutableStateFlow(prefs.getString("user_name", "Arjun FO") ?: "Arjun FO")
    val userEmail = MutableStateFlow(prefs.getString("user_email", "arjun@ics.org") ?: "arjun@ics.org")
    val isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false)) // Simulates persistent session
    val selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedVillage = MutableStateFlow("All")
    val selectedCrop = MutableStateFlow("All")
    val selectedStatus = MutableStateFlow("All")

    // Sync state
    val isSyncing = MutableStateFlow(false)

    // Observable farmers from database
    val allFarmers: StateFlow<List<FarmerWithDetails>> = repository.allFarmers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic Filtered list
    val filteredFarmers: StateFlow<List<FarmerWithDetails>> = combine(
        allFarmers,
        searchQuery,
        selectedVillage,
        selectedCrop,
        selectedStatus
    ) { baseList, query, village, crop, status ->
        var list = baseList

        // Search text matching (ID, Name, Village, ICS Group)
        if (query.isNotBlank()) {
            list = list.filter {
                it.farmer.fullName.contains(query, ignoreCase = true) ||
                        it.farmer.farmerId.contains(query, ignoreCase = true) ||
                        it.farmer.villageName.contains(query, ignoreCase = true) ||
                        it.farmer.icsGroup.contains(query, ignoreCase = true)
            }
        }

        // Filter by Village
        if (village != "All") {
            list = list.filter { it.farmer.villageName.equals(village, ignoreCase = true) }
        }

        // Filter by Crop
        if (crop != "All") {
            list = list.filter {
                it.farmer.primaryCrops.any { c -> c.equals(crop, ignoreCase = true) } ||
                        it.farmer.secondaryCrops.any { c -> c.equals(crop, ignoreCase = true) }
            }
        }

        // Filter by Status
        if (status != "All") {
            list = list.filter { it.farmer.certificationStatus.equals(status, ignoreCase = true) }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Helper to get unique list values for filter dropdowns
    val availableVillages: StateFlow<List<String>> = allFarmers.map { list ->
        list.map { it.farmer.villageName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCrops: StateFlow<List<String>> = allFarmers.map { list ->
        val crops = mutableListOf<String>()
        list.forEach {
            crops.addAll(it.farmer.primaryCrops)
            crops.addAll(it.farmer.secondaryCrops)
        }
        crops.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-Memory Staff credentials database for dynamic simulation
    val registeredStaff = MutableStateFlow<Map<String, StaffProfile>>(
        mapOf(
            "arjun@ics.org" to StaffProfile("Arjun FO", "Field Officer", "password", "+91 98452 00000"),
            "priya@ics.org" to StaffProfile("Priya Inspector", "Inspector", "password", "+91 98452 11111")
        )
    )

    init {
        com.example.data.FirebaseManager.initialize(application.applicationContext)
        // Safe startup prepopulation from Firebase
        viewModelScope.launch {
            try {
                if (com.example.data.FirebaseManager.isReady) {
                    fetchFarmersFromFirebase()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Trigger background sync checker periodically
        simulateBackgroundSync()
    }

    // Role switcher helper
    fun login(email: String, user: String, role: String) {
        val lowerEmail = email.lowercase()
        userEmail.value = lowerEmail
        userName.value = user
        userRole.value = role
        isLoggedIn.value = true
        prefs.edit().apply {
            putString("user_email", lowerEmail)
            putString("user_name", user)
            putString("user_role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun updateStaffProfile(email: String, fullName: String, phone: String, password: String, role: String) {
        val lowerEmail = email.lowercase()
        val updatedMap = registeredStaff.value.toMutableMap()
        updatedMap[lowerEmail] = StaffProfile(fullName, role, password, phone)
        registeredStaff.value = updatedMap

        // If updating the currently logged-in user, update live values
        if (userEmail.value.lowercase() == lowerEmail) {
            userName.value = fullName
            userRole.value = role
            prefs.edit().apply {
                putString("user_name", fullName)
                putString("user_role", role)
                apply()
            }
        }
    }

    fun logout() {
        isLoggedIn.value = false
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }
    }

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private fun serializeFarmerToMap(farmerWithDetails: FarmerWithDetails): Map<String, Any>? {
        return try {
            val adapter = moshi.adapter(FarmerWithDetails::class.java)
            val jsonString = adapter.toJson(farmerWithDetails)
            val mapType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val mapAdapter = moshi.adapter<Map<String, Any>>(mapType)
            mapAdapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deserializeMapToFarmer(map: Map<String, Any>): FarmerWithDetails? {
        return try {
            val mapType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val mapAdapter = moshi.adapter<Map<String, Any>>(mapType)
            val jsonString = mapAdapter.toJson(map)
            val adapter = moshi.adapter(FarmerWithDetails::class.java)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fetchFarmersFromFirebase() {
        val firestore = com.example.data.FirebaseManager.firestore ?: return
        firestore.collection("farmers")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    seedFirebaseWithRealData()
                } else {
                    viewModelScope.launch {
                        for (doc in result) {
                            val dataMap = doc.data
                            val farmerWithDetails = deserializeMapToFarmer(dataMap)
                            if (farmerWithDetails != null) {
                                val syncedFarmer = farmerWithDetails.farmer.copy(syncStatus = "Synced")
                                repository.saveFarmer(syncedFarmer, farmerWithDetails.householdMembers, farmerWithDetails.plots)
                            }
                        }
                    }
                }
            }
    }

    private fun seedFirebaseWithRealData() {
        val firestore = com.example.data.FirebaseManager.firestore ?: return
        
        val f1Id = "FO-26-8043"
        val f1 = FarmerEntity(
            farmerId = f1Id,
            fullName = "Ramesh Patel",
            dob = "1978-08-15",
            gender = "Male",
            nationalId = "9845-2101-4432",
            profilePhotoUri = null,
            primaryPhone = "+91 98452 10101",
            secondaryPhone = "+91 94452 10111",
            whatsappNumber = "+91 98452 10101",
            email = "ramesh.patel@organicfarm.org",
            villageName = "Anandpur",
            gramPanchayat = "Anandpur GP",
            taluka = "Ghataprabha",
            district = "Belagavi",
            state = "Karnataka",
            gpsLatitude = 16.2142,
            gpsLongitude = 74.8211,
            primaryCrops = listOf("Cotton", "Sorghum"),
            secondaryCrops = listOf("Pigeon Pea"),
            farmingMethod = "Organic",
            useOfPesticides = false,
            pesticideDetails = null,
            useOfChemicalFertilizers = false,
            chemicalFertilizerDetails = null,
            seedSource = "Own Saved",
            previousSeasonYield = "850 kg/acre",
            livestock = listOf(
                LivestockItem("Cattle", 3, "Dairy & Draught", "Own Farm"),
                LivestockItem("Goat", 5, "Meat", "Grazing land")
            ),
            icsGroup = "Anandpur Eco-Cluster",
            registrationDate = "2024-05-10",
            certificationStatus = "Certified",
            lastInspectionDate = "2025-11-20",
            inspectionResult = "Pass",
            nonConformanceNotes = null,
            nextScheduledInspectionDate = "2026-08-15",
            syncStatus = "Synced"
        )
        val f1Household = listOf(
            HouseholdMemberEntity(farmerId = f1Id, name = "Sunita Patel", relationship = "Spouse", age = 44, gender = "Female", role = "Farming"),
            HouseholdMemberEntity(farmerId = f1Id, name = "Rohit Patel", relationship = "Son", age = 22, gender = "Male", role = "Farming"),
            HouseholdMemberEntity(farmerId = f1Id, name = "Anjali Patel", relationship = "Daughter", age = 16, gender = "Female", role = "Non-Farming")
        )
        val f1Plots = listOf(
            FarmPlotEntity(farmerId = f1Id, plotId = "Khasra 104/A", landArea = 2.4, ownershipType = "Own", gpsLatitude = 16.2144, gpsLongitude = 74.8213, irrigationSource = "Drip", soilType = "Black Cotton Soil"),
            FarmPlotEntity(farmerId = f1Id, plotId = "Khasra 104/B", landArea = 1.1, ownershipType = "Leased", gpsLatitude = 16.2150, gpsLongitude = 74.8220, irrigationSource = "Rain-fed", soilType = "Clay Loam")
        )
        val grower1 = FarmerWithDetails(f1, f1Household, f1Plots)

        val f2Id = "FO-26-4412"
        val f2 = FarmerEntity(
            farmerId = f2Id,
            fullName = "Savitri Devi",
            dob = "1983-11-04",
            gender = "Female",
            nationalId = "3214-9988-1122",
            profilePhotoUri = null,
            primaryPhone = "+91 91234 56789",
            secondaryPhone = null,
            whatsappNumber = null,
            email = null,
            villageName = "Meghpura",
            gramPanchayat = "Meghpura Khurd",
            taluka = "Malpur",
            district = "Aravalli",
            state = "Gujarat",
            gpsLatitude = 23.3562,
            gpsLongitude = 73.4542,
            primaryCrops = listOf("Rice", "Wheat"),
            secondaryCrops = listOf("Mustard"),
            farmingMethod = "Transitional",
            useOfPesticides = true,
            pesticideDetails = "Eco-friendly botanical neem sprays only",
            useOfChemicalFertilizers = false,
            chemicalFertilizerDetails = null,
            seedSource = "Purchased",
            previousSeasonYield = "1200 kg/acre",
            livestock = listOf(
                LivestockItem("Buffalo", 2, "Dairy", "Purchased Fodder")
            ),
            icsGroup = "Meghpura Organic Union",
            registrationDate = "2025-01-18",
            certificationStatus = "Under Inspection",
            lastInspectionDate = "2025-07-15",
            inspectionResult = "Pass",
            nonConformanceNotes = "Required setup of boundary buffer zone rows",
            nextScheduledInspectionDate = "2026-06-25",
            syncStatus = "Synced"
        )
        val f2Household = listOf(
            HouseholdMemberEntity(farmerId = f2Id, name = "Karan Singh", relationship = "Spouse", age = 46, gender = "Male", role = "Farming"),
            HouseholdMemberEntity(farmerId = f2Id, name = "Vikas", relationship = "Son", age = 19, gender = "Male", role = "Farming")
        )
        val f2Plots = listOf(
            FarmPlotEntity(farmerId = f2Id, plotId = "Khasra 44", landArea = 1.8, ownershipType = "Own", gpsLatitude = 23.3568, gpsLongitude = 73.4548, irrigationSource = "Canal", soilType = "Alluvial Loam")
        )
        val grower2 = FarmerWithDetails(f2, f2Household, f2Plots)

        val f3Id = "FO-26-9011"
        val f3 = FarmerEntity(
            farmerId = f3Id,
            fullName = "Gurpreet Singh",
            dob = "1969-04-20",
            gender = "Male",
            nationalId = "7766-5544-3321",
            profilePhotoUri = null,
            primaryPhone = "+91 98765 43210",
            secondaryPhone = "+91 98765 43211",
            whatsappNumber = "+91 98765 43210",
            email = "gurpreet.punjab@organicagro.com",
            villageName = "Anandpur",
            gramPanchayat = "Anandpur GP",
            taluka = "Ghataprabha",
            district = "Belagavi",
            state = "Karnataka",
            gpsLatitude = 16.2162,
            gpsLongitude = 74.8251,
            primaryCrops = listOf("Wheat"),
            secondaryCrops = listOf("Lentils", "Chickpeas"),
            farmingMethod = "Organic",
            useOfPesticides = false,
            pesticideDetails = null,
            useOfChemicalFertilizers = false,
            chemicalFertilizerDetails = null,
            seedSource = "Government",
            previousSeasonYield = "1400 kg/acre",
            livestock = listOf(
                LivestockItem("Poultry", 45, "Eggs & Meat", "Own Farm")
            ),
            icsGroup = "Anandpur Eco-Cluster",
            registrationDate = "2025-10-02",
            certificationStatus = "Registered",
            lastInspectionDate = null,
            inspectionResult = "Pending",
            nonConformanceNotes = null,
            nextScheduledInspectionDate = "2026-06-12",
            syncStatus = "Synced"
        )
        val f3Household = listOf(
            HouseholdMemberEntity(farmerId = f3Id, name = "Harpreet Kaur", relationship = "Spouse", age = 52, gender = "Female", role = "Farming")
        )
        val f3Plots = listOf(
            FarmPlotEntity(farmerId = f3Id, plotId = "Plot 91-B", landArea = 4.1, ownershipType = "Shared", gpsLatitude = 16.2168, gpsLongitude = 74.8258, irrigationSource = "Borewell", soilType = "Red Sandy Soil")
        )
        val grower3 = FarmerWithDetails(f3, f3Household, f3Plots)

        listOf(grower1, grower2, grower3).forEach { grower ->
            val docData = serializeFarmerToMap(grower)
            if (docData != null) {
                firestore.collection("farmers")
                    .document(grower.farmer.farmerId)
                    .set(docData)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            repository.saveFarmer(grower.farmer, grower.householdMembers, grower.plots)
                        }
                    }
            }
        }
    }

    // Save a completed farmer record
    fun saveFarmer(
        farmer: FarmerEntity,
        members: List<HouseholdMemberEntity>,
        plots: List<FarmPlotEntity>
    ) {
        viewModelScope.launch {
            repository.saveFarmer(farmer, members, plots)
        }
    }

    fun getFarmerByIdFlow(id: String): Flow<FarmerWithDetails?> {
        return repository.getFarmerByIdFlow(id)
    }

    // Record an inspection result against a farmer record
    fun saveInspectionResult(
        farmerId: String,
        result: String, // Pass, Fail, Pending
        notes: String?,
        inspectorName: String
    ) {
        viewModelScope.launch {
            val record = repository.getFarmerById(farmerId)
            if (record != null) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                // Certification status logic
                val newStatus = when (result) {
                    "Pass" -> "Certified"
                    "Fail" -> "Suspended"
                    else -> "Under Inspection"
                }

                val updatedFarmer = record.farmer.copy(
                    inspectionResult = result,
                    lastInspectionDate = today,
                    nonConformanceNotes = notes.takeIf { it?.isNotBlank() == true },
                    certificationStatus = newStatus,
                    syncStatus = "Pending", // Mark as needing sync
                    lastModifiedTimestamp = System.currentTimeMillis()
                )

                repository.saveFarmer(updatedFarmer, record.householdMembers, record.plots)
            }
        }
    }

    // Delete farmer
    fun deleteFarmer(farmerId: String) {
        viewModelScope.launch {
            repository.deleteFarmer(farmerId)
        }
    }

    // Manual Sync function from App Settings
    fun triggerManualSync() {
        if (isSyncing.value) return
        viewModelScope.launch {
            isSyncing.value = true
            
            val pending = allFarmers.value.filter { it.farmer.syncStatus == "Pending" || it.farmer.syncStatus == "Failed" }
            val firestore = com.example.data.FirebaseManager.firestore
            if (firestore != null && pending.isNotEmpty()) {
                pending.forEach { p ->
                    val docData = serializeFarmerToMap(p)
                    if (docData != null) {
                        firestore.collection("farmers")
                            .document(p.farmer.farmerId)
                            .set(docData)
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    val syncedFarmer = p.farmer.copy(
                                        syncStatus = "Synced",
                                        lastModifiedTimestamp = System.currentTimeMillis()
                                    )
                                    repository.saveFarmer(syncedFarmer, p.householdMembers, p.plots)
                                }
                            }
                    }
                }
            } else if (firestore != null) {
                fetchFarmersFromFirebase()
                delay(1500)
            } else {
                delay(2000)
                pending.forEach { p ->
                    val syncedFarmer = p.farmer.copy(
                        syncStatus = "Synced",
                        lastModifiedTimestamp = System.currentTimeMillis()
                    )
                    repository.saveFarmer(syncedFarmer, p.householdMembers, p.plots)
                }
            }
            
            isSyncing.value = false
        }
    }

    // Simulated local auto background sync engine
    private fun simulateBackgroundSync() {
        viewModelScope.launch {
            while (true) {
                delay(12000) // Check every 12 seconds
                val pending = allFarmers.value.filter { it.farmer.syncStatus == "Pending" }
                if (pending.isNotEmpty()) {
                    val firestore = com.example.data.FirebaseManager.firestore
                    if (firestore != null) {
                        pending.forEach { p ->
                            val docData = serializeFarmerToMap(p)
                            if (docData != null) {
                                firestore.collection("farmers")
                                    .document(p.farmer.farmerId)
                                    .set(docData)
                                    .addOnSuccessListener {
                                        viewModelScope.launch {
                                            val syncedFarmer = p.farmer.copy(
                                                syncStatus = "Synced",
                                                lastModifiedTimestamp = System.currentTimeMillis()
                                            )
                                            repository.saveFarmer(syncedFarmer, p.householdMembers, p.plots)
                                        }
                                    }
                            }
                        }
                    } else {
                        pending.forEach { p ->
                            delay(2000)
                            val syncedFarmer = p.farmer.copy(
                                syncStatus = "Synced",
                                lastModifiedTimestamp = System.currentTimeMillis()
                            )
                            repository.saveFarmer(syncedFarmer, p.householdMembers, p.plots)
                        }
                    }
                }
            }
        }
    }

    fun exportFarmerReport(farmerId: String, context: android.content.Context, onResult: (String?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getFarmerById(farmerId)
                if (data == null) {
                    onResult("Farmer not found", false)
                    return@launch
                }
                val farmer = data.farmer
                val members = data.householdMembers
                val plots = data.plots

                // Construct CSV format
                val csvBuilder = java.lang.StringBuilder()
                csvBuilder.append("AMIHA ICS Compliance Report\n")
                csvBuilder.append("Export Date,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                csvBuilder.append("\n=== Farmer Profile ===\n")
                csvBuilder.append("Farmer ID,${farmer.farmerId}\n")
                csvBuilder.append("Full Name,${farmer.fullName}\n")
                csvBuilder.append("DOB,${farmer.dob}\n")
                csvBuilder.append("Gender,${farmer.gender}\n")
                csvBuilder.append("National ID,${farmer.nationalId}\n")
                csvBuilder.append("Phone,${farmer.primaryPhone}\n")
                csvBuilder.append("WhatsApp,${farmer.whatsappNumber ?: "N/A"}\n")
                csvBuilder.append("Email,${farmer.email ?: "N/A"}\n")
                csvBuilder.append("Location,${farmer.villageName} GP: ${farmer.gramPanchayat} Taluka: ${farmer.taluka} District: ${farmer.district} State: ${farmer.state}\n")
                csvBuilder.append("GPS Coordinate,${farmer.gpsLatitude} ${farmer.gpsLongitude}\n")
                csvBuilder.append("Farming Method,${farmer.farmingMethod}\n")
                csvBuilder.append("ICS Group,${farmer.icsGroup}\n")
                csvBuilder.append("Certification Status,${farmer.certificationStatus}\n")
                csvBuilder.append("Last Inspection Date,${farmer.lastInspectionDate ?: "N/A"}\n")
                csvBuilder.append("Inspection Result,${farmer.inspectionResult}\n")
                csvBuilder.append("Non-Conformance,${farmer.nonConformanceNotes ?: "None"}\n")

                csvBuilder.append("\n=== Household Members ===\n")
                csvBuilder.append("Name,Relationship,Age,Role\n")
                members.forEach { m ->
                    csvBuilder.append("${m.name},${m.relationship},${m.age},${m.role}\n")
                }

                csvBuilder.append("\n=== Farm Plots ===\n")
                csvBuilder.append("Plot ID/Khasra,Area (Acres),Ownership,Irrigation,Soil Type\n")
                plots.forEach { p ->
                    csvBuilder.append("${p.plotId},${p.landArea},${p.ownershipType},${p.irrigationSource},${p.soilType ?: "N/A"}\n")
                }

                // Write file to external Documents dir
                val docsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                if (docsDir == null) {
                    onResult("Documents directory unavailable", false)
                    return@launch
                }
                if (!docsDir.exists()) {
                    docsDir.mkdirs()
                }
                val filename = "AMIHA_Report_${farmer.fullName.replace(" ", "_")}_${farmer.farmerId}.csv"
                val file = java.io.File(docsDir, filename)
                file.writeText(csvBuilder.toString())

                onResult(file.name, true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(e.message ?: "Unknown error occurred during export", false)
            }
        }
    }

    fun exportAllFarmersReport(context: android.content.Context, onResult: (String?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val farmerList = allFarmers.value
                if (farmerList.isEmpty()) {
                    onResult("No farmer data available to export", false)
                    return@launch
                }

                // Construct CSV format
                val csvBuilder = java.lang.StringBuilder()
                // CSV Header
                csvBuilder.append("Farmer ID,Full Name,DOB,Gender,National ID,Primary Phone,Village,Gram Panchayat,Taluka,District,State,Latitude,Longitude,Farming Method,ICS Group,Certification Status,Crops,Total Area (Acres),Plots Count,Livestock Count\n")

                farmerList.forEach { item ->
                    val f = item.farmer
                    val totalArea = item.plots.sumOf { it.landArea }
                    val cropsStr = f.primaryCrops.joinToString(";")
                    val cleanName = f.fullName.replace(",", " ")
                    val cleanVillage = f.villageName.replace(",", " ")
                    val cleanGP = f.gramPanchayat.replace(",", " ")
                    
                    csvBuilder.append("${f.farmerId},")
                    csvBuilder.append("${cleanName},")
                    csvBuilder.append("${f.dob},")
                    csvBuilder.append("${f.gender},")
                    csvBuilder.append("${f.nationalId},")
                    csvBuilder.append("${f.primaryPhone},")
                    csvBuilder.append("${cleanVillage},")
                    csvBuilder.append("${cleanGP},")
                    csvBuilder.append("${f.taluka},")
                    csvBuilder.append("${f.district},")
                    csvBuilder.append("${f.state},")
                    csvBuilder.append("${f.gpsLatitude},")
                    csvBuilder.append("${f.gpsLongitude},")
                    csvBuilder.append("${f.farmingMethod},")
                    csvBuilder.append("${f.icsGroup},")
                    csvBuilder.append("${f.certificationStatus},")
                    csvBuilder.append("\"${cropsStr}\",")
                    csvBuilder.append("${totalArea},")
                    csvBuilder.append("${item.plots.size},")
                    csvBuilder.append("${f.livestock.size}\n")
                }

                // Write file to external Documents dir
                val docsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                if (docsDir == null) {
                    onResult("Documents directory unavailable", false)
                    return@launch
                }
                if (!docsDir.exists()) {
                    docsDir.mkdirs()
                }
                val filename = "AMIHA_All_Farmers_Report_${System.currentTimeMillis()}.csv"
                val file = java.io.File(docsDir, filename)
                file.writeText(csvBuilder.toString())

                onResult(file.name, true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(e.message ?: "Unknown error occurred during export", false)
            }
        }
    }
}
