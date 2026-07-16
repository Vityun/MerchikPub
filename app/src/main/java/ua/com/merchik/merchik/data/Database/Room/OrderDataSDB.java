package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.OrderDataSDBOverride;

@Entity(tableName = "order_data")
public class OrderDataSDB implements DataObjectUI {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @SerializedName("order_id")
    @Expose
    @ColumnInfo(name = "order_id")
    public String orderId;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("isp_id")
    @Expose
    @ColumnInfo(name = "isp_id")
    public String ispId;

    @SerializedName("order_type")
    @Expose
    @ColumnInfo(name = "order_type")
    public String orderType;

    @SerializedName("order_type_id")
    @Expose
    @ColumnInfo(name = "order_type_id")
    public String orderTypeId;

    @SerializedName("dt_create")
    @Expose
    @ColumnInfo(name = "dt_create")
    public String dtCreate;

    @SerializedName("dt_smeta_ymd")
    @Expose
    @ColumnInfo(name = "dt_smeta_ymd")
    public String dtSmetaYmd;

    @SerializedName("smeta_id")
    @Expose
    @ColumnInfo(name = "smeta_id")
    public String smetaId;

    @SerializedName("date_from_ymd")
    @Expose
    @ColumnInfo(name = "date_from_ymd")
    public String dateFromYmd;

    @SerializedName("date_to_ymd")
    @Expose
    @ColumnInfo(name = "date_to_ymd")
    public String dateToYmd;

    @SerializedName("time_start")
    @Expose
    @ColumnInfo(name = "time_start")
    public String timeStart;

    @SerializedName("time_end")
    @Expose
    @ColumnInfo(name = "time_end")
    public String timeEnd;

    @SerializedName("work_cnt")
    @Expose
    @ColumnInfo(name = "work_cnt")
    public String workCnt;

    @SerializedName("price_plan")
    @Expose
    @ColumnInfo(name = "price_plan")
    public String pricePlan;

    @SerializedName("price_act")
    @Expose
    @ColumnInfo(name = "price_act")
    public String priceAct;

    @SerializedName("invoice_id")
    @Expose
    @ColumnInfo(name = "invoice_id")
    public String invoiceId;

    @SerializedName("price_paid")
    @Expose
    @ColumnInfo(name = "price_paid")
    public String pricePaid;

    @SerializedName("order_status")
    @Expose
    @ColumnInfo(name = "order_status")
    public String orderStatus;

    @SerializedName("order_status_txt")
    @Expose
    @ColumnInfo(name = "order_status_txt")
    public String orderStatusTxt;

    @SerializedName("comment")
    @Expose
    @ColumnInfo(name = "comment")
    public String comment;

    public void normalizeId() {
        if (id == null || id.trim().isEmpty()) {
            id = orderId == null ? "" : orderId;
        }
    }

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return OrderDataSDBOverride.INSTANCE.getHidedFieldsOnUI();
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return OrderDataSDBOverride.INSTANCE.getTranslateId(key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return OrderDataSDBOverride.INSTANCE.getValueUI(key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getFieldModifier(this, key, jsonObject);
//        return OrderDataSDBOverride.INSTANCE.getFieldModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getValueModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getValueModifier(this, key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getContainerModifier(this, jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return DataObjectUI.DefaultImpls.getIdResImage(this);
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsImageOnUI(this);
    }

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsForOrderOnUI(this);
    }

    @NonNull
    @Override
    public List<String> getPreferredFieldOrder() {
        return Arrays.asList(
                "order_id",
                "order_status_txt",
                "order_type",
                "client_id",
                "isp_id",
                "date_from_ymd",
                "date_to_ymd",
                "work_cnt",
                "price_plan",
                "comment"
        );
    }

    @Override
    public @NotNull String getCommentsForImage() {
        return DataObjectUI.DefaultImpls.getCommentsForImage(this);
    }

    @Override
    public @Nullable String getCommentForImageValue(@NotNull String key, @NotNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getCommentForImageValue(this, key, jsonObject);
    }
}
