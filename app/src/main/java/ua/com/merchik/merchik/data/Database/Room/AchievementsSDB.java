package ua.com.merchik.merchik.data.Database.Room;

import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.AchievementsSDBOverride;

//@PrimaryKey(autoGenerate = true)
@Entity(tableName = "achievements", indices = {@Index(value = {"serverId"}, unique = true)})
public class AchievementsSDB implements DataObjectUI {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "serverId")
    @NonNull
    public Integer serverId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public String dt;       // Формат YYYY-MM-dd HH:mm

    @SerializedName("dt_ut")
    @Expose
    @ColumnInfo(name = "dt_ut")
    public Long dt_ut;      // Дата dt, но в формате юникст тайма

    @SerializedName("img_before_id")
    @Expose
    @ColumnInfo(name = "img_before_id")
    public Integer imgBeforeId;

    @SerializedName("img_before")
    @Expose
    @ColumnInfo(name = "img_before")
    public String imgBefore;

    @SerializedName("img_before_big")
    @Expose
    @ColumnInfo(name = "img_before_big")
    public String imgBeforeBig;

    @SerializedName("img_after_id")
    @Expose
    @ColumnInfo(name = "img_after_id")
    public Integer imgAfterId;

    @SerializedName("img_after")
    @Expose
    @ColumnInfo(name = "img_after")
    public String imgAfter;

    @SerializedName("img_after_big")
    @Expose
    @ColumnInfo(name = "img_after_big")
    public String imgAfterBig;

    @SerializedName("score")
    @Expose
    @ColumnInfo(name = "score")
    public String score;

    @SerializedName("score_who_nm")
    @Expose
    @ColumnInfo(name = "score_who_nm")
    public String scoreWhoNm;

    @SerializedName("score_dt")
    @Expose
    @ColumnInfo(name = "score_dt")
    public String scoreDt;

    @SerializedName("adresa_nm")
    @Expose
    @ColumnInfo(name = "adresa_nm")
    public String adresaNm;

    @SerializedName("adresa_addr")
    @Expose
    @ColumnInfo(name = "adresa_addr")
    public String adresaAddr;

    @SerializedName("adresa_tp")
    @Expose
    @ColumnInfo(name = "adresa_tp")
    public String adresaTp;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("spiskli_nm")
    @Expose
    @ColumnInfo(name = "spiskli_nm")
    public String spiskliNm;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long codeDad2;

    @SerializedName("user_id")
    @Expose
    @ColumnInfo(name = "user_id")
    public Integer userId;

    @SerializedName("sotr_fio")
    @Expose
    @ColumnInfo(name = "sotr_fio")
    public String sotrFio;

    @SerializedName("comment_dt")
    @Expose
    @ColumnInfo(name = "comment_dt")
    public String commentDt;

    @SerializedName("comment_txt")
    @Expose
    @ColumnInfo(name = "comment_txt")
    public String commentTxt;

    @SerializedName("comment_user")
    @Expose
    @ColumnInfo(name = "comment_user")
    public String commentUser;

    @SerializedName("prem_reason")
    @Expose
    @ColumnInfo(name = "prem_reason")
    public String premReason;

    @SerializedName("prem_amount")
    @Expose
    @ColumnInfo(name = "prem_amount")
    public String premAmount;

    @SerializedName("prem_amount_dt")
    @Expose
    @ColumnInfo(name = "prem_amount_dt")
    public String premAmountDt;

    @SerializedName("prem_sotr")
    @Expose
    @ColumnInfo(name = "prem_sotr")
    public String premSotr;

    @SerializedName("dvi")
    @Expose
    @ColumnInfo(name = "dvi")
    public Integer dvi;

    @SerializedName("confirm_state")
    @Expose
    @ColumnInfo(name = "confirm_state")
    public Integer confirmState;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer themeId;

    @SerializedName("img_before_hash")
    @Expose
    @ColumnInfo(name = "img_before_hash")
    public String img_before_hash;

    @SerializedName("img_after_hash")
    @Expose
    @ColumnInfo(name = "img_after_hash")
    public String img_after_hash;

    @SerializedName("add_requirement_id")
    @Expose
    @ColumnInfo(name = "add_requirement_id")
    public Integer addRequirementId;

    @SerializedName("manufacturer")
    @Expose
    @ColumnInfo(name = "manufacturer")
    public Integer manufacturer;

    @SerializedName("tovar_id")
    @Expose
    @ColumnInfo(name = "tovar_id")
    public Integer tovar_id;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Integer dt_change;

    // ---------- ДЛЯ ОПЦИИ КОНТРОЛЯ НАЧАЛО---------------

    @Ignore
    public Integer error;

    @Ignore
    public SpannableStringBuilder note;

    @Ignore
    public Integer currentVisit;
    // ---------- ДЛЯ ОПЦИИ КОНТРОЛЯ КОНЕЦ---------------



    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return AchievementsSDBOverride.INSTANCE.getTranslateId(key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return AchievementsSDBOverride.INSTANCE.getValueUI(key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getFieldModifier(this, key, jsonObject);
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
        return R.drawable.merchik;
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return "img_before_id, img_after_id";
    }


    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsForOrderOnUI(this);
    }

    @NonNull
    @Override
    public List<String> getPreferredFieldOrder() {
        return DataObjectUI.DefaultImpls.getPreferredFieldOrder(this);
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
