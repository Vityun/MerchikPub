package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.HashElements;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class WpDataServer {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("list")
    @Expose
    private java.util.List<WpDataDB> wp_data = null;

    @SerializedName("hash_elements")
    @Expose
    private HashElements hashElements;

    public HashElements getHashElements() {
        return hashElements;
    }

    public void setHashElements(HashElements hashElements) {
        this.hashElements = hashElements;
    }


    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public java.util.List<WpDataDB> getList() {
        // Access hashes arrive outside the rows, including in targeted plan requests.
        if (wp_data != null && hashElements != null && hashElements.getCodeDad2() != null) {
            for (WpDataDB row : wp_data) {
                if (row == null) continue;
                String hash = hashElements.getCodeDad2().get(String.valueOf(row.getCode_dad2()));
                if (hash != null && !hash.trim().isEmpty()) row.setCode_dad2_hash(hash);
            }
        }
        return wp_data;
    }

    public void setList(java.util.List<WpDataDB> list) {
        this.wp_data = list;
    }

}
