package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PremiumPremium {
    @SerializedName("state")
    @Expose
    public boolean state;
    @SerializedName("list")
    @Expose
    public PremiumPremiumList list;

}
