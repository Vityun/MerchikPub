package ua.com.merchik.merchik.retrofit;

import static ua.com.merchik.merchik.retrofit.TimeoutKt.LOGIN_SYSTEM;
import static ua.com.merchik.merchik.retrofit.TimeoutKt.UPLOAD_PHOTO_KEY;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.EKLExchange;
import ua.com.merchik.merchik.data.AppData.AppData;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.PhotoData;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHints;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjects;
import ua.com.merchik.merchik.data.PPAonResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AdditionalMaterialsAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AdditionalMaterialsGroupsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AdditionalMaterialsLinksResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AdditionalMaterialsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AddressTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ArticleTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.BonusResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ConductWpDataResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.CustomerGroups;
import ua.com.merchik.merchik.data.RetrofitResponse.models.CustomerTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.DossierSotrResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.EDRPOUResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ErrorTableResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.models.FragmentsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ImageTypes;
import ua.com.merchik.merchik.data.RetrofitResponse.models.Login;
import ua.com.merchik.merchik.data.RetrofitResponse.models.Logout;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesView;
import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PPATableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PhotoHash;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PotentialClientResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PremiumResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PromoTableResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportHint;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SMSLogResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SMSPlanResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SamplePhotoResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ServerConnection;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteAccountResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteObjectsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteURLResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SotrTable;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TARCommentsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TasksAndReclamationsResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ThemeTableRespose;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarImgResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TradeMarkResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.VacancyResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ViewListResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.WpDataServer;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AchievementsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AchievementsUpload.AchievementsUploadResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ArticleResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatGrp.ChatGrpResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.CityResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ContentResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.CustomerResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.EKL.EKLResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.LanguagesResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OblastResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OborotVedResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OpinionResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OpinionThemeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.Premial;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.PremiumPremium;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReclamationPercentage.ReclamationPercentageResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare.ReportPrepareUploadResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShelfSizeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.StandartResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TasksAndReclamationsSDBResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TovarGroupClientResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TovarGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TranslatesResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.UsersResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.VoteResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammImagesResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammTypeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammVizitShowcaseResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.reportprepare.ReportPrepareUpdateResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata.WpDataUpdateResponse;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirements.AdditionalRequirementsServerData;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks.AdditionalRequirementsSendMarksServerData;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarksServerData;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.TARCommentData.TARCommentsServerData;
import ua.com.merchik.merchik.data.ServerInfo.AppVersion.AppVersion;
import ua.com.merchik.merchik.data.ServerLogin.SessionCheck;
import ua.com.merchik.merchik.data.Translation.AddTranslation;
import ua.com.merchik.merchik.data.Translation.SiteLanguages;
import ua.com.merchik.merchik.data.Translation.SiteTranslations;
import ua.com.merchik.merchik.data.UploadToServ.LogUploadToServ;
import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;
import ua.com.merchik.merchik.data.UploadToServ.WpDataUploadToServ;
import ua.com.merchik.merchik.dialogs.EKL.DialogEKL;
import ua.com.merchik.merchik.dialogs.EKL.EKLRequests;

public interface RetrofitInterface {

    @POST("mobile_app.php?")
    @Timeout(key = LOGIN_SYSTEM)
    Call<SessionCheck> CHECK_SESSION(
            @Query("mod") String mod,
            @Query("app_data") String app_data);

    // mod=auth&act=sotr_auth&username=Фам Мария Чунговна&password=gwgm87789&app_data={"browser":{"date":"2020-10-08","name":"MerchikApp","test":"1","type":"mobile_app","VersionApp":"1.0.09.201006"},"device":{"brand":"xiaomi","model":"Redmi 5 Plus","type":"MerchikApp"},"os":{"api":"27","name":"Android","VersionApp":"8.1.0"}}
    @POST("mobile_app.php?")
    @Timeout(key = LOGIN_SYSTEM)
    Call<Login> LOGIN(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("username") String login,
            @Query("password") String password,
            @Query("app_data") String app_data);

    @POST("mobile_app.php?")
    @Timeout(key = LOGIN_SYSTEM)
    Call<JsonObject> GET_LOGIN_HINT(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("term") String term);

    @POST("mobile_app.php?")
    Call<JsonObject> GET_LOGIN_HELP(
            @Query("mod") String mod,
            @Query("act") String act);

