package ua.com.merchik.merchik.data.Database.Room;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteAccountItemResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteURLItemResponse;

@Entity(tableName = "site_account")
public class SiteAccountSDB {

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

    @SerializedName("buy_sell")
    @Expose
    @ColumnInfo(name = "buy_sell")
    public String buySell;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("import")
    @Expose
    @ColumnInfo(name = "import")
    public String importInt;

    @SerializedName("login")
    @Expose
    @ColumnInfo(name = "login")
    public String login;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("pass")
    @Expose
    @ColumnInfo(name = "pass")
    public String pass;

    @SerializedName("phrase")
    @Expose
    @ColumnInfo(name = "phrase")
    public String phrase;

    @SerializedName("prefix")
    @Expose
    @ColumnInfo(name = "prefix")
    public String prefix;

    @SerializedName("site_url_id")
    @Expose
    @ColumnInfo(name = "site_url_id")
    public Long siteUrlId;


    public SiteAccountSDB() {}

    public SiteAccountSDB(SiteAccountItemResponse from) {
        id = from.id;
        authorId = from.authorId;
        buySell = from.buySell;
        dtChange = from.dtChange;
        importInt = from.importInt;
        login = from.login;
        nm = from.nm;
        pass = from.pass;
        phrase = from.phrase;
        prefix = from.prefix;
        siteUrlId = from.siteUrlId;
    }
}
