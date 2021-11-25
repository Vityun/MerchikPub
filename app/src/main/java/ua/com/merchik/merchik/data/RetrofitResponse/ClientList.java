package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.GroupTypeDB;

public class ClientList {

    @SerializedName("images_type_list")
    @Expose
    private List<GroupTypeDB> groupTypeDBList = null;

    public List<GroupTypeDB> getGroupTypeDBList() {
        return groupTypeDBList;
    }

    public void setGroupTypeDBList(List<GroupTypeDB> groupTypeDBList) {
        this.groupTypeDBList = groupTypeDBList;
    }

}



