package ua.com.merchik.merchik.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.TelephoneMask;
import ua.com.merchik.merchik.Utils.Test.ClickableMovementMethod;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ErrorRealm;

public class DialogData {

    public Context context;

    // ---- Data input Dialog ----
    private Map<String, String> mapSpinner;
    private Map<String, String> mapSpinner2;
    private String textData, textData2;
    public TovarOptions tovarOptions;
    public ReportPrepareDB reportPrepareDB;


    // ---- Data output Dialog ----
    private String result;
    private String result2;


    // ---- UI start ----
    private Dialog dialog;
    private ConstraintLayout layoutDialog, infoLayout, operationLayout, additionalOperationLayout;
    private Drawable drawable;

    private RecyclerView rView, recycler;

    private TextView title, text, textView42, txtLinkOk;
    private TextView additionalText1, additionalText2, additionalText3, additionalText4, additionalText5, additionalText6;
    private TextView additionalTextValue1, additionalTextValue2, additionalTextValue3, additionalTextValue4, additionalTextValue5, additionalTextValue6;

    public ImageView photo, merchikIco;

    private EditText editText, editDate, editText2;
    private Spinner spinner, spinner2;
    private ExpandableListView expListView;

    public ImageButton imgBtnClose;
    public ImageButton imgBtnLesson;
    public ImageButton imgBtnVideoLesson;
    public ImageButton imgBtnCall;

    private Button ok, cancel, cancel2;
    // ---- UI end ----


    public DialogData() {
    }

    public enum Operations {
        Text, TEXT, Telephone, Number, Date, Spinner, DoubleSpinner, EditTextAndSpinner
    }

    private DialogClickListener listenerOK;


    public DialogData(Context context) {
        this.context = context;

        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_data);

        drawable = context.getResources().getDrawable(R.drawable.shape_rounded_corner);

        title = dialog.findViewById(R.id.title);
        text = dialog.findViewById(R.id.text);
        txtLinkOk = dialog.findViewById(R.id.txtLinkOk);

