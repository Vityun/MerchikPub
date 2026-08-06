package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.componentsUI.YouTubePlayerDialogUiState
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class VideoLessonsDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val lessonIdsFromBundle: List<Int> by lazy {
        parseLessonIdsFromDataJson()
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return "ID, column_name, w_all, w_kli, w_our, doljnosti, url, title, html, platform_id, dt".split(",")
    }

    override val table: KClass<out DataObjectUI>
        get() = SiteHintsDB::class

    override suspend fun getItems(): List<DataItemUI> {
        val items = repository.getAllRoom(table, contextUI, null)
        val lessonIds = lessonIdsFromBundle
        if (lessonIds.isEmpty()) return items

        val idsSet = lessonIds.map { it.toString() }.toSet()
        val orderById = lessonIds.mapIndexed { index, id -> id.toString() to index }.toMap()

        return items
            .filter { item ->
                (item.rawObj.firstOrNull() as? SiteHintsDB)
                    ?.getID()
                    ?.toString() in idsSet
            }
            .sortedBy { item ->
                val id = (item.rawObj.firstOrNull() as? SiteHintsDB)?.getID()?.toString()
                orderById[id] ?: Int.MAX_VALUE
            }
    }

    override fun updateFilters() {
        val lessonIds = lessonIdsFromBundle
        if (lessonIds.isEmpty()) return

        val lessonsById = loadVideoLessonsByIds(lessonIds)
            .associateBy { it.getID() }

        updateFilters(
            Filters(
                items = listOf(
                    ItemFilter(
                        title = "Відеоуроки",
                        clazz = SiteHintsDB::class,
                        clazzViewModel = VideoLessonsDBViewModel::class,
                        modeUI = ModeUI.MULTI_SELECT,
                        titleContext = "Перелік відео уроків",
                        subTitleContext = "Оберіть відеоуроки",
                        leftField = "ID",
                        rightField = "ID",
                        rightValuesRaw = lessonIds.map { it.toString() },
                        rightValuesUI = lessonIds.map { id ->
                            lessonsById[id]?.getTitle()
                                ?.takeIf { it.isNotBlank() }
                                ?: lessonsById[id]?.getNm()
                                ?: id.toString()
                        },
                        enabled = true
                    )
                )
            )
        )
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        val lesson = clickedDataItemUI.rawObj.firstOrNull() as? SiteHintsDB
        val lessonId = lesson?.getID()
        if (lesson == null) {
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/onClickItemImage",
                "SiteHintsDB is null"
            )
            return
        }

        val url = lesson.getUrl()?.trim().orEmpty()
        if (url.isBlank()) {
            Toast.makeText(context, "Посилання на відео відсутнє", Toast.LENGTH_LONG).show()
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/onClickItemImage",
                "empty video url, lessonId=$lessonId"
            )
            return
        }

        val videoId = extractYouTubeVideoId(url)
        if (videoId == null) {
            Toast.makeText(context, "Не удалось определить видео YouTube", Toast.LENGTH_LONG).show()
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/onClickItemImage",
                "cannot extract videoId, lessonId=$lessonId, url=$url"
            )
            return
        }

        saveViewFact(lesson)
        showYouTubeDialog(
            YouTubePlayerDialogUiState(
                lessonId = lesson.getID(),
                url = url,
                videoId = videoId,
                title = lesson.getTitle()?.takeIf { it.isNotBlank() }
                    ?: lesson.getNm()?.takeIf { it.isNotBlank() }
                    ?: "Відеоурок"
            )
        )
    }

    private fun saveViewFact(lesson: SiteHintsDB) {
        runCatching {
            RealmManager.setRowToLog(
                listOf(
                    LogDB(
                        RealmManager.getLastIdLogDB() + 1,
                        System.currentTimeMillis() / 1000,
                        "Факт перегляду відео-урока. ${lesson.getTitle() ?: lesson.getNm().orEmpty()}",
                        1261,
                        null,
                        null,
                        lesson.getID().toLong(),
                        null,
                        null,
                        Globals.session,
                        null
                    )
                )
            )
        }.onFailure {
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/saveViewFact/log",
                "Exception: $it, lessonId=${lesson.getID()}"
            )
        }

        runCatching {
            val viewListSDB = ViewListSDB()
            viewListSDB.lessonId = lesson.getID()
            viewListSDB.merchikId = Globals.userId
            viewListSDB.dt = System.currentTimeMillis() / 1000
            RoomManager.SQL_DB.videoViewDao().insertAll(listOf(viewListSDB))
        }.onFailure {
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/saveViewFact/view_list",
                "Exception: $it, lessonId=${lesson.getID()}"
            )
        }
    }

    private fun parseLessonIdsFromDataJson(): List<Int> {
        val json = dataJson?.trim().orEmpty()
        if (json.isBlank()) return emptyList()

        return runCatching {
            val element = JsonParser.parseString(json)
            when {
                element.isJsonArray -> parseLessonIds(element)
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    when {
                        obj.has("lessonIds") -> parseLessonIds(obj.get("lessonIds"))
                        obj.has("ids") -> parseLessonIds(obj.get("ids"))
                        else -> emptyList()
                    }
                }

                else -> emptyList()
            }
        }.onFailure {
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/parseLessonIdsFromDataJson",
                "Exception: $it, dataJson=$json"
            )
        }.getOrDefault(emptyList())
    }

    private fun parseLessonIds(element: JsonElement?): List<Int> {
        if (element == null || element.isJsonNull || !element.isJsonArray) return emptyList()

        return element.asJsonArray
            .mapNotNull { item ->
                runCatching {
                    when {
                        item == null || item.isJsonNull -> null
                        item.isJsonPrimitive && item.asJsonPrimitive.isNumber -> item.asInt
                        item.isJsonPrimitive -> item.asString.trim().toIntOrNull()
                        else -> null
                    }
                }.getOrNull()
            }
            .filter { it > 0 }
            .distinct()
    }

    private fun loadVideoLessonsByIds(ids: List<Int>): List<SiteHintsDB> {
        if (ids.isEmpty()) return emptyList()

        return runCatching {
            RealmManager.getVideoLesson(ids.toTypedArray()) ?: emptyList()
        }.onFailure {
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/loadVideoLessonsByIds",
                "Exception: $it, ids=${ids.joinToString()}"
            )
        }.getOrDefault(emptyList())
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val value = url.trim()
        if (value.isBlank()) return null

        val rawId = value.asYouTubeVideoIdOrNull()
        if (rawId != null) return rawId

        return runCatching {
            val uri = Uri.parse(value)
            val queryVideoId = uri.getQueryParameter("v")
                ?.asYouTubeVideoIdOrNull()
            if (queryVideoId != null) return queryVideoId

            val segments = uri.pathSegments.orEmpty()
            val host = uri.host.orEmpty()

            if (host.contains("youtu.be", ignoreCase = true) && segments.isNotEmpty()) {
                segments.firstOrNull()
                    ?.asYouTubeVideoIdOrNull()
                    ?.let { return it }
            }

            val embedIndex = segments.indexOf("embed")
            if (embedIndex >= 0 && segments.size > embedIndex + 1) {
                segments[embedIndex + 1]
                    .asYouTubeVideoIdOrNull()
                    ?.let { return it }
            }

            val shortsIndex = segments.indexOf("shorts")
            if (shortsIndex >= 0 && segments.size > shortsIndex + 1) {
                segments[shortsIndex + 1]
                    .asYouTubeVideoIdOrNull()
                    ?.let { return it }
            }

            null
        }.onFailure {
            Globals.writeToMLOG(
                "ERROR",
                "VideoLessonsDBViewModel/extractYouTubeVideoId",
                "Exception: $it, url=$url"
            )
        }.getOrNull()
    }

    private fun String.asYouTubeVideoIdOrNull(): String? {
        val candidate = trim()
            .substringBefore("?")
            .substringBefore("&")
            .substringBefore("#")
            .substringBefore("/")
            .trim()

        return candidate.takeIf {
            it.length == 11 && it.all { char ->
                char.isLetterOrDigit() || char == '-' || char == '_'
            }
        }
    }
}
