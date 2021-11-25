package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TasksAndReclamationsDB extends RealmObject {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String id;
    @SerializedName("tp")
    @Expose
    private String tp;
    @SerializedName("dt")
    @Expose
    private String dt;
    @SerializedName("dt_real_post")
    @Expose
    private String dtRealPost;
    @SerializedName("dt_change")
    @Expose
    private String dtChange;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("addr")
    @Expose
    private String addr;
    @SerializedName("client")
    @Expose
    private String client;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("photo_2")
    @Expose
    private String photo2;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("vinovnik")
    @Expose
    private String vinovnik;
    @SerializedName("vinovnik2")
    @Expose
    private String vinovnik2;
    @SerializedName("vinovnik_read_dt")
    @Expose
    private String vinovnikReadDt;
    @SerializedName("zamena_user_id")
    @Expose
    private String zamenaUserId;
    @SerializedName("zamena_dt")
    @Expose
    private String zamenaDt;
    @SerializedName("zamena_who")
    @Expose
    private String zamenaWho;
    @SerializedName("contacter_id")
    @Expose
    private String contacterId;
    @SerializedName("super_id")
    @Expose
    private String superId;
    @SerializedName("territorial_id")
    @Expose
    private String territorialId;
    @SerializedName("regional_id")
    @Expose
    private String regionalId;
    @SerializedName("nop_id")
    @Expose
    private String nopId;
    @SerializedName("dvi")
    @Expose
    private String dvi;
    @SerializedName("zakazchik")
    @Expose
    private String zakazchik;
    @SerializedName("id_1c")
    @Expose
    private String id1c;
    @SerializedName("doc_num_1c_id")
    @Expose
    private String docNum1cId;
    @SerializedName("code_dad2")
    @Expose
    private String codeDad2;
    @SerializedName("code_dad2_src_doc")
    @Expose
    private String codeDad2SrcDoc;
    @SerializedName("tel_num")
    @Expose
    private String telNum;
    @SerializedName("last_answer")
    @Expose
    private String lastAnswer;
    @SerializedName("last_answer_user_id")
    @Expose
    private String lastAnswerUserId;
    @SerializedName("last_answer_dt_change")
    @Expose
    private String lastAnswerDtChange;
    @SerializedName("respond")
    @Expose
    private String respond;
    @SerializedName("report_id")
    @Expose
    private String reportId;
    @SerializedName("discount")
    @Expose
    private String discount;
    @SerializedName("discount_smeta")
    @Expose
    private String discountSmeta;
    @SerializedName("vote_score")
    @Expose
    private String voteScore;
    @SerializedName("voter_id")
    @Expose
    private String voterId;
    @SerializedName("vinovnik_score")
    @Expose
    private String vinovnikScore;
    @SerializedName("vinovnik_score_user_id")
    @Expose
    private String vinovnikScoreUserId;
    @SerializedName("vinovnik_score_dt")
    @Expose
    private String vinovnikScoreDt;
    @SerializedName("theme_grp_id")
    @Expose
    private String themeGrpId;
    @SerializedName("theme_id")
    @Expose
    private String themeId;
    @SerializedName("sum_premiya")
    @Expose
    private String sumPremiya;
    @SerializedName("sum_penalty")
    @Expose
    private String sumPenalty;
    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("ref_id")
    @Expose
    private String refId;
    @SerializedName("summa_zp")
    @Expose
    private String summaZp;
    @SerializedName("budget")
    @Expose
    private String budget;
    @SerializedName("complete")
    @Expose
    private String complete;
    @SerializedName("sotr_opinion_id")
    @Expose
    private String sotrOpinionId;
    @SerializedName("no_need_reply")
    @Expose
    private String noNeedReply;
    @SerializedName("audio_id")
    @Expose
    private String audioId;
    @SerializedName("potential_client_id")
    @Expose
    private String potentialClientId;


    @SerializedName("dt_start_plan")
    @Expose
    private String dt_start_plan;//    dt_start_plan - плановое время начала (unixtime)

    @SerializedName("dt_end_plan")
    @Expose
    private String dt_end_plan;//    dt_end_plan - плановое время окончания (unixtime)

    @SerializedName("dt_start_fact")
    @Expose
    private String dt_start_fact;//    dt_start_fact - фактическое время начала (unixtime)

    @SerializedName("dt_end_fact")
    @Expose
    private String dt_end_fact;//    dt_end_fact - фактическое время окончания (unixtime)




    public TasksAndReclamationsDB() {
    }

    public String getID() {
        return id;
    }

    public void setID(String iD) {
        this.id = iD;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getDtRealPost() {
        return dtRealPost;
    }

    public void setDtRealPost(String dtRealPost) {
        this.dtRealPost = dtRealPost;
    }

    public String getDtChange() {
        return dtChange;
    }

    public void setDtChange(String dtChange) {
        this.dtChange = dtChange;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto2() {
        return photo2;
    }

    public void setPhoto2(String photo2) {
        this.photo2 = photo2;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVinovnik() {
        return vinovnik;
    }

    public void setVinovnik(String vinovnik) {
        this.vinovnik = vinovnik;
    }

    public String getVinovnik2() {
        return vinovnik2;
    }

    public void setVinovnik2(String vinovnik2) {
        this.vinovnik2 = vinovnik2;
    }

    public String getVinovnikReadDt() {
        return vinovnikReadDt;
    }

    public void setVinovnikReadDt(String vinovnikReadDt) {
        this.vinovnikReadDt = vinovnikReadDt;
    }

    public String getZamenaUserId() {
        return zamenaUserId;
    }

    public void setZamenaUserId(String zamenaUserId) {
        this.zamenaUserId = zamenaUserId;
    }

    public String getZamenaDt() {
        return zamenaDt;
    }

    public void setZamenaDt(String zamenaDt) {
        this.zamenaDt = zamenaDt;
    }

    public String getZamenaWho() {
        return zamenaWho;
    }

    public void setZamenaWho(String zamenaWho) {
        this.zamenaWho = zamenaWho;
    }

    public String getContacterId() {
        return contacterId;
    }

    public void setContacterId(String contacterId) {
        this.contacterId = contacterId;
    }

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public String getTerritorialId() {
        return territorialId;
    }

    public void setTerritorialId(String territorialId) {
        this.territorialId = territorialId;
    }

    public String getRegionalId() {
        return regionalId;
    }

    public void setRegionalId(String regionalId) {
        this.regionalId = regionalId;
    }

    public String getNopId() {
        return nopId;
    }

    public void setNopId(String nopId) {
        this.nopId = nopId;
    }

    public String getDvi() {
        return dvi;
    }

    public void setDvi(String dvi) {
        this.dvi = dvi;
    }

    public String getZakazchik() {
        return zakazchik;
    }

    public void setZakazchik(String zakazchik) {
        this.zakazchik = zakazchik;
    }

    public String getId1c() {
        return id1c;
    }

    public void setId1c(String id1c) {
        this.id1c = id1c;
    }

    public String getDocNum1cId() {
        return docNum1cId;
    }

    public void setDocNum1cId(String docNum1cId) {
        this.docNum1cId = docNum1cId;
    }

    public String getCodeDad2() {
        return codeDad2;
    }

    public void setCodeDad2(String codeDad2) {
        this.codeDad2 = codeDad2;
    }

    public String getCodeDad2SrcDoc() {
        return codeDad2SrcDoc;
    }

    public void setCodeDad2SrcDoc(String codeDad2SrcDoc) {
        this.codeDad2SrcDoc = codeDad2SrcDoc;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getLastAnswer() {
        return lastAnswer;
    }

    public void setLastAnswer(String lastAnswer) {
        this.lastAnswer = lastAnswer;
    }

    public String getLastAnswerUserId() {
        return lastAnswerUserId;
    }

    public void setLastAnswerUserId(String lastAnswerUserId) {
        this.lastAnswerUserId = lastAnswerUserId;
    }

    public String getLastAnswerDtChange() {
        return lastAnswerDtChange;
    }

    public void setLastAnswerDtChange(String lastAnswerDtChange) {
        this.lastAnswerDtChange = lastAnswerDtChange;
    }

    public String getRespond() {
        return respond;
    }

    public void setRespond(String respond) {
        this.respond = respond;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscountSmeta() {
        return discountSmeta;
    }

    public void setDiscountSmeta(String discountSmeta) {
        this.discountSmeta = discountSmeta;
    }

    public String getVoteScore() {
        return voteScore;
    }

    public void setVoteScore(String voteScore) {
        this.voteScore = voteScore;
    }

    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getVinovnikScore() {
        return vinovnikScore;
    }

    public void setVinovnikScore(String vinovnikScore) {
        this.vinovnikScore = vinovnikScore;
    }

    public String getVinovnikScoreUserId() {
        return vinovnikScoreUserId;
    }

    public void setVinovnikScoreUserId(String vinovnikScoreUserId) {
        this.vinovnikScoreUserId = vinovnikScoreUserId;
    }

    public String getVinovnikScoreDt() {
        return vinovnikScoreDt;
    }

    public void setVinovnikScoreDt(String vinovnikScoreDt) {
        this.vinovnikScoreDt = vinovnikScoreDt;
    }

    public String getThemeGrpId() {
        return themeGrpId;
    }

    public void setThemeGrpId(String themeGrpId) {
        this.themeGrpId = themeGrpId;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getSummaZp() {
        return summaZp;
    }

    public void setSummaZp(String summaZp) {
        this.summaZp = summaZp;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getComplete() {
        return complete;
    }

    public void setComplete(String complete) {
        this.complete = complete;
    }

    public String getSotrOpinionId() {
        return sotrOpinionId;
    }

    public void setSotrOpinionId(String sotrOpinionId) {
        this.sotrOpinionId = sotrOpinionId;
    }

    public String getNoNeedReply() {
        return noNeedReply;
    }

    public void setNoNeedReply(String noNeedReply) {
        this.noNeedReply = noNeedReply;
    }

    public String getAudioId() {
        return audioId;
    }

    public void setAudioId(String audioId) {
        this.audioId = audioId;
    }

    public String getPotentialClientId() {
        return potentialClientId;
    }

    public void setPotentialClientId(String potentialClientId) {
        this.potentialClientId = potentialClientId;
    }

    public String getDt_start_plan() {
        return dt_start_plan;
    }

    public void setDt_start_plan(String dt_start_plan) {
        this.dt_start_plan = dt_start_plan;
    }

    public String getDt_end_plan() {
        return dt_end_plan;
    }

    public void setDt_end_plan(String dt_end_plan) {
        this.dt_end_plan = dt_end_plan;
    }

    public String getDt_start_fact() {
        return dt_start_fact;
    }

    public void setDt_start_fact(String dt_start_fact) {
        this.dt_start_fact = dt_start_fact;
    }

    public String getDt_end_fact() {
        return dt_end_fact;
    }

    public void setDt_end_fact(String dt_end_fact) {
        this.dt_end_fact = dt_end_fact;
    }
}