    @POST("mobile_app.php?")
    Call<ServerConnection> takeState(@Query("mod") String mod, @Query("t") Long t);

    @Multipart
    @POST("mobile_app.php?")
    @Timeout(key = LOGIN_SYSTEM)
    Call<ServerConnection> PING_SERVER(@Part("mod") RequestBody mod,
                                       @Part("t") RequestBody t,
                                       @Part MultipartBody.Part image);

/*    @Multipart
    @POST("mobile_app.php?")
    Call<ServerConnection> PING_SERVER(@Part("mod") String mod,
                                       @Part("t") Long t,
                                       @Part MultipartBody.Part image);*/

    @POST("mobile_app.php?")
    Call<Login> loginInfo(@Query("mod") String mod, @Query("sess_id") String sessId);

    @POST("mobile_app.php?")
    Call<Logout> logoutInfo(@Query("mod") String mod);

    @POST("mobile_app.php?")
    Call<WpDataServer> wpData(@Query("mod") String mod, @Query("act") String act, @Query("date_from") String date_from, @Query("date_to") String date_to);

    @POST("mobile_app.php?")
    Call<JsonObject> wpDataJson(@Query("mod") String mod, @Query("act") String act, @Query("date_from") String date_from, @Query("date_to") String date_to);

    @POST("mobile_app.php?")
    Call<WpDataServer> GET_WPDATA_VPI(@Query("mod") String mod,
                                      @Query("act") String act,
                                      @Query("date_from") String date_from,
                                      @Query("date_to") String date_to,
                                      @Query("dt_change_from") long vpi);

