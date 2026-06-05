package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {

    @Transaction
    @Query("SELECT * FROM farmers ORDER BY lastModifiedTimestamp DESC")
    fun getAllFarmersFlow(): Flow<List<FarmerWithDetails>>

    @Transaction
    @Query("SELECT * FROM farmers WHERE farmerId = :farmerId")
    fun getFarmerByIdFlow(farmerId: String): Flow<FarmerWithDetails?>

    @Transaction
    @Query("SELECT * FROM farmers WHERE farmerId = :farmerId")
    suspend fun getFarmerById(farmerId: String): FarmerWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholdMembers(members: List<HouseholdMemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmPlots(plots: List<FarmPlotEntity>)

    @Query("DELETE FROM household_members WHERE farmerId = :farmerId")
    suspend fun deleteHouseholdMembersByFarmerId(farmerId: String)

    @Query("DELETE FROM farm_plots WHERE farmerId = :farmerId")
    suspend fun deleteFarmPlotsByFarmerId(farmerId: String)

    @Query("DELETE FROM farmers WHERE farmerId = :farmerId")
    suspend fun deleteFarmerOnly(farmerId: String)

    @Transaction
    suspend fun deleteFarmerWithDetails(farmerId: String) {
        deleteHouseholdMembersByFarmerId(farmerId)
        deleteFarmPlotsByFarmerId(farmerId)
        deleteFarmerOnly(farmerId)
    }

    @Transaction
    suspend fun saveFarmerWithDetails(
        farmer: FarmerEntity,
        members: List<HouseholdMemberEntity>,
        plots: List<FarmPlotEntity>
    ) {
        // Overwrite child lists cleanly
        deleteHouseholdMembersByFarmerId(farmer.farmerId)
        deleteFarmPlotsByFarmerId(farmer.farmerId)
        
        // Save parent profile
        insertFarmer(farmer)
        
        // Link and save children
        val updatedMembers = members.map { it.copy(farmerId = farmer.farmerId) }
        val updatedPlots = plots.map { it.copy(farmerId = farmer.farmerId) }
        
        if (updatedMembers.isNotEmpty()) {
            insertHouseholdMembers(updatedMembers)
        }
        if (updatedPlots.isNotEmpty()) {
            insertFarmPlots(updatedPlots)
        }
    }
}
