package ua.com.merchik.merchik.data.Database.Room


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "init_state")
data class InitStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,                         // всегда одна строка

    @ColumnInfo(name = "wp_loaded")
    val wpLoaded: Boolean = false,

    @ColumnInfo(name = "site_loaded")
    val siteLoaded: Boolean = false,

    @ColumnInfo(name = "options_loaded")
    val optionsLoaded: Boolean = false,

    @ColumnInfo(name = "theme_loaded")
    val themeLoaded: Boolean = false,

    @ColumnInfo(name = "customer_loaded")
    val customerLoaded: Boolean = false,

    @ColumnInfo(name = "user_loaded")
    val userLoaded: Boolean = false,


)
