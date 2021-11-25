package ua.com.merchik.merchik.data.DataFromServer.PhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoData {
    @SerializedName("menu_list")
    @Expose
    private MenuList menuList;

    public MenuList getMenuList() {
        return menuList;
    }

    public void setMenuList(MenuList menuList) {
        this.menuList = menuList;
    }
}
