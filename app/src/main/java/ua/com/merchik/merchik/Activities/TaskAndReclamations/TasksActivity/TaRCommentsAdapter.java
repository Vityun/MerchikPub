package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ServerExchange.PhotoUrlUtils;
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.dialogs.PhotoPreviewLoader;

public class TaRCommentsAdapter extends RecyclerView.Adapter<TaRCommentsAdapter.ViewHolder> {

    private Context mContext;
    private List<TARCommentsDB> data;
    private CommentPhotoClick commentPhotoClick;
    private DialogFullPhotoR photoDialog;
    private final Set<ViewHolder> boundHolders = new HashSet<>();

    public interface CommentPhotoClick {
        void commentPhotoClick(int i);
    }

    public TaRCommentsAdapter(Context mContext, List<TARCommentsDB> data, CommentPhotoClick commentPhotoClick) {
        this.mContext = mContext;
        this.data = data;
        this.commentPhotoClick = commentPhotoClick;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView layout;
        private ImageView photo;
        private PhotoPreviewLoader<StackPhotoDB> thumbnailLoader;
        private TextView textLine1, textLine2, textLine3, textLine4, textLine5;

        ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.universalItem);
            photo = v.findViewById(R.id.photo);
            textLine1 = v.findViewById(R.id.text_line_1);
            textLine2 = v.findViewById(R.id.text_line_2);
            textLine3 = v.findViewById(R.id.text_line_3);
            textLine4 = v.findViewById(R.id.text_line_4);
            textLine5 = v.findViewById(R.id.text_line_5);
        }

        public void bind(TARCommentsDB dataItem) {
            SpannableStringBuilder line1 = new SpannableStringBuilder();
            String time = new SimpleDateFormat("dd-MM-yyyy").format(Clock.timeLongToDAte(Long.parseLong(dataItem.getDt())));
            CharSequence date = Html.fromHtml("<b>Дата: </b>" + time + " ");
            line1.append(date);
            textLine1.setText(line1);

            SpannableStringBuilder line2 = new SpannableStringBuilder();
            String time2 = new SimpleDateFormat("HH:mm:ss").format(Clock.timeLongToDAte(Long.parseLong(dataItem.getDt())));
            CharSequence date2 = Html.fromHtml("<b>Время: </b>" + time2 + " ");
            line2.append(date2);
            textLine2.setText(line2);

            SpannableStringBuilder line3 = new SpannableStringBuilder();
            line3.append(Html.fromHtml("<b>Автор комментария: </b>"));
            if (dataItem.getWho() != null && !dataItem.getWho().equals("")) {
                try {
                    UsersDB usersDB = UsersRealm.getUsersDBById(Integer.parseInt(dataItem.getWho()));
                    CharSequence str = usersDB.getNm();
                    line3.append(str);
                } catch (Exception e) {
                    // TODO data is empty
                    line3.append(dataItem.getWho());
                }

            } else {
                line3.append(dataItem.getWho());
            }
            textLine3.setText(line3);

            SpannableStringBuilder line4 = new SpannableStringBuilder();
            line4.append(Html.fromHtml("<b>Комментарий: </b>"));
            line4.append(Html.fromHtml(dataItem.getComment()));
            textLine4.setText(line4);

            textLine5.setVisibility(View.GONE);


            bindPhoto(dataItem);

            // The row opens the comment text; its photo opens the original.
            layout.setOnClickListener(v -> {
                DialogData dialog = new DialogData(mContext);
                dialog.setTitle("");
                dialog.setText(Html.fromHtml("<b>Текст комментария: </b>" + dataItem.getComment()));
                dialog.setClose(dialog::dismiss);
                dialog.show();
            });
        }


        private void bindPhoto(TARCommentsDB comment) {
            clearPhotoBinding();
            photo.setImageResource(R.mipmap.merchik_m);
            if (!PhotoEvidencePolicy.hasServerPhotoId(comment.photo)
                    && !PhotoEvidencePolicy.isUsableHash(comment.photo_hash)) {
                photo.setImageResource(R.drawable.ic_47);
                photo.setOnClickListener(view -> {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) commentPhotoClick.commentPhotoClick(position);
                });
                return;
            }

            photo.setOnClickListener(view -> openCommentPhoto(comment));
            thumbnailLoader = new PhotoPreviewLoader<>(new PhotoPreviewLoader.Source<StackPhotoDB>() {
                @Override public boolean isReady(StackPhotoDB value) { return localFile(value) != null; }
                @Override public void download(StackPhotoDB value, PhotoPreviewLoader.Callback<StackPhotoDB> callback) {
                    loadThumbnail(value, comment.photo, callback);
                }
            }, new PhotoPreviewLoader.Listener<StackPhotoDB>() {
                @Override public void showPhoto(StackPhotoDB value) {
                    File file = localFile(value);
                    if (file == null) return;
                    Bitmap bitmap = decodeSampledBitmapFromResource(file, 200, 200);
                    if (bitmap != null) photo.setImageBitmap(bitmap);
                }
                @Override public void setLoading(boolean loading) { }
                @Override public void showError(String error) {
                    Globals.writeToMLOG("ERROR", "TaRComments/photo", "comment=" + comment.getID() + ", " + error);
                }
            });
            thumbnailLoader.open(getTarCommentPhoto(comment));
        }

        private void clearPhotoBinding() {
            if (thumbnailLoader != null) {
                thumbnailLoader.close();
                thumbnailLoader = null;
            }
            photo.setOnClickListener(null);
        }
    }

    private StackPhotoDB getTarCommentPhoto(TARCommentsDB comment) {
        StackPhotoDB photo = PhotoEvidencePolicy.isUsableHash(comment.photo_hash)
                ? StackPhotoRealm.getByHash(comment.photo_hash.trim()) : null;
        if (photo == null && PhotoEvidencePolicy.hasServerPhotoId(comment.photo)) {
            photo = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(comment.photo.trim());
        }
        return photo;
    }

    private void openCommentPhoto(TARCommentsDB comment) {
        if (photoDialog != null) photoDialog.dismiss();
        DialogFullPhotoR opened = new DialogFullPhotoR(mContext);
        photoDialog = opened;
        opened.hideCamera();
        opened.setClose(opened::dismiss);
        opened.show();
        opened.setPhotoWithDownload(getTarCommentPhoto(comment),
                PhotoEvidencePolicy.hasServerPhotoId(comment.photo) ? comment.photo.trim() : null);
    }

    private void loadThumbnail(StackPhotoDB photo, String serverId, PhotoPreviewLoader.Callback<StackPhotoDB> callback) {
        if (photo != null && PhotoUrlUtils.isSafeDownloadUrl(photo.getPhotoServerURL())) {
            downloadThumbnail(photo, callback);
            return;
        }
        if (!PhotoEvidencePolicy.hasServerPhotoId(serverId)) {
            callback.onFailure("Photo has no server ID or URL");
            return;
        }
        PhotoTableRequest request = new PhotoTableRequest();
        request.mod = "images_view";
        request.act = "list_image";
        request.nolimit = "1";
        request.id_list = serverId.trim();
        new PhotoDownload().getPhotoInfoAndSaveItToDB(request, new Clicks.clickObjectAndStatus<StackPhotoDB>() {
            @Override public void onSuccess(StackPhotoDB loaded) {
                if (localFile(loaded) != null) callback.onSuccess(loaded);
                else downloadThumbnail(loaded, callback);
            }
            @Override public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    private void downloadThumbnail(StackPhotoDB photo, PhotoPreviewLoader.Callback<StackPhotoDB> callback) {
        new PhotoDownload().downloadPhoto(false, photo, "/TAR", new PhotoDownload.downloadPhotoInterface() {
            @Override public void onSuccess(StackPhotoDB loaded) { callback.onSuccess(loaded); }
            @Override public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    private static File localFile(StackPhotoDB photo) {
        if (photo == null || photo.getPhoto_num() == null) return null;
        String path = Uri.parse(photo.getPhoto_num()).getPath();
        File file = path == null ? null : new File(path);
        return PhotoEvidencePolicy.isReadableNonEmptyFile(file) ? file : null;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.clearPhotoBinding();
        boundHolders.remove(holder);
        super.onViewRecycled(holder);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (photoDialog != null) {
            photoDialog.dismiss();
            photoDialog = null;
        }
        for (ViewHolder holder : boundHolders) holder.clearPhotoBinding();
        boundHolders.clear();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_universal_adapter, parent, false);
        return new TaRCommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boundHolders.add(holder);
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
