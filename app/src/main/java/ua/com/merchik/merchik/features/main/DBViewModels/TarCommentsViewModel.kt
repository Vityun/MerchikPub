package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ServerExchange.PhotoDownload
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.toItemUI
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.UsersRealm
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.dialogs.PhotoPreviewLoader
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.ItemFilterChoice
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.tar.TarCommentItem
import ua.com.merchik.merchik.features.main.tar.tarCommentsSubtitle
import java.io.File
import java.util.function.Consumer
import javax.inject.Inject

@HiltViewModel
class TarCommentsViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {
    override val table = TarCommentItem::class
    private var tarId = 0
    private var realm: Realm? = null
    private var results: RealmResults<TARCommentsDB>? = null
    private var rows = emptyList<TarCommentItem>()
    private val loaders = mutableMapOf<Pair<String, String>, PhotoPreviewLoader<StackPhotoDB>>()
    private var photoDialog: DialogFullPhotoR? = null
    private var textDialog: DialogData? = null
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    var onAddComment: Runnable? = null
    var onAddPhoto: Consumer<String>? = null

    private val listener = RealmChangeListener<RealmResults<TARCommentsDB>> { data ->
        rows = data.map { row ->
            val author = row.who?.toIntOrNull()?.let { UsersRealm.getUsersDBById(it)?.nm }
            TarCommentItem.from(row, author, plainText(row.comment))
        }
        _loading.value = false
        rebuild()
        loadPhotos()
    }

    fun configure(id: Int, type: Int, text: String?) {
        if (tarId != id) {
            stopObserving()
            rows = emptyList()
        }
        tarId = id
        title = null
        subTitle = tarCommentsSubtitle(type, plainText(text))
        contextUI = ContextUI.TAR_COMMENTS_IN_CONTAINER
        typeWindow = "container"
        modeUI = ModeUI.DEFAULT
        updateSubtitle(subTitle, subTitle)
    }

    fun startObserving() {
        if (realm != null || tarId <= 0) return
        _loading.value = true
        _error.value = null
        try {
            realm = Realm.getDefaultInstance()
            results = realm!!.where(TARCommentsDB::class.java)
                .equalTo("rId", tarId.toString()).sort("dt", Sort.DESCENDING).findAllAsync()
            results!!.addChangeListener(listener)
        } catch (failure: Exception) {
            stopObserving()
            _loading.value = false
            _error.value = "Не вдалося завантажити коментарі"
            Globals.writeToMLOG("ERROR", "TarComments/list", failure.toString())
        }
    }

    fun stopObserving() {
        results?.removeChangeListener(listener)
        results = null
        realm?.close()
        realm = null
        loaders.values.forEach { it.close() }
        loaders.clear()
    }

    fun closeDialogs() {
        photoDialog?.dismiss()
        photoDialog = null
        textDialog?.dismiss()
        textDialog = null
    }

    override fun updateContent() {
        stopObserving()
        startObserving()
    }

    private fun rebuild() = super.updateContent()
    override fun getDefaultHideUserFields() = listOf("id")
    override fun getDefaultSortUserFields() = listOf("dt")

    override suspend fun getItems(): List<DataItemUI> = rows.map { row ->
        row.toItemUI(nameUIRepository, null, null).copy(images =
            photo(row)?.photo_num?.takeIf(::isLocalPhoto)?.let { Uri.parse(it).path }?.let(::listOf))
    }

    override fun updateFilters() {
        filters = tarCommentsFilters(rows, uiState.value.filters ?: Filters())
    }

