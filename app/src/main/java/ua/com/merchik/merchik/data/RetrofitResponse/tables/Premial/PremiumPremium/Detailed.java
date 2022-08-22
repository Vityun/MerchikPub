package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Detailed {
    @SerializedName("FirKod")
    @Expose
    public String firKod;
    @SerializedName("SotKod")
    @Expose
    public String sotKod;
    @SerializedName("DocDat")
    @Expose
    public String docDat;
    @SerializedName("DocNom")
    @Expose
    public String docNom;
    @SerializedName("DocDef")
    @Expose
    public long docDef;
    @SerializedName("Prihod")
    @Expose
    public double prihod;
    @SerializedName("Rashod")
    @Expose
    public double rashod;
    @SerializedName("DocDefName")
    @Expose
    public String docDefName;
}
