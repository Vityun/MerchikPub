package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SiteURLItemResponse {

    @SerializedName("ID")
    @Expose
    public Long id;

    @SerializedName("author_id")
    @Expose
    public Long authorId;

    @SerializedName("comment")
    @Expose
    public String comment;

    @SerializedName("country_id")
    @Expose
    public Long countryId;

    @SerializedName("dt_change")
    @Expose
    public Long dtChange;

    @SerializedName("import")
    @Expose
    public String importInt;

    @SerializedName("phrase")
    @Expose
    public String phrase;

    @SerializedName("tovar_grp_id")
    @Expose
    public Long tovarGrpId;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("whitelist")
    @Expose
    public String whitelist;
}
