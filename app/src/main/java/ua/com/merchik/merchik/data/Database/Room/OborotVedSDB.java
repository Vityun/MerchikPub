package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Date;

@Entity(tableName = "oborot_ved")
public class OborotVedSDB {

//    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    @Expose
    public Integer id;


    @ColumnInfo(name = "fir_id")
    @SerializedName("fir_id")
    @Expose
    public String firId;


    @ColumnInfo(name = "kli_id")
    @SerializedName("kli_id")
    @Expose
    public String kliId;


    @ColumnInfo(name = "gru_id")
    @SerializedName("gru_id")
    @Expose
    public Integer gruId;


    @ColumnInfo(name = "adr_id")
    @SerializedName("adr_id")
    @Expose
    public Integer adrId;


    @ColumnInfo(name = "tov_id")
    @SerializedName("tov_id")
    @Expose
    public Integer tovId;


    @ColumnInfo(name = "dog_id")
    @SerializedName("dog_id")
    @Expose
    public Integer dogId;


    @ColumnInfo(name = "dat")
    @SerializedName("dat")
    @Expose
    @TypeConverters(DateConverter.class)
    public Date dat;


    @ColumnInfo(name = "date_from")
    @SerializedName("date_from")
    @Expose
    @TypeConverters(DateConverter.class)
    public Date dateFrom;


    @ColumnInfo(name = "date_to")
    @SerializedName("date_to")
    @Expose
    @TypeConverters(DateConverter.class)
    public Date dateTo;


    @ColumnInfo(name = "kol_post")
    @SerializedName("kol_post")
    @Expose
    public Integer kolPost;


    @ColumnInfo(name = "kol_prod")
    @SerializedName("kol_prod")
    @Expose
    public Integer kolProd;


    @ColumnInfo(name = "kol_ost")
    @SerializedName("kol_ost")
    @Expose
    public Integer kolOst;


    @ColumnInfo(name = "vpi")
    @SerializedName("vpi")
    @Expose
    public Integer vpi;


    @ColumnInfo(name = "DAO")
    @SerializedName("DAO")
    @Expose
    public String dao;


    @ColumnInfo(name = "author_id")
    @SerializedName("author_id")
    @Expose
    public Integer authorId;


    @ColumnInfo(name = "IZA")
    @SerializedName("IZA")
    @Expose
    public Long iza;

}
