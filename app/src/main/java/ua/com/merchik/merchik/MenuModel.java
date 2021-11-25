package ua.com.merchik.merchik;

import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;

public class MenuModel {
    public String menuName;
    public boolean hasChildren, isGroup;
    public MenuItemFromWebDB menuItemFromWebDB;

    public MenuModel(String menuName, boolean isGroup, boolean hasChildren, MenuItemFromWebDB item) {

        this.menuName = menuName;
        this.menuItemFromWebDB = item;
        this.isGroup = isGroup;
        this.hasChildren = hasChildren;
    }
}
