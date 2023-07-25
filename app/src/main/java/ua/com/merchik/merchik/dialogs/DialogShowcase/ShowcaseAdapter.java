package ua.com.merchik.merchik.dialogs.DialogShowcase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;

public class ShowcaseAdapter extends RecyclerView.Adapter<ShowcaseAdapter.ViewHolder> {

    private List<ShowcaseSDB> showcaseList;
    private Clicks.click click;

    public ShowcaseAdapter(List<ShowcaseSDB> showcaseList, Clicks.click click) {
        if (showcaseList != null && showcaseList.size() > 0){
            showcaseList.add(defaultShowcase());
            this.showcaseList = showcaseList;
            this.click = click;
        }else {
            this.showcaseList = Collections.singletonList(defaultShowcase());
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
        return showcaseList.size();
//        return Math.min(showcaseList.size(), 4);
    }

    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        TextView textViewShowcaseId, textViewClientGroup, textViewPlanogramm;
        ImageView image;

        ViewHolder(View v) {
            super(v);
            constraintLayout = v.findViewById(R.id.constraintLayout);
            textViewShowcaseId = v.findViewById(R.id.textViewShowcaseId);
            textViewClientGroup = v.findViewById(R.id.textViewClientGroup);
            textViewPlanogramm = v.findViewById(R.id.textViewPlanogramm);
            image = v.findViewById(R.id.image);
        }

        public void bind(ShowcaseSDB showcase){
            textViewShowcaseId.setText("Ідентифікатор вітрини: " + showcase.id);
            textViewClientGroup.setText("Група Товару: " + showcase.tovarGrp);
            textViewPlanogramm.setText("Планограма: " + showcase.photoPlanogramId);

            if (showcase.id == 0){
                textViewShowcaseId.setText("Створити нову вітрину");
                textViewClientGroup.setVisibility(View.GONE);
                textViewPlanogramm.setVisibility(View.GONE);
                image.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_plus));
            }else {
                textViewClientGroup.setVisibility(View.VISIBLE);
                textViewPlanogramm.setVisibility(View.VISIBLE);
                image.setImageDrawable(itemView.getContext().getResources().getDrawable(R.mipmap.merchik));
            }

            constraintLayout.setOnClickListener(v-> click.click(showcase));
        }
    }

    /**
     * 10.07.23.
     * В случае если у меня нет ни одной Витрины (или в самый конец - надо добавить новую) я создаю
     * дефолтный элемент типа "Создать новую Витрину" после чего я буду указывать нулевой идентификатор
     * для фотографии, что б она в будущем могда сохраниться как новая фотка для Витрины.
     *
     * (т.е. я в других случаях говорю что "некст фото выполнены по Этой витрине", а в случае с дефолтом
     * "нект фото будет продвигаться как Новая Витрина")
     * */
    private ShowcaseSDB defaultShowcase() {
        ShowcaseSDB res = new ShowcaseSDB();

        res.id = 0;

        return  res;
    }
}
