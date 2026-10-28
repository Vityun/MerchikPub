package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "plan_results_dynamic", indices = {
        @Index(value = {"yearmonth", "client_id", "addr_id"}, unique = true)
})
public class ResultsDynamicSDB {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public String id = "";

    @SerializedName("isp")
    @Expose
    @ColumnInfo(name = "isp")
    public String isp;

    @NonNull
    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId = "";

    @NonNull
    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public String addrId = "";

    @NonNull
    @SerializedName("yearmonth")
    @Expose
    @ColumnInfo(name = "yearmonth")
    public String yearmonth = "";

    @SerializedName("ob_prod_plan")
    @Expose
    @ColumnInfo(name = "ob_prod_plan")
    public String obProdPlan;

    @SerializedName("avg_num_plan")
    @Expose
    @ColumnInfo(name = "avg_num_plan")
    public String avgNumPlan;

    @SerializedName("num_main_plan")
    @Expose
    @ColumnInfo(name = "num_main_plan")
    public String numMainPlan;

    @SerializedName("ob_prod_done")
    @Expose
    @ColumnInfo(name = "ob_prod_done")
    public String obProdDone;

    @SerializedName("avg_num_done")
    @Expose
    @ColumnInfo(name = "avg_num_done")
    public String avgNumDone;

    @SerializedName("num_main_done")
    @Expose
    @ColumnInfo(name = "num_main_done")
    public String numMainDone;

    @SerializedName("avg_oos")
    @Expose
    @ColumnInfo(name = "avg_oos")
    public String avgOos;

    @SerializedName("total_face")
    @Expose
    @ColumnInfo(name = "total_face")
    public String totalFace;

    @SerializedName("total_sku")
    @Expose
    @ColumnInfo(name = "total_sku")
    public String totalSku;

    @SerializedName("total_documents")
    @Expose
    @ColumnInfo(name = "total_documents")
    public String totalDocuments;

    @SerializedName("last_phot")
    @Expose
    @ColumnInfo(name = "last_phot")
    public String lastPhot;
}
