package ua.com.merchik.merchik.data.DataFromServer.PhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MenuList {
    @SerializedName("mod")
    @Expose
    private List<Mod> mod = null;
    @SerializedName("act")
    @Expose
    private List<Act> act = null;
    @SerializedName("client_id")
    @Expose
    private List<ClientId> clientId = null;
    @SerializedName("addr_id")
    @Expose
    private List<AddrId> addrId = null;
    @SerializedName("client_tovar_group")
    @Expose
    private List<ClientTovarGroup> clientTovarGroup = null;
    @SerializedName("images_type_list")
    @Expose
    private List<ImagesTypeList> imagesTypeList = null;
    @SerializedName("only_selected")
    @Expose
    private List<OnlySelected> onlySelected = null;

    public List<Mod> getMod() {
        return mod;
    }

    public void setMod(List<Mod> mod) {
        this.mod = mod;
    }

    public List<Act> getAct() {
        return act;
    }

    public void setAct(List<Act> act) {
        this.act = act;
    }

    public List<ClientId> getClientId() {
        return clientId;
    }

    public void setClientId(List<ClientId> clientId) {
        this.clientId = clientId;
    }

    public List<AddrId> getAddrId() {
        return addrId;
    }

    public void setAddrId(List<AddrId> addrId) {
        this.addrId = addrId;
    }

    public List<ClientTovarGroup> getClientTovarGroup() {
        return clientTovarGroup;
    }

    public void setClientTovarGroup(List<ClientTovarGroup> clientTovarGroup) {
        this.clientTovarGroup = clientTovarGroup;
    }

    public List<ImagesTypeList> getImagesTypeList() {
        return imagesTypeList;
    }

    public void setImagesTypeList(List<ImagesTypeList> imagesTypeList) {
        this.imagesTypeList = imagesTypeList;
    }

    public List<OnlySelected> getOnlySelected() {
        return onlySelected;
    }

    public void setOnlySelected(List<OnlySelected> onlySelected) {
        this.onlySelected = onlySelected;
    }
}
