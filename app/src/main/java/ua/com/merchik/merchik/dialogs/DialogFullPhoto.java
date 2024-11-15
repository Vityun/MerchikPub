package ua.com.merchik.merchik.dialogs;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.YELLOW;
import static ua.com.merchik.merchik.Globals.generateUniqueNumber;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.gson.Gson;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogAdapter;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogPhotoAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;

public class DialogFullPhoto {

    private Dialog dialog;
    private Context context;

    private WpDataDB wpDataDB;

    private enum MoveTo {
        PREVIOUS, NEXT
    }

    public enum RatingType {
        PHOTO,      // default
        SHOWCASE,   // 4
        PLANOGRAM   // 5
    }

    private RatingType ratingType;

    private Timer timer = new Timer();
    private boolean slideshow = false;
    private int POSITION_ADAPTER;

    public String commentResult = "";
    public List<StackPhotoDB> photoLogData;
    public int position;

    private ImageButton camera;

    // ------------------------
    private RecyclerView recycler;
    private ImageView photo;
    private TextView photoInfo;
    private CheckBox dvi;
    private EditText comment;
    private ImageButton next, play, previous, openFullSize;
    private Button task;

    private RatingBar indicatorRatingBar;

    // ------------------------
    private ImageButton close, help, videoHelp, call;

    /**
     * Длинная рука
     */
    public DialogFullPhoto(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_photo_log_full_photo);

        photoInfo = dialog.findViewById(R.id.photo_info);
        dvi = dialog.findViewById(R.id.dvi);
        comment = dialog.findViewById(R.id.add_comment);
        task = dialog.findViewById(R.id.set_task);
        next = dialog.findViewById(R.id.next_photo);
        play = dialog.findViewById(R.id.play_photo);
        previous = dialog.findViewById(R.id.previous_photo);
        openFullSize = dialog.findViewById(R.id.open_full_size);
        recycler = dialog.findViewById(R.id.photos_recycler);

        indicatorRatingBar = dialog.findViewById(R.id.ratingBar3);
        camera  = dialog.findViewById(R.id.camera_hand);

        close = dialog.findViewById(R.id.imageButtonClose);