    @POST("mobile_app.php?")
    Call<WpDataServer> GET_WPDATA_VPI(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<JsonObject> GET_WPDATA_VPI_JSON(@Query("mod") String mod,
                                         @Query("act") String act,
                                         @Query("date_from") String date_from,
                                         @Query("date_to") String date_to,
                                         @Query("dt_change_from") long vpi);

    @POST("mobile_app.php?")
    Call<ImageTypes> IMAGE_TYPES_CALL(@Query("mod") String mod, @Query("act") String act, @Query("images_type_list") String images_type_list);

    @POST("mobile_app.php?")
    Call<CustomerGroups> GROUP_TYPE(@Query("mod") String mod, @Query("act") String act);


    @POST("mobile_app.php?")
    Call<OptionsServer> OPTIONS_CALL(@Query("mod") String mod, @Query("act") String act, @Query("date_from") String date_from, @Query("date_to") String date_to);

    @POST("mobile_app.php?")
    Call<OptionsServer> GET_OPTIONS(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<ReportPrepareServer> REPORT_PREPARE_CALL_ALL(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("date_from") String date_from,
            @Query("date_to") String date_to);

    @POST("mobile_app.php?")
    Call<ReportPrepareServer> REPORT_PREPARE_CALL_PIECE(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("date_from") String date_from,
            @Query("date_to") String date_to,
            @Query("vpo") long vpo);


    @Multipart
    @POST("mobile_app.php?")
    Call<JsonObject> SEND_PHOTO(@Query("mod") String mod,
                                @Query("act") String act,
                                @Query("client_id") String client_id,
                                @Query("addr_id") String addr_id,
                                @Query("date") String date,
                                @Query("img_type_id") String img_type_id,
                                @Query("photo_user_id") String photo_user_id,
                                @Query("client_tovar_group") String client_tovar_group,
                                @Query("doc_num") String doc_num,
                                @Query("theme_id") String theme_id,
                                @Query("comment") String comment,
                                @Query("gp") String gp,
                                @Part MultipartBody.Part photo);

    @Multipart
    @POST("/mobile_app.php?")
    @Timeout(key = UPLOAD_PHOTO_KEY)
    Call<JsonObject> SEND_PHOTO_2_BODY(@Part("mod") RequestBody mod,
                                       @Part("act") RequestBody act,
                                       @Part("client_id") RequestBody client_id,
                                       @Part("addr_id") RequestBody addr_id,
                                       @Part("date") RequestBody date,
                                       @Part("img_type_id") RequestBody img_type_id,
                                       @Part("photo_user_id") RequestBody photo_user_id,
                                       @Part("client_tovar_group") RequestBody client_tovar_group,
                                       @Part("doc_num") RequestBody doc_num,
                                       @Part("theme_id") RequestBody theme_id,
                                       @Part("comment") RequestBody comment,
                                       @Part("dvi") RequestBody dvi,
                                       @Part("code_dad2") RequestBody codeDad2,
                                       @Part("gp") RequestBody gp,
                                       @Part("tovar_id") RequestBody tovar_id,
                                       @Part("img_src_id") RequestBody img_src_id,
                                       @Part("showcase_id") RequestBody showcase_id,
                                       @Part("planogram_id") RequestBody planogram_id,
                                       @Part("planogram_img_id") RequestBody planogram_img_id,
                                       @Part("example_id") RequestBody example_id,
                                       @Part("example_img_id") RequestBody example_img_id,
                                       @Part MultipartBody.Part photo);

    @POST("mobile_app.php?")
    Call<PhotoHash> SEND_PHOTO_HASH(@Query("mod") String mod,
                                    @Query("act") String act,
                                    @Query("no_limit") String noLimit,
                                    @Query("date_from") String client_id,
                                    @Query("date_to") String addr_id,
                                    @Query("hash_list[]") List<String> hash);

    @POST("mobile_app.php?")
    Call<PhotoHash> SEND_PHOTO_HASH_NEW(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<TovarImgResponse> GET_TOVAR_PHOTO_INFO(@Query("mod") String mod,
                                                @Query("act") String act,
                                                @Query("tovar_only") String tovar_only,
                                                @Query("nolimit") String noLimit,
                                                @Query("image_type") String imageType,
                                                @Query("tovar_id[]") List<String> tov_id);

    @POST("mobile_app.php?")
    Call<TovarImgResponse> GET_TOVAR_PHOTO_INFO_JSON(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // http:\/\/merchik.com.ua\/photos\/03693\/24937\/thumb_010520_5310592_kiyev_mashinostroitelnaya_50_of_205_ivanovskaya_sofiya_valeryevna.jpg
    // http:\/\/merchik.com.ua\/photos\/03693\/24937\/thumb_300420_1804334_kiyev_mashinostroitelnaya_50_of_205_krush_irina_leonidovna.jpg
    // http:\/\/merchik.com.ua\/photos\/03693\/24937\/thumb_180619_3385935_kiev_mashinostroitelnaya_50_of_205_shherbinin_olga_dmitrievna.jpg

    @GET
    Call<ResponseBody> DOWNLOAD_PHOTO_BY_URL(@Url String url);

    @GET
    Call<ResponseBody> DOWNLOAD_PHOTO_BY_URL_WORKER(@Url String url); // метод для workManager

    @GET
    Single<ResponseBody> DOWNLOAD_PHOTO_BY_URL_TEST(@Url String url);

    @GET
    Single<JsonObject> DOWNLOAD_PHOTO_BY_URL_TEST_JSON(@Url String url);


    @POST("mobile_app.php?")
    Call<CustomerTableResponse> GET_CUSTOMER_T(@Query("mod") String mod,
                                               @Query("act") String act);


    @POST("mobile_app.php?")
    Call<AddressTableResponse> GET_ADDRESS_T(@Query("mod") String mod,
                                             @Query("act") String act);


    @POST("mobile_app.php?")
    Call<SotrTable> GET_SOTR_T(@Query("mod") String mod,
                               @Query("act") String act);


    @POST("mobile_app.php?")
    Call<JsonObject> GET_CITY_T(@Query("mod") String mod,
                                @Query("act") String act);


    @POST("mobile_app.php?")
    Call<JsonObject> GET_OBL_T(@Query("mod") String mod,
                               @Query("act") String act);


    @POST("mobile_app.php?")
    Call<JsonObject> GET_ADDRESS_TT_T(@Query("mod") String mod,
                                      @Query("act") String act);


    @POST("mobile_app.php?")
    Call<TovarTableResponse> GET_TOVAR_T(@Query("mod") String mod,
                                         @Query("act") String act);

    @POST("mobile_app.php?")
    Call<TovarTableResponse> GET_TOVAR_T_ID(@Query("mod") String mod,
                                            @Query("act") String act,
                                            @Query("id[]") List<String> listId);

    ;

    @POST("mobile_app.php?")
    Call<JsonObject> GET_TOVAR_T_JSON(@Query("mod") String mod,
                                      @Query("act") String act);


    @POST("mobile_app.php?")
    Call<JsonObject> GET_TOVAR_GROUP_T(@Query("mod") String mod,
                                       @Query("act") String act);


    @POST("mobile_app.php?")
    Call<TradeMarkResponse> GET_TRADE_MARKS_T(@Query("mod") String mod,
                                              @Query("act") String act);

    @POST("mobile_app.php?")
    Call<PPATableResponse> GET_PPA_T(@Query("mod") String mod,
                                     @Query("act") String act);

    @POST("mobile_app.php?")
    Call<ArticleTableResponse> GET_ARTICLE_T(@Query("mod") String mod,
                                             @Query("act") String act);


    /**
     * список акций
     */
    @POST("mobile_app.php?")
    Call<PromoTableResponce> GET_PROMO_LIST(@Query("mod") String mod,
                                            @Query("act") String act);


    /**
     * список ошибок
     */
    @POST("mobile_app.php?")
    Call<ErrorTableResponce> GET_ERROR_LIST(@Query("mod") String mod,
                                            @Query("act") String act);


    /**
     * Получаем информации по ранее вводимым значениям в отчётах
     */
    @POST("mobile_app.php?")
    Call<ReportHint> GET_REPORT_HINT(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("tovar_id") String tovar_id,
            @Query("code_dad2") String code_dad2,
            @Query("client_id") String client_id
    );

    @POST("mobile_app.php?")
    Call<JsonObject> GET_REPORT_HINT_JO(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("tovar_id") String tovar_id,
            @Query("code_dad2") String code_dad2,
            @Query("client_id") String client_id
    );


    // --------------- UPLOAD ---------------

    /**
     * Выгрузка LOG на сервер.
     *
     * @POST("testdata.php") - использую для тестов
     */
    @Multipart
    @POST("mobile_app.php?")
    //    @POST("testdata.php")    @POST("mobile_app.php?")
    Call<JsonObject> LOG(
            @Query("mod") String mod,
            @Query("act") String act,
            @Part("data[]") List<LogUploadToServ> data);

    /**
     * 17.08.2020
     * <p>
     * Send WpData to server
     */
    @Multipart
    @POST("mobile_app.php?")
    Call<JsonObject> SEND_WP_DATA(
            @Query("mod") String mod,
            @Query("act") String act,
            @Part("data[]") List<WpDataUploadToServ> data);

    /**
     * Выгрузка REPORT_PREPARE на сервер.
     * <p>
     * Данные из БД ReportPrepareDB записываются в
     * дженерик ReportPrepareServ -- частичную реализацию ReportPrepareDB
     */
    @Multipart
    @POST("mobile_app.php?")
    Call<JsonObject> UPLOAD_REPORT_PREPARE(
            @Query("mod") String mod,
            @Query("act") String act,
            @Part("data[]") List<ReportPrepareServ> data);

    // UPLOAD_REPORT_PREPARE2
    // Проверка данных которые я отправил
    @POST("mobile_app.php?")
    Call<JsonObject> UPLOAD_REPORT_PREPARE2(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("date_from") String date_from,
            @Query("date_to") String date_to,
            @Query("client_id") String client_id,
            @Query("addr_id") String addr_id);


    // LOG_2
    @Multipart
    @POST("testdata.php")
    Call<JsonObject> LOG_2(
            @Part("mod") String mod,
            @Part("act") String act,
            @Part("data[]") List<ReportPrepareServ> data);


    @FormUrlEncoded
    @POST("mobile_app.php?")
    Call<JsonObject> UPLOAD_LOG_MP(
            @Field("mod") String mod,
            @Field("act") String act,
            @FieldMap() HashMap<String, String> gp/*,
            @Field("debug_param_1") String test_something*/);


    @POST("mobile_app.php?")
    Call<JsonObject> MVS_DATA_CLI_JSON(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("client_id") String client_id,
            @Query("addr_id") String addr_id,
            @Query("client_tovar_group") String client_tovar_group,
            @Query("images_type_list") String images_type_list,
            @Query("only_selected") String only_selected);

    @POST("mobile_app.php?")
    Call<PhotoData> MVS_DATA_CLI(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("client_id") String client_id,
            @Query("addr_id") String addr_id,
            @Query("client_tovar_group") String client_tovar_group,
            @Query("images_type_list") String images_type_list,
            @Query("only_selected") String only_selected);


    @POST("mobile_app.php?")
    Call<JsonObject> MVS_DATA_ADD_JSON(
            @Query("mod") String mod2,
            @Query("act") String act2,
            @Query("addr_id") String addr_id2);

    @POST("mobile_app.php?")
    Call<PhotoData> MVS_DATA_ADD(
            @Query("mod") String mod2,
            @Query("act") String act2,
            @Query("addr_id") String addr_id2);


    @Multipart
    @POST("test_data.php?")
    Call<String> TEST_API_DATA(
            @Part("app_data[os][name]") String osName,
            @Part("app_data[os][VersionApp]") String osVer,
            @Part("app_data[browser][name]") String browName,
            @Part("app_data[browser][VersionApp]") String brovVer);


    //    @Multipart
    @POST("test_data_2.php?")
    Call<JsonObject> TEST_API_DATA_OBJ(
            @Query("app_data") String app_data);


    @POST("test_data_2.php?")
    Call<String> TEST_API_DATA_JSON(
            @Body AppData app_data);


    @POST("mobile_app.php?")
    Call<JsonObject> DOWNLOAD_MENU(
            @Query("mod") String mod);

    @POST("mobile_app.php?")
    Call<SiteObjects> DOWNLOAD_SITE_HINTS(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("lang_id") String lang_id
    );

    @POST("mobile_app.php?")
    Call<SiteHints> DOWNLOAD_VIDEO_LESSONS(
            @Query("mod") String mod,
            @Query("act") String act
    );


    @POST("mobile_app.php?")
    Call<AppVersion> GET_CONSTANT_APP_VERSION(
            @Query("mod") String mod,
            @Query("act") String act
    );


    /*Получение списка языков*/
    @POST("mobile_app.php?")
    Call<SiteLanguages> GET_LANGUAGES(
            @Query("mod") String mod,
            @Query("act") String act
    );

//    @POST("mobile_app.php?")
//    Call<JsonObject> GET_LANGUAGES(
//            @Query("mod") String mod,
//            @Query("act") String act
//    );

    /*Получение списка переводов*/
    @POST("mobile_app.php?")
    Call<SiteTranslations> GET_TRANSLATES(
            @Query("mod") String mod,
            @Query("act") String act,
            @Query("lang_id") String lang
    );


    @Multipart
    @POST("mobile_app.php?")
    Call<JsonObject> SET_NEW_TRANSLATE(
            @Query("mod") String mod,
            @Query("act") String act,
            @Part("data[]") List<AddTranslation> data
    );


//    @POST("matest.php?")
//    Call<String> TEST_JSON_UPLOAD(
//            @Body String json);


//    ===================================================================================================

    @POST("mobile_app.php?")
    Call<List<EDRPOUResponse>> GET_EDRPOU(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<Premial> GET_PREMIAL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<PremiumPremium> GET_PREMIUM_PREMIUM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // ------------------------------------------------------

    // String contentType = "application/json";EKLCheckData
    @POST("mobile_app.php?")
    Call<JsonObject> TEST_JSON_UPLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Single<JsonObject> TEST_JSON_UPLOAD_RX(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<String> TEST_STRING_UPLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Single<String> TEST_STRING_UPLOAD_RX(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @Multipart
    @POST("mobile_app.php?")
    Single<JsonObject> TEST_AUDIO_UPLOAD_RX(
            @Header("ContentType") String content,
//            @Query("mod") String mod,
//            @Query("act") String act,
//            @Body JsonObject json,
//            @PartMap Map<String, RequestBody> data,
//            @Part List<MultipartBody.Part> audio);
            @Part List<MultipartBody.Part> parts);

    // -------------------------------------------------------

    @POST("mobile_app.php?")
    Call<AchievementsUploadResponse> AchievementsUploadResponseUPLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<TovarTableResponse> GET_TOVAR_TABLE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // PlanogrammResponse
    @POST("mobile_app.php?")
    Call<PlanogrammResponse> Planogramm_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<SMSPlanResponse> SMSPlan_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<SMSLogResponse> SMSLog_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<PlanogrammAddressResponse> Planogramm_ADDRESS_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<PlanogrammGroupResponse> Planogramm_GROUP_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<PlanogrammImagesResponse> Planogramm_IMAGES_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Single<PlanogrammVizitShowcaseResponse> PLANOGRAMM_VIZIT_SHOWCASE_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    //    PTTRequest
    @POST("mobile_app.php?")
    Call<EKLRequests.PTTRequest> GET_PTT_LIST(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<LocationResponse> LOCATION_RESPONSE_CALL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /**
     * 06.03.25
     * типы ланограмм.
     */
    @POST("mobile_app.php?")
    Single<PlanogrammTypeResponse> Planogramm_TYPE_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);


    /**
     * 21.07.23
     * План Работ.
     */
    @POST("mobile_app.php?")
    Call<WpDataServer> WpDataServer_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /**
     * 21.07.23
     * Дет. отчёт.
     * Обновил интерфейя для загрузки. Теперь надо использовать имено его и отказываться от старых
     * интерпритаций. Или альтернативных
     */
    @POST("mobile_app.php?")
    Call<ReportPrepareServer> ReportPrepareServer_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /**
     * 03.07.23
     * Витрины. Точка входа для получение таблички БД Витрин.
     * Витрины не путать с полками и другими шнягами.
     */
    @POST("mobile_app.php?")
    Call<ShowcaseResponse> SHOWCASE_UPLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);


    /**
     * 18.06.23
     * Просмотры. Это контроль просмотренности роликов. Видео.
     */
    @POST("mobile_app.php?")
    Call<ViewListResponse> View_List_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /**
     * 12.06.23.
     * Премиальные. Получение подробной информации о снижении и тп..
     */

    @POST("mobile_app.php?")
    Call<PremiumResponse> Premium_get_salary_basis_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<AdditionalMaterialsGroupsResponse> AdditionalMaterialsGroupsResponse_JSON_UPLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /**
     * 11.04.23.
     */
    @POST("mobile_app.php?")
    Call<FragmentsResponse> FRAGMENTS_TABLE_RESPONSE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<ReclamationPercentageResponse> ReclamationPercentage_DOWNLOAD_TABLE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<ShelfSizeResponse> ShelfSize_DOWNLOAD_TABLE(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Вова как обычно, что? Зачем? Оно ж работало..
    @POST("mobile_app.php?")
    Call<ReportPrepareUploadResponse> SEND_RP(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Запрос на проведение отчёта исполнителя
    @POST("mobile_app.php?")
    Call<ConductWpDataResponse> CONDUCT_WP_DATA(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Загрузка таблички Групп Чатов
    @POST("mobile_app.php?")
    Call<ChatGrpResponse> CHAT_GRP_DOWNLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Загрузка таблички оценок
    @POST("mobile_app.php?")
    Call<ArticleResponse> ARTICLE_DOWNLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Загрузка таблички оценок
    @POST("mobile_app.php?")
    Call<VoteResponse> VOTES_DOWNLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Загрузка таблички достижений
    @POST("mobile_app.php?")
    Call<AchievementsResponse> ACHIEVEMENTS_DOWNLOAD(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<EKLResponse> GET_EKL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Образец Фото
    @POST("mobile_app.php?")
    Call<SamplePhotoResponse> GET_SAMPLE_PHOTO(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Потенциальный клиент
    @POST("mobile_app.php?")
    Call<PotentialClientResponse> GET_POTENTIAL_CLIENT(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение Доп. Материалов Адресов
    @POST("mobile_app.php?")
    Call<AdditionalMaterialsAddressResponse> GET_ADDITIONAL_MATERIAL_ADDRESS(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение Доп. Материалов
    @POST("mobile_app.php?")
    Call<AdditionalMaterialsResponse> GET_ADDITIONAL_MATERIAL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение ссылки на доп. материал
    @POST("mobile_app.php?")
    Call<AdditionalMaterialsLinksResponse> GET_ADDITIONAL_MATERIAL_LINK(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Выгрузка инфы о дет отчёте (фейс, цена, количество)
    @POST("mobile_app.php?")
    Call<ReportPrepareUpdateResponse> SEND_RP_INFO(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // ОТправка инфы о фотках типа ДВИ, Комментов, Оценок
    @POST("mobile_app.php?")
    Call<PhotoInfoResponse> SEND_PHOTO_INFO(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение фотографий {"act":"list_image","mod":"images_view"}
    @POST("mobile_app.php?")
    Call<ImagesViewListImageResponse> GET_PHOTOS(
            @Header("ContentType") String content,
            @Body JsonObject json);


    // Получение стандартов
    @POST("mobile_app.php?")
    Call<StandartResponse> GET_TABLE_STANDART(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение

    @POST("mobile_app.php?")
    Call<ContentResponse> GET_TABLE_CONTENT(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получаем чат с сообщениями
    @POST("mobile_app.php?")
    Call<ChatResponse> GET_TABLE_CHAT(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение таблици Групп Товаров Клиентов
    @POST("mobile_app.php?")
    Call<TovarGroupClientResponse> GET_TABLE_TOVAR_GROUP_CLIENT(
            @Header("ContentType") String content,
            @Body JsonObject json);

    // Получение таблици Групп Товаров (Отделов). Одна из основных таблиц.
    @POST("mobile_app.php?")
    Call<TovarGroupResponse> GET_TABLE_TOVAR_GROUP(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<DialogEKL.EKLRespData> EKL_RESP_DATA_CALL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<DialogEKL.EKLCheckData> EKL_CHECK_DATA_CALL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @Multipart
    @POST("mobile_app.php?")
    Call<JsonObject> TEST_JSON_UPLOAD111(
            @Header("ContentType") String content,
            @Body JsonObject json,
            @Part MultipartBody.Part photo);


    // AdditionalRequirementsSendMarksServerData
    @POST("mobile_app.php?")
    Call<AdditionalRequirementsSendMarksServerData> SEND_ADDREP_MARKS(
            @Header("ContentType") String content,
            @Body JsonObject json);


    //    ModImagesView
    @POST("mobile_app.php?")
    Call<ModImagesView> MOD_IMAGES_VIEW_CALL(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<JsonObject> MOD_IMAGES_VIEW_CALL_JSON(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<DossierSotrResponse> dossierSotr(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<VacancyResponse> vacancy(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<BonusResponse> bonus(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<SiteURLResponse> siteUrl(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<SiteAccountResponse> siteAccount(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<JsonObject> averageSalary(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<PPAonResponse> GET_TABLE_PPA(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<TasksAndReclamationsResponce> GET_TABLE_TasksAndReclamations(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<TasksAndReclamationsSDBResponce> GET_TABLE_TasksAndReclamationsSDB(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<TARCommentsResponse> GET_TABLE_ReclamationComments(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<AdditionalRequirementsServerData> GET_TABLE_AdditionalRequirementsDB(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<AdditionalRequirementsMarksServerData> GET_TABLE_AdditionalRequirementsMarksDB(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<ThemeTableRespose> GET_TABLE_Theme(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<TARCommentsServerData> UPLOAD_TAR_COMMENT(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<WpDataUpdateResponse> SEND_WP_DATA(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<JsonObject> SEND_WP_DATA_JSON(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<SiteObjects> GET_SITE_OBJECTS(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<SiteObjectsResponse> GET_SITE_OBJECTS_R(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /*Получение языков*/
    @POST("mobile_app.php?")
    Call<LanguagesResponse> GET_LANGUAGES_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /*Получение списка переводов*/
    @POST("mobile_app.php?")
    Call<TranslatesResponse> GET_TRANSLATES_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /*Получение мнений*/
    @POST("mobile_app.php?")
    Call<OpinionResponse> GET_OPINION_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /*Получение тем мнений */
    @POST("mobile_app.php?")
    Call<OpinionThemeResponse> GET_OPINION_THEME_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);


    /*Получение Оборотной ведомости*/
    @POST("mobile_app.php?")
    Call<OborotVedResponse> GET_OBOROT_VED_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<AddressResponse> GET_ADDRESS_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    /**
     * 12.09.23. ЄКЛ
     */
    @POST("mobile_app.php?")
    Call<EKLExchange.EKLResponse> GET_EKL_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<CustomerResponse> GET_CUSTOMER_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<UsersResponse> GET_USERS_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<CityResponse> GET_CITY_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);

    @POST("mobile_app.php?")
    Call<OblastResponse> GET_OBLAST_ROOM(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @POST("mobile_app.php?")
    Call<ReportPrepareServer> DOWNLOAD_REPORT_PREPARE(
            @Header("ContentType") String content,
            @Body JsonObject json);


    @Multipart
    @POST("mobile_app.php?")
    Call<Void> UPLOAD_ZIP_FILE(
            @Query("mod") String mod,
            @Query("act") String act,
            @Part MultipartBody.Part file);

    @GET
    Single<retrofit2.Response<ResponseBody>> getGoogleTime(@Url String url);

}//------ E N D ------


//    https://merchik.net/mobile_app.php

/*    String json = String.valueOf(response.body());
    int maxLogSize = 1000;
                    for(int i = 0; i <= json.length() / maxLogSize; i++) {
                            int start = i * maxLogSize;
                            int end = (i+1) * maxLogSize;
                            end = end > json.length() ? json.length() : end;
                            Log.e("TAG_JSON_4", json.substring(start, end));
                            }*/



