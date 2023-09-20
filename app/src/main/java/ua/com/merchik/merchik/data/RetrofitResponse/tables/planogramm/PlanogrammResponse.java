package ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.PlanogrammSDB;

public class PlanogrammResponse {
    public Boolean state;
    public List<PlanogrammSDB> list;
    public String error;
}
