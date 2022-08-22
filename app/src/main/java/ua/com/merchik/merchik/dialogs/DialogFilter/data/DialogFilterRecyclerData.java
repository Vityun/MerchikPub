package ua.com.merchik.merchik.dialogs.DialogFilter.data;

import java.util.List;

import ua.com.merchik.merchik.Globals;

public class DialogFilterRecyclerData <T>{
    public FilterTypes filterType;
    public String msg;
    public Globals.ViewHolderDataType dataType;
    public List<T> dataList;
}