        next.setOnClickListener(v -> recyclerScroll(MoveTo.NEXT));
        previous.setOnClickListener(v -> recyclerScroll(MoveTo.PREVIOUS));
    }

    public void setCamera(Clicks.clickVoid clickVoid){
        camera.setOnClickListener((view) -> {
            clickVoid.click();
        });
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    // ---------------------------------------------------------------------------------------------


    public RatingType getRatingType() {
        return ratingType;
    }

    public void setWpDataDB(WpDataDB wpDataDB) {
        this.wpDataDB = wpDataDB;
    }

    public void setRatingType(RatingType ratingType) {
        this.ratingType = ratingType;
    }

    public void setPhoto(Uri data) {
        File file = new File(data.toString());
        Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
        if (b != null) {
            photo.setImageBitmap(b);
        }
    }

    public void setTextInfo(CharSequence data) {
        photoInfo.setMovementMethod(new ScrollingMovementMethod());
        photoInfo.setText(data);
    }

    public void getComment(String data, DialogData.DialogClickListener clickListener) {
//        comment.setText(data);
        comment.setSelection(comment.getText().length()); // Устанавливаем курсор

        comment.setSelectAllOnFocus(true);
        comment.selectAll();

        comment.setImeOptions(EditorInfo.IME_ACTION_DONE);
        comment.setRawInputType(InputType.TYPE_CLASS_TEXT);
        comment.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String coment = v.getText().toString();

                StackPhotoDB row = photoLogData.get(POSITION_ADAPTER);

                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "coment: " + coment);
                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "StackPhotoDB row: " + new Gson().toJson(row));

                RealmManager.INSTANCE.executeTransaction(realm -> {
                    row.setComment(coment);
                    row.setCommentUpload(true);
                    row.setVpi(System.currentTimeMillis() / 1000);
                });
                RealmManager.stackPhotoSavePhoto(row);

                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Toast.makeText(context, "Комментарий сохранён", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    public void setPhotos(int pos, List<StackPhotoDB> data, PhotoLogPhotoAdapter.OnPhotoClickListener mOnPhotoClickListener, Clicks.clickVoid clickVoid) {
        List<StackPhotoDB> list = data;

        photoLogData = list;
        position = pos;

        Log.e("setPhotos", "ratingType: " + ratingType);

        Log.e("setPhotos", "position: " + position);
        Log.e("setPhotos", "photoLogData: " + photoLogData.get(position).getId());

//        openFullSize.setOnClickListener(view -> {
//            mOnPhotoClickListener.onPhotoClicked(view.getContext(), data.get(pos));
////            dialog.dismiss();
//        });

        PhotoLogPhotoAdapter adapter = new PhotoLogPhotoAdapter(list, (v, event) -> {
            boolean result = true;
            //can scroll horizontally checks if there's still a part of the image
            //that can be scrolled until you reach the edge
            if (event.getPointerCount() >= 2 || v.canScrollHorizontally(1) || v.canScrollHorizontally(-1)) {
                //multi-touch event
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Disallow RecyclerView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);//????
                        // Disable touch on view
                        result = false;
                        break;
                    case MotionEvent.ACTION_UP:
//                        v.performClick(); // Ругалось что нет этой штуки
                        // Allow RecyclerView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        result = true;
                        break;

                    default:
                        break;
                }
            }

            return result;
        }, mOnPhotoClickListener, clickVoid);

        LinearLayoutManager manager = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);


        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);

        recycler.scrollToPosition(position);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visiblePosition = manager.findFirstCompletelyVisibleItemPosition();

                POSITION_ADAPTER = visiblePosition;

                Log.e("onScrolled", "visiblePosition: " + visiblePosition);

                if (visiblePosition > -1) {
//                    View v = manager.findViewByPosition(visiblePosition);

                    //update ui

                    openFullSize.setOnClickListener(view -> {
                        mOnPhotoClickListener.onPhotoClicked(view.getContext(), photoLogData.get(visiblePosition));
                    });

                    comment.setText(photoLogData.get(visiblePosition).getComment());

                    try {
                        StackPhotoDB stackPhotoDB = photoLogData.get(visiblePosition);
                        if (!stackPhotoDB.getComment().isEmpty() && !stackPhotoDB.commentUpload) {
                            comment.setTextColor(GREEN);
                        } else if (!stackPhotoDB.getComment().isEmpty() && stackPhotoDB.commentUpload) {
                            comment.setTextColor(YELLOW);
                        } else {
                            comment.setTextColor(BLACK);
                        }
                    } catch (Exception e) {

                    }

                    photoInfo.setText(PhotoLogAdapter.photoData(photoLogData.get(visiblePosition)));


                    // УСТАНОВКА НА ДВИ и СОХРАНЕНИЕ В БД
                    // TODO сделать так что б на ДВИ могли и бойцы обычные ставить. Только что сделанные фотки
                    try {
                        Log.e("CheckBoxDVI", "Visualise: " + dvi.isChecked());
                        Log.e("CheckBoxDVI", "VisualiseData: " + photoLogData.get(visiblePosition).isDvi());

                        Integer dviDB = photoLogData.get(visiblePosition).isDvi();
                        boolean dviVal;

                        if (dviDB != null) {
                            dviVal = dviDB != 0;
                        } else {
                            dviVal = false;
                        }

                        dvi.setChecked(dviVal);
                        dvi.setOnClickListener((v) -> {
                            Log.e("CheckBoxDVI", "click");
                            StackPhotoDB row = photoLogData.get(POSITION_ADAPTER);
                            RealmManager.INSTANCE.executeTransaction((realm) -> {
                                if (dviVal) {
                                    row.setDvi(0);
                                } else {
                                    row.setDvi(1);
                                }
//                            row.setDtUpdate(System.currentTimeMillis() / 1000);
                                row.setDviUpload(true);
                            });
                            RealmManager.stackPhotoSavePhoto(row);
                            Log.e("CheckBoxDVI", "row.isDviUpload(): " + row.isDviUpload());

                            Toast.makeText(context, "Фото установлено на ДВИ", Toast.LENGTH_SHORT).show();
                        });
                        Log.e("CheckBoxDVI", "AfterVisualise: " + dvi.isChecked());
                        Log.e("CheckBoxDVI", "AfterVisualiseData: " + photoLogData.get(visiblePosition).isDvi());
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "DialogFullPhoto/setDVI", "Exception e: " + e);
                    }


                    // УСТАНОВКА РЕЙТИНГА И СОХРАНЕНИЕ ЕГО В БД
                    try {
                        if (photoLogData.get(visiblePosition).getMark() != null) {
                            indicatorRatingBar.setRating(Float.parseFloat(photoLogData.get(visiblePosition).getMark()));
                        } else {
                            indicatorRatingBar.setRating(0);
                        }

                        indicatorRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                            int rate = (int) rating;
                            indicatorRatingBar.setRating(rate);
                            Toast.makeText(context, "Оценка: " + rate + " установлена.", Toast.LENGTH_LONG).show();
                            StackPhotoDB row = photoLogData.get(POSITION_ADAPTER);

                            if (rate > 5) {
                                savePhotoData(row, rate, null, ratingType);
                            } else {
                                DialogData dialog = new DialogData(context);
                                dialog.setTitle("Низкая оценка");
                                dialog.setText("Прокомментируйте причину низкой оценки.");
                                dialog.setOperation(DialogData.Operations.TEXT, "Ваш Комментарий", null, () -> {
                                });
                                dialog.setCancel("Сохранить", () -> {
                                    String comment = dialog.getOperationResult();

                                    if (comment != null && comment.length() > 1) {
                                        // Сохранение коммента
                                        Toast.makeText(ratingBar.getContext(), "Комментарий: " + comment + " сохранён.", Toast.LENGTH_SHORT).show();

                                        savePhotoData(row, rate, comment, ratingType);
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(dialog.context, "Комментарий НЕ сохранён. Заполните корректно поле для комментария!", Toast.LENGTH_LONG).show();
                                    }
                                });
                                dialog.setClose(() -> {
                                    Toast.makeText(context, "Комментарий НЕ сохранён", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                            }


                        });
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "DialogFullPhoto.Установка рейтинга фото", "Exception e: " + e);
                    }

                }
            }
        });


        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recycler);

        play.setOnClickListener((v) -> {
            if (slideshow) {
                pausedSlideShow();
            } else {
                startSlideShow(POSITION_ADAPTER, adapter);
            }
        });
    }


    // 1 - оценка дет отчёта
    // 2 - оценка аудио файла
    // 3 - оценка достижения
    // 4 - оценка идентификатора витрины
    // 5 - оценка идентификатора планограммы
    private void savePhotoData(StackPhotoDB row, int rate, String comment, RatingType ratingType) {
//        ratingType = RatingType.PHOTO;
        switch (ratingType) {
            case SHOWCASE -> {
                VoteSDB vote = new VoteSDB();
                vote.serverId = generateUniqueNumber();
                vote.dtUpload = 0L;
                vote.codeDad2 = wpDataDB.getCode_dad2();
                vote.isp = wpDataDB.getIsp();
                vote.themeId = 1313;
                vote.kli = wpDataDB.getClient_id();
                vote.addrId = wpDataDB.getAddr_id();
                vote.dt = System.currentTimeMillis() / 1000;
                vote.merchik = wpDataDB.getUser_id();
                vote.voterId = wpDataDB.getUser_id();
                vote.photoId = row.photoServerId != null ? Long.parseLong(row.photoServerId) : 0;
                vote.voteClass = 4;
                vote.score = rate;
                vote.comments = comment;

                SQL_DB.votesDao().insertAll(Collections.singletonList(vote));
            }
            case PLANOGRAM -> {
                VoteSDB vote = new VoteSDB();
                vote.serverId = generateUniqueNumber();
                vote.dtUpload = 0L;
                vote.codeDad2 = wpDataDB.getCode_dad2();
                vote.isp = wpDataDB.getIsp();
                vote.themeId = 1314;
                vote.kli = wpDataDB.getClient_id();
                vote.addrId = wpDataDB.getAddr_id();
                vote.dt = System.currentTimeMillis() / 1000;
                vote.merchik = wpDataDB.getUser_id();
                vote.voterId = wpDataDB.getUser_id();
                vote.photoId = row.photoServerId != null ? Long.parseLong(row.photoServerId) : 0;
                vote.voteClass = 5;
                vote.score = rate;
                vote.comments = comment;

                SQL_DB.votesDao().insertAll(Collections.singletonList(vote));
            }
            default -> {
                RealmManager.INSTANCE.executeTransaction((realm) -> {
                    row.setMark(String.valueOf(rate));
                    row.setMarkUpload(true);
                    if (comment != null && !comment.equals("") && comment.length() > 1) {
                        row.setComment(comment);
                        row.setCommentUpload(true);
                    }
                });
                RealmManager.stackPhotoSavePhoto(row);
            }
        }
    }


    /**
     * 24.02.2021
     * Установка CheckBox и слушателя к нему.
     * <p>
     * Контроль и установка ДВИ к фото. Нужно сделать функцию установки на ДВИ более глобальной.
     */
    public void setDvi() {

//        Log.e("CheckBoxDVI", "here");
//        Log.e("CheckBoxDVI", "dvi: " + dvi.isChecked());
//
//        // Получаю текущее значение ДВИ
//        StackPhotoDB row = photoLogData.get(POSITION_ADAPTER);
//        boolean b = row.isDvi();
//        Log.e("CheckBoxDVI", "getStackPhotoDB.dvi: " + b);
//        dvi.setChecked(b);


//        dvi.setOnClickListener((v) -> {
//            Log.e("CheckBoxDVI", "click");
//        });


//        dvi.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//            Log.e("CheckBoxDVI", "click");
//            Log.e("CheckBoxDVI", "start.click.dvi: " + dvi.isChecked());
//
//            // Сохраняю в БД изменённое значение для текущего элемента
//            RealmManager.INSTANCE.executeTransaction((realm) -> {
//                row.setDvi(isChecked);
//                Log.e("CheckBoxDVI", "setStackPhotoDB.dvi: " + isChecked);
//            });
//            RealmManager.stackPhotoSavePhoto(row);
//
////            dvi.setChecked(!b);
//
//            Log.e("CheckBoxDVI", "end.click.dvi: " + dvi.isChecked());
//        });
    }


    public void setRating() {
//        indicatorRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
//            int rate = (int) rating;
//            indicatorRatingBar.setRating(rate);
//            Toast.makeText(context, "Оценка: " + rate + " установлена.", Toast.LENGTH_LONG).show();
//        });
    }

    // ---------------------------------------------------------------------------------------------

    private void startSlideShow(int position, PhotoLogPhotoAdapter adapter) {
        slideshow = true;
        timer = new Timer();
        timer.schedule(new ScrollTask(position, recycler, adapter), 1000, 1000);
        play.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));
    }

    private void pausedSlideShow() {
        slideshow = false;
        timer.cancel();
        play.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
    }

    private class ScrollTask extends TimerTask {
        private int position = 0;
        private RecyclerView recycler;
        private PhotoLogPhotoAdapter adapter;

        public ScrollTask(int pos, RecyclerView recycler, PhotoLogPhotoAdapter adapter) {
            this.position = pos;
            this.recycler = recycler;
            this.adapter = adapter;
        }

        public void run() {
            setRecyclerData(recycler, adapter);
        }

        private void setRecyclerData(RecyclerView recyclerView, PhotoLogPhotoAdapter adapter) {
            recyclerView.post(() -> {
                if (position == adapter.getItemCount()) {
                    pausedSlideShow();
                } else {
                    position++;
                }

                Log.e("onScrolled", "ScrollTask.position: " + position);
                recyclerView.smoothScrollToPosition(position);
            });
        }
    }

    private boolean maxSize() {
        Log.e("recyclerScroll", "maxSize.position: " + POSITION_ADAPTER);
        Log.e("recyclerScroll", "maxSize.photoLogData.size(): " + photoLogData.size());
        return POSITION_ADAPTER == photoLogData.size();
    }

    private boolean minSize() {
        Log.e("recyclerScroll", "minSize: " + POSITION_ADAPTER);
        return POSITION_ADAPTER <= 0;
    }

    private void recyclerScroll(MoveTo move) {

        Log.e("recyclerScroll", "ONE.PRESSED.MOVE: " + move);

        switch (move) {
            case PREVIOUS:
                Log.e("recyclerScroll", "PREVIOUS");
                if (!minSize()) {
                    Log.e("recyclerScroll", "PREVIOUS.MOVE");
                    recycler.scrollToPosition(--POSITION_ADAPTER);
                }
                break;


            case NEXT:
                Log.e("recyclerScroll", "NEXT");
                if (!maxSize()) {
                    Log.e("recyclerScroll", "NEXT.MOVE");
                    recycler.scrollToPosition(++POSITION_ADAPTER);
                }
                break;
        }
    }

    public void setTask(int user, int addr, String cust, long dad2, StackPhotoDB photoDB) {
        task.setOnClickListener(v -> {
            DialogCreateTAR dialog = new DialogCreateTAR(context);
            dialog.setClose(dialog::dismiss);
            dialog.setData(user, addr, cust, dad2, photoDB);

            dialog.setRecyclerView(null);


            dialog.refreshAdaper(photoDB);
            dialog.show();
        });
    }

}
