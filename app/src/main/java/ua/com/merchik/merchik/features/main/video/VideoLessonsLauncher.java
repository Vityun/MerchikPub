package ua.com.merchik.merchik.features.main.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.features.main.DBViewModels.VideoLessonsDBViewModel;

public class VideoLessonsLauncher {

    private static final String DEFAULT_TITLE = "Перелік відео уроків";
    private static final String DEFAULT_SUBTITLE = "Довідник відео уроків";

    private VideoLessonsLauncher() {
    }

    public static void openByObjectIds(Context context, Integer[] objectIds) {
        openByLessonIds(context, resolveLessonIdsFromObjectIds(objectIds), DEFAULT_TITLE, DEFAULT_SUBTITLE);
    }

    public static void openByObjectId(Context context, int objectId, String title, String subTitle) {
        openByLessonIds(
                context,
                resolveLessonIdsFromObjectIds(new Integer[]{objectId}),
                title == null || title.trim().isEmpty() ? DEFAULT_TITLE : title,
                subTitle == null || subTitle.trim().isEmpty() ? DEFAULT_SUBTITLE : subTitle
        );
    }

    public static void openByLessonIds(Context context, List<Integer> lessonIds) {
        openByLessonIds(context, lessonIds, DEFAULT_TITLE, DEFAULT_SUBTITLE);
    }

    public static void openByLessonIds(
            Context context,
            List<Integer> lessonIds,
            String title,
            String subTitle
    ) {
        if (context == null) return;

        List<Integer> cleanIds = cleanIds(lessonIds);
        if (cleanIds.isEmpty()) {
            Toast.makeText(context, "Відеоуроки не знайдено", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(context, FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", VideoLessonsDBViewModel.class.getCanonicalName());
        bundle.putString("contextUI", ContextUI.DEFAULT.toString());
        bundle.putString("modeUI", ModeUI.DEFAULT.toString());
        bundle.putString("dataJson", new Gson().toJson(cleanIds));
        bundle.putString("title", title == null || title.trim().isEmpty() ? DEFAULT_TITLE : title);
        bundle.putString("subTitle", subTitle == null || subTitle.trim().isEmpty() ? DEFAULT_SUBTITLE : subTitle);
        intent.putExtras(bundle);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    public static List<Integer> resolveLessonIdsFromObjectIds(Integer[] objectIds) {
        List<Integer> cleanObjectIds = cleanIds(objectIds);
        if (cleanObjectIds.isEmpty()) return Collections.emptyList();

        try {
            List<SiteObjectsDB> siteObjects = RealmManager.getLesson(cleanObjectIds.toArray(new Integer[0]));
            if (siteObjects == null || siteObjects.isEmpty()) return Collections.emptyList();

            Set<Integer> lessonIds = new LinkedHashSet<>();
            for (SiteObjectsDB siteObject : siteObjects) {
                if (siteObject == null || siteObject.getLessonId() == null) continue;

                try {
                    int lessonId = Integer.parseInt(siteObject.getLessonId().trim());
                    if (lessonId > 0) {
                        lessonIds.add(lessonId);
                    }
                } catch (Exception ignored) {
                }
            }

            List<Integer> result = new ArrayList<>(lessonIds);
            Collections.reverse(result);
            return result;
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "VideoLessonsLauncher/resolveLessonIdsFromObjectIds",
                    "Exception: " + e + ", objectIds=" + cleanObjectIds
            );
            return Collections.emptyList();
        }
    }

    private static List<Integer> cleanIds(Integer[] ids) {
        if (ids == null || ids.length == 0) return Collections.emptyList();

        List<Integer> result = new ArrayList<>();
        for (Integer id : ids) {
            if (id != null && id > 0) {
                result.add(id);
            }
        }
        return cleanIds(result);
    }

    private static List<Integer> cleanIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        Set<Integer> result = new LinkedHashSet<>();
        for (Integer id : ids) {
            if (id != null && id > 0) {
                result.add(id);
            }
        }
        return new ArrayList<>(result);
    }
}
