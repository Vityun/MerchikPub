package ua.com.merchik.merchik;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Locale;

public class TelephoneMask implements TextWatcher {

    /*
     * Прихордкоженные коды стран телефонов.
     *
     * "+380";   // Украина
     * "+48";    // Польша
     * "+44";    // Британия
     * "+7";    // Мордор
     * */
    public final String[] telephoneRegion = {"+380", "+48", "+44", "+7"};

    /*
     * Коды операторов мобильной связи Украины.
     * Киевстар: 067, 096, 097, 098
     * Водафон: 050, 066, 095, 099
     * Лфйф: 063, 073, 093
     * Тримоб, PeopleNet, Интертелеком: 091, 092, 094
     * */
    public final String[] operatorsUA = {"067", "096", "097", "098", "050", "066", "095", "099", "063", "073", "093", "091", "092", "094"};


    private static final int MAX_LENGTH = 9;
    private static final int MIN_LENGTH = 2;

    private String updatedText;
    private boolean editing;

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.toString().equals(updatedText) || editing) return;

        String digits = charSequence.toString().replaceAll("\\D", "");
        int length = digits.length();

        if (length <= MIN_LENGTH) {
            updatedText = digits;
            return;
        }

        if (length > MAX_LENGTH) {
            digits = digits.substring(0, MAX_LENGTH);
        }

        if (length <= 5) {
            String firstPart = digits.substring(0, 2);
            String secondPart = digits.substring(2);

            updatedText = String.format(new Locale("uk", "UA"), "(%s)%s", firstPart, secondPart);
        } else {
            String firstPart = digits.substring(0, 2);
            String secondPart = digits.substring(2, 5);
            String thirdPart = digits.substring(5);

            updatedText = String.format(new Locale("uk", "UA"), "(%s)%s-%s", firstPart, secondPart, thirdPart);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editing) return;

        editing = true;

        editable.clear();
        editable.insert(0, updatedText);

        editing = false;

    }

    public String getCurrentText() {
        return updatedText;
    }
}

/*

Польша - +48 - 7801 - 85421
раися - +7 999 000 22 22
Британия - +44 7458 038 735

+48     780185421
+7      9990002222
+44     7458038735
+380    667472811

* */
