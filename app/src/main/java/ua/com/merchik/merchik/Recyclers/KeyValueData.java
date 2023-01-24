package ua.com.merchik.merchik.Recyclers;

import ua.com.merchik.merchik.ViewHolders.Clicks;

public class KeyValueData {

    public KeyValueData(CharSequence key, CharSequence value, Clicks.clickVoid click) {
        this.key = key;
        this.value = value;
        this.click = click;
    }

    public CharSequence key;
    public CharSequence value;
    public Clicks.clickVoid click;
}
