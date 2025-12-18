package ua.com.merchik.merchik.database.room.DaoInterfaces


import androidx.room.*
import ua.com.merchik.merchik.data.Database.Room.InitStateEntity

@Dao
interface InitStateDao {

    @Query("SELECT * FROM init_state WHERE id = 1 LIMIT 1")
    fun getState(): InitStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveState(state: InitStateEntity)

    // Удобные шорткаты

    @Transaction
    fun markWpLoaded() {
        val current = getState() ?: InitStateEntity()
        saveState(current.copy(wpLoaded = true))
    }

    @Transaction
    fun markSiteLoaded() {
        val current = getState() ?: InitStateEntity()
        saveState(current.copy(siteLoaded = true))
    }

    @Transaction
    fun markOptionsLoaded() {
        val current = getState() ?: InitStateEntity()
        saveState(current.copy(optionsLoaded = true))
    }

    @Transaction
    fun markThemeLoaded() {
        val current = getState() ?: InitStateEntity()
        saveState(current.copy(themeLoaded = true))
    }

    @Transaction
    fun markCustomerLoaded() {
        val current = getState() ?: InitStateEntity()
        saveState(current.copy(customerLoaded = true))
    }

    @Transaction
    fun markUsersLoaded() {
        val current = getState() ?: InitStateEntity()
        saveState(current.copy(userLoaded = true))
    }


    // Если нужно полностью сбрасывать состояние (например, при логауте):
    @Transaction
    fun resetAll() {
        saveState(InitStateEntity())
    }
}
