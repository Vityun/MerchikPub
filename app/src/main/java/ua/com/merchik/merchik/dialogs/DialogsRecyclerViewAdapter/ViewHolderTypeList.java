package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.PremiumPremiumList;

public class ViewHolderTypeList {

    // Текст
    //.Данные
    //.Результат
    //.Клик
    public TextLayoutData textBlock;

    // Автовыпадающий список
    //.Данные
    //.Результат
    //.Клик
    public AutoTextLayoutData autoTextBlock;

    // Поле ввода
    //.Данные
    //.Результат
    //.Клик
    public EditTextLayoutData editTextBlock;

    // Кнопка
    //.Данные
    //.Результат
    //.Клик
    public ButtonLayoutData buttonBlock;

    public AddPhotoLayoutData addPhotoLayoutData;
    public ChoiceSpinnerLayoutData choiceSpinnerLayoutData;
    public ChoiceDateLayoutData choiceDateLayoutData;
    public TablePremiumLayoutData tablePremiumLayoutData;

    public int type;


    public static class TextLayoutData {
        public String data;
        public String result;
        public Click click;

        public interface Click {
            // Отработка успешного результата
            <T> void onSuccess(T data);

            // Отработка ошибки
            void onFailure(String error);
        }
    }

    public static class EditTextLayoutData {
        public String dataTitle;
        public String dataEditTextHint;
        public EditTextType editTextType;
        public String result;
        public Click click;

        public enum EditTextType {NUMBER}

        ;

        public interface Click {
            // Отработка успешного результата
            <T> void onSuccess(T data);

            // Отработка ошибки
            void onFailure(String error);
        }
    }

    public static class ButtonLayoutData {
        public String data;
        public String result;
        public Click click;

        public interface Click {
            // Отработка успешного результата
            <T> void onSuccess(T data);

            // Отработка ошибки
            void onFailure(String error);
        }
    }

    public static class AutoTextLayoutData<T> {
        public String dataTextTitle;
        public String dataTextAutoTextHint;
        public String result;
        public T resultData;
        public Click click;

        public interface Click {
            // Отработка успешного результата
            <T> void onSuccess(T data);

            // Отработка ошибки
            void onFailure(String error);
        }
    }

    public static class AddPhotoLayoutData<T> {
        public String dataTextTitle;
        public String dataText;
        public String result;
        public T resultData;
        public Click click;

        public interface Click {
            // Отработка успешного результата
            <T> void onSuccess(T data);

            // Отработка ошибки
            void onFailure(String error);
        }
    }

    public static class ChoiceSpinnerLayoutData<T> {
        public String dataTextTitle;
        public String[] dataSpinner;
        public List<String> dataSpinnerList;
        public String result;
        public T resultData;
        public ClickData click;
    }

    public static class ChoiceDateLayoutData<T> {
        public String dataTextTitle;
        public String dataTextTitle2;
        public String result;
        public Date dateFrom;
        public Date dateTo;
        public String resultDateFrom;
        public String resultDateTo;
        public T resultData;
        public ClickData click;
        public boolean state;
    }

    public static class TablePremiumLayoutData<T> {
        public String title;
        public String titleColumn1;
        public String titleColumn2;
        public String titleColumn3;
        public String titleColumn4;
        public List<PremiumTableRow> table;
        public String result;
        public ClickData click;

        public static class PremiumTableRow{
            public String titleColumn;
            public String column1;
            public String column2;
            public String column3;
            public String column4;

            public PremiumPremiumList data;

            public Clicks.clickVoid clicks;
            public ClickData click;
        }

    }

    public interface ClickData {
        <T> void onSuccess(T data);

        void onFailure(String error);
    }
}


