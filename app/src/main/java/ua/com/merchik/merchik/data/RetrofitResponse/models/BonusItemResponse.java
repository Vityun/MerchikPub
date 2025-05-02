package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BonusItemResponse {

    @SerializedName("ID")
    @Expose
    public Long id;

    @SerializedName("author_id")
    @Expose
    public Long authorId;

    @SerializedName("dt_change")
    @Expose
    public Long dtChange;

    @SerializedName("percent")
    @Expose
    public String percent;

    @SerializedName("theme_id")
    @Expose
    public Long themeId;

    @SerializedName("option_id")
    @Expose
    public Long optionId;

    @SerializedName("so")
    @Expose
    public Integer so;
}
