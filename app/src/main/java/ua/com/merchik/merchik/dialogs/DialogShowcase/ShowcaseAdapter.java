package ua.com.merchik.merchik.dialogs.DialogShowcase;

import static ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogAdapter.photoData;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogPhotoAdapter;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhoto;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;

public class ShowcaseAdapter extends RecyclerView.Adapter<ShowcaseAdapter.ViewHolder> implements Filterable {

    private List<ShowcaseSDB> showcaseList;
    private List<ShowcaseSDB> showcaseListFilterable;
    private List<ShowcaseSDB> showcaseListOrig;
    private WpDataDB wpDataDB;
    private Clicks.click click;

    public ShowcaseAdapter(WpDataDB wpDataDB, ArrayList<ShowcaseSDB> showcaseList, Clicks.click click) {
        this.wpDataDB = wpDataDB;
        if (showcaseList != null && !showcaseList.isEmpty()) {
            showcaseList.add(defaultShowcase());
            this.showcaseList = showcaseList;
            this.showcaseListFilterable = showcaseList;
            this.showcaseListOrig = showcaseList;
            this.click = click;
        } else {
            this.showcaseList = Collections.singletonList(defaultShowcase());
            this.showcaseListFilterable = Collections.singletonList(defaultShowcase());
            this.showcaseListOrig = Collections.singletonList(defaultShowcase());
            this.click = click;
        }
    }

    @NonNull
    @Override
    public ShowcaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_showcase, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowcaseAdapter.ViewHolder holder, int position) {
        holder.bind(showcaseList.get(position));
    }

    @Override
    public int getItemCount() {
        if (showcaseList != null) {
            return showcaseList.size();
        } else {
            return 0;
        }
    }

    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        TextView textViewShowcaseId, textViewShowcaseNm, textViewClientGroup, textViewPlanogramm;
        ImageView image;

        ViewHolder(View v) {
            super(v);
            constraintLayout = v.findViewById(R.id.constraintLayout);
            textViewShowcaseId = v.findViewById(R.id.textViewShowcaseId);
            textViewShowcaseNm = v.findViewById(R.id.textViewShowcaseNm);
            textViewClientGroup = v.findViewById(R.id.textViewClientGroup);
            textViewPlanogramm = v.findViewById(R.id.textViewPlanogramm);
            image = v.findViewById(R.id.image);
        }

        public void bind(ShowcaseSDB showcase) {
            try {
                if (showcase.id == 0) {
                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.white));
                } else if (showcase.showcasePhoto >= 1) {
                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.green_default));
                } else if (showcase.showcasePhoto == 0) {
                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.red_error));
                } else {
                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.red_error));
                }

                String groupName = "не встановлена";
                String planogram = "не встановлена";

                if (showcase.tovarGrp != null && showcase.tovarGrpTxt != null) {
                    groupName = showcase.tovarGrpTxt;
                }

                if (showcase.photoPlanogramTxt != null) {
                    planogram = "(" + showcase.planogramId + ") " + showcase.photoPlanogramTxt;
                    String pl = "Планограма:";
                    SpannableString spannableString = new SpannableString(pl);
                    spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, pl.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

                    Log.e("CASE: 1 $$$$$$$$$","+");
                    SpannableString spannableString2 = new SpannableString(createLinkedString(showcase, planogram));

                    SpannableStringBuilder spannableStringRes = new SpannableStringBuilder();
                    spannableStringRes.append(spannableString).append(" ").append(spannableString2);

                    textViewPlanogramm.setText(spannableStringRes);
                    textViewPlanogramm.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    textViewPlanogramm.setText(Html.fromHtml("<b>Планограма::</b> " + planogram));
                }

                StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(String.valueOf(showcase.photoId));

                textViewShowcaseId.setText(Html.fromHtml("<b>Вітрина №:</b> " + showcase.id));
                textViewShowcaseNm.setText(Html.fromHtml("<b>Назва:</b> " + showcase.nm));
                textViewClientGroup.setText(Html.fromHtml("<b>Група тов.:</b> " + groupName));

                if (showcase.id == 0) {
                    textViewShowcaseId.setText("Створити фото без зазначення вітрини");
                    textViewClientGroup.setVisibility(View.GONE);
                    textViewPlanogramm.setVisibility(View.GONE);
                    image.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_menu_camera));
                } else if (stackPhotoDB != null) {
                    textViewClientGroup.setVisibility(View.VISIBLE);
                    textViewPlanogramm.setVisibility(View.VISIBLE);
                    if (stackPhotoDB.getPhoto_num() != null) {
                        String uriStr = stackPhotoDB.photo_num;
                        Uri uri = Uri.parse(uriStr);
                        image.setImageURI(uri);
                    }
                    image.setOnClickListener(v -> {


                        try {
                            DialogFullPhoto dialog = new DialogFullPhoto(image.getContext());
                            dialog.setWpDataDB(wpDataDB);
                            dialog.setRatingType(DialogFullPhoto.RatingType.SHOWCASE);
                            dialog.setPhotos(0, Collections.singletonList(stackPhotoDB), new PhotoLogPhotoAdapter.OnPhotoClickListener() {
                                @Override
                                public void onPhotoClicked(Context context, StackPhotoDB photoDB) {
                                    try {
                                        DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(image.getContext());
                                        dialogFullPhoto.setPhoto(stackPhotoDB);

                                        // Pika
                                        dialogFullPhoto.setComment(stackPhotoDB.getComment());

                                        dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                                        dialogFullPhoto.show();
                                    } catch (Exception e) {
                                        Log.e("ShowcaseAdapter", "Exception e: " + e);
                                    }
                                }
                            }, ()->{});

                            dialog.setTextInfo(photoData(stackPhotoDB));
                            Log.e("CASE: 1 $$$$$$$$$","+");
                            dialog.getComment(stackPhotoDB.getComment(), () -> {
                                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "stackPhotoDB: " + new Gson().toJson(stackPhotoDB));
                                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "stackPhotoDB.getComment(): " + stackPhotoDB.getComment());
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    stackPhotoDB.setComment(dialog.commentResult);
                                    stackPhotoDB.setCommentUpload(true);
                                    dialog.isCommentSave = true;
                                });
                                RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                                Toast.makeText(image.getContext(), "Комментарий сохранён", Toast.LENGTH_LONG).show();
                            });

                            try {
                                dialog.setTask(stackPhotoDB.getUser_id(), stackPhotoDB.getAddr_id(), stackPhotoDB.getClient_id(), stackPhotoDB.getCode_dad2(), stackPhotoDB);
                            } catch (Exception e) {

                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.setRating();
                            dialog.setDvi();
                            dialog.show();
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "ShowcaseAdapter/bind123123", "Exception e: " + e);
                        }
                    });
                } else {
                    textViewClientGroup.setVisibility(View.VISIBLE);
                    textViewPlanogramm.setVisibility(View.VISIBLE);
                    image.setImageDrawable(itemView.getContext().getResources().getDrawable(R.mipmap.merchik));
                }

                constraintLayout.setOnClickListener(v -> {
                    click.click(showcase);
                });

            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "ShowcaseAdapter/bind", "Exception e: " + e);
            }
        }
    }

    /**
     * 10.07.23.
     * В случае если у меня нет ни одной Витрины (или в самый конец - надо добавить новую) я создаю
     * дефолтный элемент типа "Создать новую Витрину" после чего я буду указывать нулевой идентификатор
     * для фотографии, что б она в будущем могда сохраниться как новая фотка для Витрины.
     * <p>
     * (т.е. я в других случаях говорю что "некст фото выполнены по Этой витрине", а в случае с дефолтом
     * "нект фото будет продвигаться как Новая Витрина")
     */
    private ShowcaseSDB defaultShowcase() {
        ShowcaseSDB res = new ShowcaseSDB();

        res.id = 0;

        return res;
    }

    private SpannableString createLinkedString(ShowcaseSDB showcase, String msg) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
