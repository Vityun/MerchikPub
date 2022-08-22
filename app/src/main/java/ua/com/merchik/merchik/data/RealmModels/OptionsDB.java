package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class OptionsDB extends RealmObject {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String iD;
    @SerializedName("client_id")
    @Expose
    private String clientId;
    @SerializedName("addr_id")
    @Expose
    private String addrId;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("dt")
    @Expose
    private String dt;
    @SerializedName("code_dad2")
    @Expose
    private String codeDad2;
    @SerializedName("doc_id")
    @Expose
    private String docId;
    @SerializedName("doc_type")
    @Expose
    private String docType;
    @SerializedName("option_id")
    @Expose
    private String optionId;
    @SerializedName("option_control_id")
    @Expose
    private String optionControlId;

    @SerializedName("option_block_1")
    @Expose
    private String optionBlock1;

    @SerializedName("option_block_2")
    @Expose
    private String optionBlock2;

    @SerializedName("priznak")
    @Expose
    private String priznak;
    @SerializedName("proveden")
    @Expose
    private String proveden;
    @SerializedName("deleted")
    @Expose
    private String deleted;
    @SerializedName("lesson_id")
    @Expose
    private String lessonId;
    @SerializedName("so")
    @Expose
    private Integer so;
    @SerializedName("is_signal")
    @Expose
    private String isSignal;
    @SerializedName("notes")
    @Expose
    private String notes;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("dt_change")
    @Expose
    private String dtChange;
    @SerializedName("sum_premiya")
    @Expose
    private String sumPremiya;
    @SerializedName("sum_penalty")
    @Expose
    private String sumPenalty;
    @SerializedName("amount")
    @Expose
    private String amount;
    @SerializedName("amount_min")
    @Expose
    private String amountMin;
    @SerializedName("amount_max")
    @Expose
    private String amountMax;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("percent")
    @Expose
    private String percent;
    @SerializedName("block_pns")
    @Expose
    private String blockPns;
    @SerializedName("key_option")
    @Expose
    private String keyOption;
    @SerializedName("option_txt")
    @Expose
    private String optionTxt;
    @SerializedName("option_descr")
    @Expose
    private String optionDescr;
    @SerializedName("option_control_txt")
    @Expose
    private String optionControlTxt;
    @SerializedName("option_control_descr")
    @Expose
    private String optionControlDescr;
    @SerializedName("option_group")
    @Expose
    private String optionGroup;
    @SerializedName("option_group_txt")
    @Expose
    private String optionGroupTxt;

    public OptionsDB() {
    }

    public OptionsDB(String iD, String clientId, String addrId, String userId, String dt, String codeDad2, String docId, String docType, String optionId, String optionControlId, String priznak, String proveden, String deleted, String lessonId, Integer so, String isSignal, String notes, String authorId, String dtChange, String sumPremiya, String sumPenalty, String amount, String amountMin, String amountMax, String price, String percent, String blockPns, String keyOption, String optionTxt, String optionDescr, String optionControlTxt, String optionControlDescr, String optionGroup, String optionGroupTxt) {
        this.iD = iD;
        this.clientId = clientId;
        this.addrId = addrId;
        this.userId = userId;
        this.dt = dt;
        this.codeDad2 = codeDad2;
        this.docId = docId;
        this.docType = docType;
        this.optionId = optionId;
        this.optionControlId = optionControlId;
        this.priznak = priznak;
        this.proveden = proveden;
        this.deleted = deleted;
        this.lessonId = lessonId;
        this.so = so;
        this.isSignal = isSignal;
        this.notes = notes;
        this.authorId = authorId;
        this.dtChange = dtChange;
        this.sumPremiya = sumPremiya;
        this.sumPenalty = sumPenalty;
        this.amount = amount;
        this.amountMin = amountMin;
        this.amountMax = amountMax;
        this.price = price;
        this.percent = percent;
        this.blockPns = blockPns;
        this.keyOption = keyOption;
        this.optionTxt = optionTxt;
        this.optionDescr = optionDescr;
        this.optionControlTxt = optionControlTxt;
        this.optionControlDescr = optionControlDescr;
        this.optionGroup = optionGroup;
        this.optionGroupTxt = optionGroupTxt;
    }

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAddrId() {
        return addrId;
    }

    public void setAddrId(String addrId) {
        this.addrId = addrId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getCodeDad2() {
        return codeDad2;
    }

    public void setCodeDad2(String codeDad2) {
        this.codeDad2 = codeDad2;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getOptionControlId() {
        return optionControlId;
    }

    public void setOptionControlId(String optionControlId) {
        this.optionControlId = optionControlId;
    }

    public String getOptionBlock1() {
        return optionBlock1;
    }

    public void setOptionBlock1(String optionBlock1) {
        this.optionBlock1 = optionBlock1;
    }

    public String getOptionBlock2() {
        return optionBlock2;
    }

    public void setOptionBlock2(String optionBlock2) {
        this.optionBlock2 = optionBlock2;
    }

    public String getPriznak() {
        return priznak;
    }

    public void setPriznak(String priznak) {
        this.priznak = priznak;
    }

    public String getProveden() {
        return proveden;
    }

    public void setProveden(String proveden) {
        this.proveden = proveden;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public Integer getSo() {
        return so;
    }

    public void setSo(Integer so) {
        this.so = so;
    }

    public String getIsSignal() {
        return isSignal;
    }

    public void setIsSignal(String isSignal) {
        this.isSignal = isSignal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getDtChange() {
        return dtChange;
    }

    public void setDtChange(String dtChange) {
        this.dtChange = dtChange;
    }

    public String getSumPremiya() {
        return sumPremiya;
    }

    public void setSumPremiya(String sumPremiya) {
        this.sumPremiya = sumPremiya;
    }

    public String getSumPenalty() {
        return sumPenalty;
    }

    public void setSumPenalty(String sumPenalty) {
        this.sumPenalty = sumPenalty;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmountMin() {
        return amountMin;
    }

    public void setAmountMin(String amountMin) {
        this.amountMin = amountMin;
    }

    public String getAmountMax() {
        return amountMax;
    }

    public void setAmountMax(String amountMax) {
        this.amountMax = amountMax;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getBlockPns() {
        return blockPns;
    }

    public void setBlockPns(String blockPns) {
        this.blockPns = blockPns;
    }

    public String getKeyOption() {
        return keyOption;
    }

    public void setKeyOption(String keyOption) {
        this.keyOption = keyOption;
    }

    public String getOptionTxt() {
        return optionTxt;
    }

    public void setOptionTxt(String optionTxt) {
        this.optionTxt = optionTxt;
    }

    public String getOptionDescr() {
        return optionDescr;
    }

    public void setOptionDescr(String optionDescr) {
        this.optionDescr = optionDescr;
    }

    public String getOptionControlTxt() {
        return optionControlTxt;
    }

    public void setOptionControlTxt(String optionControlTxt) {
        this.optionControlTxt = optionControlTxt;
    }

    public String getOptionControlDescr() {
        return optionControlDescr;
    }

    public void setOptionControlDescr(String optionControlDescr) {
        this.optionControlDescr = optionControlDescr;
    }

    public String getOptionGroup() {
        return optionGroup;
    }

    public void setOptionGroup(String optionGroup) {
        this.optionGroup = optionGroup;
    }

    public String getOptionGroupTxt() {
        return optionGroupTxt;
    }

    public void setOptionGroupTxt(String optionGroupTxt) {
        this.optionGroupTxt = optionGroupTxt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OptionsDB optionsDB = (OptionsDB) o;

        return iD.equals(optionsDB.iD);
    }

    @Override
    public int hashCode() {
        return iD.hashCode();
    }

}
