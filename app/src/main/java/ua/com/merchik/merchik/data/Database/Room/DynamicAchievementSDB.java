package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "dynamic_achievements")
public class DynamicAchievementSDB {

    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public String id = "";

    @SerializedName("select_id")
    @Expose
    @ColumnInfo(name = "select_id")
    public String selectId;

    @SerializedName("select_name")
    @Expose
    @ColumnInfo(name = "select_name")
    public String selectName;

    @SerializedName("rack_photo_id")
    @Expose
    @ColumnInfo(name = "rack_photo_id")
    public String rackPhotoId;

    @SerializedName("rack_photo")
    @Expose
    @ColumnInfo(name = "rack_photo")
    public String rackPhoto;

    @SerializedName("rack_photo_big")
    @Expose
    @ColumnInfo(name = "rack_photo_big")
    public String rackPhotoBig;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("client_nm")
    @Expose
    @ColumnInfo(name = "client_nm")
    public String clientNm;

    @SerializedName("isp")
    @Expose
    @ColumnInfo(name = "isp")
    public String isp;

    @SerializedName("isp_nm")
    @Expose
    @ColumnInfo(name = "isp_nm")
    public String ispNm;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public String addrId;

    @SerializedName("addr_tp")
    @Expose
    @ColumnInfo(name = "addr_tp")
    public String addrTp;

    @SerializedName("addr_city")
    @Expose
    @ColumnInfo(name = "addr_city")
    public String addrCity;

    @SerializedName("addr_addr")
    @Expose
    @ColumnInfo(name = "addr_addr")
    public String addrAddr;

    @SerializedName("addr_nomer_tt")
    @Expose
    @ColumnInfo(name = "addr_nomer_tt")
    public String addrNomerTt;

    @SerializedName("rack_id")
    @Expose
    @ColumnInfo(name = "rack_id")
    public String rackId;

    @SerializedName("rack_nm")
    @Expose
    @ColumnInfo(name = "rack_nm")
    public String rackNm;

    @SerializedName("rack_form_nm")
    @Expose
    @ColumnInfo(name = "rack_form_nm")
    public String rackFormNm;

    @SerializedName("tovar_grp_id")
    @Expose
    @ColumnInfo(name = "tovar_grp_id")
    public String tovarGrpId;

    @SerializedName("tovar_grp_nm")
    @Expose
    @ColumnInfo(name = "tovar_grp_nm")
    public String tovarGrpNm;

    @SerializedName("date_from")
    @Expose
    @ColumnInfo(name = "date_from")
    public String dateFrom;

    @SerializedName("date_to")
    @Expose
    @ColumnInfo(name = "date_to")
    public String dateTo;

    @SerializedName("achieve_photo_count")
    @Expose
    @ColumnInfo(name = "achieve_photo_count")
    public String achievePhotoCount;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public String themeId;

    @SerializedName("theme_nm")
    @Expose
    @ColumnInfo(name = "theme_nm")
    public String themeNm;

    @SerializedName("planogram_id")
    @Expose
    @ColumnInfo(name = "planogram_id")
    public String planogramId;

    @SerializedName("planogram_nm")
    @Expose
    @ColumnInfo(name = "planogram_nm")
    public String planogramNm;

    @SerializedName("manufacturer_id")
    @Expose
    @ColumnInfo(name = "manufacturer_id")
    public String manufacturerId;

    @SerializedName("manufacturer_nm")
    @Expose
    @ColumnInfo(name = "manufacturer_nm")
    public String manufacturerNm;

    @SerializedName("tovar_id")
    @Expose
    @ColumnInfo(name = "tovar_id")
    public String tovarId;

    @SerializedName("tovar_nm")
    @Expose
    @ColumnInfo(name = "tovar_nm")
    public String tovarNm;

    @SerializedName("ative")
    @Expose
    @ColumnInfo(name = "ative")
    public String ative;

    @SerializedName("active")
    @Expose
    @ColumnInfo(name = "active")
    public String active;

    @SerializedName("dvi")
    @Expose
    @ColumnInfo(name = "dvi")
    public String dvi;

    @SerializedName("about")
    @Expose
    @ColumnInfo(name = "about")
    public String about;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public String authorId;

    @SerializedName("author_fio")
    @Expose
    @ColumnInfo(name = "author_fio")
    public String authorFio;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public String dtUpdate;

    @SerializedName("achieve_photo_count__title")
    @Expose
    @ColumnInfo(name = "achieve_photo_count__title")
    public String achievePhotoCountTitle;
}
