package com.example.data

import kotlinx.coroutines.flow.Flow

class FarmerRepository(private val farmerDao: FarmerDao) {

    val allFarmers: Flow<List<FarmerWithDetails>> = farmerDao.getAllFarmersFlow()

    fun getFarmerByIdFlow(id: String): Flow<FarmerWithDetails?> = farmerDao.getFarmerByIdFlow(id)

    suspend fun getFarmerById(id: String): FarmerWithDetails? = farmerDao.getFarmerById(id)

    suspend fun saveFarmer(
        farmer: FarmerEntity,
        members: List<HouseholdMemberEntity>,
        plots: List<FarmPlotEntity>
    ) {
        farmerDao.saveFarmerWithDetails(farmer, members, plots)
    }

    suspend fun deleteFarmer(farmerId: String) {
        farmerDao.deleteFarmerWithDetails(farmerId)
    }
}
