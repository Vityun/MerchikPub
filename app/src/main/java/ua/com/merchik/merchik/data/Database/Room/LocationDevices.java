package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "location_devices")
public class LocationDevices {

    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    public Long id;

    @Nullable
    @SerializedName("dt_create")
    public Long dtCreate;

    @Nullable
    @SerializedName("author_id_create")
    public Long authorIdCreate;

    @Nullable
    @SerializedName("addr_id")
    public Long addrId;

    @Nullable
    @SerializedName("client_id")
    public String clientId;

    @Nullable
    @SerializedName("client_id_numeric")
    public Long clientIdNumeric;

    @Nullable
    @SerializedName("theme_id")
    public Long themeId;

    @Nullable
    @SerializedName("mac")
    public String mac;

    @Nullable
    @SerializedName("mac_numeric")
    public Long macNumeric;

    @Nullable
    @SerializedName("platform_id")
    public Long platformId;

    @Nullable
    @SerializedName("active")
    public Integer active;

    @Nullable
    @SerializedName("dt_update_active")
    public Long dtUpdateActive;

    @Nullable
    @SerializedName("author_id_active")
    public Long authorIdActive;

    @Nullable
    @SerializedName("lat")
    public Double lat;

    @Nullable
    @SerializedName("lon")
    public Double lon;

    @Nullable
    @SerializedName("dt_update_vpi")
    public Long dtUpdateVpi;

    @Nullable
    @SerializedName("author_id_update_vpi")
    public Long authorIdUpdateVpi;

    @Nullable
    @SerializedName("object_id")
    public Long objectId;

    @Nullable
    @SerializedName("object_theme_id")
    public Long objectThemeId;

    @Nullable
    @SerializedName("src_type")
    public String srcType;

    @Nullable
    @SerializedName("src_name")
    public String srcName;

    @Nullable
    @SerializedName("notes")
    public String notes;

    public LocationDevices(@NonNull Long id) {
        this.id = id;
    }

    // Room/Gson любят пустой конструктор
    public LocationDevices() {
    }
}
