package ua.com.merchik.merchik.data.synchronization;

import java.util.Objects;

public enum TableName {

    WP_DATA("wp_data"),
    IMAGE_TP("image_tp"), //ImagesTypeListDB
    CLIENT_GROUP_TP("client_group_tp"), //GroupTypeDB
    LOG_MP("log_mp"), //LogDB
    CLIENTS("clients"), //client
    ADDRESS("address"), //address
    ADDRESS_DB("address_db"), //AddressDB
    USERS("users"), //sotr
    PROMO_LIST("promoList"), //PromoDB
    ERROR_LIST("errorsList"), //ErrorDB
    STACK_PHOTO("stack_photo"),
    TASK_AND_RECLAMATION("task_and_reclamations"), //2025123000
    PLANOGRAMM("planogram"),
    PLANOGRAMM_ADDRESS("PlanogrammAddressSDB"),
    PLANOGRAMM_GROUP("planogramm_group"),
    PLANOGRAMM_IMAGES("planogramm_img"),
    PLANOGRAMM_TYPE("planogramm_type"),
    PLANOGRAMM_VIZIT_SHOWCASE("planogram_vizit_showcase"),
    CITY("city"), //city
    OBLAST("oblast"), //oblast
    EKL("ekl"), //EKL_SDB
    TOVAR("tovar"), //TovarDB
    TradeMarkDB("TradeMarkDB"),
    SAMPLE_PHOTO("sample_photo"), //SamplePhotoSDB
    TRANSLATES("translates"),
    AppUsersDB("AppUsersDB"),
    ImagesTypeListDB("ImagesTypeListDB"),
    MenuItemFromWebDB("MenuItemFromWebDB"),
    OptionsDB("OptionsDB"),
    PromoDB("PromoDB"),
    ReportPrepareDB("ReportPrepareDB"),
    TARCommentsDB("TARCommentsDB"),
    ThemeDB("ThemeDB"),
    AchievementsSDB("AchievementsSDB"),
    AdditionalMaterialsAddressSDB("AdditionalMaterialsAddressSDB"),
    AdditionalMaterialsGroupsSDB("AdditionalMaterialsGroupsSDB"),
    AdditionalMaterialsSDB("AdditionalMaterialsSDB"),
    AddressSDB("AddressSDB"),
    ArticleSDB("ArticleSDB"),
    BonusSDB("BonusSDB"),
    CitySDB("CitySDB"),
    ContentSDB("ContentSDB"),
    CustomerSDB("CustomerSDB"),
    DateConverter("DateConverter"),
    DossierSotrSDB("DossierSotrSDB"),
    FragmentSDB("FragmentSDB"), //????
    LanguagesSDB("LanguagesSDB"),
    OblastSDB("OblastSDB"),
    OborotVedSDB("OborotVedSDB"),
    OpinionSDB("OpinionSDB"),
    OpinionThemeSDB("OpinionThemeSDB"),
    PotentialClientSDB("PotentialClientSDB"),
    ReclamationPercentageSDB("ReclamationPercentageSDB"),
    SettingsUISDB("SettingsUISDB"),
    ShowcaseSDB("ShowcaseSDB"),
    ShowcaseTypeSDB("ShowcaseTypeSDB"),
    SiteAccountSDB("SiteAccountSDB"),
    SiteObjectsSDB("SiteObjectsSDB"),
    StandartSDB("StandartSDB"),
    TasksAndReclamationsSDB("TasksAndReclamationsSDB"),
    TovarGroupClientSDB("TovarGroupClientSDB"),
    TovarGroupSDB("TovarGroupSDB"),
    TranslatesSDB("TranslatesSDB"),
    UsersSDB("UsersSDB"),
    VacancySDB("VacancySDB"),
    ViewListSDB("ViewListSDB"), // youtube
    VoteSDB("VoteSDB"),



    ;

    private final String table;

    TableName(String table) {
        this.table = table;
    }

    public String getCode() {
        return table;
    }

    public static TableName fromCode(String table) {
        for (TableName status : values()) {
            if (Objects.equals(status.table, table)) return status;
        }
        return WP_DATA;
    }
}