    override fun onClickAdditionalContent() { onAddComment?.run() }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        val row = itemUI.rawObj.filterIsInstance<TarCommentItem>().firstOrNull() ?: return
        textDialog?.dismiss()
        textDialog = DialogData(context).also { dialog ->
            dialog.setTitle("Коментар")
            dialog.setText(row.comment)
            dialog.setClose { dialog.dismiss() }
            dialog.show()
        }
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        val row = clickedDataItemUI.rawObj.filterIsInstance<TarCommentItem>().firstOrNull() ?: return
        if (row.hasPhoto == 0) {
            onAddPhoto?.accept(row.id)
            return
        }
        photoDialog?.dismiss()
        photoDialog = DialogFullPhotoR(context).also { dialog ->
            dialog.hideCamera()
            dialog.setClose { dialog.dismiss() }
            dialog.show()
            dialog.setPhotoWithDownload(photo(row), row.photo.takeIf(PhotoEvidencePolicy::hasServerPhotoId))
        }
    }

    private fun photo(row: TarCommentItem): StackPhotoDB? =
        row.photoHash.takeIf(PhotoEvidencePolicy::isUsableHash)?.let(StackPhotoRealm::getByHash)
            ?: row.photo.takeIf(PhotoEvidencePolicy::hasServerPhotoId)
                ?.let(StackPhotoRealm::stackPhotoDBGetPhotoBySiteId)

    private fun isLocalPhoto(uri: String): Boolean = runCatching {
        Uri.parse(uri).path?.let { PhotoEvidencePolicy.isReadableNonEmptyFile(File(it)) } == true
    }.getOrDefault(false)

    private fun loadPhotos() {
        rows.filter { it.hasPhoto == 1 }.forEach { row ->
            val key = row.photo to row.photoHash
            if (key in loaders) return@forEach
            val loader = PhotoPreviewLoader(object : PhotoPreviewLoader.Source<StackPhotoDB> {
                override fun isReady(value: StackPhotoDB?) = value?.photo_num?.let(::isLocalPhoto) == true
                override fun download(value: StackPhotoDB?, callback: PhotoPreviewLoader.Callback<StackPhotoDB>) {
                    if (value != null) downloadThumbnail(value, callback)
                    else if (PhotoEvidencePolicy.hasServerPhotoId(row.photo)) {
                        val request = PhotoTableRequest().apply {
                            mod = "images_view"; act = "list_image"; nolimit = "1"; id_list = row.photo
                        }
                        PhotoDownload().getPhotoInfoAndSaveItToDB(request,
                            object : Clicks.clickObjectAndStatus<StackPhotoDB> {
                                override fun onSuccess(data: StackPhotoDB) {
                                    val resolved = photo(row)
                                    if (resolved != null) downloadThumbnail(resolved, callback)
                                    else callback.onFailure("Photo metadata missing")
                                }
                                override fun onFailure(error: String) { callback.onFailure(error) }
                            })
                    } else callback.onFailure("Local photo missing")
                }
            }, object : PhotoPreviewLoader.Listener<StackPhotoDB> {
                override fun showPhoto(value: StackPhotoDB?) { if (realm != null) rebuild() }
                override fun setLoading(loading: Boolean) { }
                override fun showError(error: String) {
                    Globals.writeToMLOG("ERROR", "TarComments/photo", "comment=${row.id}, $error")
                }
            })
            loaders[key] = loader
            loader.open(photo(row))
        }
    }

    private fun downloadThumbnail(photo: StackPhotoDB, callback: PhotoPreviewLoader.Callback<StackPhotoDB>) {
        PhotoDownload().downloadPhoto(false, photo, "/TAR", object : PhotoDownload.downloadPhotoInterface {
            override fun onSuccess(data: StackPhotoDB) { callback.onSuccess(data) }
            override fun onFailure(error: String) { callback.onFailure(error) }
        })
    }

    override fun onCleared() {
        stopObserving()
        closeDialogs()
        super.onCleared()
    }

    private fun plainText(value: String?) = HtmlCompat.fromHtml(value.orEmpty(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString().trim()
}

internal fun tarCommentsFilters(rows: List<TarCommentItem>, previous: Filters): Filters {
    val author = ItemFilter(
        title = "Автор коментаря", clazz = TarCommentItem::class, modeUI = ModeUI.ONE_SELECT,
        titleContext = "Автор коментаря", subTitleContext = "", leftField = "who", rightField = "who",
        rightValuesRaw = emptyList(), rightValuesUI = emptyList(), enabled = true,
        choices = listOf(ItemFilterChoice("all", "Усі", emptyList())) + rows.distinctBy { it.who }
            .filter { it.who.isNotBlank() }.map { ItemFilterChoice(it.who, it.author) },
        defaultChoiceKey = "all"
    )
    val photo = author.copy(
        title = "Фото", titleContext = "Фото", leftField = "hasPhoto", rightField = "hasPhoto",
        key = "${TarCommentItem::class.java.name}:hasPhoto",
        choices = listOf(ItemFilterChoice("all", "Усі", emptyList()),
            ItemFilterChoice("1", "З фото"), ItemFilterChoice("0", "Без фото"))
    )
    return previous.copy(
        // A conversation must include its full history, including undated comments.
        rangeDataByKey = null,
        items = listOf(author, photo).map { filter ->
            val old = previous.items.firstOrNull { it.key == filter.key }
            if (old == null) filter.clearValues() else filter.copy(
                rightValuesRaw = old.rightValuesRaw, rightValuesUI = old.rightValuesUI,
                excludeMode = old.excludeMode)
        }
    )
}