//                    Toast.makeText(textView.getContext(), "sjbajsdakjhsdkasdbljasdfbh", Toast.LENGTH_LONG).show();

                    PlanogrammJOINSDB planogrammJOINSDB = SQL_DB.planogrammDao().getSoloBy(showcase.planogramId, null, null);
                    StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(String.valueOf(planogrammJOINSDB.planogrammPhotoId));

//                    StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(String.valueOf(showcase.photoPlanogramId));
                    if (stackPhotoDB != null) {
                        try {
                            DialogFullPhoto dialog = new DialogFullPhoto(textView.getContext());
                            dialog.setWpDataDB(wpDataDB);
                            dialog.setRatingType(DialogFullPhoto.RatingType.SHOWCASE);
                            dialog.setPhotos(0, Collections.singletonList(stackPhotoDB), new PhotoLogPhotoAdapter.OnPhotoClickListener() {
                                @Override
                                public void onPhotoClicked(Context context, StackPhotoDB photoDB) {
                                    try {
                                        DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(textView.getContext());
                                        dialogFullPhoto.setPhoto(stackPhotoDB);

                                        // Pika
                                        dialogFullPhoto.setComment(stackPhotoDB.getComment());

                                        dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                                        dialogFullPhoto.show();
                                    } catch (Exception e) {
                                        Log.e("ShowcaseAdapter", "Exception e: " + e);
                                    }
                                }
                            }, ()->{});

                            dialog.setTextInfo(photoData(stackPhotoDB));
                            dialog.getComment(stackPhotoDB.getComment(), () -> {
                                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "stackPhotoDB: " + new Gson().toJson(stackPhotoDB));
                                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "stackPhotoDB.getComment(): " + stackPhotoDB.getComment());
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    stackPhotoDB.setComment(dialog.commentResult);
                                    stackPhotoDB.setCommentUpload(true);
                                    dialog.isCommentSave = true;
                                });
                                RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                                Toast.makeText(textView.getContext(), "Комментарий сохранён", Toast.LENGTH_LONG).show();
                            });

                            try {
                                dialog.setTask(stackPhotoDB.getUser_id(), stackPhotoDB.getAddr_id(), stackPhotoDB.getClient_id(), stackPhotoDB.getCode_dad2(), stackPhotoDB);
                            } catch (Exception e) {

                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.setRating();
                            dialog.setDvi();
                            dialog.show();
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "ShowcaseAdapter/bind123123", "Exception e: " + e);
                        }
/*                        DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(textView.getContext());
                        dialogFullPhoto.setPhoto(stackPhotoDB);
                        dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                        dialogFullPhoto.show();*/
                    } else {
                        DialogData dialogData = new DialogData(textView.getContext());
                        dialogData.setTitle("Фото не знайдено.");
                        dialogData.setText("Не вийшло виявити фото Планограми. Спробуйте повторити синхронізацію, або зверніться до Вашого керівника.");
                        dialogData.setClose(dialogData::dismiss);
                        dialogData.show();
                    }
                } catch (Exception e) {
                    Log.e("ShowcaseAdapter", "Exception e: " + e);
//                    Toast.makeText(textView.getContext(), "==================================================", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ShowcaseSDB> filteredResults = null;

                if (constraint.length() == 0) {
                    filteredResults = showcaseListOrig;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter().getFilteredResultsShowcaseSDB(item, filteredResults, showcaseListFilterable);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                showcaseList = (List<ShowcaseSDB>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
