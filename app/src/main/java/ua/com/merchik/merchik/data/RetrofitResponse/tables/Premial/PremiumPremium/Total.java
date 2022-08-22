package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Total {
    @SerializedName("FirKod")
    @Expose
    public String firKod;
    @SerializedName("SotKod")
    @Expose
    public String sotKod;
    @SerializedName("NachOst")
    @Expose
    public double nachOst;
    @SerializedName("Prihod")
    @Expose
    public double prihod;
    @SerializedName("Rashod")
    @Expose
    public double rashod;
    @SerializedName("KonOst")
    @Expose
    public double konOst;

}
