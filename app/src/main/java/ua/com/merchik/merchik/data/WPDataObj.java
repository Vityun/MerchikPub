package ua.com.merchik.merchik.data;

import java.io.Serializable;
import java.util.Map;

public class WPDataObj implements Serializable {

    private Long id;
    private String date;
    private String customerId;
    private Integer addressId;
    private String photoType;
    private Map<Integer, String> customerTypeGrp;
    private String customerTypeGrpS;
    private String docNum;
    private Integer themeId;
    private String photoUserId;
    private Long dad2;
    private String customerIdTxt;
    private String addressIdTxt;
    private Float latitude;
    private Float longitude;

    public WPDataObj() {
    }

    public WPDataObj(Long id, String date, String customerId, Integer addressId,
                     String photoType, Map<Integer, String> customerTypeGrp, String docNum,
                     Integer themeId, String photoUserId, Long dad2, String customerIdTxt,
                     String addressIdTxt, Float latitude, Float longitude){
        this.id = id;
        this.date = date;
        this.customerId = customerId;
        this.addressId = addressId;
        this.photoType = photoType;
        this.customerTypeGrp = customerTypeGrp;
        this.customerTypeGrpS = customerTypeGrpS;
        this.docNum = docNum;
        this.themeId = themeId;
        this.photoUserId = photoUserId;
        this.dad2 = dad2;
        this.customerIdTxt = customerIdTxt;
        this.addressIdTxt = addressIdTxt;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public Long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public String getPhotoType() {
        return photoType;
    }

    public Map<Integer, String> getCustomerTypeGrp() {
        return customerTypeGrp;
    }

    public String getDocNum() {
        return docNum;
    }

    public Integer getThemeId() {
        return themeId;
    }

    public String getPhotoUserId() {
        return photoUserId;
    }

    public String getCustomerIdTxt() {
        return customerIdTxt;
    }

    public String getAddressIdTxt() {
        return addressIdTxt;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }


    public Long getDad2() {
        return dad2;
    }

    public void setDad2(Long dad2) {
        this.dad2 = dad2;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public void setCustomerTypeGrp(Map<Integer, String> customerTypeGrp) {
        this.customerTypeGrp = customerTypeGrp;
    }

    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

    public void setThemeId(Integer themeId) {
        this.themeId = themeId;
    }

    public void setPhotoUserId(String photoUserId) {
        this.photoUserId = photoUserId;
    }

    public void setCustomerIdTxt(String customerIdTxt) {
        this.customerIdTxt = customerIdTxt;
    }

    public void setAddressIdTxt(String addressIdTxt) {
        this.addressIdTxt = addressIdTxt;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getCustomerTypeGrpS() {
        return customerTypeGrpS;
    }

    public void setCustomerTypeGrpS(String customerTypeGrpS) {
        this.customerTypeGrpS = customerTypeGrpS;
    }
}
