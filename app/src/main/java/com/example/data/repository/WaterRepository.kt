package com.example.data.repository

import com.example.data.dao.SettingsDao
import com.example.data.dao.WaterDao
import com.example.data.model.AppSetting
import com.example.data.model.WaterDrink
import kotlinx.coroutines.flow.Flow

class WaterRepository(
    private val waterDao: WaterDao,
    private val settingsDao: SettingsDao
) {
    val allDrinks: Flow<List<WaterDrink>> = waterDao.getAllDrinks()
    val allSettings: Flow<List<AppSetting>> = settingsDao.getAllSettings()

    suspend fun addDrink(amountMl: Int, timestamp: Long = System.currentTimeMillis()) {
        waterDao.insertDrink(WaterDrink(amountMl = amountMl, timestamp = timestamp))
    }

    suspend fun deleteDrinkById(id: Int) {
        waterDao.deleteDrinkById(id)
    }

    suspend fun undoLastDrink() {
        val lastDrink = waterDao.getLastDrink()
        if (lastDrink != null) {
            waterDao.deleteDrinkById(lastDrink.id)
        }
    }

    suspend fun clearHistory() {
        waterDao.clearAllDrinks()
    }

    suspend fun saveSetting(key: String, value: String) {
        settingsDao.saveSetting(AppSetting(key, value))
    }

    suspend fun getSetting(key: String, defaultValue: String): String {
        return settingsDao.getSetting(key)?.value ?: defaultValue
    }
}
