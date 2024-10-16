package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SiteAccountItemResponse {

    @SerializedName("ID")
    @Expose
    public Long id;

    @SerializedName("buy_sell")
    @Expose
    public String buySell;

    @SerializedName("author_id")
    @Expose
    public Long authorId;

    @SerializedName("dt_change")
    @Expose
    public Long dtChange;

    @SerializedName("import")
    @Expose
    public String importInt;

    @SerializedName("login")
    @Expose
    public String login;

    @SerializedName("nm")
    @Expose
    public String nm;

    @SerializedName("pass")
    @Expose
    public String pass;

    @SerializedName("phrase")
    @Expose
    public String phrase;

    @SerializedName("prefix")
    @Expose
    public String prefix;

    @SerializedName("site_url_id")
    @Expose
    public Long siteUrlId;
}
