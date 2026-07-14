package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WaterDrink
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_drinks ORDER BY timestamp DESC")
    fun getAllDrinks(): Flow<List<WaterDrink>>

    @Query("SELECT * FROM water_drinks WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getDrinksForPeriod(startOfDay: Long, endOfDay: Long): Flow<List<WaterDrink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: WaterDrink)

    @Query("DELETE FROM water_drinks WHERE id = :id")
    suspend fun deleteDrinkById(id: Int)

    @Query("DELETE FROM water_drinks")
    suspend fun clearAllDrinks()

    @Query("SELECT * FROM water_drinks ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastDrink(): WaterDrink?
}
