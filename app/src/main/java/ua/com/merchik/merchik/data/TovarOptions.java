package ua.com.merchik.merchik.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.Globals;

public class TovarOptions {

    private List<Integer> optionId;
    private String optionShort;
    private String optionLong;
    private String orderField;
    private String optionType;
    private Globals.OptionControlName optionControlName;

    public TovarOptions() {
    }

    public TovarOptions(List<Integer> optionId, String optionShort, String optionLong, String orderField, String optionType) {
        this.optionId = optionId;
        this.optionShort = optionShort;
        this.optionLong = optionLong;
        this.orderField = orderField;
        this.optionType = optionType;
    }

    public TovarOptions(Globals.OptionControlName optionControlName, String optionShort, String optionLong, String orderField, String optionType, int... array) {
        this.optionControlName = optionControlName;
        this.optionShort = optionShort;
        this.optionLong = optionLong;
        this.orderField = orderField;
        this.optionType = optionType;
        this.optionId = new ArrayList<Integer>(array.length);
        for (int i : array) {
            optionId.add(i);
        }
    }

    public TovarOptions(int id){
        this.optionId = new ArrayList<Integer>();
        optionId.add(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TovarOptions that = (TovarOptions) o;

        for(Integer i : optionId){
            for(Integer j : that.optionId){
                if(i.equals(j)) return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, optionShort, optionLong, orderField, optionType);
    }

    public List<Integer> getOptionId() {
        return optionId;
    }

    public void setOptionId(List<Integer> optionId) {
        this.optionId = optionId;
    }

    public String getOptionShort() {
        return optionShort;
    }

    public void setOptionShort(String optionShort) {
        this.optionShort = optionShort;
    }

    public String getOptionLong() {
        return optionLong;
    }

    public void setOptionLong(String optionLong) {
        this.optionLong = optionLong;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public Globals.OptionControlName getOptionControlName() {
        return optionControlName;
    }

    public void setOptionControlName(Globals.OptionControlName optionControlName) {
        this.optionControlName = optionControlName;
    }
}
