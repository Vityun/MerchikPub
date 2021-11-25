package ua.com.merchik.merchik.data;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;

public class TEST_DATA <T>{

    public Integer type;

    public StackPhotoDB photo;
    public AddressDB address;
    public CustomerDB customer;
    public ThemeDB theme;
    public OpinionSDB opinion;
    public UsersSDB users;
    public String comment;

    public String testSpinner1;
    public String testSpinner2;

    public T data;
    public List<T> dataList;
}
