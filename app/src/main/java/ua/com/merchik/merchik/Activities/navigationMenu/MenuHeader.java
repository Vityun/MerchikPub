package ua.com.merchik.merchik.Activities.navigationMenu;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;

public class MenuHeader {
    public MenuItemFromWebDB menuItemFromWebDB;
    public boolean isExapanded;
    public List<MenuItemFromWebDB> items;

    public MenuHeader(MenuItemFromWebDB menuItemFromWebDB, List<MenuItemFromWebDB> items) {
        this.menuItemFromWebDB = menuItemFromWebDB;
        isExapanded = false;
        this.items = items;
    }
}