        layoutDialog = dialog.findViewById(R.id.layout_dialog);
        infoLayout = dialog.findViewById(R.id.layout_info);
        operationLayout = dialog.findViewById(R.id.layout_operation);
        additionalOperationLayout = dialog.findViewById(R.id.layout_additional_operation);
        rView = dialog.findViewById(R.id.recycler_view);
        recycler = dialog.findViewById(R.id.recycler);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);
        imgBtnCall = dialog.findViewById(R.id.imageButtonCall);

        merchikIco = dialog.findViewById(R.id.merchik_ico);

        // ---------- operation block ----------
        editText = dialog.findViewById(R.id.editText);
        editDate = dialog.findViewById(R.id.editDate);
        spinner = dialog.findViewById(R.id.spinner);
        spinner2 = dialog.findViewById(R.id.spinner2);
        expListView = dialog.findViewById(R.id.expListView);
        editText2 = dialog.findViewById(R.id.editText2);

        // ---------- buttons ----------
        ok = dialog.findViewById(R.id.ok);
        cancel = dialog.findViewById(R.id.cancel);
        cancel2 = dialog.findViewById(R.id.cancel2);

        textView42 = dialog.findViewById(R.id.textView42);

        imgBtnClose.setOnClickListener(v -> dialog.dismiss());
    }


    public interface DialogClickListener {
        void clicked();
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    //---------------
    public void setTitle(String title) {
        this.title.setVisibility(View.VISIBLE);
        if (title != null && !title.equals("")) {
            this.title.setText(title);
        } else {
            this.title.setVisibility(View.GONE);
        }
    }

    public void setText(CharSequence text) {
        this.text.setVisibility(View.VISIBLE);
        if (text != null && !text.equals("")) {
            this.text.setText(text);
        } else {
            this.text.setVisibility(View.GONE);
        }
    }

    public void setTextTest(SpannableStringBuilder text) {
        this.text.setVisibility(View.VISIBLE);
        if (text != null && !text.equals("")) {
            this.text.setText(text);
        } else {
            this.text.setVisibility(View.GONE);
        }
    }

    //
    public void setText(SpannableStringBuilder text, DialogClickListener clickListener) {
        this.text.setVisibility(View.VISIBLE);
        this.text.setScrollbarFadingEnabled(false);

        this.text.setMovementMethod(ClickableMovementMethod.getInstance());// Делаю возможность скролить текст
        this.text.setClickable(false);
        this.text.setLongClickable(false);

        if (text != null && !text.equals("")) {
            this.text.setText(text);
            this.text.setOnClickListener(v -> clickListener.clicked());
        } else {
            this.text.setVisibility(View.GONE);
        }
    }


    public void setClose(DialogClickListener clickListener) {
        imgBtnClose.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    public void setLesson(Context context, boolean visualise, int objectId) {
        if (visualise) imgBtnLesson.setVisibility(View.VISIBLE);
        SiteObjectsDB data = RealmManager.getLesson(objectId);

        imgBtnLesson.setOnClickListener(v -> {
            if (data != null) {
                DialogData dialogLesson = new DialogData(context);
                dialogLesson.setTitle("Подсказка");
                dialogLesson.setText(data.getComments());
                dialogLesson.show();
            } else {
                Toast.makeText(context, "Для этой странички урок ещё не создан.", Toast.LENGTH_LONG).show();
            }
        });

        imgBtnLesson.setOnLongClickListener(v -> {
            if (data != null) {
                Toast.makeText(context, data.getNm(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Для этой странички урок ещё не создан.", Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogClickListener clickListener) {
        Log.e("setVideoLesson", "click0 Oid: " + objectId);
        try {
            if (visualise) {
                imgBtnVideoLesson.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imgBtnVideoLesson.getBackground().setTint(Color.RED);
                } else {
                    imgBtnVideoLesson.setBackgroundColor(Color.RED);
                }
                imgBtnVideoLesson.setColorFilter(Color.WHITE);
            }

            SiteObjectsDB object = RealmManager.getLesson(objectId);
            Log.e("setVideoLesson", "object: " + object.getID());

            SiteHintsDB data = null;
            try {
                if (object.getLessonId() != null) {
                    data = RealmManager.getVideoLesson(Integer.parseInt(object.getLessonId()));
                } else {
                    Log.e("setVideoLesson", "getLessonId=null");
                }
            } catch (Exception e) {
                Log.e("setVideoLesson", "Exception e: " + e);
            }


            SiteHintsDB finalData = data;
            imgBtnVideoLesson.setOnClickListener(v -> {

                Log.e("setVideoLesson", "click");

                if (finalData != null) {
                    Log.e("setVideoLesson", "click1");
                    if (clickListener == null) {

                        String s = finalData.getUrl();
                        Log.e("setVideoLesson", "click2.URL: " + s);
                        s = s.replace("http://www.youtube.com/", "http://www.youtube.com/embed/");
                        Log.e("setVideoLesson", "click2.replace.URL: " + s);
                        s = s.replace("watch?v=", "");
                        Log.e("setVideoLesson", "click2.replace.URL: " + s);

                        // Отображаем видео
                        DialogVideo video = new DialogVideo(context);
//                    video.setMerchikIco();
                        video.setTitle("" + finalData.getNm());
                        video.setClose(() -> {
                            Log.e("DialogVideo", "click X");
                            video.dismiss();
                        });
                        video.setVideoLesson(context, true, 0, () -> {
                            Log.e("DialogVideo", "click Video");
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalData.getUrl())));
                        });
                        video.setVideo("<html><body><iframe width=\"700\" height=\"600\" src=\"" + s + "\"></iframe></body></html>");

                        video.show();

                    } else {
                        Log.e("setVideoLesson", "click3");
                        // Переходим по ссылке
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalData.getUrl()))); // Запускаем стартовый ролик - презентацию
                        clickListener.clicked();
                    }
                } else {
                    Log.e("setVideoLesson", "click4");
                    Toast.makeText(context, "Для этой странички Видеоурок ещё не создан.", Toast.LENGTH_LONG).show();
                }


            });


            imgBtnVideoLesson.setOnLongClickListener(v -> {
                if (finalData != null) {
                    Toast.makeText(context, finalData.getNm(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Для этой странички Видеоурок ещё не создан.", Toast.LENGTH_LONG).show();
                }
                return true;
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogData/setVideoLesson", "Exception e: " + e);
        }
    }

    public void setVideoLesson(Context context, boolean visualise, Integer[] objectIds, DialogClickListener clickListener) {
        try {
            if (visualise) {
                imgBtnVideoLesson.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imgBtnVideoLesson.getBackground().setTint(Color.RED);
                } else {
                    imgBtnVideoLesson.setBackgroundColor(Color.RED);
                }
                imgBtnVideoLesson.setColorFilter(Color.WHITE);
            }

            List<SiteObjectsDB> siteObjects = RealmManager.getLesson(objectIds);

            List<SiteHintsDB> data = null;
            try {
                if (siteObjects != null) {
                    Integer[] siteObjectIds = new Integer[siteObjects.size()];
                    for (int i = 0; i < siteObjects.size(); i++) {
                        int lessId = Integer.parseInt(siteObjects.get(i).getLessonId());
                        if (lessId != 0) siteObjectIds[i] = lessId;
                    }
                    data = RealmManager.getVideoLesson(siteObjectIds);
                }
            } catch (Exception e) {
                Log.e("setVideoLesson", "Exception e: " + e);
            }

            List<SiteHintsDB> finalData = data;
            imgBtnVideoLesson.setOnClickListener(v -> {
                if (finalData != null) {
                    Log.e("setVideoLesson", "click1");
                    if (clickListener == null) {
                        DialogVideo dialogVideo = new DialogVideo(context);
                        dialogVideo.setTitle("Перелік відео уроків");
                        dialogVideo.setVideos(finalData);
                        dialogVideo.setClose(dialogVideo::dismiss);
                        dialogVideo.show();
                    }
                } else {
                    Log.e("setVideoLesson", "click4");
                    Toast.makeText(context, "Для этой странички Видеоурок ещё не создан.", Toast.LENGTH_LONG).show();
                }


            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogData/setVideoLesson", "Exception e: " + e);
        }
    }

    public void setImgBtnCall(Context context) {

        Log.e("setImgBtnCall", "i`m here");

        imgBtnCall.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgBtnCall.getBackground().setTint(context.getResources().getColor(R.color.greenCol));
        } else {
            imgBtnCall.setBackgroundColor(Color.GREEN);
        }
        imgBtnCall.setColorFilter(Color.WHITE);
        imgBtnCall.setOnClickListener((v -> {

            Log.e("setImgBtnCall", "pressed?");

            String telephone = "+380674484493";

            Globals.telephoneCall(context, telephone);

        }));

        Log.e("setImgBtnCall", "and here");
    }


    public void setMerchikIco(Context context) {
        merchikIco.setVisibility(View.VISIBLE);
        Drawable drawable = merchikIco.getBackground();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(context.getResources().getColor(R.color.colotSelectedTab));
        }
    }

    public void setDialogIco() {
        merchikIco.setVisibility(View.VISIBLE);
        merchikIco.setImageDrawable(dialog.getContext().getResources().getDrawable(R.drawable.ic_caution));
//        Drawable drawable = merchikIco.getBackground();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            drawable.setTint(dialog.getContext().getResources().getColor(R.color.colotSelectedTab));
//        }
    }

    public void setImage(boolean visualise, File file) {
        photo = dialog.findViewById(R.id.img);
        if (visualise) infoLayout.setVisibility(View.VISIBLE);
        if (visualise) photo.setVisibility(View.VISIBLE);

        if (file != null && file.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            photo.setImageBitmap(myBitmap);
        }

    }

    public void setAdditionalText(PhotoDescriptionText data) {
        additionalText1 = dialog.findViewById(R.id.additionalText1);
        additionalText2 = dialog.findViewById(R.id.additionalText2);
        additionalText3 = dialog.findViewById(R.id.additionalText3);
        additionalText4 = dialog.findViewById(R.id.additionalText4);
        additionalText5 = dialog.findViewById(R.id.additionalText5);
        additionalText6 = dialog.findViewById(R.id.additionalText6);

        additionalTextValue1 = dialog.findViewById(R.id.additionalTextValue1);
        additionalTextValue2 = dialog.findViewById(R.id.additionalTextValue2);
        additionalTextValue3 = dialog.findViewById(R.id.additionalTextValue3);
        additionalTextValue4 = dialog.findViewById(R.id.additionalTextValue4);
        additionalTextValue5 = dialog.findViewById(R.id.additionalTextValue5);
        additionalTextValue6 = dialog.findViewById(R.id.additionalTextValue6);


//        Map<String, String> map = new HashMap<>();
//
//        map.put("Data", new Date().toString());
//        map.put("Location", new Date().toString());
//        map.put("Data", new Date().toString());
//        map.put("Data", new Date().toString());

        // 1
        if (checkString(data.row1Text)) {
            additionalText1.setVisibility(View.VISIBLE);
            additionalText1.setText(data.row1Text);
        }

        if (checkString(data.row1TextValue)) {
            additionalTextValue1.setVisibility(View.VISIBLE);
            additionalTextValue1.setText(data.row1TextValue);
        }

        // 2
        if (checkString(data.row2Text)) {
            additionalText2.setVisibility(View.VISIBLE);
            additionalText2.setText(data.row2Text);
        }

        if (checkString(data.row2TextValue)) {
            additionalTextValue2.setVisibility(View.VISIBLE);
            additionalTextValue2.setText(data.row2TextValue);
        }

        // 3
        if (checkString(data.row3Text)) {
            additionalText3.setVisibility(View.VISIBLE);
            additionalText3.setText(data.row3Text);
        }

        if (checkString(data.row3TextValue)) {
            additionalTextValue3.setVisibility(View.VISIBLE);
            additionalTextValue3.setText(data.row3TextValue);
        }

        // 4
        if (checkString(data.row4Text)) {
            additionalText4.setVisibility(View.VISIBLE);
            additionalText4.setText(data.row4Text);
        }

        if (checkString(data.row4TextValue)) {
            additionalTextValue4.setVisibility(View.VISIBLE);
            additionalTextValue4.setText(data.row4TextValue);
        }

        // 5
        if (checkString(data.row5Text)) {
            additionalText5.setVisibility(View.VISIBLE);
            additionalText5.setText(data.row5Text);
        }

        if (checkString(data.row5TextValue)) {
            additionalTextValue5.setVisibility(View.VISIBLE);
            additionalTextValue5.setText(data.row5TextValue);
        }

        // 6
        if (checkString(data.row6Text)) {
            additionalText6.setVisibility(View.VISIBLE);
            additionalText6.setText(data.row6Text);
        }

        if (checkString(data.row6TextValue)) {
            additionalTextValue6.setVisibility(View.VISIBLE);
            additionalTextValue6.setText(data.row6TextValue);
        }
    }


    public void setEditTextHint(String hint) {
        if (editText != null) {
            editText.setHint(hint);
        }
    }

    /*27.03.2022
     * Использование базового EditText как поля для внесения телефона
     * */
    public void setTelephoneEditText(String data, Clicks.clickText click) {
        operationLayout.setVisibility(View.VISIBLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        editText.setVisibility(View.VISIBLE);
        if (data != null && !data.equals(""))
            editText.setText(data);   // Если поле для заполениня не пустое - заполняем значением
        editText.setSelection(editText.getText().length());

        editText.setSelectAllOnFocus(true);
        editText.selectAll();

        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setRawInputType(InputType.TYPE_CLASS_PHONE);

        editText.addTextChangedListener(new TelephoneMask());

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                click.click(v.getText().toString());
                return true;
            }
            return false;
        });

        ok.setVisibility(View.VISIBLE);
        ok.setText("Зарегистрироваться");
        ok.setOnClickListener(v -> click.click(editText.getText().toString()));
    }

    /**
     * 11.01.2021
     * Установка операции.
     * <p>
     * 1 - EditText текст
     * 2 - EditText число
     * 3 - EditText дата
     * 4 - Spinner выбор
     */
    public void setOperation(Operations operation, String data, Map<String, String> map, DialogClickListener listener) {
        listenerOK = listener;
        operationLayout.setVisibility(View.VISIBLE);
        ok.setVisibility(View.VISIBLE);
        ok.setText("Сохранить");

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        switch (operation) {
            case TEXT:
                ok.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
                editText.setHint(data);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        result = editable.toString();
                    }
                });
                break;
            case Text:
                editText.setVisibility(View.VISIBLE);
                editText.setText(data);
                editText.setSelection(editText.getText().length()); // Устанавливаем курсор

                editText.setSelectAllOnFocus(true);
                editText.selectAll();

                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Your action on done
                        //todo hide keyborad
                        result = v.getText().toString();
                        listener.clicked();
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                });
                ok.setOnClickListener(v -> {
                    result = editText.getText().toString();
                    Log.e("setOperation", "Text.result: " + result);

                    listener.clicked();
                    dialog.dismiss();
                });
                break;


            case Telephone:
                editText.setVisibility(View.VISIBLE);
                editText.setText(data);
                editText.setSelection(editText.getText().length());

                editText.setSelectAllOnFocus(true);
                editText.selectAll();

                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editText.setRawInputType(InputType.TYPE_CLASS_PHONE);

                editText.addTextChangedListener(new TelephoneMask());

                editText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        result = v.getText().toString();
                        listener.clicked();
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                });

                ok.setOnClickListener(v -> {
                    result = editText.getText().toString();
                    listener.clicked();
                    dialog.dismiss();
                });

                break;

            case Number:
                editText.setVisibility(View.VISIBLE);
                editText.setText(data);
                editText.setSelection(editText.getText().length());

                editText.setSelectAllOnFocus(true);
                editText.selectAll();

                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Your action on done
                        //todo hide keyborad
                        result = v.getText().toString();
                        listener.clicked();
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                });
                ok.setOnClickListener(v -> {
                    result = editText.getText().toString();
                    Log.e("setOperation", "Number.result: " + result);

                    listener.clicked();
                    dialog.dismiss();
                });
                break;

            case Date:
                Log.e("DATE_PICKER", "Here1: " + data);

                editDate.setVisibility(View.VISIBLE);
                editDate.setText(data);
                editDate.setOnClickListener(v -> {
                    Log.e("DATE_PICKER", "Here");

                    Calendar mcurrentDate = Calendar.getInstance();
                    int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                    int mMonth = mcurrentDate.get(Calendar.MONTH);
                    int mYear = mcurrentDate.get(Calendar.YEAR);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {

                        month = month + 1;
                        String date = year + "-" + month + "-" + dayOfMonth;

                        Log.e("DATE_PICKER", "Here2: " + data);

                        editDate.setText(date);

                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                });

                ok.setOnClickListener(v -> {
                    String date = editDate.getText().toString();
                    if (date.equals("0000-00-00")) {
                        listener.clicked();
                        dialog.dismiss();
                    }

                    result = editDate.getText().toString();
                    Log.e("setOperation", "Date.result: " + result);
                    listener.clicked();
                    dialog.dismiss();
                });
                break;

            case Spinner:
                spinner.setVisibility(View.VISIBLE);

                Log.e("setOperation", "map: " + map);

                if (map != null) {
                    String[] res = map.values().toArray(new String[0]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_item, res);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinner.setAdapter(adapter);

                    SpinnerDialogData spinnerDialogData = new SpinnerDialogData();
                    spinnerDialogData.setData(map);
                    Log.e("setOperation", "map: " + map);
                    spinner.setOnItemSelectedListener(spinnerDialogData);

                    ok.setOnClickListener(v -> {
                        result = spinner.getSelectedItem().toString();
                        Log.e("setOperation", "Spinner.result: " + result);
                        listener.clicked();
                        dialog.dismiss();
                    });
                }
                break;


            case DoubleSpinner:
                spinner.setVisibility(View.VISIBLE);
                spinner2.setVisibility(View.VISIBLE);

                if (mapSpinner != null && mapSpinner.size() > 0) {
                    String[] res = mapSpinner.values().toArray(new String[0]);

//                    ArrayList<String> res = new ArrayList<>();
//                    for (Map.Entry<String, String> entry : mapSpinner.entrySet()){
//                        res.add(Integer.parseInt(entry.getKey()), entry.getValue());
//                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_item, res);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinner.setAdapter(adapter);

                    int spinnerPosition = adapter.getPosition(textData);
                    spinner.setSelection(spinnerPosition);

                    SpinnerDialogData spinnerDialogData = new SpinnerDialogData();
                    spinnerDialogData.setData(mapSpinner);
                    Log.e("setOperation", "map: " + mapSpinner);
                    spinner.setOnItemSelectedListener(spinnerDialogData);
                }

                if (mapSpinner2 != null && mapSpinner2.size() > 0) {
                    String[] res = mapSpinner2.values().toArray(new String[0]);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_item, res);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinner2.setAdapter(adapter);

                    int spinnerPosition = adapter.getPosition(textData2);
                    spinner2.setSelection(spinnerPosition);

                    SpinnerDialogData spinnerDialogData = new SpinnerDialogData();
                    spinnerDialogData.setData(mapSpinner2);
                    Log.e("setOperation", "map: " + mapSpinner2);
                    spinner2.setOnItemSelectedListener(spinnerDialogData);
                }


                ok.setOnClickListener(v -> {
                    result = Globals.getKeyForValueS(spinner.getSelectedItem().toString(), mapSpinner);
                    result2 = Globals.getKeyForValueS(spinner2.getSelectedItem().toString(), mapSpinner2);

                    listener.clicked();
                    dialog.dismiss();
                });

                break;


            case EditTextAndSpinner:
                spinner.setVisibility(View.VISIBLE);
                editText2.setVisibility(View.VISIBLE);


                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);

                    // Get private mPopup member variable and try cast to ListPopupWindow
                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

                    // Set popupWindow height to 500px
                    popupWindow.setHeight(200);
                } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }


                if (mapSpinner != null && mapSpinner.size() > 0) {
                    String[] res = mapSpinner.values().toArray(new String[0]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_item, res);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinner.setAdapter(adapter);

                    int spinnerPosition = adapter.getPosition(textData);
                    spinner.setSelection(spinnerPosition);

                    SpinnerDialogData spinnerDialogData = new SpinnerDialogData();
                    spinnerDialogData.setData(mapSpinner);
                    Log.e("setOperation", "map: " + mapSpinner);
                    spinner.setOnItemSelectedListener(spinnerDialogData);
                }

                Log.e("EditTextAndSpinner", "textData2: " + textData2);

                if (textData2 != null && !textData2.equals("")) {
                    editText2.setText(textData2);
                    editText2.setSelection(editText2.getText().length());

                    editText2.setSelectAllOnFocus(true);
                    editText2.selectAll();

                }

                ok.setOnClickListener(v -> {
                    result = Globals.getKeyForValueS(spinner.getSelectedItem().toString(), mapSpinner);
                    result2 = editText2.getText().toString();
                    Log.e("EditTextAndSpinner", "result: " + result);
                    Log.e("EditTextAndSpinner", "result2: " + result2);
                    listener.clicked();
                    dialog.dismiss();
                });

                break;
        }

        Log.e("setOperation", "All.result: " + result);
    }

    public void setExpandableListView(SimpleExpandableListAdapter adapter, DialogClickListener listener) {
        operationLayout.setVisibility(View.VISIBLE);
        ok.setVisibility(View.VISIBLE);
        ok.setText("Сохранить");

        expListView.setVisibility(View.VISIBLE);
        editText2.setVisibility(View.VISIBLE);
        editText2.setHint("Примечание");

        expListView.setAdapter(adapter);
        expListView.setOnChildClickListener(getErrorExpandableListView());

        ok.setOnClickListener(v -> {
            String res2 = editText2.getText().toString();
            String res = result;

            if (res != null && res.length() > 0) {
                if (res.equals("9") && res2.length() >= 15) {
                    Toast.makeText(context, "Внесите корректно Примечание!\nОно должено быть не короче 15 символов", Toast.LENGTH_SHORT).show();
                } else {
                    result2 = res2;
                    listener.clicked();
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(context, "Вы не выбрали Ошибку!\nВыберите ошибку из списка выше", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private OnChildClickListener getErrorExpandableListView() {
        return (expandableListView, view, groupPos, childPos, l) -> {
            Map<String, String> map;
            map = (Map<String, String>) expandableListView.getExpandableListAdapter().getChild(groupPos, childPos);

            String str = map.get("monthName");
            String res = str.replace("* ", "");

            Toast.makeText(context, "Выбрали ошибку: " + res, Toast.LENGTH_SHORT).show();

            result = ErrorRealm.getErrorDbByNm(res).getID();
            return false;
        };
    }


    /**
     * 27.01.2021
     * Устанавливаем данные для спинера 1
     */
    public void setOperationSpinnerData(Map<String, String> map) {
        mapSpinner = map;
    }

    /**
     * 27.01.2021
     * Устанавливаем данные для спинера 2
     */
    public void setOperationSpinner2Data(Map<String, String> map) {
        mapSpinner2 = map;
    }

    /**
     * 27.01.2021
     * Устанавливаем текстовую дату которую используем в классе
     */
    public void setOperationTextData(String data) {
        textData = data;
    }

    /**
     * 27.01.2021
     * Устанавливаем текстовую дату2 которую используем в классе
     */
    public void setOperationTextData2(String data) {
        textData2 = data;
    }


    public void setAdditionalOperation(RecyclerView.Adapter adapter, RecyclerView.LayoutManager layout) {
        if (adapter != null) {
            additionalOperationLayout.setVisibility(View.VISIBLE);
            rView.setVisibility(View.VISIBLE);
            textView42.setVisibility(View.VISIBLE);

            GradientDrawable backgroundGradient = (GradientDrawable) rView.getBackground();
            backgroundGradient.setStroke(2, Color.parseColor("#B1BCBE"));

            rView.setHasFixedSize(true);
            rView.setLayoutManager(layout);
            rView.setAdapter(adapter);
        }
    }

    public void setRecycler(RecyclerView.Adapter adapter, RecyclerView.LayoutManager layout) {
        if (adapter != null) {
            additionalOperationLayout.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.VISIBLE);

            recycler.setLayoutManager(layout);
            recycler.setAdapter(adapter);
        }
    }

    public void setOk(CharSequence setButtonText, DialogClickListener clickListener) {
        ok.setVisibility(View.VISIBLE);
        if (setButtonText != null) {
            ok.setText(setButtonText);
        }
        ok.setOnClickListener(v -> {
            if (clickListener != null) clickListener.clicked();
            dismiss();
        });
    }

    public void setTxtLinkOk(CharSequence setButtonText, DialogClickListener clickListener) {
        txtLinkOk.setVisibility(View.VISIBLE);
        txtLinkOk.setPaintFlags(txtLinkOk.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (setButtonText != null) {
            txtLinkOk.setText(setButtonText);
        }
        txtLinkOk.setOnClickListener(v -> {
            if (clickListener != null) clickListener.clicked();
            dismiss();
        });
    }

    public void setCancel(CharSequence setButtonText, DialogClickListener clickListener) {
        cancel.setVisibility(View.VISIBLE);
        cancel.setText(setButtonText);
        cancel.setOnClickListener(v -> {
            if (clickListener != null) clickListener.clicked();
        });
    }

    public void setCancel2(CharSequence setButtonText, DialogClickListener clickListener) {
        cancel2.setVisibility(View.VISIBLE);
        cancel2.setText(setButtonText);
        cancel2.setOnClickListener(v -> {
            if (clickListener != null) clickListener.clicked();
        });
    }


    public void setDialogErrorColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(context.getResources().getColor(R.color.red_error));
        }
    }

    public void setDialogColorRed() {
        drawable = context.getResources().getDrawable(R.drawable.shape_rounded_corner_red);
        layoutDialog.setBackground(drawable);
    }

    public void setDialogColorDefault() {
        drawable = context.getResources().getDrawable(R.drawable.shape_rounded_corner);
        layoutDialog.setBackground(drawable);
    }


//    public void setDialogColorBack(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            drawable = context.getResources().getDrawable(R.drawable.shape_rounded_corner);
//        }
//    }

    // --------------------------

    /**
     * 11.01.2021
     */
    private boolean checkString(String s) {
        return s != null;
    }


    // --- R_E_T_U_R_N_E_D START ---

    public String getOperationResult() {
        return result;
    }

    public String getOperationResult2() {
        return result2;
    }

    // --- R_E_T_U_R_N_E_D END ---


    /**
     * 25.01.2021
     */
    private class SpinnerDialogData implements AdapterView.OnItemSelectedListener {
        private Globals globals = new Globals();
        private String spinnerString = "";
        private Map<String, String> mapSpinner = new HashMap<>();

        public void setData(Map<String, String> data) {
            mapSpinner = data;
        }

        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            try {
                String s = parent.getSelectedItem().toString();
                spinnerString = globals.getKeyForValueS(s, mapSpinner);
            } catch (Exception e) {
                // TODO Рассматривать ошибку
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }


}
