package ua.com.merchik.merchik.data.Database.Room.Planogram;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

@Entity(tableName = "planogram_vizit_showcase")
public class PlanogrammVizitShowcaseSDB implements DataObjectUI {

    @SerializedName("ID")
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Expose
    @NonNull
    public Integer id;  // Уникальный ИД таблицы БД (автоприращение)


    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt; // Время визита в Юникс


    @SerializedName("isp")
    @Expose
    @ColumnInfo(name = "isp")
    public String isp;  // Код фирмы (строка)


    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String client_id;    // Код заказчика (строка)


    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addr_id;  // Код адреса (число)


    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long code_dad2;    // Код ДАД2 (число)

    @SerializedName("planogram_id")
    @Expose
    @ColumnInfo(name = "planogram_id")
    public Integer planogram_id;     // ИД планограммы

    @SerializedName("planogram_photo_id")
    @Expose
    @ColumnInfo(name = "planogram_photo_id")
    public Integer planogram_photo_id;  // ИД фото планограммы (ИД идентификатора планограммы)

    @SerializedName("showcase_id")
    @Expose
    @ColumnInfo(name = "showcase_id")
    public Integer showcase_id;  // ИД витрины

    @SerializedName("showcase_photo_id")
    @Expose
    @ColumnInfo(name = "showcase_photo_id")
    public Integer showcase_photo_id;  // ИД фото витрины (ИД идентификатора витрины)

    @SerializedName("photo_do_id")
    @Expose
    @ColumnInfo(name = "photo_do_id")
    public Integer photo_do_id;  // Код фото ДО

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer theme_id;  // Код темы (число)

    @SerializedName("option_id")
    @Expose
    @ColumnInfo(name = "option_id")
    public Integer option_id;  // Код опции (число)

    @SerializedName("comments")
    @Expose
    @ColumnInfo(name = "comments")
    public String comments;  // Комментарий (строка 200)

    @SerializedName("object_a")
    @Expose
    @ColumnInfo(name = "object_a")
    public Integer object_a;  // Код объекта А (число)

    @SerializedName("object_a_theme_id")
    @Expose
    @ColumnInfo(name = "object_a_theme_id")
    public Integer object_a_theme_id;  // Код темы объекта А (число)

    @SerializedName("object_b")
    @Expose
    @ColumnInfo(name = "object_b")
    public Integer object_b;  // Код объекта Б (число)

    @SerializedName("object_b_theme_id")
    @Expose
    @ColumnInfo(name = "object_b_theme_id")
    public Integer object_b_theme_id;  // Код темы объекта Б (число)

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer author_id;  // Код автора изменений в БДСайта

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public String dt_update;  // Время последнего изменения

    @SerializedName("kol")
    @Expose
    @ColumnInfo(name = "kol")
    public Integer kol;  // Количество (для суммирования при свертке)

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return DataObjectUI.DefaultImpls.getFieldTranslateId(this, key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return DataObjectUI.DefaultImpls.getValueUI(this, key, value);
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

}
