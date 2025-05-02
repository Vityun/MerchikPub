package ua.com.merchik.merchik.data.Database.Room;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteURLItemResponse;

@Entity(tableName = "site_url")
public class SiteUrlSDB {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Long id;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Long authorId;

    @SerializedName("comment")
    @Expose
    @ColumnInfo(name = "comment")
    public String comment;

    @SerializedName("country_id")
    @Expose
    @ColumnInfo(name = "country_id")
    public Long countryId;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("import")
    @Expose
    @ColumnInfo(name = "import")
    public String importInt;

    @SerializedName("phrase")
    @Expose
    @ColumnInfo(name = "phrase")
    public String phrase;

    @SerializedName("tovar_grp_id")
    @Expose
    @ColumnInfo(name = "tovar_grp_id")
    public Long tovarGrpId;

    @SerializedName("url")
    @Expose
    @ColumnInfo(name = "url")
    public String url;

    @SerializedName("whitelist")
    @Expose
    @ColumnInfo(name = "whitelist")
    public String whitelist;

    public SiteUrlSDB() {}

    public SiteUrlSDB(SiteURLItemResponse from) {
        id = from.id;
        authorId = from.authorId;
        comment = from.comment;
        countryId = from.countryId;
        dtChange = from.dtChange;
        importInt = from.importInt;
        phrase = from.phrase;
        tovarGrpId = from.tovarGrpId;
        url = from.url;
        whitelist = from.whitelist;
    }
}
