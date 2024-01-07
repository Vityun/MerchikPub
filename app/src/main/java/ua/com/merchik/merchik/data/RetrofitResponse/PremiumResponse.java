package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PremiumResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("error")
    @Expose
    public String error;

    // Pika (это текст основания начисления - устарело)
    @SerializedName("basis")
    @Expose
    public String basis;

    @SerializedName("basis_list")
    @Expose
    public List<String> basisList;

    // Pika (Володя недавно добавил это - тут массив строк в том виде как они задуманы по новому принципу)
    // то есть в каждой строке в которой есть ссылка представленв в виде {строка ИД типа объекта|строка самого ИД объекта|строка отображения обънета|ИД площадки}
    @SerializedName("result_list")
    @Expose
    public List<Result> result_list;

    // Pika (класс расшифровки для result_list при получении данных их Джейсон)
   public class Result {
        @SerializedName("DocNom")
        @Expose
        public String docNom;
        @SerializedName("DocDat")
        @Expose
        public String docDat;
        @SerializedName("OtvKod")
        @Expose
        public String otvKod;
        @SerializedName("OtvNaim")
        @Expose
        public String otvNaim;
        @SerializedName("TemaStr")
        @Expose
        public String temaStr;
        @SerializedName("Osnovanie")
        @Expose
        public String osnovanie;
        @SerializedName("ZakStr")
        @Expose
        public String zakStr;
        @SerializedName("AdrStr")
        @Expose
        public String adrStr;
        @SerializedName("SumNZPSotrDoc")
        @Expose
        public long sumNZPSotrDoc;

    }
}


