package ua.com.merchik.merchik.dialogs.DialogShowcase;

import static ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogAdapter.photoData;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
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
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogFullPhoto;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;

public class PlanogramAdapter extends RecyclerView.Adapter<PlanogramAdapter.ViewHolder> implements Filterable {

    private List<PlanogrammJOINSDB> planogrammList;
    private List<PlanogrammJOINSDB> planogrammListFilterable;
    private List<PlanogrammJOINSDB> planogrammListOrig;
    private Clicks.click click;

    public PlanogramAdapter(ArrayList<PlanogrammJOINSDB> planogrammList, Clicks.click click) {
        if (planogrammList != null && !planogrammList.isEmpty()) {
            planogrammList.add(defaultShowcase());
            this.planogrammList = planogrammList;
            this.planogrammListFilterable = planogrammList;
            this.planogrammListOrig = planogrammList;
            this.click = click;
        } else {
            this.planogrammList = Collections.singletonList(defaultShowcase());
            this.planogrammListFilterable = Collections.singletonList(defaultShowcase());
            this.planogrammListOrig = Collections.singletonList(defaultShowcase());
            this.click = click;
        }
    }

    private PlanogrammJOINSDB defaultShowcase() {
        PlanogrammJOINSDB res = new PlanogrammJOINSDB();
        res.id = 0;
        return res;
    }

    @NonNull
    @Override
    public PlanogramAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_showcase, parent, false);
        return new PlanogramAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanogramAdapter.ViewHolder holder, int position) {
        holder.bind(planogrammList.get(position));
    }

    @Override
    public int getItemCount() {
        if (planogrammList != null) {
            return planogrammList.size();
        } else {
            return 0;
        }
    }

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

        public void bind(PlanogrammJOINSDB planogrammData) {
            try {
//                if (planogramm.id == 0) {
                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.white));
//                } else if (planogramm.showcasePhoto >= 1){
//                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.green_default));
//                } else if (planogramm.showcasePhoto == 0){
//                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.red_error));
//                }else {
//                    constraintLayout.setBackgroundColor(constraintLayout.getContext().getResources().getColor(R.color.red_error));
//                }

                String groupName = planogrammData.planogrammGroupTxt == null ? "не встановлена" : planogrammData.planogrammGroupTxt;
                String planogram = "не встановлена";

//                if (planogrammData.tovarGrp != null && planogrammData.tovarGrpTxt != null) {
//                    groupName = planogrammData.tovarGrpTxt;
//                }

                if (planogrammData.planogrammName != null) {
                    planogram = planogrammData.planogrammName;
                }

                StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(String.valueOf(planogrammData.planogrammPhotoId));

                textViewShowcaseId.setText(Html.fromHtml("<b>Планограма №:</b> " + planogrammData.id));
                textViewShowcaseNm.setText(Html.fromHtml("<b>Назва:</b> " + planogrammData.planogrammName));
                textViewClientGroup.setText(Html.fromHtml("<b>Група тов.:</b> " + groupName));
                textViewPlanogramm.setText(Html.fromHtml("<b>Планограма:</b> " + planogram));

                if (planogrammData.id == 0) {
                    textViewShowcaseId.setText("Створити фото без зазначення Планограми");
                    textViewClientGroup.setVisibility(View.GONE);
                    textViewPlanogramm.setVisibility(View.GONE);
                    image.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_menu_camera));
                } else if (stackPhotoDB != null) {
                    textViewClientGroup.setVisibility(View.VISIBLE);
                    textViewPlanogramm.setVisibility(View.VISIBLE);
                    String uriStr = stackPhotoDB.photo_num;
                    Uri uri = Uri.parse(uriStr);
                    image.setImageURI(uri);
                    image.setOnClickListener(v -> {
                        try {
                            DialogFullPhoto dialog = new DialogFullPhoto(image.getContext());
                            dialog.setRatingType(DialogFullPhoto.RatingType.PLANOGRAM);
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
                            dialog.getComment(stackPhotoDB.getComment(), () -> {
                                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "stackPhotoDB: " + new Gson().toJson(stackPhotoDB));
                                Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "stackPhotoDB.getComment(): " + stackPhotoDB.getComment());
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    stackPhotoDB.setComment(dialog.commentResult);
                                    stackPhotoDB.setCommentUpload(true);
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

                        }
                    });
                } else {
                    textViewClientGroup.setVisibility(View.VISIBLE);
                    textViewPlanogramm.setVisibility(View.VISIBLE);
                    image.setImageDrawable(itemView.getContext().getResources().getDrawable(R.mipmap.merchik));
                }

                constraintLayout.setOnClickListener(v -> click.click(planogrammData));

            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "ShowcaseAdapter/bind", "Exception e: " + e);
            }
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<PlanogrammJOINSDB> filteredResults = null;

                if (constraint.length() == 0) {
                    filteredResults = planogrammListOrig;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter().getFilteredResultsPlanogrammSDB(item, filteredResults, planogrammListFilterable);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                planogrammList = (List<PlanogrammJOINSDB>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
