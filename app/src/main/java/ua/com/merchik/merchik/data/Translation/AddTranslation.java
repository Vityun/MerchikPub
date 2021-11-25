package ua.com.merchik.merchik.data.Translation;

public class AddTranslation {

    public String element_id;
    public String internal_name;
    public String default_value;
    public String script_mod;
    public String script_act;
    public String url;

    public AddTranslation(String element_id, String internal_name, String default_value, String script_mod, String script_act, String url) {
        this.element_id = element_id;
        this.internal_name = internal_name;
        this.default_value = default_value;
        this.script_mod = script_mod;
        this.script_act = script_act;
        this.url = url;
    }

}
