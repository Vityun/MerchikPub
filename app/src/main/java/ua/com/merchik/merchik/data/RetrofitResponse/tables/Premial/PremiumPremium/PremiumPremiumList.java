package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PremiumPremiumList {
    @SerializedName("state")
    @Expose
    public boolean state;
    @SerializedName("total")
    @Expose
    public Total total;
    @SerializedName("detailed")
    @Expose
    public List<Detailed> detailed = null;
}
