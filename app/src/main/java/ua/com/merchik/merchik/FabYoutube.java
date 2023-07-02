package ua.com.merchik.merchik;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class FabYoutube {

    public void setFabVideo(FloatingActionButton fabYouTube, Integer[] lessons, Clicks.clickVoid click){
        fabYouTube.setOnClickListener(view -> {
            DialogVideo dialogVideo = new DialogVideo(view.getContext());
            dialogVideo.setTitle("Перелік відео уроків");
            dialogVideo.setVideos(getSiteHints(lessons), click);
            dialogVideo.setClose(dialogVideo::dismiss);
            dialogVideo.show();
        });
    }

    public void showYouTubeFab(FloatingActionButton fabYouTube, TextView badgeTextView, Integer[] lessons){
        List<ViewListSDB> videos = checkVideos(lessons, ()->{});
        if (videos.size() >= lessons.length){
            fabYouTube.setVisibility(View.GONE);
            badgeTextView.setVisibility(View.GONE);
        }else {
            fabYouTube.setVisibility(View.VISIBLE);
            int must = lessons.length;
            int have = videos.size();
            int res = must - have;
            badgeTextView.setText("" + res);
        }
    }

    private List<ViewListSDB> checkVideos(Integer[] ids, Clicks.clickVoid click) {
        List<ViewListSDB> viewListSDB = new ArrayList<>();
        List<SiteObjectsDB> object = RealmManager.getLesson(ids);
        List<SiteHintsDB> data = null;
        List<Integer> objectLessonIds = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        if (object != null && !object.isEmpty()) {
            for (SiteObjectsDB item : object) {
                String lessonId = item.getLessonId();
                if (lessonId != null && !lessonId.isEmpty()) {
                    objectLessonIds.add(Integer.valueOf(lessonId));
                }
            }

            if (!objectLessonIds.isEmpty()) {
                Integer[] lessonIds = objectLessonIds.toArray(new Integer[0]);
                data = RealmManager.getVideoLesson(lessonIds);
            }
        }

        if (data != null) {
            for (SiteHintsDB item : data){
                ViewListSDB view = SQL_DB.videoViewDao().getOneByLessonId(item.getID());
                if (view != null){
                    viewListSDB.add(view);
                }
            }
        }

        return viewListSDB;
    }

    private List<SiteHintsDB> getSiteHints(Integer[] integers){
        List<SiteObjectsDB> siteObjects = RealmManager.getLesson(integers);
        List<SiteHintsDB> data = null;
        try {
            if (siteObjects != null) {
                Integer[] siteObjectIds = new Integer[siteObjects.size()];
                for (int i = 0; i < siteObjects.size(); i++) {
                    int lessId = Integer.parseInt(siteObjects.get(i).getLessonId());
                    if (lessId != 0) siteObjectIds[i] = lessId;
                }
                data = RealmManager.getVideoLesson(siteObjectIds);
                if (data != null) Collections.reverse(data);
            }
        } catch (Exception e) {
            Log.e("setVideoLesson", "Exception e: " + e);
        }

        return data;
    }

}
