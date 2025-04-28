package ua.com.merchik.merchik.data.Database.Room.Planogram;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.AdditionalRequirementsDBOverride;
import ua.com.merchik.merchik.features.main.PlanogrammVizitShowcaseSDBOverride;

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

    @SerializedName("photo_do_hash")
    @Expose
    @ColumnInfo(name = "photo_do_hash")
    public String photo_do_hash;  // добавленно 18.04, на случай когда делаем фото и сразу добавляем в планограму не дожидаясь обмена и получения у фото photoServerId

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

    @Ignore
    public String score;

    @Ignore
    public String color;

    //    @SerializedName("uploadStatus")
//    @Expose
    @ColumnInfo(name = "uploadStatus")
    public Integer uploadStatus; // 0 = не надо выгружать/уже выгруженно, 1 = надо загрузит на сервер

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return "ID, dt, isp, client_id, addr_id, code_dad2, planogram_id, planogram_photo_id, " +
                "showcase_id, showcase_photo_id, photo_do_id, theme_id, option_id, object_a, " +
                "object_a_theme_id, object_b, object_b_theme_id, author_id, dt_update, kol, score";
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
        return PlanogrammVizitShowcaseSDBOverride.INSTANCE.getValueModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return PlanogrammVizitShowcaseSDBOverride.INSTANCE.getContainerModifier(jsonObject);
//        return DataObjectUI.DefaultImpls.getContainerModifier(this, jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return R.drawable.merchik;
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return "planogram_photo_id, showcase_photo_id, photo_do_id";
    }

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        String[] parts = "Планограма, Вітрина, Фото вітрини до п.р.".split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }

    public PlanogrammVizitShowcaseSDB copy() {
        PlanogrammVizitShowcaseSDB copy = new PlanogrammVizitShowcaseSDB();
        copy.id = this.id;
        copy.dt = this.dt;
        copy.isp = this.isp;
        copy.client_id = this.client_id;
        copy.addr_id = this.addr_id;
        copy.code_dad2 = this.code_dad2;
        copy.planogram_id = this.planogram_id;
        copy.planogram_photo_id = this.planogram_photo_id;
        copy.showcase_id = this.showcase_id;
        copy.showcase_photo_id = this.showcase_photo_id;
        copy.photo_do_id = this.photo_do_id;
        copy.photo_do_hash = this.photo_do_hash;
        copy.theme_id = this.theme_id;
        copy.option_id = this.option_id;
        copy.comments = this.comments;
        copy.object_a = this.object_a;
        copy.object_a_theme_id = this.object_a_theme_id;
        copy.object_b = this.object_b;
        copy.object_b_theme_id = this.object_b_theme_id;
        copy.author_id = this.author_id;
        copy.dt_update = this.dt_update;
        copy.kol = this.kol;
        copy.score = this.score;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanogrammVizitShowcaseSDB that = (PlanogrammVizitShowcaseSDB) o;

        return
                Objects.equals(id, that.id) &&
                        Objects.equals(dt, that.dt) &&
                        Objects.equals(isp, that.isp) &&
                        Objects.equals(client_id, that.client_id) &&
                        Objects.equals(addr_id, that.addr_id) &&
                        Objects.equals(code_dad2, that.code_dad2) &&
                        Objects.equals(planogram_id, that.planogram_id) &&
                        Objects.equals(planogram_photo_id, that.planogram_photo_id) &&
                        Objects.equals(showcase_id, that.showcase_id) &&
                        Objects.equals(showcase_photo_id, that.showcase_photo_id) &&
                        Objects.equals(photo_do_id, that.photo_do_id) &&
                        areValidHashesEqual(photo_do_hash, that.photo_do_hash) &&
//                        Objects.equals(photo_do_hash, that.photo_do_hash) && that.photo_do_hash != null && !that.photo_do_hash.isEmpty() && !that.photo_do_hash.equals("0") &&
//                        that.photo_do_hash != null &&
//                         !that.photo_do_hash.isEmpty() &&
//                        && !that.photo_do_hash.equals("0")
//                        Objects.equals(photo_do_hash, that.photo_do_hash) &&
                        Objects.equals(theme_id, that.theme_id) &&
                        Objects.equals(option_id, that.option_id) &&
                        Objects.equals(comments, that.comments) &&
                        Objects.equals(object_a, that.object_a) &&
                        Objects.equals(object_a_theme_id, that.object_a_theme_id) &&
                        Objects.equals(object_b, that.object_b) &&
                        Objects.equals(object_b_theme_id, that.object_b_theme_id) &&
                        Objects.equals(author_id, that.author_id) &&
                        Objects.equals(dt_update, that.dt_update) &&
                        Objects.equals(kol, that.kol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, dt, isp, client_id, addr_id, code_dad2, planogram_id,
                planogram_photo_id, showcase_id, showcase_photo_id, photo_do_id,
                theme_id, option_id, comments, object_a, object_a_theme_id,
                object_b, object_b_theme_id, author_id, dt_update, kol
        );
    }

    private boolean areValidHashesEqual(String a, String b) {
        // нормализуем: если пустая строка, "0" или null — считаем как null
        a = normalizeHash(a);
        b = normalizeHash(b);

        return Objects.equals(a, b);
    }

    private String normalizeHash(String hash) {
        if (hash == null) return null;
        hash = hash.trim();
        if (hash.isEmpty() || hash.equals("0")) return null;
        return hash;
    }
}
