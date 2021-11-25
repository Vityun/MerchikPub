package ua.com.merchik.merchik.data;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;

public class TestViewHolderData <T>{
    /*
    * Тип ViewHolder-а. Сделано для того что б можно было собирать с разных элементов RecyclerView
    *
    * Тип 0 -- Фото, инфа о фото.
    * Тип 1 -- Автотекст
    * Тип 2 -- Просто правка текста
    * Тип 3 -- Кнопка
    * Тип 4 -- Спинер
    * */
    public int typeNumber;
    public Globals.NewTARDataType type;
    public String msg;

    public StackPhotoDB photo;
    public List<AddressDB> addressList;
    public List<CustomerDB> customerList;
    public List<ThemeDB> themeList;
    public List<OpinionSDB> opinionList;
    public List<T> dataList;

    public TestViewHolderData() {
    }

}
