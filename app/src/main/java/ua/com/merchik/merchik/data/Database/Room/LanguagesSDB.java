package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * 10.05.2021
 * Структура БД "Языки":
 *
 * id   -- id-шник записи в базе данных
 * nm   -- Название языка (полное) (ex: Украинский)
 * nm_short     -- Название языка (сокращённое) (ex: UA)
 * */

@Entity(tableName = "languages")
public class LanguagesSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id;   // id-шник записи в базе данных

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;   // Название языка (полное)

    @SerializedName("nm_short")
    @Expose
    @ColumnInfo(name = "nm_short")
    public String nmShort;  // Название языка (сокращённое)
}
