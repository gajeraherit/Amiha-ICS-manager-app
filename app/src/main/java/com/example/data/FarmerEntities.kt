package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Data Class for Livestock details
data class LivestockItem(
    val type: String, // e.g. "Cattle", "Goat", "Poultry", etc.
    val count: Int,
    val primaryUse: String, // e.g. "Dairy", "Draught", "Meat", "Eggs", "Other"
    val fodderSource: String // e.g. "Own Farm", "Purchased"
)

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey val farmerId: String, // Unique Farmer ID / Registration Number
    val fullName: String,
    val dob: String,
    val gender: String,
    val nationalId: String,
    val profilePhotoUri: String?, // Optional local photo URI
    
    // Contact details
    val primaryPhone: String,
    val secondaryPhone: String?,
    val whatsappNumber: String?,
    val email: String?,
    
    // Location Details
    val villageName: String,
    val gramPanchayat: String,
    val taluka: String,
    val district: String,
    val state: String,
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    
    // Crops & Practices
    val primaryCrops: List<String>,
    val secondaryCrops: List<String>,
    val farmingMethod: String, // "Organic", "Conventional", "Transitional"
    val useOfPesticides: Boolean,
    val pesticideDetails: String?,
    val useOfChemicalFertilizers: Boolean,
    val chemicalFertilizerDetails: String?,
    val seedSource: String, // "Own Saved", "Purchased", "Government"
    val previousSeasonYield: String?,
    
    // Livestock Details List (JSON)
    val livestock: List<LivestockItem>,
    
    // Certification & Compliance
    val icsGroup: String,
    val registrationDate: String,
    val certificationStatus: String, // "Registered", "Under Inspection", "Certified", "Suspended"
    val lastInspectionDate: String?,
    val inspectionResult: String?, // "Pass", "Fail", "Pending"
    val nonConformanceNotes: String?,
    val nextScheduledInspectionDate: String?,
    
    // Synchronization
    val syncStatus: String, // "Synced", "Pending", "Failed"
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "household_members")
data class HouseholdMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val farmerId: String, // Foreign key referencing FarmerEntity.farmerId
    val name: String,
    val relationship: String, // "Spouse", "Son", "Daughter", "Parent", "Sibling", "Other"
    val age: Int,
    val gender: String,
    val role: String // "Farming", "Non-Farming"
)

@Entity(tableName = "farm_plots")
data class FarmPlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val farmerId: String, // Foreign key referencing FarmerEntity.farmerId
    val plotId: String, // Khasra Number / Plot ID
    val landArea: Double, // in acres/hectares
    val ownershipType: String, // "Own", "Leased", "Shared"
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    val irrigationSource: String, // "Rain-fed", "Drip", "Canal", "Borewell", "Other"
    val soilType: String?
)

// Main relation class to fetch complete farmer details combined
data class FarmerWithDetails(
    @Embedded val farmer: FarmerEntity,
    @Relation(
        parentColumn = "farmerId",
        entityColumn = "farmerId"
    )
    val householdMembers: List<HouseholdMemberEntity>,
    @Relation(
        parentColumn = "farmerId",
        entityColumn = "farmerId"
    )
    val plots: List<FarmPlotEntity>
)

// Room Type Converters for JSON serialization and Deserialization
class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromLivestockList(value: List<LivestockItem>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, LivestockItem::class.java)
        val adapter = moshi.adapter<List<LivestockItem>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toLivestockList(value: String?): List<LivestockItem>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, LivestockItem::class.java)
        val adapter = moshi.adapter<List<LivestockItem>>(type)
        return adapter.fromJson(value)
    }
}
